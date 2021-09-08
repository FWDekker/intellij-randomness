package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.State
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.AnActionButton
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.CommonActionsPanel
import com.intellij.ui.LayeredIcon
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.event.TreeWillExpandListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel
import kotlin.math.min


/**
 * A tree containing [Template]s and [Scheme]s.
 *
 * The tree contains the templates and schemes defined in the [TemplateList] of [currentState] by loading that list into
 * this tree's [TemplateTreeModel]. Furthermore, when a new [Scheme] is added or copied, it will use the [currentState].
 *
 * This tree reads from [originalState] to determine whether particular [Scheme]s have been modified. Modified [Scheme]s
 * can be reset, in which case the original state is copied into that scheme in the [currentState].
 *
 * @property originalState The original settings before any modifications were made.
 * @property currentState The current settings which include modifications.
 */
class TemplateJTree(
    private val originalState: SettingsState,
    private var currentState: SettingsState
) : Tree(TemplateTreeModel(currentState.templateList)) {
    /**
     * The tree's model.
     *
     * This field cannot be named `model` because this causes an NPE during initialization. This field cannot be named
     * `treeModel` because this name is already taken and cannot be overridden.
     */
    val myModel: TemplateTreeModel
        get() = super.getModel() as TemplateTreeModel

    /**
     * The currently selected node, or `null` if no node is selected, or `null` if the root is selected.
     */
    val selectedNodeNotRoot: StateNode?
        get() = (lastSelectedPathComponent as? StateNode)
            ?.let {
                if (it.state == model.root) null
                else it
            }

    /**
     * The currently selected scheme, or `null` if no scheme is currently selected.
     *
     * Setting `null` will select the first leaf in the tree.
     */
    var selectedScheme: Scheme?
        get() = selectedNodeNotRoot?.state as? Scheme
        set(value) {
            val node = StateNode(value ?: myModel.getFirstLeaf().state)
            selectionPath = myModel.getPathToRoot(node)
        }

    /**
     * UUIDs of templates that have explicitly been collapsed by the user.
     */
    private val explicitlyCollapsed = mutableSetOf<String>()


    init {
        TreeSpeedSearch(this) { path -> path.path.filterIsInstance<Scheme>().joinToString { it.name } }

        emptyText.text = EMPTY_TEXT
        isRootVisible = false
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        setCellRenderer(CellRenderer())

        addTreeWillExpandListener(object : TreeWillExpandListener {
            override fun treeWillExpand(event: TreeExpansionEvent) {
                explicitlyCollapsed.remove((event.path.lastPathComponent as StateNode).state.uuid)
            }

            override fun treeWillCollapse(event: TreeExpansionEvent) {
                explicitlyCollapsed.add((event.path.lastPathComponent as StateNode).state.uuid)
            }
        })
    }

    /**
     * Returns a panel containing this tree decorated with accessible action buttons.
     *
     * @return a panel containing this tree decorated with accessible action buttons
     */
    fun asDecoratedPanel(): JPanel =
        ToolbarDecorator.createDecorator(this)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())
            .setScrollPaneBorder(JBUI.Borders.empty())
            .disableAddAction()
            .disableRemoveAction()
            .addExtraAction(AddSchemeActionButton())
            .addExtraAction(RemoveActionButton())
            .addExtraAction(CopyActionButton())
            .addExtraAction(UpActionButton())
            .addExtraAction(DownActionButton())
            .addExtraAction(ResetActionButton())
            .setButtonComparator("Add", "Edit", "Remove", "Copy", "Up", "Down", "Reset")
            .createPanel()


    /**
     * Loads the given list of templates.
     *
     * This is a wrapper around [TemplateTreeModel.reload] that additionally tries to retain the current selection
     * and expansion state.
     */
    fun reload() {
        val oldSelectedScheme = selectedScheme

        myModel.reload()
        myModel.list.templates
            .filterNot { it.uuid in explicitlyCollapsed }
            .forEach { expandPath(myModel.getPathToRoot(StateNode(it))) }

        selectedScheme = oldSelectedScheme
    }


    /**
     * Adds the given scheme at an appropriate location in the tree based on the currently selected node.
     *
     * @param newScheme the scheme to add. Must be an instance of [Template] if [selectedNodeNotRoot] is null
     */
    fun addScheme(newScheme: Scheme) {
        val newNode = StateNode(newScheme)
        val selectedNode = selectedNodeNotRoot
        if (newScheme is Template) newScheme.name = findUniqueNameFor(newScheme)

        if (selectedNode == null) {
            if (newScheme is Template)
                myModel.insertNode(myModel.root, newNode)
            else
                error("Cannot add non-template to root.")
        } else if (selectedNode.state is Template) {
            if (newScheme is Template)
                myModel.insertNodeAfter(myModel.root, newNode, selectedNode)
            else
                myModel.insertNode(selectedNode, newNode)
        } else {
            if (newScheme is Template)
                myModel.insertNodeAfter(myModel.root, newNode, selectedNode)
            else
                myModel.insertNodeAfter(myModel.getParentOf(selectedNode)!!, newNode, selectedNode)
        }
    }

    /**
     * Removes the given scheme from the tree, and selects an appropriate other scheme.
     *
     * Throws an exception if the scheme is not in this tree.
     *
     * @param scheme the scheme to remove
     */
    fun removeScheme(scheme: Scheme) {
        val node = StateNode(scheme)
        val parent = myModel.getParentOf(node)!!
        val oldIndex = myModel.getIndexOfChild(parent, node)

        myModel.removeNode(node)
        if (myModel.isLeaf(parent) && parent == myModel.root)
            clearSelection()
        else
            selectionPath =
                myModel.getPathToRoot(
                    if (myModel.isLeaf(parent)) parent
                    else myModel.getChild(parent, min(oldIndex, myModel.getChildCount(parent) - 1))
                )
    }

    /**
     * Moves the given scheme down the given number of positions.
     *
     * The scheme will be moved down relative to its siblings. The parent does not change.
     *
     * Throws an exception if the scheme is not in this tree.
     *
     * @param scheme the scheme to move down
     * @param positions the number of positions to move the scheme down by within its parent; can be negative
     */
    fun moveSchemeDownBy(scheme: Scheme, positions: Int) {
        if (positions == 0) return

        val node = StateNode(scheme)
        val parent = myModel.getParentOf(node)!!
        val oldIndex = myModel.getIndexOfChild(parent, node)

        myModel.removeNode(node)
        myModel.insertNode(parent, node, oldIndex + positions)
        selectionPath = myModel.getPathToRoot(node)
        expandPath(selectionPath)
    }

    /**
     * Returns true if and only if the given scheme can be moved down by the given number of positions using
     * [moveSchemeDownBy].
     *
     * Throws an exception if the scheme is not in this tree.
     *
     * @param scheme the scheme to check whether it can be moved
     * @param positions the number of positions to move the scheme down by within its parent; can be negative
     * @return true if and only if the given scheme can be moved down by the given number of positions using
     * [moveSchemeDownBy]
     */
    fun canMoveSchemeDownBy(scheme: Scheme, positions: Int): Boolean {
        val node = StateNode(scheme)
        val parent = myModel.getParentOf(node)
        val newIndex = myModel.getIndexOfChild(parent, node) + positions

        return newIndex in 0 until myModel.getChildCount(parent)
    }


    /**
     * Returns true if and only if the given scheme has been modified with respect to the [originalState].
     *
     * @param scheme the scheme to check for modification
     * @return true if and only if the given scheme has been modified with respect to the [originalState]
     */
    private fun isModified(scheme: Scheme) =
        originalState.templateList.getSchemeByUuid(scheme.uuid) != scheme ||
            scheme is StringScheme && originalState.symbolSetSettings != currentState.symbolSetSettings ||
            scheme is WordScheme && originalState.dictionarySettings != currentState.dictionarySettings

    /**
     * Finds a good, unique name for the given template so that it can be inserted into this list without conflict.
     *
     * If the name is already unique, that name is returned. Otherwise, the name is appended with the first number `i`
     * such that `$name ($i)` is unique. If the template's current name already ends with a number in parentheses, that
     * number is taken as the starting number.
     *
     * @param template the template to find a good name for
     * @return a unique name for the given template
     */
    private fun findUniqueNameFor(template: Template): String {
        val templateNames = myModel.list.templates.map { it.name }
        if (template.name !in templateNames) return template.name

        var i = 1
        var name = template.name

        if (name.matches(Regex(".* \\([1-9][0-9]*\\)"))) {
            i = name.substring(name.lastIndexOf('(') + 1, name.lastIndexOf(')')).toInt()
            name = name.substring(0, name.lastIndexOf('(') - 1)
        }

        while ("$name ($i)" in templateNames) i++
        return "$name ($i)"
    }


    /**
     * Renders a cell in the tree.
     */
    private inner class CellRenderer : ColoredTreeCellRenderer() {
        override fun customizeCellRenderer(
            tree: JTree,
            value: Any?,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ) {
            val scheme = (value as? StateNode)?.state as? Scheme
            if (scheme == null) {
                append("<unknown>")
                return
            }

            icon = scheme.icon

            append(
                scheme.name.ifBlank { "<empty>" },
                when {
                    scheme.doValidate() != null -> SimpleTextAttributes.ERROR_ATTRIBUTES
                    isModified(scheme) -> SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES
                    else -> SimpleTextAttributes.REGULAR_ATTRIBUTES
                }
            )
        }
    }


    /**
     * Displays a popup to add a scheme, or immediately adds a template if nothing is currently selected.
     */
    private inner class AddSchemeActionButton : AnActionButton("Add", AllIcons.General.Add) {
        override fun actionPerformed(event: AnActionEvent) {
            if (selectedNodeNotRoot == null) {
                addScheme(AVAILABLE_ADD_SCHEMES[0])
                return
            }

            JBPopupFactory.getInstance()
                .createListPopup(AddSchemePopupStep())
                .show(preferredPopupPoint ?: return)
        }

        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.ADD)

        override fun updateButton(event: AnActionEvent) {
            super.updateButton(event)

            event.presentation.icon =
                if (selectedNodeNotRoot == null) AllIcons.General.Add
                else LayeredIcon.ADD_WITH_DROPDOWN
        }


        /**
         * The [Scheme]-related entries in [AddSchemeActionButton].
         */
        private inner class AddSchemePopupStep : BaseListPopupStep<Scheme>(
            null,
            AVAILABLE_ADD_SCHEMES
        ) {
            override fun getIconFor(value: Scheme?) = value?.icon

            override fun getTextFor(value: Scheme?) = value?.name ?: Scheme.DEFAULT_NAME

            override fun onChosen(value: Scheme?, finalChoice: Boolean): PopupStep<*>? {
                if (value != null) addScheme(value.deepCopy().also { it.setSettingsState(currentState) })

                return null
            }

            override fun isSpeedSearchEnabled() = true

            override fun getSeparatorAbove(value: Scheme?) =
                if (value == AVAILABLE_ADD_SCHEMES[1]) ListSeparator()
                else null

            override fun getDefaultOptionIndex() = 0
        }
    }

    /**
     * Removes the selected scheme.
     */
    private inner class RemoveActionButton : AnActionButton("Remove", AllIcons.General.Remove) {
        override fun actionPerformed(event: AnActionEvent) = removeScheme(selectedScheme!!)

        override fun isEnabled() = selectedNodeNotRoot != null

        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.REMOVE)
    }

    /**
     * Duplicates the selected scheme.
     */
    private inner class CopyActionButton : AnActionButton("Copy", AllIcons.Actions.Copy) {
        override fun actionPerformed(event: AnActionEvent) {
            val copy = selectedScheme!!.deepCopy()
            copy.setSettingsState(currentState)

            addScheme(copy)
        }

        override fun isEnabled() = selectedNodeNotRoot != null
    }

    /**
     * Moves a scheme up by one position.
     */
    private inner class UpActionButton : AnActionButton("Up", AllIcons.Actions.MoveUp) {
        override fun actionPerformed(event: AnActionEvent) = moveSchemeDownBy(selectedScheme!!, -1)

        override fun isEnabled() = selectedScheme?.let { canMoveSchemeDownBy(it, -1) } ?: false

        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.UP)
    }

    /**
     * Moves a scheme down by one position.
     */
    private inner class DownActionButton : AnActionButton("Down", AllIcons.Actions.MoveDown) {
        override fun actionPerformed(event: AnActionEvent) = moveSchemeDownBy(selectedScheme!!, 1)

        override fun isEnabled() = selectedScheme?.let { canMoveSchemeDownBy(it, 1) } ?: false

        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.DOWN)
    }

    /**
     * Resets the selected scheme to its original state, or removes it if it has no original state.
     */
    private inner class ResetActionButton : AnActionButton("Reset", AllIcons.General.Reset) {
        override fun actionPerformed(event: AnActionEvent) {
            val toReset = selectedScheme!!
            val toResetFrom = originalState.templateList.getSchemeByUuid(toReset.uuid)
            if (toResetFrom == null) {
                removeScheme(toReset)
                return
            }

            toReset.copyFrom(toResetFrom)
            toReset.setSettingsState(currentState)

            myModel.fireNodeStructureChanged(StateNode(toReset))
            selectedScheme = toReset
        }

        override fun isEnabled() = selectedScheme?.let { isModified(it) } ?: false
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The text that is displayed when the table is empty.
         */
        const val EMPTY_TEXT = "No templates configured."

        /**
         * Returns the list of schemes that the user can add from the add action.
         */
        val AVAILABLE_ADD_SCHEMES: List<Scheme>
            get() = listOf(
                Template("Template", emptyList()),
                IntegerScheme(),
                DecimalScheme(),
                StringScheme(),
                WordScheme(),
                UuidScheme(),
                LiteralScheme(),
                TemplateReference()
            )
    }
}


