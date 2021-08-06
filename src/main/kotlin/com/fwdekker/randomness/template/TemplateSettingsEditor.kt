package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.decimal.DecimalSchemeEditor
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.integer.IntegerSchemeEditor
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.literal.LiteralSchemeEditor
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.string.StringSchemeEditor
import com.fwdekker.randomness.template.TemplateSettings.Companion.default
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.uuid.UuidSchemeEditor
import com.fwdekker.randomness.word.WordScheme
import com.fwdekker.randomness.word.WordSchemeEditor
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.AnActionButton
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import com.intellij.ui.LayeredIcon
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.ListSelectionModel


/**
 * Component for editing [TemplateSettings].
 *
 * @param templates the settings to edit
 */
class TemplateSettingsEditor(templates: TemplateSettings = default) : StateEditor<TemplateSettings>(templates) {
    override val rootComponent = JPanel(BorderLayout())
    private val templateList: JBList<Template>
    private val templateListModel = DefaultListModel<Template>()

    private val unsavedState = TemplateSettings(listOf())
    private val changeListeners = mutableListOf<() -> Unit>()


    init {
        val splitter = JBSplitter(false, DEFAULT_SPLITTER_PROPORTION)
        rootComponent.add(splitter, BorderLayout.CENTER)

        templateList = JBList(templateListModel)
        templateList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        templateList.setCellRenderer { _, entry, _, _, _ ->
            JBLabel(entry.name)
                .also { label ->
                    if (originalState.templates.firstOrNull { it.name == entry.name } != entry)
                        label.foreground = MODIFIED_COLOR
                }
        }
        splitter.firstComponent = JBScrollPane(decorateTemplateList(templateList))

        templateList.addListSelectionListener { event ->
            val selection = templateList.selectedValue
            if (!event.valueIsAdjusting && selection != null) {
                val editor = TemplateEditor(selection)
                editor.addChangeListener {
                    editor.applyState()
                    changeListeners.forEach { it() }
                    templateList.repaint() // Force template names to update
                }
                splitter.secondComponent = editor.rootComponent
            }
        }

        loadState()
    }

    /**
     * Decorates the given list with buttons for adding, removing, copying, etc.
     *
     * @param templateList the list to decorate
     * @return a panel containing both the decorator and the given list
     */
    private fun decorateTemplateList(templateList: JBList<Template>) =
        ToolbarDecorator.createDecorator(templateList)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())
            .setScrollPaneBorder(JBUI.Borders.empty())
            .disableAddAction()
            .addExtraAction(
                AnActionButton.GroupPopupWrapper(
                    DefaultActionGroupBuilder(templateList, templateListModel)
                        .addAction("Integer") { Template("Integer", listOf(IntegerScheme())) }
                        .addAction("Decimal") { Template("Decimal", listOf(DecimalScheme())) }
                        .addAction("String") { Template("String", listOf(StringScheme())) }
                        .addAction("Word") { Template("Word", listOf(WordScheme())) }
                        .addAction("UUID") { Template("UUID", listOf(UuidScheme())) }
                        .buildActionGroup()
                )
            )
            .addExtraAction(object : AnActionButton("Copy", PlatformIcons.COPY_ICON) {
                override fun actionPerformed(e: AnActionEvent) {
                    templateList.selectedValue?.let { template ->
                        templateListModel.addElement(template.deepCopy())
                        templateList.selectedIndex = templateListModel.size - 1
                    }
                }

                override fun isEnabled() = templateList.selectedValue != null
            })
            .setButtonComparator("Add", "Remove", "Copy", "Up", "Down")
            .createPanel()


    override fun loadState(state: TemplateSettings) {
        super.loadState(state)
        unsavedState.loadState(state.deepCopy())

        templateListModel.removeAllElements()
        templateListModel.addAll(unsavedState.templates.toList())
        templateList.selectedIndex = -1

        changeListeners.forEach { it() }
        if (!templateListModel.isEmpty)
            templateList.selectedIndex = 0
    }

    override fun readState() = unsavedState


    override fun isModified(): Boolean {
        val loadedTemplates = originalState.templates
        val unsavedTemplates = unsavedState.templates

        return super.isModified() ||
            loadedTemplates.size != unsavedTemplates.size ||
            loadedTemplates.zip(unsavedTemplates).any { it.first != it.second }
    }

    override fun addChangeListener(listener: () -> Unit) {
        changeListeners += listener
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default proportion of the splitter component.
         */
        const val DEFAULT_SPLITTER_PROPORTION = .2f
    }
}

/**
 * Component for editing an individual [Template].
 *
 * @param template
 * @see TemplateSettingsEditor
 */
private class TemplateEditor(template: Template) : StateEditor<Template>(template) {
    override val rootComponent = JPanel(BorderLayout())
    private val nameInput: JTextField
    private val schemeList: JList<Scheme<*>>
    private val schemeListModel = DefaultListModel<Scheme<*>>()
    private val schemeEditorPanel: JPanel

    private val changeListeners = mutableListOf<() -> Unit>()


