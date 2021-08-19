package com.fwdekker.randomness.template

import com.fwdekker.randomness.ActuallyPrivate
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.State
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.decimal.DecimalSchemeEditor
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.integer.IntegerSchemeEditor
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.literal.LiteralSchemeEditor
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.string.StringSchemeEditor
import com.fwdekker.randomness.template.TemplateSettings.Companion.default
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.uuid.UuidSchemeEditor
import com.fwdekker.randomness.word.WordScheme
import com.fwdekker.randomness.word.WordSchemeEditor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.AnActionButton
import com.intellij.ui.AnActionButtonRunnable
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.JBSplitter
import com.intellij.ui.LayeredIcon
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel
import kotlin.math.min


/**
 * Component for editing [TemplateList]s.
 *
 * The editor consists of a left side and a right side. The left side contains a tree with the templates as roots and
 * their schemes as the leaves. When the user selects a scheme, the appropriate [StateEditor] for that scheme is loaded
 * on the right side.
 *
 * The tree itself contains copies of the entries in the given [TemplateList]. Changes in the editor are immediately
 * written to these copies so that when another scheme editor is displayed the changes are not lost. The changes in the
 * copies are written into the given template list only when [applyState] is invoked.
 *
 * @param templates the list of templates to edit
 */
class TemplateListEditor(templates: TemplateList = default.state) : StateEditor<TemplateList>(templates) {
    override val rootComponent = JPanel(BorderLayout())
    private val templateTreeModel = DefaultTreeModel(DefaultMutableTreeNode("Root"))
    private val templateTree = Tree(templateTreeModel)
    private var schemeEditorPanel = JPanel(BorderLayout())
    private var schemeEditor: StateEditor<*>? = null

    private val changeListeners = mutableListOf<() -> Unit>()

    /**
     * The name of the template to select after the next invocation of [reset].
     *
     * @see TemplateSettingsConfigurable
     * @see TemplateSettingsAction
     */
    var queueSelection: String? = null


    init {
        val splitter = JBSplitter(false, DEFAULT_SPLITTER_PROPORTION)
        rootComponent.add(splitter, BorderLayout.CENTER)

        // Left half
        templateTree.isRootVisible = false
        templateTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        templateTree.cellRenderer = object : ColoredTreeCellRenderer() {
            override fun customizeCellRenderer(
                tree: JTree,
                value: Any?,
                selected: Boolean,
                expanded: Boolean,
                leaf: Boolean,
                row: Int,
                hasFocus: Boolean
            ) {
                val scheme = (value as DefaultMutableTreeNode).userObject as? Scheme
                if (scheme == null) {
                    append("<unknown>")
                    return
                }

                icon = scheme.icon
                append(
                    scheme.name.ifBlank { "<empty>" },
                    when {
                        scheme.doValidate() != null ->
                            SimpleTextAttributes.ERROR_ATTRIBUTES
                        scheme is Template &&
                            originalState.templates.firstOrNull { it.name == scheme.name } != scheme ->
                            SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES
                        else ->
                            SimpleTextAttributes.REGULAR_ATTRIBUTES
                    }
                )
            }
        }
        templateTree.setExpandableItemsEnabled(false)
        templateTree.emptyText.text = EMPTY_TEXT
        templateTree.addTreeSelectionListener {
            schemeEditor?.also { schemeEditorPanel.remove(it.rootComponent) }

            val selectedObject = templateTree.getSelectedNode()?.userObject
            if (selectedObject !is Scheme) {
                changeListeners.forEach { it() }
                return@addTreeSelectionListener
            }

            schemeEditor = createEditor(selectedObject)
                .also { editor ->
                    editor.addChangeListener {
                        editor.applyState()
                        changeListeners.forEach { it() }

                        templateTree.updateUI()
                    }

                    schemeEditorPanel.add(editor.rootComponent)
                    schemeEditorPanel.revalidate()
                    changeListeners.forEach { it() }
                }
        }
        splitter.firstComponent = JBScrollPane(decorateTemplateList(templateTree))

        // Right half
        val previewPanel = PreviewPanel {
            val selectedNode = templateTree.getSelectedNode() ?: return@PreviewPanel LiteralScheme("")
            val selectedTemplate = selectedNode.userObject as? Template
                ?: (selectedNode.parent as DefaultMutableTreeNode).userObject as Template

            readState().templates.first { it.name == selectedTemplate.name }
        }
        addChangeListener { previewPanel.updatePreview() }
        schemeEditorPanel.add(previewPanel.rootComponent, BorderLayout.SOUTH)

        splitter.secondComponent = JBScrollPane(schemeEditorPanel)

        loadState()
    }

