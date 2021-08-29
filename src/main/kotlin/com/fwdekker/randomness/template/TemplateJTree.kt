package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.State
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.treeStructure.Tree
import java.util.Collections
import java.util.Enumeration
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel
import kotlin.math.min


/**
 * A tree containing templates and schemes.
 *
 * The tree is initially empty. Templates can be loaded into the tree using [loadList]. See its documentation for more
 * information.
 *
 * @property isModified Returns true if and only if the given scheme has been modified.
 */
class TemplateJTree(
    private val isModified: (Scheme) -> Boolean
) : Tree(DefaultTreeModel(TemplateListTreeNode(TemplateList(emptyList())))) {
    /**
     * The tree's model.
     *
     * This field cannot be named `model` because this causes an NPE during initialization. This field cannot be named
     * `treeModel` because this name is already taken and cannot be overridden.
     */
    val myModel: DefaultTreeModel
        get() = super.getModel() as DefaultTreeModel

    /**
     * The undisplayed root element of the tree.
     */
    internal val root: TemplateListTreeNode
        get() = model.root as TemplateListTreeNode

    /**
     * The currently selected node, or `null` if no node is selected, or `null` if the root is selected.
     */
    val selectedNode: StateTreeNode<*>?
        get() = (lastSelectedPathComponent as? StateTreeNode<*>)
            ?.let {
                if (it == model.root) null
                else it
            }


    init {
        TreeSpeedSearch(this) { path ->
            path.path.map { (it as StateTreeNode<*>).state }.filterIsInstance<Scheme>().joinToString { it.name }
        }

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
     * @param list the state to load into the tree
     */
    fun loadList(list: TemplateList) {
        myModel.setRoot(TemplateListTreeNode(list))
        myModel.reload()

        root.children().toList().forEach { expandPath(getPathToRoot(it)) }
        root.firstLeaf()?.also { selectionPath = getPathToRoot(it) }
    }


    /**
     * Adds the given scheme at an appropriate location in the tree based on the currently selected node.
     *
     * @param newScheme the scheme to add. Must be an instance of [Template] if [selectedNode] is null.
     */
    fun addScheme(newScheme: Scheme) {
        val selectedNode = selectedNode
        if (newScheme is Template) newScheme.name = findUniqueNameFor(newScheme)

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

        myModel.insertNodeInto(childNode, parent, index)
        selectionPath = getPathToRoot(childNode)
        expandPath(selectionPath)
    }

    /**
     * Removes the given node from the tree, and selects an appropriate other node.
     *
     * @param node the node to remove
     */
    fun removeNode(node: StateTreeNode<*>) {
        val parent = node.parent as StateTreeNode<*>
        val oldIndex = parent.getIndex(node)

        myModel.removeNodeFromParent(node)
        if (parent.isLeaf && parent == myModel.root)
            clearSelection()
        else
            selectionPath =
                getPathToRoot(
                    if (parent.isLeaf) parent
                    else parent.getChildAt(min(oldIndex, parent.childCount - 1))
                )
    }

    /**
     * Moves the given node down the given number of positions.
     *
     * The node will be moved down relative to its siblings. The parent does not change.
     *
     * @param node the node to move down
     * @param positions the number of positions to move the node down by within its parent; can be negative
     */
    fun moveNodeDownBy(node: StateTreeNode<*>, positions: Int) {
        if (positions == 0) return

        val parent = node.parent as StateTreeNode<*>
        val oldIndex = parent.getIndex(node)

        myModel.removeNodeFromParent(node)
        myModel.insertNodeInto(node, parent, oldIndex + positions)
        selectionPath = getPathToRoot(node)
        expandPath(selectionPath)
    }

    /**
     * Selects the scheme with the given UUID, if it exists; otherwise, nothing happens.
     *
     * @param targetUuid the UUID of the scheme to select, or `null` if nothing should be done
     * @return true if and only if the scheme was found and selected
     */
    fun selectScheme(targetUuid: String?) =
        root.recursiveChildren().toList().firstOrNull { it.state.uuid == targetUuid }
            ?.also { selectionPath = getPathToRoot(it) } != null


    /**
     * Returns the path from the given node to the root path, including both ends.
     *
     * @param treeNode the node to return the path to root from
     * @return the path from the given node to the root path, including both ends
     */
    private fun getPathToRoot(treeNode: TreeNode) = TreePath(myModel.getPathToRoot(treeNode))

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
        val templateNames = root.children().toList().map { it.state.name }
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
            val scheme = (value as StateTreeNode<*>).state as? Scheme
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
     * A mutable tree node that contains a state of type [S].
     *
     * The node is mutable in the sense that its [state] is mutable, but the reference to the state is not.
     *
     * `StateTreeNode`s are peculiar in that its parent and all its children must also be `StateTreeNode`s.
     *
     * @param S the type of state contained in the mutable tree node
     */
    sealed class StateTreeNode<S : State> : MutableTreeNode {
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
         * @param newParent the new parent of this node, or `null` if the parent should be removed; must be an instance
         * of [StateTreeNode]
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
         * Returns all nodes recursively contained in this node in depth-first order.
         *
         * @return all nodes recursively contained in this node in depth-first order
         */
        fun recursiveChildren(): Enumeration<out StateTreeNode<*>> {
            val children = children().toList()
            return Collections.enumeration(children.flatMap { listOf(it) + it.recursiveChildren().toList() })
        }

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
    sealed class StateTreeListNode<S : State, T : State> : StateTreeNode<S>() {
        /**
         * The entries of [T] contained in [state].
         *
         * This field connects this node's children to the backing field in [state], so that this field can be modified
         * to modify [state] whenever the children of this node are changed.
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
    class TemplateListTreeNode(override val state: TemplateList) : StateTreeListNode<TemplateList, Template>() {
        override var entries: List<Template>
            get() = state.templates
            set(value) {
                state.templates = value.toList()
            }


        override fun createChildNodeFor(child: Template) = TemplateTreeNode(child)


        /**
         * Returns the depth-first node without children in the tree rooted at this node that is not this node, or
         * `null` if no such node exists.
         *
         * @return the depth-first node without children in the tree rooted at this node that is not this node, or
         * `null` if no such node exists
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
    class TemplateTreeNode(override val state: Template) : StateTreeListNode<Template, Scheme>() {
        override var entries: List<Scheme>
            get() = state.schemes
            set(value) {
                state.schemes = value.toList()
            }


        override fun createChildNodeFor(child: Scheme) = SchemeTreeNode(child)
    }

    /**
     * A [StateTreeNode] for [Scheme]s.
     *
     * @property state the scheme represented by this node
     */
    class SchemeTreeNode(override val state: Scheme) : StateTreeNode<Scheme>() {
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
        override fun insert(child: MutableTreeNode?, index: Int) =
            throw UnsupportedOperationException(NO_CHILDREN_MESSAGE)

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
}
