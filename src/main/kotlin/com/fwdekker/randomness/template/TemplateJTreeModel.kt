package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.State
import com.fwdekker.randomness.setAll
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
 * If the list is modified externally, this model should be notified using a `fire` method (e.g. [fireNodeChanged]).
 * Except for the `fire` methods, the behavior of this model is undefined while uninformed of external changes.
 *
 * @param list the list to be modeled
 */
@Suppress("detekt:TooManyFunctions") // Normal for Swing implementations
class TemplateJTreeModel(list: TemplateList = TemplateList(mutableListOf())) : TreeModel, EditableModel {
    /**
     * The listeners that are informed when the state of the tree changes.
     */
    private val treeModelListeners = mutableListOf<TreeModelListener>()

    /**
     * The root of the tree, containing the original [TemplateList].
     */
    private val root = StateNode(list)

    /**
     * The list of all [StateNode]s in this tree.
     */
    private val nodes get() = listOf(root) + root.descendants


    /**
     * Returns [root].
     */
    override fun getRoot() = root


    /**
     * Not implemented because this method is used only if this is a model for a table.
     *
     * @throws UnsupportedOperationException always
     */
    @Throws(UnsupportedOperationException::class)
    override fun addRow() = throw UnsupportedOperationException()

    /**
     * Not implemented because this method is used only if this is a model for a table.
     *
     * @throws UnsupportedOperationException always
     */
    @Throws(UnsupportedOperationException::class)
    override fun removeRow(index: Int) = throw UnsupportedOperationException()


    /**
     * Inserts [child] as the [index]th child of [parent].
     *
     * Throws an exception if [index] is out of bounds in [parent] (of course allowing [index] to equal the
     * [getChildCount] of [parent]).
     */
    fun insertNode(parent: StateNode, child: StateNode, index: Int = getChildCount(parent)) {
        require(parent in nodes) { Bundle("template_list.error.node_not_in_tree") }
        require(parent.canHaveChild(child)) { Bundle("template_list.error.wrong_child_type") }

        parent.children = parent.children.toMutableList().also { it.add(index, child) }

        if (parent == root && parent.children.count() == 1)
            fireNodeStructureChanged(root)
        else
            fireNodeInserted(child, parent, index)
    }

    /**
     * Inserts [child] as a child into [parent] right after [after].
     *
     * Throws an exception if [after] is not a child of [parent].
     */
    fun insertNodeAfter(parent: StateNode, after: StateNode, child: StateNode) {
        require(after in parent.children) { Bundle("template_list.error.wrong_parent") }

        insertNode(parent, child, parent.children.indexOf(after) + 1)
    }

    /**
     * Removes [node] from this model.
     *
     * Throws an exception if [node] is not contained in this model, or if [node] is [getRoot].
     */
    fun removeNode(node: StateNode) {
        require(node in nodes) { Bundle("template_list.error.node_not_in_tree") }
        require(node != root) { Bundle("template_list.error.cannot_remove_root") }

        val parent = getParentOf(node)!!
        val oldIndex = parent.children.indexOf(node)

        parent.children = parent.children.toMutableList().also { it.remove(node) }
        fireNodeRemoved(node, parent, oldIndex)
    }

    /**
     * Exchanges two consecutive rows, moving the node at row [oldIndex] to row [newIndex].
     *
     * If [oldIndex] and [newIndex] both refer to a template or both refer to a scheme, then the node at [oldIndex] will
     * be inserted so that it has the same index in its parent as the node at [newIndex] had before invoking this
     * method. Otherwise, if [oldIndex] refers to a non-template scheme and [newIndex] refers to a template, then the
     * non-template scheme is moved into the template at [newIndex]; specifically, if [oldIndex] is less than [newIndex]
     * then the non-template scheme becomes the template's first child, otherwise it becomes the template's last child.
     */
    override fun exchangeRows(oldIndex: Int, newIndex: Int) {
        require(canExchangeRows(oldIndex, newIndex)) {
            Bundle("template_list.error.cannot_swap_rows", oldIndex, newIndex)
        }

        val nodeToMove = root.descendants[oldIndex]
        val targetNode = root.descendants[newIndex]

        val targetNodeParent: StateNode
        val targetIndexInParent: Int
        if (nodeToMove.state !is Template && targetNode.state is Template) {
            if (newIndex < oldIndex) {
                targetNodeParent = root.children[root.children.indexOf(targetNode) - 1]
                targetIndexInParent = targetNodeParent.children.count()
            } else {
                targetNodeParent = targetNode
                targetIndexInParent = 0
            }
        } else {
            targetNodeParent = getParentOf(targetNode)!!
            targetIndexInParent = targetNodeParent.children.indexOf(targetNode)
        }

        removeNode(nodeToMove)
        insertNode(targetNodeParent, nodeToMove, targetIndexInParent)
    }

