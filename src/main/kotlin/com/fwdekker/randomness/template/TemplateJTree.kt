package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.datetime.DateTimeScheme
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
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
 * The tree contains the templates and schemes defined in the [TemplateList] of [currentSettings] by loading that list
 * into this tree's [TemplateJTreeModel]. Furthermore, when a new [Scheme] is added or copied, it will use the
 * [currentSettings].
 *
 * This tree reads from [originalSettings] to determine whether particular [Scheme]s have been modified. Modified
 * [Scheme]s can be reset, in which case the original state is copied into that scheme in the [currentSettings].
 *
 * @property originalSettings The original settings before any modifications were made.
 * @property currentSettings The current settings which include modifications.
 */
class TemplateJTree(
    private val originalSettings: Settings,
    private var currentSettings: Settings,
) : Tree(TemplateJTreeModel(currentSettings.templateList)) {
    /**
     * The tree's model.
     *
     * This field cannot be named `model` because this causes an NPE during initialization. This field cannot be named
     * `treeModel` because this name is already taken and cannot be overridden.
     */
    val myModel: TemplateJTreeModel
        get() = super.getModel() as TemplateJTreeModel

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
     * The currently selected template, or `null` if no template is currently selected.
     *
     * If a non-template scheme is currently selected, the template parent of that scheme is returned.
     *
     * Setting `null` will select the first template in the tree, or the root if there are no templates.
     */
    var selectedTemplate: Template?
        get() {
            val node = selectedNodeNotRoot ?: return null

            return if (node.state is Template) node.state
            else myModel.getParentOf(node)!!.state as Template
        }
        set(value) {
            selectedScheme = value
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
                .toSet()
            return myModel.root.recursiveChildren.minus(hidden)
        }


    init {
        TreeSpeedSearch(this, true) { path -> path.path.filterIsInstance<Scheme>().joinToString { it.name } }

        emptyText.text = Bundle("template_list.ui.empty")
        isRootVisible = false
        showsRootHandles = true
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
     */
    fun asDecoratedPanel(): JPanel =
        ToolbarDecorator.createDecorator(this)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())
            .setScrollPaneBorder(JBUI.Borders.empty())
            .disableAddAction()
            .disableRemoveAction()
            .addExtraAction(AddButton() as AnAction)
            .addExtraAction(RemoveButton() as AnAction)
            .addExtraAction(CopyButton() as AnAction)
            .addExtraAction(UpButton() as AnAction)
            .addExtraAction(DownButton() as AnAction)
            .addExtraAction(ResetButton() as AnAction)
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
     * This is a wrapper around [TemplateJTreeModel.reload] that additionally tries to retain the current selection
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
     * @param newScheme the scheme to add; must be an instance of [Template] if [selectedNodeNotRoot] is `null`
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
     * Moves [scheme] by one position; down if [moveDown] is `true, and up otherwise.
     *
     * If a non-[Template] is moved up or down outside its parent's boundaries, it is moved to the next parent in that
     * direction.
     */
    fun moveSchemeByOnePosition(scheme: Scheme, moveDown: Boolean) {
        val node = StateNode(scheme)
        val index = myModel.nodeToRow(node)

        myModel.exchangeRows(index, getMoveTargetIndex(scheme, moveDown))
        // `exchangeRows` expands and selects the moved node
    }

    /**
     * Returns `true` if and only if [moveSchemeByOnePosition] can be invoked with these parameters.
     */
    fun canMoveSchemeByOnePosition(scheme: Scheme, moveDown: Boolean) =
        myModel.canExchangeRows(myModel.nodeToRow(StateNode(scheme)), getMoveTargetIndex(scheme, moveDown))

    /**
     * Returns the index to which [scheme] is moved (down if [moveDown] is `true`, or up otherwise) when
     * [moveSchemeByOnePosition] is invoked, or an out-of-range index if [scheme] cannot be moved in that direction.
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
     */
    private fun expandNode(node: StateNode?) {
        if (node == null) return

        val path = myModel.getPathToRoot(node)
        expandPath(path.parentPath)
        expandPath(path)
    }

    /**
     * Returns `true` if and only if [scheme] has been modified with respect to [originalSettings].
     */
    private fun isModified(scheme: Scheme) = originalSettings.templateList.getSchemeByUuid(scheme.uuid) != scheme

    /**
     * Finds a good, unique name for [template] so that it can be inserted into this list without conflict.
     *
     * If the name is already unique, that name is returned. Otherwise, the name is appended with the first number `i`
     * such that `$name ($i)` is unique. If the template's current name already ends with a number in parentheses, that
     * number is taken as the starting number.
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
         * Renders the [Scheme] in [value], ignoring other parameters.
         */
        override fun customizeCellRenderer(
            tree: JTree,
            value: Any,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean,
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

            if (scheme is StringScheme && scheme.isSimple())
                append("  ${scheme.generateStrings()[0]}", SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES)
        }
    }


    /**
     * Displays a popup to add a scheme, or immediately adds a template if nothing is currently selected.
     */
    private inner class AddButton : AnActionButton(Bundle("shared.action.add"), AllIcons.General.Add) {
        /**
         * Specifies the thread in which [update] is invoked.
         */
        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        /**
         * Updates the presentation of the button.
         *
         * @param event carries contextual information
         */
        override fun updateButton(event: AnActionEvent) {
            super.updateButton(event)

            event.presentation.icon =
                if (selectedNodeNotRoot == null) AllIcons.General.Add
                else LayeredIcon.ADD_WITH_DROPDOWN
        }

        /**
         * Returns the shortcut for this action.
         */
        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.ADD)

        /**
         * Displays a popup to add a scheme, or immediately adds a template if nothing is currently selected.
         *
         * @param event ignored
         */
        override fun actionPerformed(event: AnActionEvent) =
            JBPopupFactory.getInstance()
                .createListPopup(MainPopupStep(templatesOnly = selectedNodeNotRoot == null))
                .show(preferredPopupPoint)


        /**
         * A [PopupStep] for a list of [Scheme]s that can be inserted.
         *
         * Elements can be nested by overriding [hasSubstep] and [onChosen].
         *
         * @param schemes the schemes that can be inserted
         */
        private abstract inner class AbstractPopupStep(
            schemes: List<Scheme>,
        ) : BaseListPopupStep<Scheme>(null, schemes) {
            /**
             * Returns [value]'s icon.
             */
            override fun getIconFor(value: Scheme?) = value?.icon

            /**
             * Returns [value]'s name.
             */
            override fun getTextFor(value: Scheme?) = value?.name ?: Bundle("misc.default_scheme_name")

            /**
             * Inserts [value] into the tree.
             *
             * Subclasses may modify the behavior of this method to instead return the `PopupStep` nested under this
             * entry.
             *
             * @param value the value to insert
             * @param finalChoice ignored
             * @return `null`, or the `PopupStep` that is nested under this entry
             */
            override fun onChosen(value: Scheme?, finalChoice: Boolean): PopupStep<*>? {
                if (value != null)
                    addScheme(value.deepCopy().also { it.applyContext(currentSettings) })

                return null
            }


            /**
             * Returns `true`.
             */
            override fun isSpeedSearchEnabled() = true

            /**
             * Returns the index of the entry to select by default.
             */
            override fun getDefaultOptionIndex() = 0
        }

        /**
         * The top-level [PopupStep], which includes the default templates and various reference types.
         *
         * @property templatesOnly `true` if and only if non-[Template] schemes cannot be inserted.
         */
        private inner class MainPopupStep(private val templatesOnly: Boolean) : AbstractPopupStep(POPUP_STEP_SCHEMES) {
            override fun onChosen(value: Scheme?, finalChoice: Boolean) =
                when (value) {
                    POPUP_STEP_SCHEMES[0] -> TemplatesPopupStep()
                    POPUP_STEP_SCHEMES[POPUP_STEP_SCHEMES.size - 1] -> ReferencesPopupStep()
                    else -> super.onChosen(value, finalChoice)
                }

            /**
             * Returns `true` if and only if [value] is a [Template] or [templatesOnly] is `false`.
             */
            override fun isSelectable(value: Scheme?) = value is Template || !templatesOnly

            /**
             * Returns `true` if and only if [value] equals the [Template] or [TemplateReference] entry.
             */
            override fun hasSubstep(value: Scheme?) =
                value == POPUP_STEP_SCHEMES[0] || value == POPUP_STEP_SCHEMES[POPUP_STEP_SCHEMES.size - 1]

            /**
             * Returns a separator if [value] should be preceded by a separator, or `null` otherwise.
             */
            override fun getSeparatorAbove(value: Scheme?) =
                if (value == POPUP_STEP_SCHEMES[1]) ListSeparator()
                else null
        }

        /**
         * A [PopupStep] that shows only the default templates.
         */
        private inner class TemplatesPopupStep :
            AbstractPopupStep(listOf(Template("Empty")) + TemplateList.DEFAULT_TEMPLATES)

        /**
         * A [PopupStep] that contains a [TemplateReference] for each [Template] that can currently be referenced from
         * [selectedTemplate].
         *
         * Ineligible [Template]s are automatically filtered out.
         */
        private inner class ReferencesPopupStep : AbstractPopupStep(
            currentSettings.templates
                .filter { selectedTemplate!!.canReference(it) }
                .map { TemplateReference(it.uuid) }
        )
    }

    /**
     * Removes the selected scheme from the tree.
     */
    private inner class RemoveButton : AnActionButton(Bundle("shared.action.remove"), AllIcons.General.Remove) {
        /**
         * Specifies the thread in which [update] is invoked.
         */
        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        /**
         * Returns `true` if and only if this action is enabled.
         */
        override fun isEnabled() = selectedNodeNotRoot != null

        /**
         * Returns the shortcut for this action.
         */
        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.REMOVE)

        /**
         * Removes the selected scheme from the tree.
         *
         * @param event ignored
         */
        override fun actionPerformed(event: AnActionEvent) = removeScheme(selectedScheme!!)
    }

    /**
     * Copies the selected scheme in the tree.
     */
    private inner class CopyButton : AnActionButton(Bundle("shared.action.copy"), AllIcons.Actions.Copy) {
        /**
         * Specifies the thread in which [update] is invoked.
         */
        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        /**
         * Returns `true` if and only if this action is enabled.
         *
         * @return `true` if and only if this action is enabled
         */
        override fun isEnabled() = selectedNodeNotRoot != null

        /**
         * Copies the selected scheme in the tree.
         *
         * @param event ignored
         */
        override fun actionPerformed(event: AnActionEvent) {
            val copy = selectedScheme!!.deepCopy()
            copy.applyContext(currentSettings)

            addScheme(copy)
        }
    }

    /**
     * Moves the selected scheme up by one position in the tree.
     */
    private inner class UpButton : AnActionButton(Bundle("shared.action.up"), AllIcons.Actions.MoveUp) {
        /**
         * Specifies the thread in which [update] is invoked.
         */
        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        /**
         * Returns `true` if and only if this action is enabled.
         */
        override fun isEnabled() = selectedScheme?.let { canMoveSchemeByOnePosition(it, moveDown = false) } ?: false

        /**
         * Returns the shortcut for this action.
         */
        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.UP)

        /**
         * Moves the selected scheme up by one position in the tree.
         *
         * @param event ignored
         */
        override fun actionPerformed(event: AnActionEvent) = moveSchemeByOnePosition(selectedScheme!!, moveDown = false)
    }

    /**
     * Moves the selected scheme down by one position in the tree.
     */
    private inner class DownButton : AnActionButton(Bundle("shared.action.down"), AllIcons.Actions.MoveDown) {
        /**
         * Specifies the thread in which [update] is invoked.
         */
        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        /**
         * Returns `true` if and only if this action is enabled.
         */
        override fun isEnabled() = selectedScheme?.let { canMoveSchemeByOnePosition(it, moveDown = true) } ?: false

        /**
         * Returns the shortcut for this action.
         */
        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.DOWN)

        /**
         * Moves the selected scheme down by one position in the tree.
         *
         * @param event ignored
         */
        override fun actionPerformed(event: AnActionEvent) = moveSchemeByOnePosition(selectedScheme!!, moveDown = true)
    }

    /**
     * Resets the selected scheme to its original state, or removes it if it has no original state.
     */
    private inner class ResetButton : AnActionButton(Bundle("shared.action.reset"), AllIcons.General.Reset) {
        /**
         * Specifies the thread in which [update] is invoked.
         */
        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        /**
         * Returns `true` if and only if this action is enabled.
         */
        override fun isEnabled() = selectedScheme?.let { isModified(it) } ?: false

        /**
         * Resets the selected scheme to its original state, or removes it if it has no original state.
         *
         * @param event ignored
         */
        override fun actionPerformed(event: AnActionEvent) {
            val toReset = selectedScheme!!
            val toResetFrom = originalSettings.templateList.getSchemeByUuid(toReset.uuid)
            if (toResetFrom == null) {
                removeScheme(toReset)
                return
            }

            toReset.copyFrom(toResetFrom)
            toReset.applyContext(currentSettings)

            myModel.fireNodeChanged(StateNode(toReset))
            myModel.fireNodeStructureChanged(StateNode(toReset))

            clearSelection()
            myModel.expandAndSelect(StateNode(toReset))
        }
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The list of schemes that the user can add from the add action.
         */
        val POPUP_STEP_SCHEMES: List<Scheme>
            get() = listOf(
                Template(Bundle("template.title"), mutableListOf()),
                IntegerScheme(),
                DecimalScheme(),
                StringScheme(),
                WordScheme(),
                UuidScheme(),
                DateTimeScheme(),
                TemplateReference(),
            )

        /**
         * Number of [Template]s that should be rendered with the index in front.
         */
        const val INDEXED_TEMPLATE_COUNT = 10
    }
}
