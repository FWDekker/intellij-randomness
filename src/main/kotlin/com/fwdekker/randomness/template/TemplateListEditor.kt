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
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.AnActionButton
import com.intellij.ui.AnActionButtonRunnable
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
    private val templateTree = TemplateTree { scheme ->
        val templates = originalState.templateList.templates
        val schemesAndTemplates = templates.flatMap { it.schemes } + templates

        schemesAndTemplates.firstOrNull { it.uuid == scheme.uuid } != scheme
    }
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
            .setAddAction {
                it.preferredPopupPoint?.let { point ->
                    JBPopupFactory.getInstance().createListPopup(AddPopupStep()).show(point)
                }
            }
            .setAddActionName("Add")
            .setAddIcon(LayeredIcon.ADD_WITH_DROPDOWN)
            .setRemoveAction(RemoveAction())
            .setRemoveActionName("Remove")
            .setRemoveActionUpdater { templateTree.selectedNode != null }
            .addExtraAction(CopyActionButton())
            .addExtraAction(UpActionButton())
            .addExtraAction(DownActionButton())
            .setButtonComparator("Add", "Remove", "Copy", "Up", "Down")
            .createPanel()

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
                editor.addChangeListener(
                    {
                        editor.applyState()
                        templateTree.myModel.nodeChanged(selectedNode)
                        templateTree.myModel.nodeStructureChanged(selectedNode)
                    }.also { it() } // Invoke listener to apply automatic fixes from editor's constructor
                )

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
     * @see TemplateTree.addScheme
     */
    @ActuallyPrivate("Exposed for testing because popup cannot easily be tested.")
    internal fun addScheme(newScheme: Scheme) = templateTree.addScheme(newScheme)


    override fun loadState(state: SettingsState) {
        super.loadState(state)

        currentSettingsState.copyFrom(state)
        templateTree.loadList(currentSettingsState.templateList)
    }

    override fun readState() = currentSettingsState.deepCopy(retainUuid = true)

    override fun reset() {
        super.reset()

        queueSelection?.also { selection ->
            templateTree.selectTemplate(selection)
            SwingUtilities.invokeLater { schemeEditor?.preferredFocusedComponent?.requestFocus() }

            queueSelection = null
        }
    }


    override fun addChangeListener(listener: () -> Unit) {
        templateTree.model.addTreeModelListener(SimpleTreeModelListener(listener))
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

            override fun onChosen(value: Template?, finalChoice: Boolean): PopupStep<*>? {
                if (value != null)
                    templateTree.addScheme(value.deepCopy().also { it.setSettingsState(currentSettingsState) })

                return null
            }

            override fun isSpeedSearchEnabled() = true
        }

        /**
         * The [Scheme]-related entries in [AddPopupStep].
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
        }
    }

    /**
     * The action to invoke when the remove button is pressed.
     */
    private inner class RemoveAction : AnActionButtonRunnable {
        override fun run(t: AnActionButton?) {
            templateTree.removeNode(templateTree.selectedNode ?: return)
        }
    }

    /**
     * The button to display for copying an entry in the tree.
     */
    private inner class CopyActionButton : AnActionButton("Copy", AllIcons.Actions.Copy) {
        override fun actionPerformed(event: AnActionEvent) {
            val node = templateTree.selectedNode ?: return
            val copy = (node.state as Scheme).deepCopy()
                .also { it.setSettingsState(currentSettingsState) }

            templateTree.addScheme(copy)
        }

        override fun isEnabled() = templateTree.selectedNode != null
    }

    /**
     * The button to display for moving an entry up the tree.
     */
    private inner class UpActionButton : AnActionButton("Up", AllIcons.Actions.MoveUp) {
        override fun actionPerformed(event: AnActionEvent) {
            templateTree.selectedNode?.also { templateTree.moveNodeDownBy(it, -1) }
        }

        override fun isEnabled(): Boolean {
            val node = templateTree.selectedNode ?: return false
            return node.parent!!.getIndex(node) > 0
        }
    }

    /**
     * The button to display for moving an entry down the tree.
     */
    private inner class DownActionButton : AnActionButton("Down", AllIcons.Actions.MoveDown) {
        override fun actionPerformed(event: AnActionEvent) {
            templateTree.selectedNode?.also { templateTree.moveNodeDownBy(it, 1) }
        }

        override fun isEnabled(): Boolean {
            val node = templateTree.selectedNode ?: return false
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
