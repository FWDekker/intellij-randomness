package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.State
import com.intellij.util.ui.EditableModel
import javax.swing.JTree
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath


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
 * @param list the list to be modeled
 */
@Suppress("detekt:TooManyFunctions") // Normal for Swing implementations
class TemplateJTreeModel(list: TemplateList = TemplateList(emptyList())) : TreeModel, EditableModel {
    /**
     * The listeners that are informed when the state of the tree changes.
     */
    private val treeModelListeners = mutableListOf<TreeModelListener>()

    /**
     * The list that is modeled by this model.
     *
     * Replace or re-synchronize the list by invoking [reload].
     */
    var list: TemplateList = list
        private set

    /**
     * Returns the node at the given row index, considering which nodes are currently collapsed, ignoring the root.
     *
     * Used to implement [EditableModel].
     */
    var rowToNode: (Int) -> StateNode? = { root.recursiveChildren.getOrNull(it) }

    /**
     * Returns the row index of the given node, considering which nodes are currently collapsed, ignoring the root, or
     * `-1` if the node could not be found.
     *
     * Used to implement [EditableModel].
     */
    var nodeToRow: (StateNode?) -> Int = { root.recursiveChildren.indexOf(it) }

    /**
     * Expands and selects the given node.
     *
     * Used to implement [EditableModel].
     */
    var expandAndSelect: (StateNode) -> Unit = {}


    /**
     * Returns [list] as a [StateNode].
     */
    override fun getRoot() = StateNode(list)

    /**
     * Reloads the entire model's structure to synchronize it with the changes in [newList].
     *
     * Use this method if the entire model should be reloaded from the list because major changes have been made. If
     * small changes have been made, consider using one of the `fire` methods such as [fireNodeChanged].
     *
     * @param newList the new list, or `null` to use the current root list
     */
    fun reload(newList: TemplateList = list) {
        list = newList

        fireNodeStructureChanged(root)
    }


    /**
     * Not implemented because this method is used only if this is a model for a table.
     */
    override fun addRow() = error(Bundle("template_list.error.add_empty_row"))

    /**
     * Not implemented because this method is used only if this is a model for a table.
     *
     * @param index ignored
     */
    override fun removeRow(index: Int) = error(Bundle("template_list.error.remove_row_by_index"))

    /**
     * Moves the node at row [oldIndex] to row [newIndex], and expands and selects the moved node.
     *
     * Indices are looked up using [rowToNode], and are typically relative to the current expansion state of the view.
     *
     * If [oldIndex] and [newIndex] both refer to a template or both refer to a scheme, then the node at [oldIndex] will
     * be inserted so that it has the same index in its parent as the node at [newIndex] had before invoking this
     * method. Otherwise, if [oldIndex] is a non-template scheme and [newIndex] is a template, then the non-template
     * scheme is moved to the template at [newIndex]; if [oldIndex] is less than [newIndex] then the non-template scheme
     * becomes the template's first child, otherwise it becomes the template's last child.
     *
     * @param oldIndex the index of the row of the node to move
     * @param newIndex the index of the row to move the node to
     */
    override fun exchangeRows(oldIndex: Int, newIndex: Int) {
        val nodeToMove = rowToNode(oldIndex)!!
        val targetNode = rowToNode(newIndex)!!

        val targetNodeParent: StateNode
        val targetIndexInParent: Int
        if (nodeToMove.state !is Template && targetNode.state is Template) {
            if (newIndex < oldIndex) {
                targetNodeParent = getChild(root, getIndexOfChild(root, targetNode) - 1)
                targetIndexInParent = getChildCount(targetNodeParent)
            } else {
                targetNodeParent = targetNode
                targetIndexInParent = 0
            }
        } else {
            targetNodeParent = getParentOf(targetNode)!!
            targetIndexInParent = getIndexOfChild(targetNodeParent, targetNode)
        }

        removeNode(nodeToMove)
        insertNode(targetNodeParent, nodeToMove, targetIndexInParent)
        expandAndSelect(nodeToMove)
    }

    /**
     * Returns `true` if and only if the node at row [oldIndex] can be moved to row [newIndex].
     *
     * Indices are looked up using [rowToNode], and are typically relative to the current expansion state of the view.
     *
     * @param oldIndex the index of the row of the node to move
     * @param newIndex the index of the row to move the node to
     * @return `true` if and only if the row at [oldIndex] can be moved to [newIndex]
     */
    override fun canExchangeRows(oldIndex: Int, newIndex: Int): Boolean {
        val oldNode = rowToNode(oldIndex)
        val newNode = rowToNode(newIndex)

        return oldNode != null && newNode != null &&
            if (oldNode.state is Template) newNode.state is Template
            else newIndex != 0
    }


