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
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_QUOTATION
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_TYPE
import com.intellij.ui.components.JBCheckBox
import com.intellij.uiDesigner.core.GridConstraints
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
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
            textSeparatorCell(Bundle("uuid.ui.value_separator"))

            panel {
                row {
                    cell { label("typeLabel", Bundle("uuid.type_option")) }

                    row {
                        typeGroup = buttonGroup("type")

                        cell { radioButton("type1", Bundle("uuid.type1"), "1") }
                        cell { radioButton("type4", Bundle("uuid.type4"), "4") }
                    }
                }

                row {
                    cell { label("quotationLabel", Bundle("uuid.ui.quotation_marks.option")) }

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
                    cell { label("capitalizationLabel", Bundle("uuid.ui.capitalization_option")) }

                    row {
                        capitalizationGroup = buttonGroup("capitalization")

                        cell { radioButton("capitalizationLower", Bundle("shared.capitalization.lower"), "lower") }
                        cell { radioButton("capitalizationUpper", Bundle("shared.capitalization.upper"), "upper") }
                    }
                }

                cell {
                    JBCheckBox(Bundle("uuid.add_dashes"))
                        .withName("addDashesCheckBox")
                        .also { addDashesCheckBox = it }
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
