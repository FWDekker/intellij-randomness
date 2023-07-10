package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.DEFAULT_DECIMAL_SEPARATOR
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.DEFAULT_GROUPING_SEPARATOR
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.MIN_DECIMAL_COUNT
import com.fwdekker.randomness.ui.GridPanelBuilder
import com.fwdekker.randomness.ui.JDoubleSpinner
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.MinMaxLengthDocumentFilter
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.uiDesigner.core.GridConstraints
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.ChangeEvent


/**
 * Component for editing random decimal settings.
 *
 * @param scheme the scheme to edit in the component
 */
class DecimalSchemeEditor(scheme: DecimalScheme = DecimalScheme()) : StateEditor<DecimalScheme>(scheme) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = minValue.editorComponent

    private lateinit var minValue: JDoubleSpinner
    private lateinit var maxValue: JDoubleSpinner
    private lateinit var decimalCount: JIntSpinner
    private lateinit var showTrailingZeroesCheckBox: JCheckBox
    private lateinit var groupingSeparatorGroup: ButtonGroup
    private lateinit var customGroupingSeparator: VariableLabelRadioButton
    private lateinit var decimalSeparatorGroup: ButtonGroup
    private lateinit var customDecimalSeparator: VariableLabelRadioButton
    private lateinit var prefixInput: JTextField
    private lateinit var suffixInput: JTextField
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        rootComponent = GridPanelBuilder.panel {
            textSeparatorCell(Bundle("decimal.ui.value_separator"))

            panel {
                row {
                    cell { label("minValueLabel", Bundle("decimal.ui.min_value_option")) }

                    cell(constraints(fixedWidth = UIConstants.SIZE_VERY_LARGE)) {
                        JDoubleSpinner()
                            .withName("minValue")
                            .also { minValue = it }
                    }
                }

                row {
                    cell { label("maxValueLabel", Bundle("decimal.ui.max_value_option")) }

                    cell(constraints(fixedWidth = UIConstants.SIZE_VERY_LARGE)) {
                        JDoubleSpinner()
                            .withName("maxValue")
                            .also { maxValue = it }
                    }
                }

                bindSpinners(minValue, maxValue, DecimalScheme.MAX_VALUE_DIFFERENCE)

                row {
                    cell { label("decimalCountLabel", Bundle("decimal.ui.number_of_decimals_option")) }

                    cell(constraints(fixedWidth = UIConstants.SIZE_SMALL)) {
                        JIntSpinner(value = MIN_DECIMAL_COUNT, minValue = MIN_DECIMAL_COUNT)
                            .withName("decimalCount")
                            .also { decimalCount = it }
                    }
                }

                row {
                    skip()

                    cell {
                        JBCheckBox(Bundle("decimal.ui.show_trailing_zeroes"))
                            .withName("showTrailingZeroes")
                            .also {
                                decimalCount.addChangeListener(
                                    { _: ChangeEvent? ->
                                        it.isEnabled = decimalCount.value > 0
                                    }.also { it(null) }
                                )
                            }
                            .also { showTrailingZeroesCheckBox = it }
                    }
                }
            }

            vSeparatorCell()

            panel {
                row {
                    cell { label("groupingSeparatorLabel", Bundle("decimal.ui.grouping_separator.option")) }

                    row {
                        groupingSeparatorGroup = buttonGroup("groupingSeparator")

                        cell { radioButton("groupingSeparatorNone", Bundle("shared.option.none")) }
                        cell { radioButton("groupingSeparatorPeriod", ".") }
                        cell { radioButton("groupingSeparatorComma", ",") }
                        cell { radioButton("groupingSeparatorUnderscore", "_") }
                        cell {
                            VariableLabelRadioButton(UIConstants.SIZE_TINY, MaxLengthDocumentFilter(1))
                                .withName("groupingSeparatorCustom")
                                .also { customGroupingSeparator = it }
                        }
                    }
                }

                row {
                    cell { label("decimalSeparatorLabel", Bundle("decimal.ui.decimal_separator_option")) }

                    row {
                        decimalSeparatorGroup = buttonGroup("decimalSeparator")

                        cell { radioButton("decimalSeparatorComma", ",") }
                        cell { radioButton("decimalSeparatorPeriod", ".") }
                        cell {
                            VariableLabelRadioButton(UIConstants.SIZE_TINY, MinMaxLengthDocumentFilter(1, 1))
                                .withName("decimalSeparatorCustom")
                                .also { customDecimalSeparator = it }
                        }
                    }
                }
            }

            vSeparatorCell()

            panel {
                row {
                    cell { label("prefixLabel", Bundle("decimal.ui.prefix_option")) }

                    cell(constraints(fixedWidth = UIConstants.SIZE_SMALL)) {
                        JBTextField()
                            .withName("prefix")
                            .also { prefixInput = it }
                    }
                }

                row {
                    cell { label("suffixLabel", Bundle("decimal.ui.suffix_option")) }

                    cell(constraints(fixedWidth = UIConstants.SIZE_SMALL)) {
                        JBTextField()
                            .withName("suffix")
                            .also { suffixInput = it }
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


    override fun loadState(state: DecimalScheme) {
        super.loadState(state)

        minValue.value = state.minValue
        maxValue.value = state.maxValue
        decimalCount.value = state.decimalCount
        showTrailingZeroesCheckBox.isSelected = state.showTrailingZeroes
        customGroupingSeparator.label = state.customGroupingSeparator
        groupingSeparatorGroup.setValue(state.groupingSeparator)
        customDecimalSeparator.label = state.customDecimalSeparator
        decimalSeparatorGroup.setValue(state.decimalSeparator)
        prefixInput.text = state.prefix
        suffixInput.text = state.suffix
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        DecimalScheme(
            minValue = minValue.value,
            maxValue = maxValue.value,
            decimalCount = decimalCount.value,
            showTrailingZeroes = showTrailingZeroesCheckBox.isSelected,
            groupingSeparator = groupingSeparatorGroup.getValue() ?: DEFAULT_GROUPING_SEPARATOR,
            customGroupingSeparator = customGroupingSeparator.label,
            decimalSeparator = decimalSeparatorGroup.getValue() ?: DEFAULT_DECIMAL_SEPARATOR,
            customDecimalSeparator = customDecimalSeparator.label,
            prefix = prefixInput.text,
            suffix = suffixInput.text,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minValue, maxValue, decimalCount, showTrailingZeroesCheckBox, groupingSeparatorGroup,
            customGroupingSeparator, decimalSeparatorGroup, customDecimalSeparator, prefixInput, suffixInput,
            arrayDecoratorEditor,
            listener = listener
        )
}
