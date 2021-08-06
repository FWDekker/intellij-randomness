package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.ValidationInfo
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
import com.intellij.ui.JBSplitter
import com.intellij.ui.LayeredIcon
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel


/**
 * Component for editing a list of templates.
 *
 * @param settings the settings to edit in the component
 *
 * @see TemplateSettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class TemplateSettingsComponent(val settings: TemplateSettings = default) :
    SettingsComponent<TemplateSettings>(settings) {
    override lateinit var rootPane: JPanel private set
    private lateinit var templatePanel: JPanel
    private lateinit var templateListEditor: TemplateListEditor


    init {
        loadSettings()
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        templateListEditor = TemplateListEditor()
        templatePanel = templateListEditor
    }


    override fun loadSettings(settings: TemplateSettings) = templateListEditor.loadTemplateList(settings.templates)

    override fun saveSettings(settings: TemplateSettings) {
        settings.templates = templateListEditor.saveTemplateList()
    }

    // TODO: Implement validation
    override fun doValidate(): ValidationInfo? = null


    override fun addChangeListener(listener: () -> Unit) = templateListEditor.addChangeListener(listener)
}

/**
 * Component for editing a list of [Template]s.
 *
 * Template lists can be loaded into and saved from this component. When a template list is loaded, its templates are
 * displayed in a list. When a template is selected by the user, a [TemplateEditor] is displayed inside this component.
 * Changes made through the template's editor are reflected in the state of this component.
 *
 * @see TemplateEditor
 */
private class TemplateListEditor : JPanel(BorderLayout()) {
    val rootPane = JBSplitter(false, .2f)

    private val changeListeners = mutableListOf<() -> Unit>()
    private var isAdjusting: Boolean = false

    private val templateList: JList<Template>
    private val templateListModel = DefaultListModel<Template>()
    private val templateEditor: TemplateEditor


    init {
        templateList = JBList(templateListModel)
        templateList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        templateList.setCellRenderer { _, value, _, _, _ -> JBLabel("Template (${value.schemes.size})") }
        rootPane.firstComponent = JBScrollPane(decorateTemplateList(templateList))

        templateEditor = TemplateEditor()
        templateEditor.addChangeListener {
            val index = templateList.selectedIndex
            if (index in 0 until templateListModel.size)
                templateListModel.set(index, templateEditor.saveTemplate())

            changeListeners.forEach { it() }
        }
        rootPane.secondComponent = templateEditor

        templateList.addListSelectionListener { event ->
            if (isAdjusting) return@addListSelectionListener

            val index = templateList.selectedIndex
            if (!event.valueIsAdjusting && index in 0 until templateListModel.size)
                templateEditor.loadTemplate(templateListModel.get(index))
        }
        add(rootPane, BorderLayout.CENTER)
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
            .addExtraAction(AnActionButton.GroupPopupWrapper(
                DefaultActionGroupBuilder(templateList, templateListModel)
                    .addAction("Integer") { Template(listOf(IntegerScheme())) }
                    .addAction("Decimal") { Template(listOf(DecimalScheme())) }
                    .addAction("String") { Template(listOf(StringScheme())) }
                    .addAction("Word") { Template(listOf(WordScheme())) }
                    .addAction("UUID") { Template(listOf(UuidScheme())) }
                    .buildActionGroup()
            ))
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


    /**
     * Unloads the current template list (if any) and loads the given template list.
     *
     * @param templates the template list to load for editing
     * @see addChangeListener
     */
    fun loadTemplateList(templates: List<Template>) {
        isAdjusting = true
        templateListModel.removeAllElements()
        templateListModel.addAll(templates)
        templateList.selectedIndex = -1
        isAdjusting = false

        if (!templateListModel.isEmpty) {
            templateList.selectedIndex = 0
            changeListeners.forEach { it() }
        }
    }

    /**
     * Returns the template list as adjusted through this editor.
     *
     * @return the template list as adjusted through this editor
     */
    fun saveTemplateList() = templateListModel.elements().toList()


    /**
     * Adds a listener that is invoked whenever the user changes the list of templates or any of its templates.
     *
     * When `loadTemplateList` is invoked, the listener is invoked once when a template editor is opened to edit the
     * loaded template list's first template, if there is one.
     *
     * @param listener the function to invoke when the template list or any of its templates are changed
     */
    fun addChangeListener(listener: () -> Unit) {
        val suppressibleListener = { if (!isAdjusting) listener() }
        addChangeListenerTo(templateList, listener = suppressibleListener)
        changeListeners += suppressibleListener
    }
}

/**
 * Component for editing [Template]s.
 *
 * Templates can be loaded into and saved from this component. When a template is loaded, its schemes are displayed in a
 * list. When a scheme is selected by the user, the corresponding [com.fwdekker.randomness.SchemeEditor](SchemeEditor)
 * is displayed inside this component. Changes made through the scheme's editor are reflected in the state of this
 * component.
 *
 * @see TemplateListEditor
 */
private class TemplateEditor : JPanel(BorderLayout()) {
    val rootPane = JBSplitter(false, .2f)