/**
 * The underlying model of the [TemplateJTree], which synchronizes its state with a [TemplateList] so that modifications
 * to the model appear in the list and vice versa.
 *
 * If the list is modified externally, this model should be notified. The entire model can be refreshed or a different
 * list can be loaded using [reload]. Otherwise, use the `fire` methods such as [fireNodeChanged] to inform of partial
 * changes.
 *
 * The various [State]s that are contained in the [TemplateList] are hierarchically structured as [StateNode]s. As long
 * as the appropriate events are fired when external modifications are made to the [TemplateList], this model provides a
 * number of consistency guarantees. Firstly, the root node is always a [TemplateList], second-level nodes are always
 * [Template]s, and third-level nodes are always [Scheme]s. Secondly, except for the root node, all nodes that are
 * contained in this model have a parent.
 *
 * @property list The list to be modeled.
 */
class TemplateTreeModel(var list: TemplateList = TemplateList(emptyList())) : TreeModel {
    /**
     * The listeners that are informed when the state of the tree changes.
     */
    private val treeModelListeners = mutableListOf<TreeModelListener>()


    /**
     * Returns [list] as a [StateNode].
     */
    override fun getRoot() = StateNode(list)

    /**
     * Reloads the entire model's structure to synchronize it with the changes in the given list.
     *
     * Use this method if the entire model should be reloaded from the list because major changes have been made. If
     * small changes have been made, consider using one of the `fire` methods such as [fireNodeChanged].
     *
     * @param newList the new list, or `null` to use the current root list
     */
    fun reload(newList: TemplateList? = null) {
        list = newList ?: list

        fireNodeStructureChanged(root)
    }