    /**
     * Returns `true` if and only if [node] does not have children.
     *
     * @param node the node to check whether it is a leaf
     * @return `true` if and only if [node] does not have children
     */
    override fun isLeaf(node: Any?): Boolean {
        require(node is StateNode) {
            Bundle("template_list.error.must_be_state_node", "node", node?.let { it::class.java.canonicalName })
        }

        return !node.canHaveChildren || node.children.isEmpty()
    }

    /**
     * Returns the child at the index [index] in [parent].
     *
     * Throws an exception if [index] is out of bounds in [parent].
     *
     * @param parent the parent to return the child of
     * @param index the index of the child in the parent
     * @return the child at the index [index] in [parent]
     */
    override fun getChild(parent: Any?, index: Int): StateNode {
        require(parent is StateNode) {
            Bundle("template_list.error.must_be_state_node", "parent", parent?.let { it::class.java.canonicalName })
        }
        require(parent.canHaveChildren) { Bundle("template_list.error.child_of_infertile_parent") }

        return parent.children[index]
    }

    /**
     * Returns the number of children in the parent.
     *
     * @param parent the parent to return the number of children of
     * @return the number of children in the parent
     */
    override fun getChildCount(parent: Any?): Int {
        require(parent is StateNode) {
            Bundle("template_list.error.must_be_state_node", "parent", parent?.let { it::class.java.canonicalName })
        }

        return if (!parent.canHaveChildren) 0
        else parent.children.size
    }

    /**
     * Returns the index of the child in the parent, or `-1` if either the parent or the child is `null`, or `-1` if the
     * child is not in the parent.
     *
     * @param parent the parent to find the index in
     * @param child the child to return the index of
     * @return the index of the child in the parent, or `-1` if either the parent or the child is `null`, or `-1` if the
     * child is not in the parent
     */
    override fun getIndexOfChild(parent: Any?, child: Any?): Int {
        if (parent == null || child == null) return -1

        require(parent is StateNode) {
            Bundle("template_list.error.must_be_state_node", "parent", parent::class.java.canonicalName)
        }
        require(child is StateNode) {
            Bundle("template_list.error.must_be_state_node", "child", child::class.java.canonicalName)
        }

        return parent.children.indexOf(child)
    }

    /**
     * Returns the parent of [node], or `null` if the node has no parent.
     *
     * Throws an exception if [node] is not contained in this model.
     *
     * @param node the node to return the parent of
     * @return the parent of [node], or `null` if the node has no parent
     */
    fun getParentOf(node: StateNode): StateNode? {
        require(root.contains(node)) { Bundle("template_list.error.parent_of_node_not_in_model") }

        return when (node.state) {
            is TemplateList -> null
            is Template -> root
            else -> root.children.first { node in it.children }
        }
    }

    /**
     * Returns the path from the [root] to [node].
     *
     * Throws an exception if [node] is not contained in this model.
     *
     * @param node the node to return the path to
     * @return the path from the [root] to [node]
     */
    fun getPathToRoot(node: StateNode): TreePath {
        require(root.contains(node)) { Bundle("template_list.error.path_of_node_not_in_model") }

        return when (node.state) {
            is TemplateList -> TreePath(arrayOf(node))
            is Template -> TreePath(arrayOf(root, node))
            else -> TreePath(arrayOf(root, getParentOf(node)!!, node))
        }
    }


    /**
     * Inserts [child] as a child into [parent] at index [index].
     *
     * Throws an exception if [index] is out of bounds in [parent].
     *
     * @param parent the node to insert the child into
     * @param child the node to insert into the parent
     * @param index the index in the parent to insert the child at
     */
    fun insertNode(parent: StateNode, child: StateNode, index: Int = getChildCount(parent)) {
        parent.children = parent.children.toMutableList().also { it.add(index, child) }

        if (parent == root && parent.children.size == 1)
            fireNodeStructureChanged(root)
        else
            fireNodeInserted(child, parent, index)
    }

    /**
     * Inserts [child] as a child into [parent] right after [after].
     *
     * Throws an exception if [after] is not a child of [parent].
     *
     * @param parent the state to insert the child into
     * @param child the state to insert into the parent
     * @param after the state to insert the child after
     */
    fun insertNodeAfter(parent: StateNode, child: StateNode, after: StateNode) {
        val afterIndex = getIndexOfChild(parent, after)
        require(afterIndex >= 0) { Bundle("template_list.error.find_node_insert_parent") }

        insertNode(parent, child, afterIndex + 1)
    }

    /**
     * Removes [node] from this model.
     *
     * Throws an exception if [node] is not contained in this model, or if [node] is [getRoot].
     *
     * @param node the node to remove from the model
     */
    fun removeNode(node: StateNode) {
        require(root.contains(node)) { Bundle("template_list.error.remove_node_not_in_model") }
        require(node != root) { Bundle("template_list.error.remove_root") }

        val parent = getParentOf(node)!!
        val oldIndex = getIndexOfChild(parent, node)

        parent.children = parent.children.toMutableList().also { it.remove(node) }
        fireNodeRemoved(node, parent, oldIndex)
    }


