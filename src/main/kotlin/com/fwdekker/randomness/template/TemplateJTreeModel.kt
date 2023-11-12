package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.State
import com.fwdekker.randomness.setAll
import com.intellij.ui.RowsDnDSupport.RefinedDropSupport
import com.intellij.ui.RowsDnDSupport.RefinedDropSupport.Position
import com.intellij.util.ui.EditableModel
import javax.swing.JComponent
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
class TemplateJTreeModel(
    list: TemplateList = TemplateList(mutableListOf()),
) : TreeModel, EditableModel, RefinedDropSupport {
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
     * Converts the row index in the view to the corresponding row index in the model.
     *
     * For example, if the model has rows `[0, 3]` with children `[1, 2]` and `[4]`, respectively, and row `0` is
     * collapsed in the view, then the view has rows `[0, 3, 4]` while the model has rows `[0, 1, 2, 3, 4]`, and this
     * method functions as the map `{0: 0, 1: 3, 2: 4}`. Inputs outside the view's valid row indices are not supported.
     *
     * This field is used only in the methods [canDrop] and [drop] in order to implement [RefinedDropSupport], which
     * assumes that the model knows view-based indices.
     *
     * @see RefinedDropSupport
     */
    var viewIndexToModelIndex: (Int) -> Int = { it }

    /**
     * A wrapper provided by the view and used by [TemplateJTreeModel.drop] to ensure the view's selection and expansion
     * state are retained.
     *
     * This wrapper is invoked in the method [drop] with a lambda that performs the dropping when invoked. By default,
     * the wrapper does nothing special, but can be overridden to perform some actions before and after dropping the
     * [StateNode].
     *
     * This field is used only in the method [drop] in order to implement [RefinedDropSupport], which assumes that the
     * model can somehow retain the view's state.
     *
     * @see RefinedDropSupport
     */
    var wrapDrop: (() -> Unit) -> Unit = { it() }


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
        require(child !in parent.children) { Bundle("template_list.error.duplicate_uuid") }

        if (parent == root) {
            val childChildren = child.children

            root.children
                .associateWith { rootChild -> rootChild.children.filter { it in childChildren } }
                .filterValues { it.isNotEmpty() }
                .forEach { (rootChild, duplicates) ->
                    rootChild.children = rootChild.children.toMutableList()
                        .map {
                            if (it in duplicates) StateNode(it.state.deepCopy(retainUuid = false))
                            else it
                        }

                    fireNodeStructureChanged(rootChild)
                }
        }
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
     * Use [moveRow] instead.
     *
     * @throws UnsupportedOperationException always
     * @see moveRow
     */
    @Throws(UnsupportedOperationException::class)
    override fun exchangeRows(oldIndex: Int, newIndex: Int) = throw UnsupportedOperationException()

    /**
     * Use [canMoveRow] instead.
     *
     * @throws UnsupportedOperationException always
     * @see canMoveRow
     */
    @Throws(UnsupportedOperationException::class)
    override fun canExchangeRows(oldIndex: Int, newIndex: Int): Boolean = throw UnsupportedOperationException()

    /**
     * Returns `true` if and only if [moveRow] can be invoked with the given parameters.
     *
     * @see moveRow
     */
    fun canMoveRow(fromIndex: Int, toIndex: Int, position: Position): Boolean {
        val descendants = root.descendants
        val templates = root.children

        val fromNode = descendants.getOrNull(fromIndex)
        val toNode = descendants.getOrNull(toIndex)

        return when {
            fromIndex == toIndex || fromNode == null || toNode == null -> false

            fromNode.state is Template ->
                when (position) {
                    Position.INTO -> false
                    Position.BELOW -> toNode == templates.last() && toNode != fromNode
                    Position.ABOVE ->
                        toNode.state is Template && toNode != templates.run { getOrNull(indexOf(fromNode) + 1) }
                }

            else -> (position == Position.INTO) xor (toNode.state !is Template)
        }
    }

    /**
     * Moves the node at row [fromIndex] near the node at row [toIndex] as specified by [position].
     *
     * If [fromIndex] refers to a [Template], then the [Template] can either be moved [Position.ABOVE] another
     * [Template], or [Position.BELOW] the very last [Scheme] of the [TemplateList].
     *
     * If [fromIndex] refers to a non-[Template] [Scheme], then the [Scheme] can either be moved [Position.INTO] a
     * [Template] to append it to that [Template], or [Position.ABOVE] or [Position.BELOW] another non-[Template]
     * [Scheme].
     *
     * @throws IllegalArgumentException if [canMoveRow] returns `false` for the given arguments
     */
    fun moveRow(fromIndex: Int, toIndex: Int, position: Position) {
        require(canMoveRow(fromIndex, toIndex, position)) {
            Bundle("template_list.error.cannot_move_row", fromIndex, position.name, toIndex)
        }

        val descendants = root.descendants
        val fromNode = descendants[fromIndex]
        val toNode = descendants[toIndex]

        removeNode(fromNode)

        if (fromNode.state is Template) {
            when (position) {
                Position.ABOVE -> insertNode(root, fromNode, root.children.indexOf(toNode))
                Position.BELOW -> insertNode(root, fromNode)
                Position.INTO -> error("Bug: 'canMoveRow' should have caught this case.")
            }
        } else {
            val toNodeParent = getParentOf(toNode)!!
            when (position) {
                Position.ABOVE -> insertNode(toNodeParent, fromNode, toNodeParent.children.indexOf(toNode))
                Position.BELOW -> insertNodeAfter(toNodeParent, toNode, fromNode)
                Position.INTO -> insertNode(toNode, fromNode)
            }
        }
    }

    /**
     * Returns `true` if and only if the node at [fromIndex] can be moved [Position.INTO] the node at [toIndex].
     *
     * @see canMoveRow
     * @see RefinedDropSupport
     */
    override fun isDropInto(component: JComponent?, fromIndex: Int, toIndex: Int) =
        canDrop(fromIndex, toIndex, Position.INTO)

    /**
     * Invokes [canMoveRow] after converting [fromIndex] and [toIndex] using [viewIndexToModelIndex].
     *
     * @see canMoveRow
     * @see RefinedDropSupport
     */
    override fun canDrop(fromIndex: Int, toIndex: Int, position: Position) =
        canMoveRow(viewIndexToModelIndex(fromIndex), viewIndexToModelIndex(toIndex), position)

    /**
     * Invokes [moveRow] after converting [fromIndex] and [toIndex] using [viewIndexToModelIndex].
     *
     * @see moveRow
     * @see RefinedDropSupport
     */
    override fun drop(fromIndex: Int, toIndex: Int, position: Position) =
        wrapDrop { moveRow(viewIndexToModelIndex(fromIndex), viewIndexToModelIndex(toIndex), position) }


    /**
     * Returns `true` if and only if [node] is contained in this model and [node] does not have children.
     *
     * Throws an exception if [node] is not a [StateNode].
     *
     * Unlike methods such as [getChild], this method does not throw an exception if [node] is not contained in this
     * model, because [com.intellij.ui.tree.ui.DefaultTreeUI] calls this method on non-nodes during drag-and-drop.
     */
    override fun isLeaf(node: Any): Boolean {
        require(node is StateNode) {
            Bundle("template_list.error.unknown_node_type", "node", node.javaClass.canonicalName)
        }

        return node in nodes && (!node.canHaveChildren || node.children.isEmpty())
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