    /**
     * Returns true if and only if [node] does not have children.
     *
     * @param node the node to check whether it is a leaf
     * @return true if and only if [node] does not have children
     */
    override fun isLeaf(node: Any?): Boolean {
        require(node is StateNode) { "`node` must be a StateNode." }

        return !node.canHaveChildren || node.children.isEmpty()
    }

    /**
     * Returns the child at the given index in the parent.
     *
     * Throws an exception if the index is out of bounds.
     *
     * @param parent the parent to return the child of
     * @param index the index of the child in the parent
     * @return the child at the given index in the parent
     */
    override fun getChild(parent: Any?, index: Int): StateNode {
        require(parent is StateNode) { mustBeAStateNode }

        return parent.children[index]
    }

    /**
     * Returns the number of children in the parent.
     *
     * @param parent the parent to return the number of children of
     * @return the number of children in the parent
     */
    override fun getChildCount(parent: Any?): Int {
        require(parent is StateNode) { mustBeAStateNode }

        return if (!parent.canHaveChildren) 0
        else parent.children.size
    }

    /**
     * Returns the index of the child in the parent, or -1 if either the parent or the child is null.
     *
     * Throws an exception if the child is not contained in the parent.
     *
     * @param parent the parent to find the index in
     * @param child the child to return the index of
     * @return the index of the child in the parent, or -1 if either the parent or the child is null
     */
    override fun getIndexOfChild(parent: Any?, child: Any?): Int {
        if (parent == null || child == null) return -1

        require(parent is StateNode) { mustBeAStateNode }
        require(child is StateNode) { "`child` must be a StateNode." }

        return parent.children.indexOf(child)
    }

