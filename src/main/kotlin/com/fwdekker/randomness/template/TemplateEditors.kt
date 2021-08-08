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
import com.fwdekker.randomness.template.TemplateList.Companion.DEFAULT_TEMPLATES
import com.fwdekker.randomness.template.TemplateSettings.Companion.default
import com.fwdekker.randomness.ui.ListSchemeEditor
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.uuid.UuidSchemeEditor
import com.fwdekker.randomness.word.WordScheme
import com.fwdekker.randomness.word.WordSchemeEditor
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.JBColor
import com.intellij.ui.LayeredIcon
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTextField


/**
 * Component for editing [TemplateSettings].
 *
 * @param templates the templates to edit in this list
 */
class TemplateListEditor(templates: TemplateList = default.state) : ListSchemeEditor<TemplateList, Template>(
    templates,
    { it.templates },
    { TemplateList(it) },
    ADD_ACTION_GROUP_GENERATOR,
    EMPTY_TEXT
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
                    .also { builder -> DEFAULT_TEMPLATES.forEach { builder.addAction(it.name) { it } } }
                    .buildActionGroup()
            }

        /**
         * The text that is displayed when the table is empty.
         */
        const val EMPTY_TEXT = "No templates configured."
    }
}

/**
 * Component for editing an individual [Template].
 *
 * @param template
 * @see TemplateListEditor
 */
class TemplateEditor(template: Template) : ListSchemeEditor<Template, Scheme>(
    template,
    { it.schemes },
    { Template("", it) },
    ADD_ACTION_GROUP_GENERATOR,
    EMPTY_TEXT
) {
    private val nameInput: JTextField


    init {
        val namePanel = JPanel(BorderLayout())
        namePanel.add(JLabel("Name:"), BorderLayout.WEST)
        nameInput = JBTextField()
        namePanel.add(nameInput)
        rootComponent.add(namePanel, BorderLayout.NORTH)

        stateList.setCellRenderer { _, entry, _, _, _ ->
            JBLabel(entry::class.simpleName?.dropLast("Scheme".length) ?: "Unnamed scheme")
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


    override fun addChangeListener(listener: () -> Unit) =
        super.addChangeListener(listener).also { addChangeListenerTo(nameInput, listener = listener) }


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

        /**
         * The text that is displayed when the table is empty.
         */
        const val EMPTY_TEXT = "No schemes configured."
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
