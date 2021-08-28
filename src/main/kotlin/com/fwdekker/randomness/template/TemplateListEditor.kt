package com.fwdekker.randomness.template

import com.fwdekker.randomness.ActuallyPrivate
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SettingsState
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
import com.fwdekker.randomness.string.SymbolSetSettings
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.uuid.UuidSchemeEditor
import com.fwdekker.randomness.word.DictionarySettings
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
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.util.Collections
import java.util.Enumeration
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel
import kotlin.math.min


/**
 * Component for editing [TemplateList]s while keeping in mind the overall [SettingsState].
 *
 * The editor consists of a left side and a right side. The left side contains a tree with the templates as roots and
 * their schemes as the leaves. When the user selects a scheme, the appropriate [StateEditor] for that scheme is loaded
 * on the right side.
 *
 * The tree itself contains copies of the entries in the given [TemplateList]. Changes in the editor are immediately
 * written to these copies so that when another scheme editor is displayed the changes are not lost. The changes in the
 * copies are written into the given template list only when [applyState] is invoked.
 *
 * @param settings the settings containing the templates to edit
 */
class TemplateListEditor(settings: SettingsState = SettingsState.default) : StateEditor<SettingsState>(settings) {
    override val rootComponent = JPanel(BorderLayout())
    private var dictionarySettings: DictionarySettings = DictionarySettings()
    private var symbolSetSettings: SymbolSetSettings = SymbolSetSettings()
    private val templateTreeModel = DefaultTreeModel(TemplateListTreeNode(TemplateList(mutableListOf())))
    private val templateTree = Tree(templateTreeModel)
    private var schemeEditorPanel = JPanel(BorderLayout())
    private var schemeEditor: StateEditor<*>? = null

    /**
     * The UUID of the template to select after the next invocation of [reset].
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
                val scheme = (value as StateTreeNode<*>).state as? Scheme
                if (scheme == null) {
                    append("<unknown>")
                    return
                }

                icon = scheme.icon

                val templates = originalState.templateList.templates
                val schemes = templates.flatMap { it.schemes }
                append(
                    scheme.name.ifBlank { "<empty>" },
                    when {
                        scheme.doValidate() != null ->
                            SimpleTextAttributes.ERROR_ATTRIBUTES
                        scheme is Template && templates.firstOrNull { it.uuid == scheme.uuid } != scheme ->
                            SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES
                        scheme !is Template && schemes.firstOrNull { it.uuid == scheme.uuid } != scheme ->
                            SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES
                        else ->
                            SimpleTextAttributes.REGULAR_ATTRIBUTES
                    }
                )
            }
        }
        templateTree.emptyText.text = EMPTY_TEXT
        templateTree.addTreeSelectionListener {
            schemeEditor?.also {
                schemeEditorPanel.remove(it.rootComponent)
                schemeEditor = null
            }

            val selectedNode = templateTree.getSelectedNode()
            val selectedObject = selectedNode?.state
            if (selectedObject !is Scheme) {
                templateTreeModel.nodeChanged(selectedNode)
                return@addTreeSelectionListener
            }

            schemeEditor = createEditor(selectedObject)
                .also { editor ->
                    editor.addChangeListener(
                        {
                            editor.applyState()
                            templateTreeModel.nodeChanged(selectedNode)
                            templateTreeModel.nodeStructureChanged(selectedNode)
                        }.also { it() } // Invoke listener to apply automatic fixes from editor's constructor
                    )

                    schemeEditorPanel.add(editor.rootComponent)
                    schemeEditorPanel.revalidate() // Show editor immediately
                }
        }
        splitter.firstComponent = JBScrollPane(decorateTemplateList(templateTree))

        // Right half
        val previewPanel = PreviewPanel {
            val selectedNode = templateTree.getSelectedNode() ?: return@PreviewPanel LiteralScheme("")
            val selectedTemplate =
                selectedNode.state.let {
                    if (it is Template) it
                    else selectedNode.parent!!.state as Scheme
                }

            readState().templateList.templates.first { it.uuid == selectedTemplate.uuid }
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
            is TemplateReference -> TemplateReferenceEditor(scheme)
            is Template -> TemplateNameEditor(scheme)
            else -> error("Unknown scheme type '${scheme.javaClass.canonicalName}'.")
        }

    /**
     * Adds the given scheme at an appropriate location in the tree based on the currently selected node.
     *
     * @param newScheme the scheme to add
     */
    @ActuallyPrivate("Exposed for testing.")
    internal fun addScheme(newScheme: Scheme) {
        val root = templateTreeModel.root as TemplateListTreeNode
        val selectedNode = templateTree.getSelectedNode()
        newScheme.setSettingsState(SettingsState(root.state, symbolSetSettings, dictionarySettings))

        val (childNode, parent, index) =
            if (selectedNode == null) {
                if (newScheme is Template)
                    Triple(TemplateTreeNode(newScheme), root, root.childCount)
                else
                    error("Cannot add non-template to root.")
            } else if (selectedNode is TemplateTreeNode) {
                if (newScheme is Template)
                    Triple(TemplateTreeNode(newScheme), root, root.getIndex(selectedNode) + 1)
                else
                    Triple(SchemeTreeNode(newScheme), selectedNode, selectedNode.childCount)
            } else if (selectedNode is SchemeTreeNode) {
                if (newScheme is Template)
                    Triple(TemplateTreeNode(newScheme), root, root.getIndex(selectedNode.parent) + 1)
                else
                    selectedNode.parent!!.let { Triple(SchemeTreeNode(newScheme), it, it.getIndex(selectedNode) + 1) }
            } else {
                error("Unknown node type '${selectedNode.javaClass.canonicalName}'.")
            }

        templateTreeModel.insertNodeInto(childNode, parent, index)
        templateTree.selectionPath = templateTree.getPathToRoot(childNode)
        templateTree.expandPath(templateTree.selectionPath)
    }