    /**
     * Returns the parent of the given node, or `null` if the node has no parent.
     *
     * Throws an exception if the node is not contained in this model.
     *
     * @param node the node to return the parent of
     * @return the parent of the given node, or `null` if the node has no parent
     */
    fun getParentOf(node: StateNode): StateNode? =
        if (!root.contains(node))
            error("Cannot get parent of node not in this model.")
        else
            when (node.state) {
                is TemplateList -> null
                is Template -> root
                else -> StateNode(list.templates.first { node.state in it.schemes })
            }

    /**
     * Returns the path from the [root] to the given node.
     *
     * Throws an exception if the node is not contained in this model.
     *
     * @param node the node to return the path to
     * @return the path from the [root] to the given node
     */
    fun getPathToRoot(node: StateNode): TreePath =
        if (!root.contains(node))
            error("Cannot get path to node not in this model.")
        else
            when (node.state) {
                is TemplateList -> TreePath(arrayOf(node))
                is Template -> TreePath(arrayOf(root, node))
                is Scheme -> TreePath(arrayOf(root, getParentOf(node)!!, node))
                else -> error("Unknown parent type '${node.javaClass}'.")
            }

    /**
     * Returns the first node that is a leaf.
     *
     * @return the first node that is a leaf
     */
    fun getFirstLeaf(): StateNode =
        if (list.templates.isEmpty()) root
        else if (list.templates[0].schemes.isEmpty()) StateNode(list.templates[0])
        else StateNode(list.templates[0].schemes[0])


