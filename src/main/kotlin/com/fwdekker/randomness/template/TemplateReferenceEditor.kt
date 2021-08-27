package com.fwdekker.randomness.template

import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArraySchemeDecoratorEditor
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.components.JBList
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
class TemplateReferenceEditor(reference: TemplateReference) : StateEditor<TemplateReference>(reference) {
    override val rootComponent = JPanel(BorderLayout())
    private val templateListModel = DefaultListModel<Template>()
    private val templateList = JBList(templateListModel)
    private val arrayDecoratorEditor: ArraySchemeDecoratorEditor


    init {
        templateList.cellRenderer = object : ColoredListCellRenderer<Template>() {
            override fun customizeCellRenderer(
                list: JList<out Template>,
                value: Template?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                append(value?.name ?: "<unknown>")
            }
        }
        templateList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        templateList.setEmptyText("Cannot reference any other template without causing recursion.")
        rootComponent.add(templateList, BorderLayout.CENTER)

        arrayDecoratorEditor = ArraySchemeDecoratorEditor(originalState.decorator)
        rootComponent.add(arrayDecoratorEditor.rootComponent, BorderLayout.SOUTH)

        loadState()
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
        templateList.setSelectedValue(
            if (state.templateUuid == null) validTemplates.firstOrNull()
            else state.template,
            true
        )
        arrayDecoratorEditor.loadState(state.decorator)
    }

    override fun readState() =
        TemplateReference(
            templateUuid = templateList.selectedValue?.uuid,
            decorator = arrayDecoratorEditor.readState()
        ).also {
            it.uuid = originalState.uuid
            it.templateList = originalState.templateList.copy()
        }


    override fun addChangeListener(listener: () -> Unit) {
        templateList.addListSelectionListener { listener() }
        addChangeListenerTo(arrayDecoratorEditor, listener = listener)
    }
}