    /**
     * Moves the given node down the given number of positions.
     *
     * The node will be moved down relative to its siblings. The parent does not change.
     *
     * @param node the node to move down
     * @param positions the number of positions to move the node down by within its parent; can be negative
     */
    private fun moveNodeDownBy(node: StateTreeNode<*>, positions: Int) {
        val parent = node.parent as StateTreeNode<*>
        val oldIndex = parent.getIndex(node)

        templateTreeModel.removeNodeFromParent(node)
        templateTreeModel.insertNodeInto(node, parent, oldIndex + positions)
        templateTree.selectionPath = templateTree.getPathToRoot(node)
        templateTree.expandPath(templateTree.selectionPath)
    }


    override fun loadState(state: SettingsState) {
        super.loadState(state)

        val stateCopy = state.deepCopy(retainUuid = true)
        dictionarySettings = stateCopy.dictionarySettings
        symbolSetSettings = stateCopy.symbolSetSettings
        templateTreeModel.setRoot(TemplateListTreeNode(stateCopy.templateList.applySettingsState(stateCopy)))
        templateTreeModel.reload()

        val root = templateTreeModel.root as TemplateListTreeNode
        root.children().toList().forEach { templateTree.expandPath(templateTree.getPathToRoot(it)) }
        root.firstLeaf()?.also { templateTree.selectionPath = templateTree.getPathToRoot(it) }
    }

    override fun readState() =
        SettingsState(
            (templateTreeModel.root as TemplateListTreeNode).state.deepCopy(retainUuid = true),
            symbolSetSettings.deepCopy(retainUuid = true),
            dictionarySettings.deepCopy(retainUuid = true)
        ).also { it.uuid = originalState.uuid }

    override fun applyState() = originalState.copyFrom(readState().also { it.templateList.applySettingsState(it) })

    override fun reset() {
        super.reset()

        queueSelection?.also { selection ->
            (templateTreeModel.root as TemplateListTreeNode)
                .children().toList()
                .firstOrNull { it.state.uuid == selection }
                ?.also {
                    templateTree.selectionPath = templateTree.getPathToRoot(it)
                    SwingUtilities.invokeLater { schemeEditor?.preferredFocusedComponent?.requestFocus() }
                }

            queueSelection = null
        }
    }


    override fun doValidate() = readState().doValidate()