    /**
     * Returns `true` if and only if the node at row [oldIndex] can be moved to row [newIndex].
     */
    override fun canExchangeRows(oldIndex: Int, newIndex: Int): Boolean {
        val oldNode = root.descendants.getOrNull(oldIndex)
        val newNode = root.descendants.getOrNull(newIndex)

        return oldNode != null && newNode != null &&
            if (oldNode.state is Template) newNode.state is Template
            else newIndex != 0
    }


    /**
     * Returns `true` if and only if [node] does not have children.
     *
     * Throws an exception if [node] is not a [StateNode] or if [node] is not contained in this model.
     */
    override fun isLeaf(node: Any): Boolean {
        require(node is StateNode) {
            Bundle("template_list.error.unknown_node_type", "node", node.javaClass.canonicalName)
        }
        require(node in nodes) { Bundle("template_list.error.node_not_in_tree") }

        return !node.canHaveChildren || node.children.isEmpty()
    }

    /**
     * Returns the [index]th child of [parent].
     *
     * Throws an exception if [parent] is not a [StateNode], if [parent] is not contained in this model, if [parent]
     * cannot have children, or if [parent] has no [index]th child.
     */
    override fun getChild(parent: Any, index: Int): StateNode {
        require(parent is StateNode) {
            Bundle("template_list.error.unknown_node_type", "parent", parent.javaClass.canonicalName)
        }
        require(parent in nodes) { Bundle("template_list.error.node_not_in_tree") }

        return parent.children[index]
    }

    /**
     * Returns the number of children of [parent].
     *
     * Throws an exception if [parent] is not a [StateNode] or if [parent] is not contained in this model.
     */
    override fun getChildCount(parent: Any): Int {
        require(parent is StateNode) {
            Bundle("template_list.error.unknown_node_type", "parent", parent.javaClass.canonicalName)
        }
        require(parent in nodes) { Bundle("template_list.error.node_not_in_tree") }

        return if (!parent.canHaveChildren) 0
        else parent.children.size
    }

    /**
     * Returns the relative index of [child] in [parent], or `-1` if (1) either [parent] or [child] is `null`,
     * (2) either [parent] or [child] is not a [StateNode], (3) either [parent] or [child] is not contained in this
     * model, or (4) if [child] is not a child of [parent].
     */
    override fun getIndexOfChild(parent: Any?, child: Any?): Int =
        if (parent !is StateNode || child !is StateNode) -1
        else parent.children.indexOf(child)

    /**
     * Returns the parent of [node], or `null` if [node] has no parent.
     *
     * Throws an exception if [node] is not contained in this model.
     */
    fun getParentOf(node: StateNode): StateNode? {
        require(node in nodes) { Bundle("template_list.error.node_not_in_tree") }

        return when (node.state) {
            is TemplateList -> null
            is Template -> root
            else -> root.children.first { node in it.children }
        }
    }

    /**
     * Returns the path from [root] to [node].
     *
     * Throws an exception if [node] is not contained in this model.
     */
    fun getPathToRoot(node: StateNode): TreePath {
        require(node in nodes) { Bundle("template_list.error.node_not_in_tree") }

        return when (node.state) {
            is TemplateList -> TreePath(arrayOf(node))
            is Template -> TreePath(arrayOf(root, node))
            else -> TreePath(arrayOf(root, getParentOf(node)!!, node))
        }
    }


    /**
     * Informs listeners that [node] has been changed.
     *
     * This method is applicable if [node]'s internal state has changed and the way it is displayed should be updated.
     * However, this method is not applicable if the entire node has been replaced with a different instance or if the
     * children of [node] have been changed. In those two latter scenarios, use [fireNodeStructureChanged].
     *
     * Does nothing if [node] is `null`.
     */
    fun fireNodeChanged(node: StateNode?) {
        if (node == null) return

        val event =
            if (node == root) {
                TreeModelEvent(this, getPathToRoot(node))
            } else {
                val parent = getParentOf(node)!!
                TreeModelEvent(this, getPathToRoot(parent), intArrayOf(parent.children.indexOf(node)), arrayOf(node))
            }

        treeModelListeners.forEach { it.treeNodesChanged(event) }
    }