    /**
     * Informs listeners that [node] has been changed.
     *
     * This method is applicable if [node]'s internal state has changed and the way it is displayed should be updated.
     * However, this method is not applicable if the entire node has been replaced with a different instance or if the
     * children of [node] have been changed. In those two latter scenarios, use [fireNodeStructureChanged].
     *
     * Does nothing if [node] is `null`.
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
     * Informs listeners that [node] has been inserted into this model.
     *
     * Does nothing if [node] is `null`.
     *
     * @param node the node that has been inserted into the model
     * @param parent the parent into which the child was inserted
     * @param index the index at which the child was inserted
     */
    fun fireNodeInserted(node: StateNode?, parent: StateNode, index: Int) {
        if (node == null) return
        require(node.state !is TemplateList) { Bundle("template_list.error.insert_list") }

        treeModelListeners.forEach {
            it.treeNodesInserted(TreeModelEvent(this, getPathToRoot(parent), intArrayOf(index), arrayOf(node)))
        }
    }

    /**
     * Informs listeners that [child] has been removed from this model.
     *
     * If [child] has children nodes itself, then you must **not** invoke this method on those children.
     *
     * Does nothing if [child] is `null`.
     *
     * @param child the child that has been removed from the model
     * @param parent the parent from which the child was removed
     * @param index the former index of the child in the parent
     */
    fun fireNodeRemoved(child: StateNode?, parent: StateNode, index: Int) {
        if (child == null) return
        require(child.state !is TemplateList) { Bundle("template_list.error.remove_list") }

        treeModelListeners.forEach {
            it.treeNodesRemoved(TreeModelEvent(this, getPathToRoot(parent), intArrayOf(index), arrayOf(child)))
        }
    }

    /**
     * Informs listeners that [node]'s structure has been changed.
     *
     * Does nothing if [node] is `null`.
     *
     * @param node the node of which the structure has been changed
     */
    fun fireNodeStructureChanged(node: StateNode?) {
        if (node == null) return

        treeModelListeners.forEach { it.treeStructureChanged(TreeModelEvent(this, getPathToRoot(node))) }
    }


    /**
     * Not implemented because this model does not contain an editor component.
     *
     * @param path ignored
     * @param newValue ignored
     */
    override fun valueForPathChanged(path: TreePath, newValue: Any) =
        error(Bundle("template_list.error.change_value_by_path"))

    /**
     * Adds [listener] as a listener.
     *
     * @param listener the listener to add
     */
    override fun addTreeModelListener(listener: TreeModelListener) {
        treeModelListeners.add(listener)
    }

    /**
     * Removes [listener] as a listener.
     *
     * @param listener the listener to remove
     */
    override fun removeTreeModelListener(listener: TreeModelListener) {
        treeModelListeners.remove(listener)
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
 * @see TemplateJTreeModel
 */
class StateNode(val state: State) {
    /**
     * `true` if and only if this node can have children.
     */
    val canHaveChildren: Boolean
        get() = state is TemplateList || state is Template

    /**
     * The child nodes contained in this node.
     *
     * The getter and setter throw an exception if this node cannot contain children. An exception is thrown if and
     * only if [canHaveChildren] is `false`.
     */
    var children: List<StateNode>
        get() =
            when (state) {
                is TemplateList -> state.templates.map { StateNode(it) }
                is Template -> state.schemes.map { StateNode(it) }
                else -> error(Bundle("template_list.error.unknown_parent_type", state.javaClass.canonicalName))
            }
        set(value) {
            when (state) {
                is TemplateList -> state.templates = value.map { it.state as Template }
                is Template -> state.schemes = value.map { it.state as Scheme }
                else -> error(Bundle("template_list.error.unknown_parent_type", state.javaClass.canonicalName))
            }
        }

    /**
     * The recursive children of this node in depth-first order, excluding itself.
     *
     * Returns an empty list if [canHaveChildren] is `false`.
     */
    val recursiveChildren: List<StateNode>
        get() =
            if (!canHaveChildren) emptyList()
            else children.flatMap { listOf(it) + it.recursiveChildren }


    /**
     * Returns `true` if and only if [node] is contained in the tree rooted at this node.
     *
     * @param node the node to find
     * @return `true` if and only if [node] is contained in the tree rooted at this node
     */
    fun contains(node: StateNode): Boolean =
        this == node || canHaveChildren && children.any { it.contains(node) }


    /**
     * Returns `true` if and only if [other]'s UUID is the same as [state]'s UUID.
     *
     * @param other the object to compare against
     * @return `true` if and only if [other]'s UUID is the same as [state]'s UUID
     */
    override fun equals(other: Any?) = other is StateNode && this.state.uuid == other.state.uuid

    /**
     * Returns the hash code of the [state]'s UUID.
     *
     * @return the hash code of the [state]'s UUID
     */
    override fun hashCode() = state.uuid.hashCode()
}