    override fun addChangeListener(listener: () -> Unit) {
        templateTreeModel.addTreeModelListener(object : TreeModelListener {
            override fun treeNodesChanged(event: TreeModelEvent) {
                listener()
            }

            override fun treeNodesInserted(event: TreeModelEvent) {
                listener()
            }

            override fun treeNodesRemoved(event: TreeModelEvent) {
                listener()
            }

            override fun treeStructureChanged(event: TreeModelEvent) {
                listener()
            }
        })
        templateTree.addTreeSelectionListener { listener() }
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
        private inner class AddTemplatePopupStep : BaseListPopupStep<Template>(null, AVAILABLE_ADD_TEMPLATES) {
            override fun getIconFor(value: Template?) = value?.icon

            override fun getTextFor(value: Template?) = value?.name ?: Template.DEFAULT_NAME

            override fun onChosen(value: Template?, finalChoice: Boolean): PopupStep<*>? =
                value?.also { addScheme(it.deepCopy()) }?.let { null }

            override fun isSpeedSearchEnabled() = true
        }

        /**
         * The [Scheme]-related entries in [AddPopupStep].
         */
        private inner class AddSchemePopupStep : BaseListPopupStep<Scheme>(null, AVAILABLE_ADD_SCHEMES) {
            override fun getIconFor(value: Scheme?) = value?.icon

            override fun getTextFor(value: Scheme?) = value?.name ?: Scheme.DEFAULT_NAME

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
            val parent = selectedNode.parent as StateTreeNode<*>
            val oldIndex = parent.getIndex(selectedNode)

            templateTreeModel.removeNodeFromParent(selectedNode)
            if (parent.isLeaf && parent == templateTreeModel.root)
                templateTree.clearSelection()
            else
                templateTree.selectionPath =
                    templateTree.getPathToRoot(
                        if (parent.isLeaf) parent
                        else parent.getChildAt(min(oldIndex, parent.childCount - 1))
                    )
        }
    }

    /**
     * The button to display for copying an entry in the tree.
     */
    private inner class CopyActionButton : AnActionButton("Copy", AllIcons.Actions.Copy) {
        override fun actionPerformed(event: AnActionEvent) {
            val node = templateTree.getSelectedNode() ?: return

            addScheme((node.state as Scheme).deepCopy())
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

            return node.parent!!.getIndex(node) > 0
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

            return node.parent!!.getIndex(node) < node.parent!!.childCount - 1
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

        /**
         * Returns the list of templates that the user can add from the add action.
         */
        val AVAILABLE_ADD_TEMPLATES: List<Template>
            get() = TemplateList.DEFAULT_TEMPLATES

        /**
         * Returns the list of schemes that the user can add from the add action.
         */
        val AVAILABLE_ADD_SCHEMES: List<Scheme>
            get() = listOf(
                LiteralScheme(),
                IntegerScheme(),
                DecimalScheme(),
                StringScheme(),
                WordScheme(),
                UuidScheme(),
                TemplateReference()
            )
    }
}


/**
 * Returns the currently selected node, or `null` if no node is selected, or `null` if the root is selected.
 *
 * @return the currently selected node
 */
private fun JTree.getSelectedNode() =
    (lastSelectedPathComponent as? StateTreeNode<*>)
        ?.let {
            if (it == model.root) null
            else it
        }

/**
 * Returns the path from the given node to the root path, including both ends.
 *
 * @param treeNode the node to return the path to root from
 * @return the path from the given node to the root path, including both ends
 */
private fun JTree.getPathToRoot(treeNode: TreeNode) = TreePath((model as DefaultTreeModel).getPathToRoot(treeNode))


/**
 * A mutable tree node that contains a state of type [S].
 *
 * The node is mutable in the sense that its [state] is mutable, but the reference to the state is not.
 *
 * `StateTreeNode`s are peculiar in that its parent and all its children must also be `StateTreeNode`s.
 *
 * @param S the type of state contained in the mutable tree node
 */
private sealed class StateTreeNode<S : State> : MutableTreeNode {
    /**
     * The parent of this node.
     */
    private var _parent: StateTreeNode<*>? = null

    /**
     * The mutable [State] contained in this node.
     */
    abstract val state: S


    /**
     * Returns the parent of this node, as assigned through [setParent], or `null` if there is no parent (anymore).
     *
     * @return the parent of this node
     */
    override fun getParent(): StateTreeNode<*>? = _parent

