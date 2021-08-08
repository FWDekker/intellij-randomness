package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeEditor
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
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.uuid.UuidSchemeEditor
import com.fwdekker.randomness.word.WordScheme
import com.fwdekker.randomness.word.WordSchemeEditor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.AnActionButton
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import com.intellij.ui.LayeredIcon
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel
import kotlin.math.min


/**
 * Component for editing [TemplateList].
 *
 * The editor consists of a left side and a right side. The left side contains a tree with the templates as roots and
 * their schemes as the leaves. When the user selects a scheme, the appropriate [SchemeEditor] for that scheme is loaded
 * on the right side.
 *
 * The tree itself contains copies of the entries in the given [TemplateList]. Changes in the editor are immediately
 * written to these copies so that when another scheme editor is displayed the changes are not lost. The changes in the
 * copies are written into the given template list only when [applyScheme] is invoked.
 *
 * @param templates the list of templates to edit
 */
class TemplateListEditor(templates: TemplateList = default.state) : SchemeEditor<TemplateList>(templates) {
    override val rootComponent = JPanel(BorderLayout())
    private val templateTreeModel = DefaultTreeModel(DefaultMutableTreeNode("Root"))
    private val templateTree = Tree(templateTreeModel)
    private var schemeEditorPanel = JPanel(BorderLayout())
    private var schemeEditor: SchemeEditor<*>? = null

    private val changeListeners = mutableListOf<() -> Unit>()