    /**
     * Decorates the given list with buttons for adding, removing, copying, etc.
     *
     * @param templateList the list to decorate
     * @return a panel containing both the decorator and the given list
     */
    private fun decorateTemplateList(templateList: Tree) =
        ToolbarDecorator.createDecorator(templateList)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())
            .setScrollPaneBorder(JBUI.Borders.empty())
            .setAddAction {
                it.preferredPopupPoint?.let { point ->
                    JBPopupFactory.getInstance().createListPopup(AddPopupStep()).show(point)
                }
            }
            .setAddActionName("Add")
            .setAddIcon(LayeredIcon.ADD_WITH_DROPDOWN)
            .setRemoveAction(RemoveAction())
            .setRemoveActionName("Remove")
            .setRemoveActionUpdater { templateTree.getSelectedNode() != null }
            .addExtraAction(CopyActionButton())
            .addExtraAction(UpActionButton())
            .addExtraAction(DownActionButton())
            .setButtonComparator("Add", "Remove", "Copy", "Up", "Down")
            .let {
                val panel = it.createPanel()
                it.actionsPanel.name = "templateListToolbar"
                panel
            }


    /**
     * Creates an editor to edit the given scheme.
     *
     * @param scheme the scheme to create an editor for
     */
    private fun createEditor(scheme: Scheme) =
        when (scheme) {
            is IntegerScheme -> IntegerSchemeEditor(scheme)
            is DecimalScheme -> DecimalSchemeEditor(scheme)
            is StringScheme -> StringSchemeEditor(scheme)
            is UuidScheme -> UuidSchemeEditor(scheme)
            is WordScheme -> WordSchemeEditor(scheme)
            is LiteralScheme -> LiteralSchemeEditor(scheme)
            is Template -> TemplateEditor(scheme)
            else -> error("Unknown scheme type '${scheme.javaClass.canonicalName}'.")
        }

    /**
     * Adds the given scheme at an appropriate location in the tree based on the currently selected node.
     *
     * @param scheme the scheme to add
     */
    @ActuallyPrivate("Exposed for testing.")
    internal fun addScheme(scheme: Scheme) {
        val selectedNode = templateTree.getSelectedNode()
        val child = DefaultMutableTreeNode(scheme)
        if (scheme is Template)
            scheme.schemes.forEach { child.add(DefaultMutableTreeNode(it)) }

        when {
            selectedNode == null ->
                if (scheme is Template) templateTree.getRoot().add(child)
                else error("Cannot add non-template to root.")
            selectedNode.userObject is Template ->
                if (scheme !is Template) selectedNode.add(child)
                else templateTree.getRoot().insert(child, templateTree.getRoot().getIndex(selectedNode) + 1)
            else ->
                if (scheme !is Template)
                    (selectedNode.parent as DefaultMutableTreeNode)
                        .insert(child, selectedNode.parent.getIndex(selectedNode) + 1)
                else
                    templateTree.getRoot().insert(child, templateTree.getRoot().getIndex(selectedNode.parent) + 1)
        }

        templateTreeModel.reload()
        templateTree.expandAll()
        templateTree.select(child)
    }

    /**
     * Moves the given node down the given number of positions.
     *
     * The node will be moved down relative to its siblings. The parent does not change.
     *
     * @param node the node to move down
     * @param positions the number of positions to move the node down by within its parent; can be negative
     */
    private fun moveNodeDownBy(node: DefaultMutableTreeNode, positions: Int) {
        val parent = node.parent as DefaultMutableTreeNode
        val oldIndex = parent.getIndex(node)

        parent.remove(node)
        parent.insert(node, oldIndex + positions)

        templateTreeModel.reload()
        templateTree.expandAll()
        templateTree.select(node)
    }


    override fun loadState(state: TemplateList) {
        super.loadState(state)

        val root = templateTreeModel.root as DefaultMutableTreeNode
        root.removeAllChildren()
        state.deepCopy().templates.forEach { template ->
            val templateNode = DefaultMutableTreeNode(template)
            template.schemes.forEach { templateNode.add(DefaultMutableTreeNode(it)) }
            root.add(templateNode)
        }
        templateTreeModel.reload()

        templateTree.expandAll()
        templateTree.select(templateTree.getRoot().firstLeaf)
    }

    override fun readState() =
        TemplateList(
            templateTree.getRoot().children()
                .toList().filterIsInstance<DefaultMutableTreeNode>()
                .map { templateNode ->
                    Template(
                        (templateNode.userObject as Template).name,
                        templateNode.children()
                            .toList().filterIsInstance<DefaultMutableTreeNode>()
                            .map { it.userObject }.filterIsInstance<Scheme>()
                    )
                }
        )

    override fun reset() {
        super.reset()

        queueSelection?.also { select(it) }
        queueSelection = null
    }

    /**
     * Selects the template with the given name.
     *
     * @param name the name of the template to select
     */
    private fun select(name: String) {
        templateTree.getRoot()
            .children().toList().filterIsInstance<DefaultMutableTreeNode>()
            .firstOrNull { (it.userObject as Template).name.equals(name, ignoreCase = true) }
            ?.also { templateTree.select(it) }
    }


    override fun doValidate() = readState().doValidate()


    override fun addChangeListener(listener: () -> Unit) {
        changeListeners += listener
    }


    /**
     * Component for editing only the name of [Template]s.
     *
     * @param template the template to edit
     */
    private class TemplateEditor(template: Template) : StateEditor<Template>(template) {
        override val rootComponent = JPanel(BorderLayout())
        private val nameInput = JBTextField().also { it.name = "templateName" }


        init {
            val namePanel = JPanel(BorderLayout())
            namePanel.add(JLabel("Name:"), BorderLayout.WEST)
            namePanel.add(nameInput)
            rootComponent.add(namePanel, BorderLayout.NORTH)

            loadState()
        }


        override fun loadState(state: Template) = super.loadState(state).also { nameInput.text = state.name.trim() }

        override fun readState() = originalState.deepCopy().also { it.name = nameInput.text.trim() }


        override fun addChangeListener(listener: () -> Unit) = addChangeListenerTo(nameInput, listener = listener)
    }


    /**
     * The entries to display in a popup when the add button is pressed.
     */
    private inner class AddPopupStep : BaseListPopupStep<String>(null, listOf("Template", "Scheme")) {
        override fun hasSubstep(selectedValue: String?) = true

        override fun onChosen(selectedValue: String?, finalChoice: Boolean) =
            when (selectedValue) {
                "Template" -> AddTemplatePopupStep()
                "Scheme" -> AddSchemePopupStep()
                else -> null
            }


        /**
         * The [Template]-related entries in [AddPopupStep].
         */
        private inner class AddTemplatePopupStep : BaseListPopupStep<Template>(null, TemplateList.DEFAULT_TEMPLATES) {
            override fun getIconFor(value: Template?) = value?.icon

            override fun getTextFor(value: Template?) = value?.name ?: Template.DEFAULT_NAME

            override fun onChosen(value: Template?, finalChoice: Boolean): PopupStep<*>? =
                value?.also { addScheme(it.deepCopy()) }?.let { null }

            override fun isSpeedSearchEnabled() = true
        }

        /**
         * The [Scheme]-related entries in [AddPopupStep].
         */
        private inner class AddSchemePopupStep : BaseListPopupStep<Scheme>(
            null,
            listOf(LiteralScheme(), IntegerScheme(), DecimalScheme(), StringScheme(), WordScheme(), UuidScheme())
        ) {
            override fun getIconFor(value: Scheme?) = value?.icon

            override fun getTextFor(value: Scheme?) = value?.name ?: State.DEFAULT_NAME

            override fun onChosen(value: Scheme?, finalChoice: Boolean): PopupStep<*>? =
                value?.also { addScheme(it.deepCopy()) }?.let { null }

            override fun isSpeedSearchEnabled() = true
        }
    }

    /**
     * The action to invoke when the remove button is pressed.
     */
    private inner class RemoveAction : AnActionButtonRunnable {
        override fun run(t: AnActionButton?) {
            val selectedNode = templateTree.getSelectedNode() ?: return
            val parent = selectedNode.parent
            val childIndex = parent.getIndex(selectedNode)

            templateTreeModel.removeNodeFromParent(selectedNode)

            templateTree.select(
                if (parent.childCount == 0) parent as DefaultMutableTreeNode
                else parent.getChildAt(min(childIndex, parent.childCount - 1)) as DefaultMutableTreeNode
            )
        }
    }

    /**
     * The button to display for copying an entry in the tree.
     */
    private inner class CopyActionButton : AnActionButton("Copy", AllIcons.Actions.Copy) {
        override fun actionPerformed(event: AnActionEvent) {
            val node = templateTree.getSelectedNode() ?: return

            addScheme((node.userObject as Scheme).deepCopy())
        }

        override fun isEnabled() = templateTree.getSelectedNode() != null
    }

    /**
     * The button to display for moving an entry up the tree.
     */
    private inner class UpActionButton : AnActionButton("Up", AllIcons.Actions.MoveUp) {
        override fun actionPerformed(event: AnActionEvent) {
            templateTree.getSelectedNode()?.also { moveNodeDownBy(it, -1) }
        }

        override fun isEnabled(): Boolean {
            val node = templateTree.getSelectedNode() ?: return false

            return node.parent.getIndex(node) > 0
        }
    }

    /**
     * The button to display for moving an entry down the tree.
     */
    private inner class DownActionButton : AnActionButton("Down", AllIcons.Actions.MoveDown) {
        override fun actionPerformed(event: AnActionEvent) {
            templateTree.getSelectedNode()?.also { moveNodeDownBy(it, 1) }
        }

        override fun isEnabled(): Boolean {
            val node = templateTree.getSelectedNode() ?: return false

            return node.parent.getIndex(node) < node.parent.childCount - 1
        }
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default proportion of the splitter component.
         */
        const val DEFAULT_SPLITTER_PROPORTION = .2f

        /**
         * The text that is displayed when the table is empty.
         */
        const val EMPTY_TEXT = "No templates configured."
    }
}


/**
 * Returns the root of the tree as a [DefaultMutableTreeNode].
 *
 * @return the root of the tree as a [DefaultMutableTreeNode]
 */
private fun JTree.getRoot() = (model as DefaultTreeModel).root as DefaultMutableTreeNode

/**
 * Expands all nodes in the tree.
 */
private fun JTree.expandAll() {
    getRoot().children().toList().forEach { expandPath(TreePath((it as DefaultMutableTreeNode).path)) }
}

/**
 * Selects the given node.
 *
 * @param node the node to select
 */
private fun JTree.select(node: DefaultMutableTreeNode) {
    selectionModel.selectionPath = TreePath(node.path)
}

/**
 * Returns the currently selected node, or `null` if no node is selected, or `null` if the root is not visible and the
 * root is selected.
 *
 * @return the currently selected node
 */
private fun JTree.getSelectedNode() =
    (lastSelectedPathComponent as? DefaultMutableTreeNode?)
        ?.let {
            if (!isRootVisible && it == getRoot()) null
            else it
        }