    private val changeListeners = mutableListOf<() -> Unit>()
    private var isAdjusting: Boolean = false

    private val schemeList: JList<Scheme<*>>
    private val schemeListModel = DefaultListModel<Scheme<*>>()
    private val schemeEditorPanel: JPanel
    private val previewPanel: PreviewPanel


    init {
        schemeList = JBList(schemeListModel)
        schemeList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        schemeList.setCellRenderer { _, value, _, _, _ -> JBLabel(value::class.simpleName ?: "Scheme") }
        rootPane.firstComponent = JBScrollPane(decorateSchemeList(schemeList))

        schemeEditorPanel = JPanel(BorderLayout())
        schemeList.addListSelectionListener { event ->
            if (isAdjusting) return@addListSelectionListener

            val index = schemeList.selectedIndex
            if (!event.valueIsAdjusting && index in 0 until schemeListModel.size)
                displaySchemeEditor(index)
        }
        rootPane.secondComponent = schemeEditorPanel
        add(rootPane, BorderLayout.CENTER)

        previewPanel = PreviewPanel { TemplateInsertAction(TemplateSettings(listOf(saveTemplate()))) }
        addChangeListener { previewPanel.updatePreview() }
        add(previewPanel.rootPane, BorderLayout.SOUTH)
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
            .addExtraAction(AnActionButton.GroupPopupWrapper(
                DefaultActionGroupBuilder(schemeList, schemeListModel)
                    .addAction("Literal") { LiteralScheme() }
                    .addAction("Integer") { IntegerScheme() }
                    .addAction("Decimal") { DecimalScheme() }
                    .addAction("String") { StringScheme() }
                    .addAction("Word") { WordScheme() }
                    .addAction("UUID") { UuidScheme() }
                    .buildActionGroup()
            ))
            .addExtraAction(object : AnActionButton("Copy", PlatformIcons.COPY_ICON) {
                override fun actionPerformed(e: AnActionEvent) {
                    schemeList.selectedValue?.let { scheme ->
                        schemeListModel.addElement(scheme.deepCopy() as Scheme<*>)
                        schemeList.selectedIndex = schemeListModel.size - 1
                    }
                }

                override fun isEnabled() = schemeList.selectedValue != null
            })
            .setButtonComparator("Add", "Remove", "Copy", "Up", "Down")
            .createPanel()

    /**
     * Removes the current scheme editor and replaces it with another one for the indicated scheme.
     *
     * @param listIndex the index in the scheme list of the scheme to be edited
     */
    private fun displaySchemeEditor(listIndex: Int) {
        val scheme = schemeListModel.get(listIndex)
        val editor = when (scheme) {
            is IntegerScheme -> IntegerSchemeEditor(scheme)
            is DecimalScheme -> DecimalSchemeEditor(scheme)
            is StringScheme -> StringSchemeEditor(scheme)
            is UuidScheme -> UuidSchemeEditor(scheme)
            is WordScheme -> WordSchemeEditor(scheme)
            is LiteralScheme -> LiteralSchemeEditor(scheme)
            else -> error("Unknown scheme type '${scheme.javaClass.canonicalName}'.")
        }
        editor.addChangeListener {
            if (isAdjusting) return@addChangeListener

            schemeListModel.set(listIndex, editor.saveScheme())
            changeListeners.forEach { it() }
        }

        schemeEditorPanel.removeAll()
        schemeEditorPanel.add(editor.rootPane)
        changeListeners.forEach { it() }
    }


    /**
     * Unloads the current template (if any) and loads the given template.
     *
     * @param template the template to load for editing
     * @see addChangeListener
     */
    fun loadTemplate(template: Template) {
        isAdjusting = true
        schemeListModel.removeAllElements()
        schemeListModel.addAll(template.schemes)
        schemeList.selectedIndex = -1
        isAdjusting = false

        if (!schemeListModel.isEmpty) {
            schemeList.selectedIndex = 0
            changeListeners.forEach { it() }
        }
    }

    /**
     * Returns the template as adjusted through this editor.
     *
     * @return the template as adjusted through this editor
     */
    fun saveTemplate() = Template(schemeListModel.elements().toList())


    /**
     * Adds a listener that is invoked whenever the user changes the template or any of its schemes.
     *
     * When `loadTemplate` is invoked, the listener is invoked once when a scheme editor is opened to edit the loaded
     * template's first scheme, if there is one.
     *
     * @param listener the function to invoke when the template or any of its schemes are changed
     */
    fun addChangeListener(listener: () -> Unit) {
        val suppressibleListener = { if (!isAdjusting) listener() }
        addChangeListenerTo(schemeList, listener = suppressibleListener)
        changeListeners += suppressibleListener
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