    init {
        val splitter = JBSplitter(false, DEFAULT_SPLITTER_PROPORTION)
        rootComponent.add(splitter, BorderLayout.CENTER)

        // Left half
        templateTree.isRootVisible = false
        templateTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        templateTree.setCellRenderer { _, value, _, _, _, _, _ ->
            JLabel((value as? DefaultMutableTreeNode)?.userObject?.let { scheme ->
                when (scheme) {
                    is Template -> scheme.name
                    is Scheme -> scheme::class.simpleName?.dropLast("Scheme".length) ?: "Scheme"
                    else -> "Scheme"
                }
            })
        }
        templateTree.setExpandableItemsEnabled(false)
        templateTree.emptyText.text = EMPTY_TEXT
        templateTree.addTreeSelectionListener {
            schemeEditor?.also { schemeEditorPanel.remove(it.rootComponent) }

            val selectedObject = templateTree.getSelectedNode()?.userObject as? Scheme
            if (selectedObject == null || selectedObject is Template) {
                return@addTreeSelectionListener
            }

            schemeEditor = createEditor(selectedObject)
                .also { editor ->
                    editor.addChangeListener {
                        editor.applyScheme()
                        changeListeners.forEach { it() }
                    }

                    schemeEditorPanel.add(editor.rootComponent)
                    schemeEditorPanel.revalidate()
                }
        }
        splitter.firstComponent = JBScrollPane(decorateTemplateList(templateTree))

        // Right half
        val previewPanel = PreviewPanel { readScheme() }
        addChangeListener { previewPanel.updatePreview() }
        schemeEditorPanel.add(previewPanel.rootComponent, BorderLayout.SOUTH)

        splitter.secondComponent = JBScrollPane(schemeEditorPanel)

        loadScheme()
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
                val selectedNode = templateTree.getSelectedNode()
                val (parent, child) =
                    when {
                        selectedNode == null -> templateTree.getRoot() to DefaultMutableTreeNode(Template())
                        selectedNode.userObject is Template -> selectedNode to DefaultMutableTreeNode(IntegerScheme())
                        else -> selectedNode.parent as DefaultMutableTreeNode to DefaultMutableTreeNode(IntegerScheme())
                    }

                parent.insert(child, parent.getIndex(selectedNode) + 1)
                templateTreeModel.reload()
                templateTree.expandAll()
                templateTree.select(child)
            }
            .setAddActionName("Add")
            .setRemoveAction {
                val selectedNode = templateTree.getSelectedNode() ?: return@setRemoveAction
                val parent = selectedNode.parent ?: return@setRemoveAction
                val childIndex = parent.getIndex(selectedNode)

                templateTreeModel.removeNodeFromParent(templateTree.getSelectedNode())

                templateTree.select(
                    if (parent.childCount == 0) parent as DefaultMutableTreeNode
                    else parent.getChildAt(min(childIndex, parent.childCount - 1)) as DefaultMutableTreeNode
                )
            }
            .setRemoveActionName("Remove")
            .setRemoveActionUpdater { templateTree.lastSelectedPathComponent != templateTree.getRoot() }
            .addExtraAction(object : AnActionButton("Copy", AllIcons.Actions.Copy) {
                override fun actionPerformed(event: AnActionEvent) {
                    val selectedNode = templateTree.getSelectedNode()
                    val selectedScheme = selectedNode?.userObject as? Scheme ?: return
                    val parent = selectedNode.parent as? DefaultMutableTreeNode ?: return

                    val newNode = DefaultMutableTreeNode(selectedScheme.deepCopy())
                    parent.add(newNode)
                    templateTreeModel.reload()
                    templateTree.expandAll()
                    templateTree.select(newNode)
                }

                override fun isEnabled() = templateTree.lastSelectedPathComponent != templateTree.getRoot()
            })
            .addExtraAction(object : AnActionButton("Up", AllIcons.Actions.MoveUp) {
                override fun actionPerformed(event: AnActionEvent) {
                    val child = templateTree.getSelectedNode()
                    val parent = child?.parent as? DefaultMutableTreeNode ?: return

                    val currentIndex = parent.getIndex(child)
                    parent.remove(child)
                    parent.insert(child, currentIndex - 1)

                    templateTreeModel.reload()
                    templateTree.expandAll()
                    templateTree.select(child)
                }

                override fun isEnabled(): Boolean {
                    val node = templateTree.getSelectedNode()
                    val parent = node?.parent ?: return false
                    val value = node.userObject

                    return value != null && parent.getIndex(node) > 0
                }
            })
            .addExtraAction(object : AnActionButton("Down", AllIcons.Actions.MoveDown) {
                override fun actionPerformed(event: AnActionEvent) {
                    val child = templateTree.getSelectedNode()
                    val parent = child?.parent as? DefaultMutableTreeNode ?: return

                    val currentIndex = parent.getIndex(child)
                    parent.remove(child)
                    parent.insert(child, currentIndex + 1)

                    templateTreeModel.reload()
                    templateTree.expandAll()
                    templateTree.select(child)
                }

                override fun isEnabled(): Boolean {
                    val node = templateTree.getSelectedNode()
                    val parent = node?.parent ?: return false
                    val value = node.userObject

                    return value != null && parent.getIndex(node) < parent.childCount - 1
                }
            })
            .setButtonComparator("Add", "Remove", "Copy", "Up", "Down")
            .createPanel()

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
            else -> error("Unknown scheme type '${scheme.javaClass.canonicalName}'.")
        }


    override fun loadScheme(scheme: TemplateList) {
        super.loadScheme(scheme)

        val root = templateTreeModel.root as DefaultMutableTreeNode
        root.removeAllChildren()
        scheme.deepCopy().templates.forEach { template ->
            val templateNode = DefaultMutableTreeNode(template)
            template.schemes.forEach { templateNode.add(DefaultMutableTreeNode(it)) }
            root.add(templateNode)
        }
        templateTreeModel.reload()

        templateTree.expandAll()
        templateTree.getRoot().firstLeaf?.also { templateTree.select(it) }
    }

    override fun readScheme() =
        TemplateList(
            templateTree.getRoot().children()
                .toList().filterIsInstance<DefaultMutableTreeNode>()
                .mapNotNull { templateNode ->
                    val template = templateNode.userObject as? Template ?: return@mapNotNull null

                    Template(
                        template.name,
                        templateNode.children()
                            .toList().filterIsInstance<DefaultMutableTreeNode>()
                            .map { it.userObject }.filterIsInstance<Scheme>()
                    )
                }
        )


    override fun isModified(): Boolean {
        val originalEntries = originalScheme.templates
        val unsavedEntries = readScheme().templates

        return super.isModified() ||
            originalEntries.size != unsavedEntries.size ||
            originalEntries.zip(unsavedEntries).any { it.first != it.second }
    }

    override fun doValidate() = schemeEditor?.doValidate() ?: readScheme().doValidate()


    override fun addChangeListener(listener: () -> Unit) {
        changeListeners += listener
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
 * Utility class for building a `DefaultActionGroup` consisting of actions that add a specified element to a list.
 *
 * @param T the type of element
 * @property list the list that actions add items to
 * @property listModel the model of the list that actions add items to
 */
private class DefaultActionGroupBuilder<T>(
    private val list: JList<T>,
    private val listModel: DefaultListModel<T>,
) {
    private val actions = mutableListOf<DumbAwareAction>()


    /**
     * Adds an action to the group.
     *
     * @param name the name of the action to add
     * @param item returns the item to be added whenever the action is invoked
     * @return this `DefaultActionGroupBuilder`
     */
    fun addAction(name: String, item: () -> T): DefaultActionGroupBuilder<T> {
        actions.add(object : DumbAwareAction(name) {
            override fun actionPerformed(e: AnActionEvent) {
                listModel.addElement(item())
                list.selectedIndex = listModel.size - 1
            }
        })
        return this
    }

    /**
     * Builds the `DefaultActionGroup` consisting of the added actions.
     *
     * @return the `DefaultActionGroup` consisting of the added actions
     */
    fun buildActionGroup() =
        DefaultActionGroup(actions).apply {
            templatePresentation.icon = LayeredIcon.ADD_WITH_DROPDOWN
            templatePresentation.text = "Add"
            registerCustomShortcutSet(CommonShortcuts.getNewForDialogs(), null)
        }
}


private fun JTree.getRoot() = (model as DefaultTreeModel).root as DefaultMutableTreeNode

private fun JTree.expandAll() {
    getRoot().children().toList().forEach { expandPath(TreePath((it as DefaultMutableTreeNode).path)) }
}

private fun JTree.select(node: DefaultMutableTreeNode) {
    selectionModel.selectionPath = TreePath(node.path)
}

private fun JTree.getSelectedNode() = lastSelectedPathComponent as? DefaultMutableTreeNode?


private val MODIFIED_COLOR = JBColor.namedColor("Tree.modifiedItemForeground", JBColor.BLUE)
private val ERROR_COLOR = JBColor.namedColor("Tree.errorForeground", JBColor.RED)