    init {
        val namePanel = JPanel(BorderLayout())
        namePanel.add(JLabel("Name:"), BorderLayout.WEST)
        nameInput = JBTextField()
        namePanel.add(nameInput)
        rootComponent.add(namePanel, BorderLayout.NORTH)

        val splitter = JBSplitter(false, DEFAULT_SPLITTER_PROPORTION)
        rootComponent.add(splitter, BorderLayout.CENTER)

        schemeList = JBList(schemeListModel)
        schemeList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        schemeList.setCellRenderer { _, value, _, _, _ -> JBLabel(value::class.simpleName ?: "Unnamed scheme") }
        splitter.firstComponent = JBScrollPane(decorateSchemeList(schemeList))

        schemeEditorPanel = JPanel(BorderLayout())
        schemeList.addListSelectionListener { event ->
            val selection = schemeList.selectedValue
            if (!event.valueIsAdjusting && selection != null) {
                val editor = getSchemeEditor(selection)
                editor.addChangeListener {
                    editor.applyState()
                    changeListeners.forEach { it() }
                }

                schemeEditorPanel.removeAll()
                schemeEditorPanel.add(editor.rootComponent)
                changeListeners.forEach { it() }
            }
        }
        splitter.secondComponent = schemeEditorPanel

        val previewPanel = PreviewPanel { TemplateInsertAction(TemplateSettings(listOf(readState()))) }
        addChangeListener { previewPanel.updatePreview() }
        rootComponent.add(previewPanel.rootPane, BorderLayout.SOUTH)

        loadState()
    }

    /**
     * Decorates the given list with buttons for adding, removing, copying, etc.
     *
     * @param schemeList the list to decorate
     * @return a panel containing both the decorator and the given list
     */
    private fun decorateSchemeList(schemeList: JBList<Scheme<*>>) =
        ToolbarDecorator
            .createDecorator(schemeList)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())
            .setScrollPaneBorder(JBUI.Borders.empty())
            .disableAddAction()
            .addExtraAction(
                AnActionButton.GroupPopupWrapper(
                    DefaultActionGroupBuilder(schemeList, schemeListModel)
                        .addAction("Literal") { LiteralScheme() }
                        .addAction("Integer") { IntegerScheme() }
                        .addAction("Decimal") { DecimalScheme() }
                        .addAction("String") { StringScheme() }
                        .addAction("Word") { WordScheme() }
                        .addAction("UUID") { UuidScheme() }
                        .buildActionGroup()
                )
            )
            .addExtraAction(object : AnActionButton("Copy", PlatformIcons.COPY_ICON) {
                override fun actionPerformed(e: AnActionEvent) {
                    schemeList.selectedValue?.let { scheme ->
                        schemeListModel.addElement(scheme.deepCopy())
                        schemeList.selectedIndex = schemeListModel.size - 1
                    }
                }

                override fun isEnabled() = schemeList.selectedValue != null
            })
            .setButtonComparator("Add", "Remove", "Copy", "Up", "Down")
            .createPanel()

    /**
     * Creates a scheme editor for the given scheme.
     *
     * @param scheme the scheme to create an editor for
     */
    private fun getSchemeEditor(scheme: Scheme<*>): StateEditor<*> {
        return when (scheme) {
            is IntegerScheme -> IntegerSchemeEditor(scheme)
            is DecimalScheme -> DecimalSchemeEditor(scheme)
            is StringScheme -> StringSchemeEditor(scheme)
            is UuidScheme -> UuidSchemeEditor(scheme)
            is WordScheme -> WordSchemeEditor(scheme)
            is LiteralScheme -> LiteralSchemeEditor(scheme)
            else -> error("Unknown scheme type '${scheme.javaClass.canonicalName}'.")
        }
    }


    override fun loadState(state: Template) {
        super.loadState(state)

        nameInput.text = state.name

        schemeListModel.removeAllElements()
        schemeListModel.addAll(state.schemes)
        schemeList.selectedIndex = -1

        if (!schemeListModel.isEmpty)
            schemeList.selectedIndex = 0
        changeListeners.forEach { it() }
    }

    override fun readState() = Template(nameInput.text, schemeListModel.elements().toList())


    override fun addChangeListener(listener: () -> Unit) {
        addChangeListenerTo(nameInput, listener = listener)
        changeListeners += listener
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default proportion of the splitter component.
         */
        const val DEFAULT_SPLITTER_PROPORTION = .2f
    }
}


/**
 * Utility class for building a `DefaultActionGroup` consisting of actions that add a specified element to a list.
 *
 * @param T the type of element
 * @property list the list that actions add items to
 * @property listModel the model of the list that actions add items to
 */
private class DefaultActionGroupBuilder<T>(
    private val list: JList<T>,
    private val listModel: DefaultListModel<T>,
) {
    private val actions = mutableListOf<DumbAwareAction>()


    /**
     * Adds an action to the group.
     *
     * @param name the name of the action to add
     * @param item returns the item to be added whenever the action is invoked
     * @return this `DefaultActionGroupBuilder`
     */
    fun addAction(name: String, item: () -> T): DefaultActionGroupBuilder<T> {
        actions.add(object : DumbAwareAction(name) {
            override fun actionPerformed(e: AnActionEvent) {
                listModel.addElement(item())
                list.selectedIndex = listModel.size - 1
            }
        })
        return this
    }

    /**
     * Builds the `DefaultActionGroup` consisting of the added actions.
     *
     * @return the `DefaultActionGroup` consisting of the added actions
     */
    fun buildActionGroup() =
        DefaultActionGroup(actions).apply {
            templatePresentation.icon = LayeredIcon.ADD_WITH_DROPDOWN
            templatePresentation.text = "Add"
            registerCustomShortcutSet(CommonShortcuts.getNewForDialogs(), null)
        }
}


private val MODIFIED_COLOR = JBColor.namedColor("Tree.modifiedItemForeground", JBColor.BLUE)
// private val ERROR_COLOR = JBColor.namedColor("Tree.errorForeground", JBColor.RED)