    /**
     * Informs listeners that [node] has been inserted into this model as the [index]th child of [parent].
     *
     * Does nothing if [node] is `null`.
     */
    fun fireNodeInserted(node: StateNode?, parent: StateNode, index: Int) {
        if (node == null) return
        require(node.state !is TemplateList) { Bundle("template_list.error.cannot_insert_root") }

        treeModelListeners.forEach {
            it.treeNodesInserted(TreeModelEvent(this, getPathToRoot(parent), intArrayOf(index), arrayOf(node)))
        }
    }

    /**
     * Informs listeners that [child] has been removed from this model, but was formerly the [index]th child of
     * [parent].
     *
     * If [child] has children nodes itself, then you must **not** invoke this method on those children.
     *
     * Does nothing if [child] is `null`.
     */
    fun fireNodeRemoved(child: StateNode?, parent: StateNode, index: Int) {
        if (child == null) return
        require(child.state !is TemplateList) { Bundle("template_list.error.cannot_remove_root") }

        treeModelListeners.forEach {
            it.treeNodesRemoved(TreeModelEvent(this, getPathToRoot(parent), intArrayOf(index), arrayOf(child)))
        }
    }

    /**
     * Informs listeners that [node]'s structure has been changed.
     */
    fun fireNodeStructureChanged(node: StateNode = root) {
        treeModelListeners.forEach { it.treeStructureChanged(TreeModelEvent(this, getPathToRoot(node))) }
    }


    /**
     * Not implemented because this model does not contain an editor component.
     *
     * @throws UnsupportedOperationException always
     */
    @Throws(UnsupportedOperationException::class)
    override fun valueForPathChanged(path: TreePath, newValue: Any) = throw UnsupportedOperationException()

    /**
     * Adds [listener] as a listener.
     */
    override fun addTreeModelListener(listener: TreeModelListener) {
        treeModelListeners.add(listener)
    }

    /**
     * Removes [listener] as a listener.
     */
    override fun removeTreeModelListener(listener: TreeModelListener) {
        treeModelListeners.remove(listener)
    }
}

/**
 * Represents a [State] in a [TemplateJTreeModel].
 *
 * By default, two [State]s are equal if their fields are equal, regardless of their UUID. However, this is problematic
 * for [JTree]s because a [JTree] cannot contain multiple objects that equal each other. The [StateNode] class thus
 * "replaces" the equals of the contained [State] within the context of the [JTree].
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
     * An exception is thrown if this node cannot have children.
     */
    var children: List<StateNode>
        get() {
            return when (state) {
                is TemplateList -> state.templates.map { StateNode(it) }
                is Template -> state.schemes.map { StateNode(it) }
                else -> error(Bundle("template_list.error.infertile_parent"))
            }
        }
        set(value) {
            when (state) {
                is TemplateList -> state.templates.setAll(value.map { it.state as Template })
                is Template -> state.schemes.setAll(value.map { it.state as Scheme })
                else -> error(Bundle("template_list.error.infertile_parent"))
            }
        }

    /**
     * The (recursive) descendants of this node in depth-first order (excluding `this` node itself), or an empty list
     * if this node cannot have children.
     */
    val descendants: List<StateNode>
        get() {
            return when (state) {
                is TemplateList -> children.flatMap { listOf(it) + it.children }
                is Template -> children
                else -> emptyList()
            }
        }


    /**
     * Returns `true` if and only if this node can have [child] as a child.
     */
    fun canHaveChild(child: StateNode) =
        when (state) {
            is TemplateList -> child.state is Template
            is Template -> child.state is Scheme && child.state !is Template
            else -> false
        }


    /**
     * Returns `true` if and only if the UUIDs of `this`' [state] and [other]'s [state] are equal.
     */
    override fun equals(other: Any?) = other is StateNode && this.state.uuid == other.state.uuid

    /**
     * Returns the hash code of the [state]'s UUID.
     */
    override fun hashCode() = state.uuid.hashCode()
}
