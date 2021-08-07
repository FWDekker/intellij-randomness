package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeEditor
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
import com.intellij.openapi.actionSystem.ActionGroup
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
 * A component that can be used to edit a scheme of type [S] which consists of a list of schemes of type [E].
 *
 * The interface consists of a list on the left, containing the instances of [E] inside [originalScheme], and displays
 * an editor on the right whenever an entry in the list is pressed.
 *
 * @param S the type of scheme that can be edited in this component
 * @param E the type of scheme that [S] consists of
 * @property stateToEntryList converts an instance of [S] into a list of type [E]
 * @property addActionGroupGenerator returns the actions that can be taken when the "Add" button is pressed
 * @param originalScheme the scheme to be edited in this component
 * @param dummyStateGenerator returns a simple placeholder instance of type [S]
 */
abstract class ListSchemeEditor<S : Scheme, E : Scheme>(
    originalScheme: S,
    private val stateToEntryList: (S) -> List<E>,
    dummyStateGenerator: () -> S,
    private val addActionGroupGenerator: (JBList<E>, DefaultListModel<E>) -> ActionGroup
) : SchemeEditor<S>(originalScheme) {
    private val unsavedState = dummyStateGenerator()
    private val stateListModel = DefaultListModel<E>()
    private val changeListeners = mutableListOf<() -> Unit>()

    final override val rootComponent = JPanel(BorderLayout())

    /**
     * The list of entries of type [E].
     *
     * This list includes possibly unsaved entries.
     */
    val stateList = JBList(stateListModel)

    /**
     * The current entry editor displayed on the right.
     */
    var entryEditor: SchemeEditor<out E>? = null


    init {
        val splitter = JBSplitter(false, DEFAULT_SPLITTER_PROPORTION)
        rootComponent.add(splitter, BorderLayout.CENTER)

        stateList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        splitter.firstComponent = JBScrollPane(decorateTemplateList(stateList))

        stateList.addListSelectionListener { event ->
            val entry = stateList.selectedValue
            if (event.valueIsAdjusting || entry == null)
                return@addListSelectionListener

            entryEditor = createEditor(entry)
                .also { editor ->
                    editor.addChangeListener {
                        editor.applyScheme()
                        changeListeners.forEach { it() }
                        stateList.repaint() // Force updates to be visible
                    }

                    splitter.secondComponent = JBScrollPane(editor.rootComponent)
                }
        }
    }

    /**
     * Decorates the given list with buttons for adding, removing, copying, etc.
     *
     * @param templateList the list to decorate
     * @return a panel containing both the decorator and the given list
     */
    private fun decorateTemplateList(templateList: JBList<E>) =
        ToolbarDecorator.createDecorator(templateList)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())
            .setScrollPaneBorder(JBUI.Borders.empty())
            .disableAddAction()
            .addExtraAction(AnActionButton.GroupPopupWrapper(addActionGroupGenerator(stateList, stateListModel)))
            .addExtraAction(object : AnActionButton("Copy", PlatformIcons.COPY_ICON) {
                override fun actionPerformed(e: AnActionEvent) {
                    templateList.selectedValue?.let { entry ->
                        @Suppress("UNCHECKED_CAST") // `deepCopy` always returns instance of its own class
                        stateListModel.addElement(entry.deepCopy() as E)
                        templateList.selectedIndex = stateListModel.size - 1
                    }
                }

                override fun isEnabled() = templateList.selectedValue != null
            })
            .setButtonComparator("Add", "Remove", "Copy", "Up", "Down")
            .createPanel()


    /**
     * Creates an editor for the given entry.
     *
     * @param entry the entry to edit
     * @return an editor for the given entry
     */
    abstract fun createEditor(entry: E): SchemeEditor<out E>


    override fun loadScheme(scheme: S) {
        super.loadScheme(scheme)

        unsavedState.copyFrom(scheme.deepCopy())

        stateListModel.removeAllElements()
        stateListModel.addAll(stateToEntryList(unsavedState).toList())
        stateList.selectedIndex = -1

        changeListeners.forEach { it() }
        if (!stateListModel.isEmpty)
            stateList.selectedIndex = 0
    }

    override fun readScheme() = unsavedState


    override fun isModified(): Boolean {
        val originalEntries = stateToEntryList(originalScheme)
        val unsavedEntries = stateToEntryList(unsavedState)

        return super.isModified() ||
            originalEntries.size != unsavedEntries.size ||
            originalEntries.zip(unsavedEntries).any { it.first != it.second }
    }

    override fun doValidate() = entryEditor?.doValidate() ?: unsavedState.doValidate()


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
 * Component for editing [TemplateSettings].
 *
 * @param templates the templates to edit in this list
 */
