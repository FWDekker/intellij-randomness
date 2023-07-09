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
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.uiDesigner.core.GridConstraints
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JLabel
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
            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                SeparatorFactory.createSeparator(Bundle("decimal.ui.value_separator"), null)
            }

            panel {
                row {
                    lateinit var minCountLabel: JLabel

                    cell {
                        JBLabel(Bundle("decimal.ui.min_value_option"))
                            .also { minCountLabel = it }
                    }

                    cell {
                        JDoubleSpinner()
                            .withName("minValue")
                            .forceWidth(UIConstants.WIDTH_VERY_LARGE)
                            .setLabel(minCountLabel)
                            .also { minValue = it }
                    }
                }

                row {
                    lateinit var maxCountLabel: JLabel

                    cell {
                        JBLabel(Bundle("decimal.ui.max_value_option"))
                            .also { maxCountLabel = it }
                    }

                    cell {
                        JDoubleSpinner()
                            .withName("maxValue")
                            .forceWidth(UIConstants.WIDTH_VERY_LARGE)
                            .setLabel(maxCountLabel)
                            .also { maxValue = it }
                    }

                    run { bindSpinners(minValue, maxValue, DecimalScheme.MAX_VALUE_DIFFERENCE) }
                }

                row {
                    lateinit var decimalCountLabel: JLabel

                    cell {
                        JBLabel(Bundle("decimal.ui.number_of_decimals_option"))
                            .also { decimalCountLabel = it }
                    }

                    cell {
                        JIntSpinner(value = MIN_DECIMAL_COUNT, minValue = MIN_DECIMAL_COUNT)
                            .withName("decimalCount")
                            .forceWidth(UIConstants.WIDTH_SMALL)
                            .setLabel(decimalCountLabel)
                            .also { decimalCount = it }
                    }
                }

                row {
                    skip()

                    cell {
                        JBCheckBox(Bundle("decimal.ui.show_trailing_zeroes"))
                            .withName("showTrailingZeroes")
                            .loadMnemonic()
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

            vspacer(height = 15)

            panel {
                row {
                    lateinit var groupingSeparatorLabel: JLabel

                    cell {
                        JBLabel(Bundle("decimal.ui.grouping_separator.option"))
                            .loadMnemonic()
                            .also { groupingSeparatorLabel = it }
                    }

                    row {
                        run { groupingSeparatorGroup = ButtonGroup() }

                        cell {
                            JBRadioButton(Bundle("decimal.ui.grouping_separator.none"))
                                .withName("groupingSeparatorNone")
                                .withActionCommand("")
                                .inGroup(groupingSeparatorGroup)
                        }

                        cell {
                            JBRadioButton(".")
                                .withName("groupingSeparatorPeriod")
                                .inGroup(groupingSeparatorGroup)
                        }

                        cell {
                            JBRadioButton(",")
                                .withName("groupingSeparatorComma")
                                .inGroup(groupingSeparatorGroup)
                        }

                        cell {
                            JBRadioButton("_")
                                .withName("groupingSeparatorUnderscore")
                                .inGroup(groupingSeparatorGroup)
                        }

                        cell {
                            VariableLabelRadioButton(UIConstants.WIDTH_TINY, MaxLengthDocumentFilter(1))
                                .withName("groupingSeparatorCustom")
                                .also { it.addToButtonGroup(groupingSeparatorGroup) }
                                .also { customGroupingSeparator = it }
                        }

                        run { groupingSeparatorGroup.setLabel(groupingSeparatorLabel) }
                    }
                }

                row {
                    lateinit var decimalSeparatorLabel: JLabel

                    cell {
                        JBLabel(Bundle("decimal.ui.decimal_separator_option"))
                            .loadMnemonic()
                            .also { decimalSeparatorLabel = it }
                    }

                    row {
                        run { decimalSeparatorGroup = ButtonGroup() }

                        cell {
                            JBRadioButton(",")
                                .withName("decimalSeparatorComma")
                                .inGroup(decimalSeparatorGroup)
                        }

                        cell {
                            JBRadioButton(".")
                                .withName("decimalSeparatorPeriod")
                                .inGroup(decimalSeparatorGroup)
                        }

                        cell {
                            VariableLabelRadioButton(UIConstants.WIDTH_TINY, MinMaxLengthDocumentFilter(1, 1))
                                .withName("decimalSeparatorCustom")
                                .also { it.addToButtonGroup(decimalSeparatorGroup) }
                                .also { customDecimalSeparator = it }
                        }

                        run { decimalSeparatorGroup.setLabel(decimalSeparatorLabel) }
                    }
                }
            }

            vspacer(height = 15)

            panel {
                row {
                    lateinit var prefixLabel: JLabel

                    cell {
                        JBLabel(Bundle("decimal.ui.prefix_option"))
                            .also { prefixLabel = it }
                    }

                    cell {
                        JBTextField()
                            .withName("prefix")
                            .forceWidth(UIConstants.WIDTH_SMALL)
                            .setLabel(prefixLabel)
                            .also { prefixInput = it }
                    }
                }

                row {
                    lateinit var suffixLabel: JLabel

                    cell {
                        JBLabel(Bundle("decimal.ui.suffix_option"))
                            .also { suffixLabel = it }
                    }

                    cell {
                        JBTextField()
                            .withName("suffix")
                            .forceWidth(UIConstants.WIDTH_SMALL)
                            .setLabel(suffixLabel)
                            .also { suffixInput = it }
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