    /**
     * Sets the parent of this node.
     *
     * @param newParent the new parent of this node, or `null` if the parent should be removed; must be an instance of
     * [StateTreeNode]
     */
    override fun setParent(newParent: MutableTreeNode?) {
        require(newParent is StateTreeNode<*>?) { "Parent of StateTreeNode must be a StateTreeNode." }

        _parent = newParent
    }

    /**
     * Removes this node as a child from its parent.
     *
     * Note that this does not remove this node's reference to its parent; use [setParent] for that.
     */
    override fun removeFromParent() {
        _parent?.remove(this)
    }


    /**
     * Returns the children of this node, all of which are `StateTreeNode`s.
     *
     * If this node does not or cannot contain children, an empty enumeration is returned.
     *
     * @return the children of this node
     */
    abstract override fun children(): Enumeration<out StateTreeNode<*>>

    /**
     * Returns the child at the given index.
     *
     * If the node cannot have children or the index is out of bounds, an exception is thrown.
     *
     * @param childIndex the index of the child to retrieve, corresponding to the ordering in [children]
     * @return the child at the given index
     */
    abstract override fun getChildAt(childIndex: Int): StateTreeNode<*>


    /**
     * Not implemented; [state] cannot be replaced by another instance.
     *
     * @param userObject ignored
     * @throws UnsupportedOperationException this method has not been implemented
     */
    final override fun setUserObject(userObject: Any?) =
        throw UnsupportedOperationException("Cannot replace state of StateTreeNode.")
}

/**
 * A [StateTreeNode] of which the children correspond directly to a field in [state].
 *
 * Modifications to the children of this node directly affect the entries in [state] through the [entries] field.
 *
 * @param S the type of state represented by this node
 * @param T the type of state contained as children in an [S]
 */
private sealed class StateTreeListNode<S : State, T : State> : StateTreeNode<S>() {
    /**
     * The entries of [T] contained in [state].
     *
     * This field connects this node's children to the backing field in [state], so that this field can be modified to
     * modify [state] whenever the children of this node are changed.
     */
    protected abstract var entries: List<T>


    /**
     * Creates a node containing [child] that can be added to this node.
     *
     * @param child the child to wrap in a node
     * @return the child wrapped in a node
     */
    abstract fun createChildNodeFor(child: T): StateTreeNode<T>


    /**
     * Returns true.
     *
     * @return true
     */
    override fun getAllowsChildren() = true

    /**
     * Returns the schemes in [state] mapped to nodes.
     *
     * This method creates new nodes each time it is called, but the nodes wrap the same [Scheme] instances.
     *
     * @return the schemes in [state] mapped to nodes
     */
    override fun children(): Enumeration<StateTreeNode<T>> =
        Collections.enumeration(entries.map { createChildNodeFor(it) }.onEach { it.setParent(this) })

    /**
     * Returns the child at the given index.
     *
     * If the index is out of bounds, an exception is thrown.
     *
     * @param childIndex the index of the child to retrieve, corresponding to the ordering in [children]
     * @return the child at the given index
     */
    override fun getChildAt(childIndex: Int): StateTreeNode<T> =
        createChildNodeFor(entries[childIndex]).also { it.setParent(this) }

    /**
     * Returns the number of schemes in [state].
     *
     * @return the number of schemes in [state]
     */
    override fun getChildCount() = entries.size

    /**
     * Returns true if and only if [state] has no schemes.
     *
     * @return true if and only if [state] has no schemes
     */
    override fun isLeaf() = entries.isEmpty()


    /**
     * Returns the index of the child's scheme in [state]'s schemes, or -1 if [child] is not a child of this node.
     *
     * @param child the child to return the index of
     * @return the index of the child's scheme in [state]'s schemes, or -1 if [child] is not a child of this node
     */
    override fun getIndex(child: TreeNode?): Int {
        if (child !is StateTreeNode<*>) return -1

        return entries.indexOfFirst { it.uuid == child.state.uuid }
    }

    /**
     * Adds the child's scheme to [state]'s schemes.
     *
     * @param node the child of which the scheme should be added to [state]
     * @param index the index to insert the scheme at; should be in the range 0..[getChildCount]
     */
    @Suppress("UNCHECKED_CAST") // No way around it because we're breaking covariance
    override fun insert(node: MutableTreeNode, index: Int) {
        require(node is StateTreeNode<*>) { "Cannot add child that does not contain a Scheme." }

        node.setParent(this)
        entries = entries.toMutableList().also { it.add(index, node.state as T) }
    }

