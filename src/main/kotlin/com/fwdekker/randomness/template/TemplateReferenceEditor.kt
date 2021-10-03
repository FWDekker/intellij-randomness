package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel


/**
 * Component for editing [TemplateReference]s.
 *
 * @param reference the reference to edit
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class TemplateReferenceEditor(reference: TemplateReference) : StateEditor<TemplateReference>(reference) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = templateList

    private lateinit var templateListPanel: JPanel
    private lateinit var templateListModel: DefaultListModel<Template>
    private lateinit var templateList: JBList<Template>
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor
    private lateinit var arrayDecoratorEditorPanel: JPanel


    init {
        loadState()
    }

    /**
     * Initializes custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        templateListModel = DefaultListModel<Template>()
        templateList = JBList(templateListModel)
        templateList.cellRenderer = object : ColoredListCellRenderer<Template>() {
            override fun customizeCellRenderer(
                list: JList<out Template>,
                value: Template?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                icon = value?.icon ?: Template.DEFAULT_ICON
                append(value?.name ?: Bundle("template.name.unknown"))
            }
        }
        templateList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        templateList.setEmptyText(Bundle("reference.ui.empty"))
        templateListPanel = JPanel(BorderLayout())
        templateListPanel.add(JBScrollPane(templateList), BorderLayout.WEST)

        arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
        arrayDecoratorEditorPanel = arrayDecoratorEditor.rootComponent
    }


    override fun loadState(state: TemplateReference) {
        super.loadState(state)

        // Find templates that would not cause recursion if selected
        val listCopy = (+state.templateList).deepCopy(retainUuid = true)
            .also { it.applySettingsState(SettingsState(it)) }
        val referenceCopy =
            listCopy.templates.flatMap { it.schemes }.single { it.uuid == originalState.uuid } as TemplateReference
        val validTemplates =
            (+state.templateList).templates
                .filter { template ->
                    referenceCopy.template = template
                    listCopy.findRecursionFrom(referenceCopy) == null
                }

        templateListModel.removeAllElements()
        templateListModel.addAll(validTemplates)
        templateList.setSelectedValue(state.template, true)
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        TemplateReference(
            templateUuid = templateList.selectedValue?.uuid,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also {
            it.uuid = originalState.uuid
            it.templateList = originalState.templateList.copy()
        }


    override fun addChangeListener(listener: () -> Unit) {
        templateList.addListSelectionListener { listener() }
        addChangeListenerTo(arrayDecoratorEditor, listener = listener)
    }
}
