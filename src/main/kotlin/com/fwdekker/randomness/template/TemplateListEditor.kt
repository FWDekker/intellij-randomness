package com.fwdekker.randomness.template

import com.fwdekker.randomness.ActuallyPrivate
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.decimal.DecimalSchemeEditor
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.integer.IntegerSchemeEditor
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.literal.LiteralSchemeEditor
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.string.StringSchemeEditor
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.SimpleTreeModelListener
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.uuid.UuidSchemeEditor
import com.fwdekker.randomness.word.WordScheme
import com.fwdekker.randomness.word.WordSchemeEditor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.AnActionButton
import com.intellij.ui.CommonActionsPanel
import com.intellij.ui.JBSplitter
import com.intellij.ui.LayeredIcon
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities


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
    private var currentSettingsState: SettingsState = SettingsState()
    private val templateTree = TemplateJTree { isModified(it) }
    private var schemeEditorPanel = JPanel(BorderLayout())
    private var schemeEditor: StateEditor<*>? = null

    /**
     * The UUID of the scheme to select after the next invocation of [reset].
     *
     * @see TemplateSettingsConfigurable
     * @see TemplateSettingsAction
     */
    var queueSelection: String? = null

    /**
     * Whether to focus on the scheme editor after the next invocation of [reset].
     */
    var queueFocus: Boolean = false


    init {
        val splitter = JBSplitter(false, SPLITTER_PROPORTION_KEY, DEFAULT_SPLITTER_PROPORTION)
        rootComponent.add(splitter, BorderLayout.CENTER)

        // Left half
        templateTree.addTreeSelectionListener { onTreeSelection() }
        splitter.firstComponent = JBScrollPane(decorateTemplateList(templateTree))

        // Right half
        val previewPanel = PreviewPanel {
            val selectedNode = templateTree.selectedNode ?: return@PreviewPanel LiteralScheme("")
            val selectedTemplate =
                selectedNode.state.let {
                    if (it is Template) it
                    else selectedNode.parent!!.state as Scheme
                }

            currentSettingsState.templateList.templates.first { it.uuid == selectedTemplate.uuid }
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
            .also {
                // Focus on editor when `Enter` is pressed
                object : AnAction() {
                    override fun actionPerformed(event: AnActionEvent) {
                        schemeEditor?.preferredFocusedComponent?.requestFocus()
                    }
                }.registerCustomShortcutSet(CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.EDIT), it)
            }

    /**
     * Invoked when an entry is (de)selected in the tree.
     */
    private fun onTreeSelection() {
        schemeEditor?.also {
            schemeEditorPanel.remove(it.rootComponent)
            schemeEditor = null
        }

        val selectedNode = templateTree.selectedNode
        val selectedObject = selectedNode?.state
        if (selectedObject !is Scheme) {
            templateTree.myModel.nodeChanged(selectedNode)
            return
        }

        schemeEditor = createEditor(selectedObject)
            .also { editor ->
                editor.addChangeListener {
                    editor.applyState()
                    templateTree.myModel.nodeChanged(selectedNode)
                    templateTree.myModel.nodeStructureChanged(selectedNode)
                }

                schemeEditorPanel.add(editor.rootComponent)
                schemeEditorPanel.revalidate() // Show editor immediately
            }
    }

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
     * Adds the given scheme to the tree.
     *
     * @param newScheme the scheme to add
     * @see TemplateJTree.addScheme
     */
    @ActuallyPrivate("Exposed for testing because popup cannot easily be tested.")
    internal fun addScheme(newScheme: Scheme) = templateTree.addScheme(newScheme)

    /**
     * Returns true if and only if the given scheme has been modified with respect to [originalState].
     *
     * @param scheme the scheme to check for modification
     * @return true if and only if the given scheme has been modified with respect to [originalState]
     */
    private fun isModified(scheme: Scheme) = originalState.templateList.getSchemeByUuid(scheme.uuid) != scheme


    override fun loadState(state: SettingsState) {
        super.loadState(state)

        currentSettingsState.copyFrom(state)
        templateTree.loadList(currentSettingsState.templateList)
    }

    override fun readState() = currentSettingsState.deepCopy(retainUuid = true)

    override fun reset() {
        if (queueSelection == null) {
            queueFocus = false
            queueSelection = templateTree.selectedNode?.state?.uuid
        }

        super.reset()

        if (queueSelection != null) {
            templateTree.selectScheme(queueSelection)
            queueSelection = null
        }
        if (queueFocus) {
            SwingUtilities.invokeLater { schemeEditor?.preferredFocusedComponent?.requestFocus() }
            queueFocus = false
        }
    }


    override fun addChangeListener(listener: () -> Unit) {
        templateTree.model.addTreeModelListener(SimpleTreeModelListener(listener))
        templateTree.addTreeSelectionListener { listener() }
    }


    /**
     * Displays a popup to add a scheme, or immediately adds a template if nothing is currently selected.
     */
    private inner class AddSchemeActionButton : AnActionButton("Add", AllIcons.General.Add) {
        override fun actionPerformed(event: AnActionEvent) {
            if (templateTree.selectedNode == null) {
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
                if (templateTree.selectedNode == null) AllIcons.General.Add
                else LayeredIcon.ADD_WITH_DROPDOWN
        }


        /**
         * The [Scheme]-related entries in [AddSchemeActionButton].
         */
        private inner class AddSchemePopupStep : BaseListPopupStep<Scheme>(null, AVAILABLE_ADD_SCHEMES) {
            override fun getIconFor(value: Scheme?) = value?.icon

            override fun getTextFor(value: Scheme?) = value?.name ?: Scheme.DEFAULT_NAME

            override fun onChosen(value: Scheme?, finalChoice: Boolean): PopupStep<*>? {
                if (value != null)
                    templateTree.addScheme(value.deepCopy().also { it.setSettingsState(currentSettingsState) })

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
     * The action to invoke when the remove button is pressed.
     */
    private inner class RemoveActionButton : AnActionButton("Remove", AllIcons.General.Remove) {
        override fun actionPerformed(event: AnActionEvent) = templateTree.removeNode(templateTree.selectedNode!!)

        override fun isEnabled() = templateTree.selectedNode != null

        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.REMOVE)
    }

    /**
     * The button to display for copying an entry in the tree.
     */
    private inner class CopyActionButton : AnActionButton("Copy", AllIcons.Actions.Copy) {
        override fun actionPerformed(event: AnActionEvent) {
            val node = templateTree.selectedNode!!
            val copy = (node.state as Scheme).deepCopy()
            copy.setSettingsState(currentSettingsState)

            templateTree.addScheme(copy)
        }

        override fun isEnabled() = templateTree.selectedNode != null
    }

    /**
     * The button to display for moving an entry up the tree.
     */
    private inner class UpActionButton : AnActionButton("Up", AllIcons.Actions.MoveUp) {
        override fun actionPerformed(event: AnActionEvent) =
            templateTree.moveNodeDownBy(templateTree.selectedNode!!, -1)

        override fun isEnabled(): Boolean {
            val node = templateTree.selectedNode ?: return false
            return node.parent!!.getIndex(node) > 0
        }

        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.UP)
    }

    /**
     * The button to display for moving an entry down the tree.
     */
    private inner class DownActionButton : AnActionButton("Down", AllIcons.Actions.MoveDown) {
        override fun actionPerformed(event: AnActionEvent) =
            templateTree.moveNodeDownBy(templateTree.selectedNode!!, 1)

        override fun isEnabled(): Boolean {
            val node = templateTree.selectedNode ?: return false
            return node.parent!!.getIndex(node) < node.parent!!.childCount - 1
        }

        override fun getShortcut() = CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.DOWN)
    }

    /**
     * The button to display for resetting an entry to its original state.
     */
    private inner class ResetActionButton : AnActionButton("Reset", AllIcons.General.Reset) {
        override fun actionPerformed(event: AnActionEvent) {
            val targetNode = templateTree.selectedNode!!
            val targetUuid = (targetNode.state as Scheme).uuid

            val toReset = currentSettingsState.templateList.getSchemeByUuid(targetUuid) ?: return
            val toResetFrom = originalState.templateList.getSchemeByUuid(targetUuid)
            if (toResetFrom == null) {
                templateTree.removeNode(targetNode)
                return
            }

            toReset.copyFrom(toResetFrom)
            toReset.setSettingsState(currentSettingsState)
            templateTree.selectScheme(toReset.uuid)
        }

        override fun isEnabled() = templateTree.selectedNode?.let { isModified(it.state as Scheme) } ?: false
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The key to store the user's last-used splitter proportion under.
         */
        const val SPLITTER_PROPORTION_KEY = "com.fwdekker.randomness.template.TemplateListEditor"

        /**
         * The default proportion of the splitter component.
         */
        const val DEFAULT_SPLITTER_PROPORTION = .2f

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
