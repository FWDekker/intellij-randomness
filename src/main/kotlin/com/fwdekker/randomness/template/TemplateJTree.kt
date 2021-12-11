package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.datetime.DateTimeScheme
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
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
import javax.swing.event.TreeWillExpandListener
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
                if (it == model.root) null
                else it
            }

    /**
     * The currently selected scheme, or `null` if no scheme is currently selected.
     *
     * Setting `null` will select the first template in the tree, or the root if there are no templates.
     */
    var selectedScheme: Scheme?
        get() = selectedNodeNotRoot?.state as? Scheme
        set(value) {
            val node = value?.let { StateNode(it) }

            setSelectionRow(
                myModel.nodeToRow(
                    if (node != null && myModel.root.contains(node)) node
                    else myModel.root.children.firstOrNull() ?: myModel.root
                )
            )
        }

    /**
     * UUIDs of templates that have explicitly been collapsed by the user.
     */
    private val explicitlyCollapsed = mutableSetOf<String>()

    /**
     * All currently visible nodes in depth-first order.
     */
    private val visibleNodes: List<StateNode>
        get() {
            val hidden = explicitlyCollapsed
                .mapNotNull { myModel.list.getTemplateByUuid(it) }
                .flatMap { it.schemes }
                .map { StateNode(it) }
            return myModel.root.recursiveChildren.minus(hidden)
        }


    init {
        TreeSpeedSearch(this) { path -> path.path.filterIsInstance<Scheme>().joinToString { it.name } }

        emptyText.text = Bundle("template_list.ui.empty")
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

        myModel.rowToNode = { visibleNodes.getOrNull(it) }
        myModel.nodeToRow = { visibleNodes.indexOf(it) }
        myModel.expandAndSelect = {
            expandNode(it)
            selectedScheme = it.state as Scheme
        }

        myModel.list.templates.forEach { expandPath(myModel.getPathToRoot(StateNode(it))) }
        selectedScheme = null
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
            .addExtraAction(AddButton())
            .addExtraAction(RemoveButton())
            .addExtraAction(CopyButton())
            .addExtraAction(UpButton())
            .addExtraAction(DownButton())
            .addExtraAction(ResetButton())
            .setButtonComparator(
                Bundle("shared.action.add"),
                Bundle("shared.action.edit"),
                Bundle("shared.action.remove"),
                Bundle("shared.action.copy"),
                Bundle("shared.action.up"),
                Bundle("shared.action.down"),
                Bundle("shared.action.reset")
            )
            .setForcedDnD()
            .createPanel()


    /**
     * Reloads the list of templates in [myModel].
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
     * Adds [newScheme] at an appropriate location in the tree based on the currently selected node.
     *
     * @param newScheme the scheme to add. Must be an instance of [Template] if [selectedNodeNotRoot] is `null`.
     */
    fun addScheme(newScheme: Scheme) {
        val newNode = StateNode(newScheme)
        val selectedNode = selectedNodeNotRoot
        if (newScheme is Template) newScheme.name = findUniqueNameFor(newScheme)

        if (selectedNode == null) {
            require(newScheme is Template) { Bundle("template_list.error.add_template_to_non_root") }
            myModel.insertNode(myModel.root, newNode)
        } else if (selectedNode.state is Template) {
            if (newScheme is Template)
                myModel.insertNodeAfter(myModel.root, newNode, selectedNode)
            else
                myModel.insertNode(selectedNode, newNode)
        } else {
            if (newScheme is Template)
                myModel.insertNodeAfter(myModel.root, newNode, myModel.getParentOf(selectedNode)!!)
            else
                myModel.insertNodeAfter(myModel.getParentOf(selectedNode)!!, newNode, selectedNode)
        }

        expandNode(newNode)
        selectedScheme = newScheme
    }

    /**
     * Removes [scheme] from the tree, and selects an appropriate other scheme.
     *
     * Throws an exception if [scheme] is not in this tree.
     *
     * @param scheme the scheme to remove
     */
    fun removeScheme(scheme: Scheme) {
        val node = StateNode(scheme)
        val parent = myModel.getParentOf(node)!!
        val oldIndex = myModel.getIndexOfChild(parent, node)

        myModel.removeNode(node)

        selectionPath =
            myModel.getPathToRoot(
                if (myModel.isLeaf(parent)) parent
                else myModel.getChild(parent, min(oldIndex, myModel.getChildCount(parent) - 1))
            )
    }

    /**
     * Moves [scheme] up or down by one position.
     *
     * If a non-[Template] is moved up or down outside its parent's boundaries, it is moved to a different parent.
     *
     * @param scheme the scheme to move up or down
     * @param moveDown `true` if the scheme should be moved down, `false` if the scheme should be moved up
     */
    fun moveSchemeByOnePosition(scheme: Scheme, moveDown: Boolean) {
        val node = StateNode(scheme)
        val index = myModel.nodeToRow(node)

        myModel.exchangeRows(index, getMoveTargetIndex(scheme, moveDown))
        // `exchangeRows` expands and selects the moved node
    }

    /**
     * Returns `true` if and only if [moveSchemeByOnePosition] can be invoked with these parameters.
     *
     * Throws an exception if the scheme is not in this tree.
     *
     * @param scheme the scheme to move up or down
     * @param moveDown `true` if the scheme should be moved down, `false` if the scheme should be moved up
     * @return `true` if and only if [moveSchemeByOnePosition] can be invoked with these parameters
     */
    fun canMoveSchemeByOnePosition(scheme: Scheme, moveDown: Boolean) =
        myModel.canExchangeRows(myModel.nodeToRow(StateNode(scheme)), getMoveTargetIndex(scheme, moveDown))

    /**
     * Returns the index to which [scheme] is moved when [moveSchemeByOnePosition] is invoked, or an out-of-range index
     * if the scheme cannot be moved.
     *
     * @param scheme the scheme to move up or down
     * @param moveDown `true` if the scheme should be moved down, `false` if the scheme should be moved up
     * @return the index to which [scheme] is moved when [moveSchemeByOnePosition] is invoked, or an out-of-range index
     * if the scheme cannot be moved
     */
    private fun getMoveTargetIndex(scheme: Scheme, moveDown: Boolean): Int {
        val node = StateNode(scheme)

        if (scheme !is Template)
            return myModel.nodeToRow(node) + if (moveDown) 1 else -1

        val parent = myModel.getParentOf(node)!!
        val indexInParent = myModel.getIndexOfChild(parent, node)
        val uncleIndexInParent = indexInParent + if (moveDown) 1 else -1

        return if (uncleIndexInParent !in 0 until myModel.getChildCount(parent)) -1
        else myModel.nodeToRow(myModel.getChild(parent, uncleIndexInParent))
    }


    /**
     * Expands the path to [node] even if it is a leaf node.
     *
     * @param node the node to expand
     */
    private fun expandNode(node: StateNode?) {
        if (node == null) return

        val path = myModel.getPathToRoot(node)
        expandPath(path.parentPath)
        expandPath(path)
    }

    /**
     * Returns `true` if and only if [scheme] has been modified with respect to [originalState].
     *
     * @param scheme the scheme to check for modification
     * @return `true` if and only if [scheme] has been modified with respect to [originalState]
     */
    private fun isModified(scheme: Scheme) =
        originalState.templateList.getSchemeByUuid(scheme.uuid) != scheme ||
            scheme is WordScheme && originalState.dictionarySettings != currentState.dictionarySettings

    /**
     * Finds a good, unique name for [template] so that it can be inserted into this list without conflict.
     *
     * If the name is already unique, that name is returned. Otherwise, the name is appended with the first number `i`
     * such that `$name ($i)` is unique. If the template's current name already ends with a number in parentheses, that
     * number is taken as the starting number.
     *
     * @param template the template to find a good name for
     * @return a unique name for [template]
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
        /**
         * Renders the [Scheme] in [value].
         *
         * @param tree ignored
         * @param value the node to render
         * @param selected ignored
         * @param expanded ignored
         * @param leaf ignored
         * @param row ignored
         * @param hasFocus ignored
         */
        override fun customizeCellRenderer(
            tree: JTree,
            value: Any,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ) {
            val scheme = (value as StateNode).state as? Scheme
            if (scheme == null) {
                append(Bundle("template.name.unknown"))
                return
            }

            icon = scheme.icon

            if (scheme is Template) {
                val index = myModel.list.templates.indexOf(scheme) + 1
                if (index <= INDEXED_TEMPLATE_COUNT)
                    append("${index % INDEXED_TEMPLATE_COUNT} ", SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES, false)
            }

            append(
                scheme.name.ifBlank { Bundle("template.name.empty") },
                when {
                    scheme.doValidate() != null -> SimpleTextAttributes.ERROR_ATTRIBUTES
                    isModified(scheme) -> SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES
                    else -> SimpleTextAttributes.REGULAR_ATTRIBUTES
                }
            )

            if (scheme is StringScheme && scheme.doValidate() == null) {
                val randomString = scheme.generateStrings(1)[0]
                if (randomString == scheme.pattern)
                    append("  $randomString", SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES)
            }
        }
    }


    /**
     * Displays a popup to add a scheme, or immediately adds a template if nothing is currently selected.
     */
    private inner class AddButton : AnActionButton(Bundle("shared.action.add"), AllIcons.General.Add) {
        /**
         * Displays a popup to add a scheme, or immediately adds a template if nothing is currently selected.
         *
         * @param event ignored
         */
        override fun actionPerformed(event: AnActionEvent) {
            if (selectedNodeNotRoot == null) {
                addScheme(AVAILABLE_ADD_SCHEMES[0])
                return
            }

            JBPopupFactory.getInstance()
                .createListPopup(AddSchemePopupStep())
                .show(preferredPopupPoint ?: return)
        }

        /**
         * Returns the shortcut for this action.
         *
         * @return the shortcut for this action
         */
        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.ADD)

        /**
         * Updates the presentation of the button.
         *
         * @param event carries contextual information
         */
        override fun updateButton(event: AnActionEvent) {
            super.updateButton(event)

            event.presentation.icon =
                if (selectedNodeNotRoot == null) AllIcons.General.Add
                // TODO: Replace with `LayeredIcon.ADD_WITH_DROPDOWN` starting at versions >= 2021.2
                else LayeredIcon(AllIcons.General.Add, AllIcons.General.Dropdown)
        }


        /**
         * The [Scheme]-related entries in [AddButton].
         */
        private inner class AddSchemePopupStep : BaseListPopupStep<Scheme>(null, AVAILABLE_ADD_SCHEMES) {
            /**
             * Returns [value]'s icon.
             *
             * @param value the value to return the icon of
             * @return [value]'s icon
             */
            override fun getIconFor(value: Scheme?) = value?.icon

            /**
             * Returns [value]'s name.
             *
             * @param value the value to return the name of
             * @return [value]'s name
             */
            override fun getTextFor(value: Scheme?) = value?.name ?: Bundle("misc.default_scheme_name")

            /**
             * Inserts [value] into the tree.
             *
             * @param value the value to insert
             * @param finalChoice ignored
             * @return `null`
             */
            override fun onChosen(value: Scheme?, finalChoice: Boolean): PopupStep<*>? {
                if (value != null) addScheme(value.deepCopy().also { it.setSettingsState(currentState) })

                return null
            }

            /**
             * Returns `true`.
             *
             * @return `true`
             */
            override fun isSpeedSearchEnabled() = true

            /**
             * Returns a separator if [value] should be preceded by a separator, or `null` otherwise.
             *
             * @param value the value to determine by whether to return a separator
             * @return a separator if [value] should be preceded by a separator, or `null` otherwise
             */
            override fun getSeparatorAbove(value: Scheme?) =
                if (value == AVAILABLE_ADD_SCHEMES[1]) ListSeparator()
                else null

            /**
             * Returns the index of the entry to select by default.
             *
             * @return the index of the entry to select by default
             */
            override fun getDefaultOptionIndex() = 0
        }
    }

    /**
     * Removes the selected scheme from the tree.
     */
    private inner class RemoveButton : AnActionButton(Bundle("shared.action.remove"), AllIcons.General.Remove) {
        /**
         * Removes the selected scheme from the tree.
         *
         * @param event ignored
         */
        override fun actionPerformed(event: AnActionEvent) = removeScheme(selectedScheme!!)

        /**
         * Returns `true` if and only if this action is enabled.
         *
         * @return `true` if and only if this action is enabled
         */
        override fun isEnabled() = selectedNodeNotRoot != null

        /**
         * Returns the shortcut for this action.
         *
         * @return the shortcut for this action
         */
        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.REMOVE)
    }

    /**
     * Copies the selected scheme in the tree.
     */
    private inner class CopyButton : AnActionButton(Bundle("shared.action.copy"), AllIcons.Actions.Copy) {
        /**
         * Copies the selected scheme in the tree.
         *
         * @param event ignored
         */
        override fun actionPerformed(event: AnActionEvent) {
            val copy = selectedScheme!!.deepCopy()
            copy.setSettingsState(currentState)

            addScheme(copy)
        }

        /**
         * Returns `true` if and only if this action is enabled.
         *
         * @return `true` if and only if this action is enabled
         */
        override fun isEnabled() = selectedNodeNotRoot != null
    }

    /**
     * Moves the selected scheme up by one position in the tree.
     */
    private inner class UpButton : AnActionButton(Bundle("shared.action.up"), AllIcons.Actions.MoveUp) {
        /**
         * Moves the selected scheme up by one position in the tree.
         *
         * @param event ignored
         */
        override fun actionPerformed(event: AnActionEvent) = moveSchemeByOnePosition(selectedScheme!!, moveDown = false)

        /**
         * Returns `true` if and only if this action is enabled.
         *
         * @return `true` if and only if this action is enabled
         */
        override fun isEnabled() = selectedScheme?.let { canMoveSchemeByOnePosition(it, moveDown = false) } ?: false

        /**
         * Returns the shortcut for this action.
         *
         * @return the shortcut for this action
         */
        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.UP)
    }

    /**
     * Moves the selected scheme down by one position in the tree.
     */
    private inner class DownButton : AnActionButton(Bundle("shared.action.down"), AllIcons.Actions.MoveDown) {
        /**
         * Moves the selected scheme down by one position in the tree.
         *
         * @param event ignored
         */
        override fun actionPerformed(event: AnActionEvent) = moveSchemeByOnePosition(selectedScheme!!, moveDown = true)

        /**
         * Returns `true` if and only if this action is enabled.
         *
         * @return `true` if and only if this action is enabled
         */
        override fun isEnabled() = selectedScheme?.let { canMoveSchemeByOnePosition(it, moveDown = true) } ?: false

        /**
         * Returns the shortcut for this action.
         *
         * @return the shortcut for this action
         */
        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.DOWN)
    }

    /**
     * Resets the selected scheme to its original state, or removes it if it has no original state.
     */
    private inner class ResetButton : AnActionButton(Bundle("shared.action.reset"), AllIcons.General.Reset) {
        /**
         * Resets the selected scheme to its original state, or removes it if it has no original state.
         *
         * @param event ignored
         */
        override fun actionPerformed(event: AnActionEvent) {
            val toReset = selectedScheme!!
            val toResetFrom = originalState.templateList.getSchemeByUuid(toReset.uuid)
            if (toResetFrom == null) {
                removeScheme(toReset)
                return
            }

            toReset.copyFrom(toResetFrom)
            toReset.setSettingsState(currentState)

            myModel.fireNodeChanged(StateNode(toReset))
            myModel.fireNodeStructureChanged(StateNode(toReset))

            clearSelection()
            myModel.expandAndSelect(StateNode(toReset))
        }

        /**
         * Returns `true` if and only if this action is enabled.
         *
         * @return `true` if and only if this action is enabled
         */
        override fun isEnabled() = selectedScheme?.let { isModified(it) } ?: false
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * Returns the list of schemes that the user can add from the add action.
         */
        val AVAILABLE_ADD_SCHEMES: List<Scheme>
            get() = listOf(
                Template(Bundle("template.title"), emptyList()),
                IntegerScheme(),
                DecimalScheme(),
                StringScheme(),
                WordScheme(),
                UuidScheme(),
                DateTimeScheme(),
                TemplateReference()
            )

        /**
         * Number of [Template]s that should be rendered with the index in front.
         */
        const val INDEXED_TEMPLATE_COUNT = 10
    }
}
