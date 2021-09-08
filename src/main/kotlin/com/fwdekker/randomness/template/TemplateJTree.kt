package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.State
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.treeStructure.Tree
import javax.swing.JTree
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel
import kotlin.math.min


/**
 * A tree containing templates and schemes.
 *
 * The tree is initially empty. Templates can be loaded into the tree using [reload]. See its documentation for more
 * information.
 *
 * @property isModified Returns true if and only if the given scheme has been modified.
 */
class TemplateJTree(private val isModified: (Scheme) -> Boolean) : Tree(TemplateTreeModel()) {
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
    val selectedNodeNotRoot: TemplateTreeModel.StateNode?
        get() = (lastSelectedPathComponent as? TemplateTreeModel.StateNode)
            ?.let {
                if (it.state == model.root) null
                else it
            }

    /**
     * The currently selected scheme, or `null` if no scheme is currently selected.
     */
    val selectedScheme: Scheme?
        get() = selectedNodeNotRoot?.state as? Scheme


    init {
        TreeSpeedSearch(this) { path -> path.path.filterIsInstance<Scheme>().joinToString { it.name } }

        emptyText.text = TemplateListEditor.EMPTY_TEXT
        isRootVisible = false
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        setCellRenderer(CellRenderer())
    }


    /**
     * Loads the given list of templates.
     *
     * The list loaded there will be contained directly in this tree. Addition, removal, and reordering of templates or
     * schemes from this tree will be synchronised with [list], and vice versa.
     *
     * Attempts to retain the current selection.
     *
     * @param list the state to load into the tree. Do not pass this argument to simply reload the current list
     */
    fun reload(list: TemplateList? = null) {
        val oldSelectedUuid = selectedNodeNotRoot?.state?.uuid

        myModel.reload(list)
        // TODO: Retain expansion instead of expanding all
        myModel.list.templates.map { TemplateTreeModel.StateNode(it) }.forEach { expandPath(myModel.getPathToRoot(it)) }

        if (oldSelectedUuid != null) selectScheme(oldSelectedUuid)
        else selectionPath = myModel.getPathToRoot(myModel.getFirstLeaf())
    }


    /**
     * Adds the given scheme at an appropriate location in the tree based on the currently selected node.
     *
     * @param newScheme the scheme to add. Must be an instance of [Template] if [selectedNodeNotRoot] is null
     */
    fun addScheme(newScheme: Scheme) {
        val newNode = TemplateTreeModel.StateNode(newScheme)
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
     * @param scheme the scheme to remove
     */
    fun removeScheme(scheme: Scheme) {
        val node = TemplateTreeModel.StateNode(scheme)
        val parent = myModel.getParentOf(node)!!
        val oldIndex = myModel.getIndexOfChild(parent, scheme)

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
     * @param scheme the scheme to move down
     * @param positions the number of positions to move the scheme down by within its parent; can be negative
     */
    fun moveSchemeDownBy(scheme: Scheme, positions: Int) {
        if (positions == 0) return

        val node = TemplateTreeModel.StateNode(scheme)
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
     * @param scheme the scheme to check for movability
     * @param positions the number of positions to move the scheme down by within its parent; can be negative
     * @return true if and only if the given scheme can be moved down by the given number of positions using
     * [moveSchemeDownBy]
     */
    fun canMoveSchemeDownBy(scheme: Scheme, positions: Int): Boolean {
        val node = TemplateTreeModel.StateNode(scheme)
        val parent = myModel.getParentOf(node)!!
        val newIndex = myModel.getIndexOfChild(parent, node) + positions

        return newIndex in 0 until myModel.getChildCount(parent)
    }

    /**
     * Selects the scheme with the given UUID, if it exists; otherwise, nothing happens.
     *
     * @param targetUuid the UUID of the scheme to select, or `null` if nothing should be done
     * @return true if and only if the scheme was found and selected
     */
    fun selectScheme(targetUuid: String?) {
        if (targetUuid == null) return

        val scheme = myModel.list.getSchemeByUuid(targetUuid) ?: return
        selectionPath = myModel.getPathToRoot(TemplateTreeModel.StateNode(scheme))
    }


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
            val scheme = (value as? TemplateTreeModel.StateNode)?.state as? Scheme
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
}


/**
 * The underlying model of the [TemplateJTree], which synchronizes its state with a [TemplateList] so that modifications
 * to the model appear in the list and vice versa.
 *
 * If the list is modified externally, this model should be notified. The entire model can be refreshed or a different
 * list can be loaded using [reload]. Otherwise, use the fire methods such as [fireNodeChanged] to inform of partial
 * changes.
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
     * Reloads the entire model's structure to synchronize it with the given list.
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
     * Informs listeners that the given node has been changed; it has not been replaced nor have its children changed.
     *
     * Does nothing if the node is null.
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
     * Wraps around a state and equals another node if the contained states' UUIDs are the same.
     *
     * A [JTree] cannot contain two objects that equal each other. However, two [State]s can equal each other even if
     * their UUIDs are different, because of how equals is generated for data classes. Therefore, this node class
     * "replaces" the equals of the contained state for the tree, allowing it to contain two nodes that equal each other
     * as long as their UUIDs are different.
     *
     * @property state The state contained in this node.
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
