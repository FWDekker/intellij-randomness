package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.template.TemplateReference.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.template.TemplateReference.Companion.DEFAULT_QUOTATION
import com.fwdekker.randomness.ui.GridPanelBuilder
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.uiDesigner.core.GridConstraints
import javax.swing.ButtonGroup
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
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = templateList

    private lateinit var templateListModel: DefaultListModel<Template>
    private lateinit var templateList: JBList<Template>
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var customQuotation: VariableLabelRadioButton
    private lateinit var quotationGroup: ButtonGroup
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        rootComponent = GridPanelBuilder.panel {
            textSeparatorCell(Bundle("reference.ui.template_list"))

            cell {
                templateListModel = DefaultListModel<Template>()
                templateList = JBList(templateListModel)
                templateList.cellRenderer = object : ColoredListCellRenderer<Template>() {
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
                templateList.selectionMode = ListSelectionModel.SINGLE_SELECTION
                templateList.setEmptyText(Bundle("reference.ui.empty"))

                JBScrollPane(templateList)
            }

            vSeparatorCell()

            textSeparatorCell(Bundle("reference.ui.appearance"))

            panel {
                row {
                    cell { label("quotationLabel", Bundle("reference.ui.quotation_marks.option")) }

                    row {
                        quotationGroup = buttonGroup("quotation")

                        cell { radioButton("quotationNone", Bundle("shared.option.none"), "") }
                        cell { radioButton("quotationSingle", "'") }
                        cell { radioButton("quotationDouble", "\"") }
                        cell { radioButton("quotationBacktick", "`") }
                        cell {
                            VariableLabelRadioButton(UIConstants.SIZE_TINY, MaxLengthDocumentFilter(2))
                                .withName("quotationCustom")
                                .also { customQuotation = it }
                        }
                    }
                }

                row {
                    cell { label("capitalizationLabel", Bundle("reference.ui.capitalization_option")) }

                    row {
                        capitalizationGroup = buttonGroup("capitalization")

                        cell { radioButton("capitalizationRetain", Bundle("shared.capitalization.retain"), "retain") }
                        cell { radioButton("capitalizationLower", Bundle("shared.capitalization.lower"), "lower") }
                        cell { radioButton("capitalizationUpper", Bundle("shared.capitalization.upper"), "upper") }
                        cell { radioButton("capitalizationRandom", Bundle("shared.capitalization.random"), "random") }
                        cell {
                            radioButton(
                                "capitalizationSentence",
                                Bundle("shared.capitalization.sentence"),
                                "sentence"
                            )
                        }
                        cell {
                            radioButton(
                                "capitalizationFirstLetter",
                                Bundle("shared.capitalization.first_letter"),
                                "first letter"
                            )
                        }
                    }
                }
            }

            vSeparatorCell()

            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                ArrayDecoratorEditor(originalState.arrayDecorator)
                    .also { arrayDecoratorEditor = it }
                    .rootComponent
            }

            vSpacerCell()
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

        templateListModel.removeAllElements()
        templateListModel.addAll(validTemplates)
        templateList.setSelectedValue(state.template, true)

        customQuotation.label = state.customQuotation
        quotationGroup.setValue(state.quotation)
        capitalizationGroup.setValue(state.capitalization)

        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        TemplateReference(
            templateUuid = templateList.selectedValue?.uuid,
            quotation = quotationGroup.getValue() ?: DEFAULT_QUOTATION,
            customQuotation = customQuotation.label,
            capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also {
            it.uuid = originalState.uuid
            it.templateList = originalState.templateList.copy()
        }


    override fun addChangeListener(listener: () -> Unit) {
        templateList.addListSelectionListener { listener() }
        addChangeListenerTo(
            capitalizationGroup, quotationGroup, customQuotation, arrayDecoratorEditor,
            listener = listener
        )
    }
}