    /**
     * Removes the child at the given index from this node.
     *
     * If the index is out of bounds, an exception is thrown.
     *
     * @param index the index of the child to remove
     */
    override fun remove(index: Int) {
        getChildAt(index).setParent(null)
        entries = entries.toMutableList().also { it.removeAt(index) }
    }

    /**
     * Removes the given node as a child from this node.
     *
     * If the given node is not a child of this node, an exception is thrown.
     *
     * @param child the node to remove as a child from this node; must be a [SchemeTreeNode]
     */
    override fun remove(child: MutableTreeNode?) = remove(getIndex(child))
}

/**
 * A [StateTreeListNode] for a [TemplateList], which is composed of [Template]s.
 *
 * @property state the template list represented by this node; its templates are this node's children
 */
private class TemplateListTreeNode(override val state: TemplateList) : StateTreeListNode<TemplateList, Template>() {
    override var entries: List<Template>
        get() = state.templates
        set(value) {
            state.templates = value.toMutableList()
        }


    override fun createChildNodeFor(child: Template) = TemplateTreeNode(child)


    /**
     * Returns the depth-first node without children in the tree rooted at this node that is not this node, or `null` if
     * no such node exists.
     *
     * @return the depth-first node without children in the tree rooted at this node that is not this node, or `null` if
     * no such node exists
     */
    fun firstLeaf() =
        if (isLeaf) null
        else if (getChildAt(0).isLeaf) getChildAt(0)
        else getChildAt(0).getChildAt(0)
}

/**
 * A [StateTreeListNode] for [Template]s, which is composed of [Scheme]s.
 *
 * @property state the template represented by this node; its schemes are this node's children
 */
private class TemplateTreeNode(override val state: Template) : StateTreeListNode<Template, Scheme>() {
    override var entries: List<Scheme>
        get() = state.schemes
        set(value) {
            state.schemes = value.toMutableList()
        }


    override fun createChildNodeFor(child: Scheme) = SchemeTreeNode(child)
}

/**
 * A [StateTreeNode] for [Scheme]s.
 *
 * @property state the scheme represented by this node
 */
private class SchemeTreeNode(override val state: Scheme) : StateTreeNode<Scheme>() {
    /**
     * Returns false.
     *
     * @return false
     */
    override fun getAllowsChildren() = false

    /**
     * Returns an empty enumeration.
     *
     * @return an empty enumeration
     */
    override fun children(): Enumeration<StateTreeNode<*>> = Collections.emptyEnumeration()

    /**
     * Not implemented; schemes cannot contain children.
     *
     * @param childIndex ignored
     * @throws UnsupportedOperationException this method has not been implemented
     */
    override fun getChildAt(childIndex: Int) = throw UnsupportedOperationException(NO_CHILDREN_MESSAGE)

    /**
     * Returns 0.
     *
     * @return 0
     */
    override fun getChildCount() = 0

    /**
     * Returns true.
     *
     * @return true
     */
    override fun isLeaf() = true


    /**
     * Returns -1 as this node cannot contain children.
     *
     * @param child ignored
     * @return -1
     */
    override fun getIndex(child: TreeNode?) = -1

    /**
     * Not implemented; schemes cannot contain children.
     *
     * @param child ignored
     * @param index ignored
     * @throws UnsupportedOperationException this method has not been implemented
     */
    override fun insert(child: MutableTreeNode?, index: Int) = throw UnsupportedOperationException(NO_CHILDREN_MESSAGE)

    /**
     * Not implemented; schemes cannot contain children.
     *
     * @param childIndex ignored
     * @throws UnsupportedOperationException this method has not been implemented
     */
    override fun remove(childIndex: Int) = throw UnsupportedOperationException(NO_CHILDREN_MESSAGE)

    /**
     * Not implemented; schemes cannot contain children.
     *
     * @param child ignored
     * @throws UnsupportedOperationException this method has not been implemented
     */
    override fun remove(child: MutableTreeNode?) = throw UnsupportedOperationException(NO_CHILDREN_MESSAGE)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The exception message when an unsupported function is invoked.
         */
        const val NO_CHILDREN_MESSAGE = "Schemes cannot have children."
    }
}
