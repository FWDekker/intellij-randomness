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
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBScrollPane
import com.intellij.uiDesigner.core.GridConstraints
import javax.swing.ButtonGroup
import javax.swing.DefaultListModel
import javax.swing.JLabel
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
    private lateinit var capitalizationLabel: JLabel
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var customQuotation: VariableLabelRadioButton
    private lateinit var quotationGroup: ButtonGroup
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        rootComponent = GridPanelBuilder.panel {
            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                SeparatorFactory.createSeparator(Bundle("reference.ui.template_list"), null)
            }

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

            vspacer(height = 15)

            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                SeparatorFactory.createSeparator(Bundle("reference.ui.appearance"), null)
            }

            panel {
                row {
                    lateinit var quotationLabel: JLabel

                    cell {
                        JBLabel(Bundle("reference.ui.quotation_marks.option"))
                            .loadMnemonic()
                            .also { quotationLabel = it }
                    }

                    row {
                        run { quotationGroup = ButtonGroup() }

                        cell {
                            JBRadioButton(Bundle("reference.ui.quotation_marks.none"))
                                .withName("quotationNone")
                                .withActionCommand("")
                                .inGroup(quotationGroup)
                        }

                        cell {
                            JBRadioButton("'")
                                .withName("quotationSingle")
                                .inGroup(quotationGroup)
                        }

                        cell {
                            JBRadioButton(""""""")
                                .withName("quotationDouble")
                                .inGroup(quotationGroup)
                        }

                        cell {
                            JBRadioButton("`")
                                .withName("quotationBacktick")
                                .inGroup(quotationGroup)
                        }

                        cell {
                            VariableLabelRadioButton(UIConstants.WIDTH_TINY, MaxLengthDocumentFilter(2))
                                .withName("quotationCustom")
                                .also { it.addToButtonGroup(quotationGroup) }
                                .also { customQuotation = it }
                        }

                        run { quotationGroup.setLabel(quotationLabel) }
                    }
                }

                row {
                    cell {
                        JBLabel(Bundle("reference.ui.capitalization_option"))
                            .loadMnemonic()
                            .also { capitalizationLabel = it }
                    }

                    row {
                        run { capitalizationGroup = ButtonGroup() }

                        cell {
                            JBRadioButton(Bundle("shared.capitalization.retain"))
                                .withActionCommand("retain")
                                .withName("capitalizationRetain")
                                .inGroup(capitalizationGroup)
                        }

                        cell {
                            @Suppress("DialogTitleCapitalization") // Intentional
                            JBRadioButton(Bundle("shared.capitalization.lower"))
                                .withActionCommand("lower")
                                .withName("capitalizationLower")
                                .inGroup(capitalizationGroup)
                        }

                        cell {
                            JBRadioButton(Bundle("shared.capitalization.upper"))
                                .withActionCommand("upper")
                                .withName("capitalizationUpper")
                                .inGroup(capitalizationGroup)
                        }

                        cell {
                            JBRadioButton(Bundle("shared.capitalization.random"))
                                .withActionCommand("random")
                                .withName("capitalizationRandom")
                                .inGroup(capitalizationGroup)
                        }

                        cell {
                            JBRadioButton(Bundle("shared.capitalization.sentence"))
                                .withActionCommand("sentence")
                                .withName("capitalizationSentence")
                                .inGroup(capitalizationGroup)
                        }

                        cell {
                            @Suppress("DialogTitleCapitalization") // Intentional
                            JBRadioButton(Bundle("shared.capitalization.first_letter"))
                                .withActionCommand("first letter")
                                .withName("capitalizationFirstLetter")
                                .inGroup(capitalizationGroup)
                        }

                        run { capitalizationGroup.setLabel(capitalizationLabel) }
                    }
                }
            }

            vspacer(height = 15)

            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                ArrayDecoratorEditor(originalState.arrayDecorator)
                    .also { arrayDecoratorEditor = it }
                    .rootComponent
            }

            vspacer()
        }

        capitalizationGroup.setLabel(capitalizationLabel)

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