class TemplateListEditor(templates: TemplateList = default.state) : ListSchemeEditor<TemplateList, Template>(
    templates,
    { it.templates },
    { TemplateList() },
    ADD_ACTION_GROUP_GENERATOR
) {
    init {
        stateList.setCellRenderer { _, entry, _, _, _ ->
            JBLabel(entry.name.ifBlank { "<empty>" })
                .also { label ->
                    if (entry.doValidate() != null)
                        label.foreground = ERROR_COLOR
                    else if (originalScheme.templates.firstOrNull { it.name == entry.name } != entry)
                        label.foreground = MODIFIED_COLOR
                }
        }

        loadScheme()
    }


    override fun createEditor(entry: Template) = TemplateEditor(entry)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The group of available actions when the "Add" button is pressed.
         */
        val ADD_ACTION_GROUP_GENERATOR: (JBList<Template>, DefaultListModel<Template>) -> ActionGroup =
            { stateList, stateListModel ->
                DefaultActionGroupBuilder(stateList, stateListModel)
                    .addAction("Integer") { Template("Integer", listOf(IntegerScheme())) }
                    .addAction("Decimal") { Template("Decimal", listOf(DecimalScheme())) }
                    .addAction("String") { Template("String", listOf(StringScheme())) }
                    .addAction("Word") { Template("Word", listOf(WordScheme())) }
                    .addAction("UUID") { Template("UUID", listOf(UuidScheme())) }
                    .buildActionGroup()
            }
    }
}

/**
 * Component for editing an individual [Template].
 *
 * @param template
 * @see TemplateListEditor
 */
class TemplateEditor(template: Template) :
    ListSchemeEditor<Template, Scheme>(template, { it.schemes }, { Template() }, ADD_ACTION_GROUP_GENERATOR) {
    private val nameInput: JTextField


    init {
        val namePanel = JPanel(BorderLayout())
        namePanel.add(JLabel("Name:"), BorderLayout.WEST)
        nameInput = JBTextField()
        namePanel.add(nameInput)
        rootComponent.add(namePanel, BorderLayout.NORTH)

        stateList.setCellRenderer { _, entry, _, _, _ ->
            JBLabel(entry::class.simpleName ?: "Unnamed scheme")
                .also { label ->
                    if (entry.doValidate() != null)
                        label.foreground = ERROR_COLOR
                }
        }

        val previewPanel = PreviewPanel { TemplateList(listOf(readScheme())) }
        addChangeListener { previewPanel.updatePreview() }
        rootComponent.add(previewPanel.rootComponent, BorderLayout.SOUTH)

        loadScheme()
    }

    override fun createEditor(entry: Scheme): SchemeEditor<out Scheme> {
        return when (entry) {
            is IntegerScheme -> IntegerSchemeEditor(entry)
            is DecimalScheme -> DecimalSchemeEditor(entry)
            is StringScheme -> StringSchemeEditor(entry)
            is UuidScheme -> UuidSchemeEditor(entry)
            is WordScheme -> WordSchemeEditor(entry)
            is LiteralScheme -> LiteralSchemeEditor(entry)
            else -> error("Unknown scheme type '${entry.javaClass.canonicalName}'.")
        }
    }


    override fun loadScheme(scheme: Template) = super.loadScheme(scheme).also { nameInput.text = scheme.name }

    override fun readScheme() = super.readScheme().also { it.name = nameInput.text }


    override fun doValidate(): String? =
        if (nameInput.text.isBlank()) "Template name should not be empty."
        else super.doValidate()


    override fun addChangeListener(listener: () -> Unit) {
        super.addChangeListener(listener)

        addChangeListenerTo(nameInput, listener = listener)
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The group of available actions when the "Add" button is pressed.
         */
        val ADD_ACTION_GROUP_GENERATOR: (JBList<Scheme>, DefaultListModel<Scheme>) -> ActionGroup =
            { stateList, stateListModel ->
                DefaultActionGroupBuilder(stateList, stateListModel)
                    .addAction("Literal") { LiteralScheme() }
                    .addAction("Integer") { IntegerScheme() }
                    .addAction("Decimal") { DecimalScheme() }
                    .addAction("String") { StringScheme() }
                    .addAction("Word") { WordScheme() }
                    .addAction("UUID") { UuidScheme() }
                    .buildActionGroup()
            }
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
private val ERROR_COLOR = JBColor.namedColor("Tree.errorForeground", JBColor.RED)
