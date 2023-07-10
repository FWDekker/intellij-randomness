package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.GridPanelBuilder
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.buttons
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_QUOTATION
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_TYPE
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.uiDesigner.core.GridConstraints
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel


/**
 * Component for editing random UUID settings.
 *
 * @param scheme the scheme to edit in the component
 */
class UuidSchemeEditor(scheme: UuidScheme = UuidScheme()) : StateEditor<UuidScheme>(scheme) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = typeGroup.buttons().firstOrNull { it.isSelected }

    private lateinit var typeGroup: ButtonGroup
    private lateinit var quotationGroup: ButtonGroup
    private lateinit var customQuotation: VariableLabelRadioButton
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var addDashesCheckBox: JCheckBox
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        rootComponent = GridPanelBuilder.panel {
            textSeparator(Bundle("uuid.ui.value_separator"))

            panel {
                row {
                    lateinit var typeLabel: JLabel

                    cell {
                        JBLabel(Bundle("uuid.type_option"))
                            .loadMnemonic()
                            .also { typeLabel = it }
                    }

                    row {
                        run { typeGroup = ButtonGroup() }

                        cell {
                            JBRadioButton(Bundle("uuid.type1"))
                                .withActionCommand("1")
                                .withName("type1")
                                .inGroup(typeGroup)
                        }

                        cell {
                            JBRadioButton(Bundle("uuid.type4"))
                                .withActionCommand("4")
                                .withName("type4")
                                .inGroup(typeGroup)
                        }

                        run { typeGroup.setLabel(typeLabel) }
                    }
                }

                row {
                    lateinit var quotationLabel: JLabel

                    cell {
                        JBLabel(Bundle("uuid.ui.quotation_marks.option"))
                            .loadMnemonic()
                            .also { quotationLabel = it }
                    }

                    row {
                        run { quotationGroup = ButtonGroup() }

                        cell {
                            JBRadioButton(Bundle("uuid.ui.quotation_marks.none"))
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
                            VariableLabelRadioButton(UIConstants.SIZE_TINY, MaxLengthDocumentFilter(2))
                                .withName("quotationCustom")
                                .also { it.addToButtonGroup(quotationGroup) }
                                .also { customQuotation = it }
                        }

                        run { quotationGroup.setLabel(quotationLabel) }
                    }
                }

                row {
                    lateinit var capitalizationLabel: JLabel

                    cell {
                        JBLabel(Bundle("uuid.ui.capitalization_option"))
                            .loadMnemonic()
                            .also { capitalizationLabel = it }
                    }

                    row {
                        run { capitalizationGroup = ButtonGroup() }

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

                        run { capitalizationGroup.setLabel(capitalizationLabel) }
                    }
                }

                cell {
                    JBCheckBox(Bundle("uuid.add_dashes"))
                        .withName("addDashesCheckBox")
                        .loadMnemonic()
                        .also { addDashesCheckBox = it }
                }
            }

            vSeparator()

            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                ArrayDecoratorEditor(originalState.arrayDecorator)
                    .also { arrayDecoratorEditor = it }
                    .rootComponent
            }

            vSpacer()
        }

        loadState()
    }


    override fun loadState(state: UuidScheme) {
        super.loadState(state)

        typeGroup.setValue(state.type.toString())
        customQuotation.label = state.customQuotation
        quotationGroup.setValue(state.quotation)
        capitalizationGroup.setValue(state.capitalization)
        addDashesCheckBox.isSelected = state.addDashes
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState(): UuidScheme =
        UuidScheme(
            type = typeGroup.getValue()?.toInt() ?: DEFAULT_TYPE,
            quotation = quotationGroup.getValue() ?: DEFAULT_QUOTATION,
            customQuotation = customQuotation.label,
            capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION,
            addDashes = addDashesCheckBox.isSelected,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            typeGroup, quotationGroup, customQuotation, capitalizationGroup, addDashesCheckBox, arrayDecoratorEditor,
            listener = listener
        )
}