    /**
     * Inserts the given node as a child to the parent at the given index.
     *
     * Throws an exception if the index is out of bounds.
     *
     * @param parent the node to insert the child into
     * @param child the node to insert into the parent
     * @param index the index in the parent to insert the child at
     */
    fun insertNode(parent: StateNode, child: StateNode, index: Int = getChildCount(parent)) {
        parent.children = parent.children.toMutableList().also { it.add(index, child) }
        fireNodeInserted(child, parent, index)
    }

    /**
     * Inserts the given state as a child to the parent right after [after].
     *
     * Throws an exception if [child] and [after] are not siblings.
     *
     * @param parent the state to insert the child into
     * @param child the state to insert into the parent
     * @param after the state to insert the child after
     */
    fun insertNodeAfter(parent: StateNode, child: StateNode, after: StateNode) =
        insertNode(parent, child, getIndexOfChild(parent, after) + 1)

    /**
     * Removes the given node from the model.
     *
     * Throws an exception if the node is not contained in this model, or if the node to remove is the root.
     *
     * @param node the node to remove from the model
     */
    fun removeNode(node: StateNode) {
        require(root.contains(node)) { "Cannot remove state not contained in this model." }
        require(node != root) { "Cannot remove root from model." }

        val parent = getParentOf(node)!!
        val oldIndex = getIndexOfChild(parent, node)

        parent.children = parent.children.toMutableList().also { it.remove(node) }
        fireNodeRemoved(node, parent, oldIndex)
    }


    /**
     * Informs listeners that the given node has been changed.
     *
     * This method is applicable if the given node's internal state has changed and the way it is displayed should be
     * updated. However, this method is not applicable if the entire node has been replaced with a different instance
     * or if the children of the node have been changed. In those two latter scenarios, use [fireNodeStructureChanged].
     *
     * Does nothing if the node is `null`.
     *
     * @param node the node that has been changed
     */
    fun fireNodeChanged(node: StateNode?) {
        if (node == null) return

        val event =
            if (node.state is TemplateList) {
                TreeModelEvent(this, getPathToRoot(node))
            } else {
                val parent = getParentOf(node)!!
                TreeModelEvent(this, getPathToRoot(parent), intArrayOf(getIndexOfChild(parent, node)), arrayOf(node))
            }

        treeModelListeners.forEach { it.treeNodesChanged(event) }
    }

    /**
     * Informs listeners that the given node has been inserted into the model.
     *
     * Does nothing if the node is null.
     *
     * @param node the node that has been inserted into the model
     * @param parent the parent into which the child was inserted
     * @param index the index at which the child was inserted
     */
    fun fireNodeInserted(node: StateNode?, parent: StateNode, index: Int) {
        if (node == null) return
        require(node.state !is TemplateList) { "Template list cannot have parent so cannot be inserted." }

        treeModelListeners.forEach {
            it.treeNodesInserted(TreeModelEvent(this, getPathToRoot(parent), intArrayOf(index), arrayOf(node)))
        }
    }

