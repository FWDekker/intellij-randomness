package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.CapitalizationComboBox
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.StringComboBox
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.ComboBoxModel
import javax.swing.JList
import javax.swing.JPanel


/**
 * Component for editing [TemplateReference]s.
 *
 * @param reference the reference to edit
 */
class TemplateReferenceEditor(reference: TemplateReference) : StateEditor<TemplateReference>(reference) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = templateComboBox

    private lateinit var templateComboBoxModel: ComboBoxModel<Template>
    private lateinit var templateComboBox: ComboBox<Template>
    private lateinit var capitalizationComboBox: ComboBox<CapitalizationMode>
    private lateinit var quotationComboBox: ComboBox<String>
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        rootComponent = panel {
            group(Bundle("reference.ui.value.header")) {
                row(Bundle("reference.ui.value.template_option")) {
                    comboBox(
                        emptyList(),
                        object : ColoredListCellRenderer<Template>() {
                            override fun customizeCellRenderer(
                                list: JList<out Template>,
                                value: Template?,
                                index: Int,
                                selected: Boolean,
                                hasFocus: Boolean,
                            ) {
                                icon = value?.icon ?: Template.DEFAULT_ICON
                                append(value?.name ?: Bundle("template.name.unknown"))
                            }
                        }
                    )
                        .also { it.component.name = "template" }
                        .also { templateComboBoxModel = it.component.model }
                        .also { templateComboBox = it.component }
                }

                row(Bundle("reference.ui.value.quotation_marks_option")) {
                    cell(StringComboBox(listOf("", "'", "\"", "`"), MaxLengthDocumentFilter(2)))
                        .also { it.component.isEditable = true }
                        .also { it.component.name = "quotation" }
                        .also { quotationComboBox = it.component }
                }

                row(Bundle("reference.ui.value.capitalization_option")) {
                    cell(
                        CapitalizationComboBox(
                            listOf(
                                CapitalizationMode.RETAIN,
                                CapitalizationMode.LOWER,
                                CapitalizationMode.UPPER,
                                CapitalizationMode.RANDOM,
                                CapitalizationMode.SENTENCE,
                                CapitalizationMode.FIRST_LETTER,
                            )
                        )
                    )
                        .also { it.component.name = "capitalization" }
                        .also { capitalizationComboBox = it.component }
                }
            }

            row {
                arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
                cell(arrayDecoratorEditor.rootComponent).horizontalAlign(HorizontalAlign.FILL)
            }
        }

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

        templateComboBox.removeAllItems()
        validTemplates.forEach { templateComboBox.addItem(it) }
        templateComboBox.selectedItem = state.template

        quotationComboBox.item = state.quotation
        capitalizationComboBox.item = state.capitalization

        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        TemplateReference(
            templateUuid = (templateComboBox.selectedItem as? Template)?.uuid,
            quotation = quotationComboBox.item,
            capitalization = capitalizationComboBox.item,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also {
            it.uuid = originalState.uuid
            it.templateList = originalState.templateList.copy()
        }


    override fun addChangeListener(listener: () -> Unit) {
        addChangeListenerTo(
            templateComboBox, capitalizationComboBox, quotationComboBox, arrayDecoratorEditor,
            listener = listener
        )
    }
}