    /**
     * Informs listeners that the given child has been removed from the model.
     *
     * If `child` has children nodes itself, then you must **not** invoke this method on those children.
     *
     * Does nothing if the child is null.
     *
     * @param child the child that has been removed from the model
     * @param parent the parent from which the child was removed
     * @param index the former index of the child in the parent
     */
    fun fireNodeRemoved(child: StateNode?, parent: StateNode, index: Int) {
        if (child == null) return
        require(child.state !is TemplateList) { "Template list cannot have parent so cannot be removed." }

        treeModelListeners.forEach {
            it.treeNodesRemoved(TreeModelEvent(this, getPathToRoot(parent), intArrayOf(index), arrayOf(child)))
        }
    }

    /**
     * Informs listeners that the given node's structure has been changed.
     *
     * Does nothing if the node is null.
     *
     * @param node the node of which the structure has been changed
     */
    fun fireNodeStructureChanged(node: StateNode?) {
        if (node == null) return

        treeModelListeners.forEach { it.treeStructureChanged(TreeModelEvent(this, getPathToRoot(node))) }
    }


    /**
     * Updates the value at the given path.
     *
     * Use this when a state has been modified. Do not use this when a state has been replaced by another state.
     *
     * @param path the path to update the value at
     * @param newValue the new value containing the desired changes
     */
    override fun valueForPathChanged(path: TreePath, newValue: Any) {
        require(newValue is StateNode) { "Cannot update value of type '${newValue.javaClass}'." }
        println("VALUE FOR PATH CHANGED")

        // TODO: Do we need to implement this method?
        (path.lastPathComponent as StateNode).state.copyFrom(newValue.state)
        fireNodeChanged(newValue)
    }

    /**
     * Adds the given listener.
     *
     * @param listener the listener to add
     */
    override fun addTreeModelListener(listener: TreeModelListener?) {
        if (listener != null)
            treeModelListeners.add(listener)
    }

    /**
     * Removes the given listener.
     *
     * @param listener the listener to remove
     */
    override fun removeTreeModelListener(listener: TreeModelListener?) {
        if (listener != null)
            treeModelListeners.remove(listener)
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * Error message for type-checking errors when parameter `parent`.
         */
        const val mustBeAStateNode = "`parent` must be a StateNode."
    }
}

/**
 * Wraps around a state and equals another node if the contained states' UUIDs are the same.
 *
 * A [JTree] cannot contain two objects that equal each other. However, two [State]s can equal each other even if
 * their UUIDs are different because of how equals is generated for data classes. This makes it impossible to use
 * [State]s in a [JTree]. Therefore, this node class "replaces" the equals of the contained [State] for the tree,
 * allowing it to contain two nodes that equal each other as long as their UUIDs are different.
 *
 * @property state The state contained in this node.
 * @see TemplateTreeModel
 */
class StateNode(val state: State) {
    /**
     * True if and only if this node can have children.
     */
    val canHaveChildren: Boolean
        get() = state is TemplateList || state is Template

    /**
     * The child nodes contained in this node.
     *
     * The getter and setter throw an exception if this node cannot contain children. An exception is thrown if and
     * only if [canHaveChildren] is false.
     */
    var children: List<StateNode>
        get() =
            when (state) {
                is TemplateList -> state.templates.map { StateNode(it) }
                is Template -> state.schemes.map { StateNode(it) }
                else -> error("Unknown parent type '${state.javaClass}'.")
            }
        set(value) {
            when (state) {
                is TemplateList -> state.templates = value.map { it.state as Template }
                is Template -> state.schemes = value.map { it.state as Scheme }
                else -> error("Unknown parent type '${state.javaClass}'.")
            }
        }


    /**
     * Returns true if and only if the given node is contained in the tree rooted at this node.
     *
     * @param node the node to find
     * @return true if and only if the given node is contained in the tree rooted at this node
     */
    fun contains(node: StateNode): Boolean =
        this == node || canHaveChildren && children.any { it.contains(node) }


    /**
     * Returns true if and only if [other]'s UUID is the same as [state]'s UUID.
     *
     * @param other the object to compare against
     * @return true if and only if [other]'s UUID is the same as [state]'s UUID
     */
    override fun equals(other: Any?) = other is StateNode && this.state.uuid == other.state.uuid

    /**
     * Returns the hash code of the [state]'s UUID.
     *
     * @return the hash code of the [state]'s UUID
     */
    override fun hashCode() = state.uuid.hashCode()
}
