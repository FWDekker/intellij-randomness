package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.DEFAULT_DECIMAL_SEPARATOR
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.DEFAULT_GROUPING_SEPARATOR
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.MIN_DECIMAL_COUNT
import com.fwdekker.randomness.ui.JDoubleSpinner
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.MinMaxLengthDocumentFilter
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.add
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.hasValue
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.ui.withFixedWidth
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.Label
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.EMPTY_LABEL
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.util.ui.DialogUtil
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextField


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
        rootComponent = panel {
            group(Bundle("decimal.ui.value.header")) {
                row(Bundle("decimal.ui.value.min_option")) {
                    cell(JDoubleSpinner())
                        .withFixedWidth(UIConstants.SIZE_VERY_LARGE)
                        .also { it.component.name = "minValue" }
                        .also { minValue = it.component }
                }

                row(Bundle("decimal.ui.value.max_option")) {
                    cell(JDoubleSpinner())
                        .withFixedWidth(UIConstants.SIZE_VERY_LARGE)
                        .also { it.component.name = "maxValue" }
                        .also { maxValue = it.component }
                }

                bindSpinners(minValue, maxValue, DecimalScheme.MAX_VALUE_DIFFERENCE)
            }

            group(Bundle("decimal.ui.format.header")) {
                row(Bundle("decimal.ui.format.number_of_decimals_option")) {
                    cell(JIntSpinner(value = MIN_DECIMAL_COUNT, minValue = MIN_DECIMAL_COUNT))
                        .withFixedWidth(UIConstants.SIZE_SMALL)
                        .also { it.component.name = "decimalCount" }
                        .also { decimalCount = it.component }
                }

                row(EMPTY_LABEL) {
                    checkBox(Bundle("decimal.ui.format.show_trailing_zeroes"))
                        .also { DialogUtil.registerMnemonic(it.component, '&') }
                        .enabledIf(decimalCount.hasValue { it > 0 })
                        .also { it.component.name = "showTrailingZeroes" }
                        .also { showTrailingZeroesCheckBox = it.component }
                }.bottomGap(BottomGap.SMALL)

                val groupingSeparatorLabel = Label(Bundle("decimal.ui.format.grouping_separator_option"))
                row(groupingSeparatorLabel) {
                    groupingSeparatorGroup = ButtonGroup()

                    cell(JBRadioButton(Bundle("shared.option.none")))
                        .also { it.component.actionCommand = "" }
                        .also { it.component.name = "groupingSeparatorNone" }
                        .also { groupingSeparatorGroup.add(it.component) }
                    cell(JBRadioButton("."))
                        .also { it.component.name = "groupingSeparatorPeriod" }
                        .also { groupingSeparatorGroup.add(it.component) }
                    cell(JBRadioButton(","))
                        .also { it.component.name = "groupingSeparatorComma" }
                        .also { groupingSeparatorGroup.add(it.component) }
                    cell(JBRadioButton("_"))
                        .also { it.component.name = "groupingSeparatorUnderscore" }
                        .also { groupingSeparatorGroup.add(it.component) }
                    cell(VariableLabelRadioButton(UIConstants.SIZE_TINY, MaxLengthDocumentFilter(1)))
                        .also { it.component.name = "groupingSeparatorCustom" }
                        .also { groupingSeparatorGroup.add(it.component) }
                        .also { customGroupingSeparator = it.component }

                    groupingSeparatorGroup.setLabel(groupingSeparatorLabel)
                }

                val decimalSeparatorLabel = Label(Bundle("decimal.ui.format.decimal_separator_option"))
                row(decimalSeparatorLabel) {
                    decimalSeparatorGroup = ButtonGroup()

                    cell(JBRadioButton(","))
                        .also { it.component.name = "decimalSeparatorComma" }
                        .also { decimalSeparatorGroup.add(it.component) }
                    cell(JBRadioButton("."))
                        .also { it.component.name = "decimalSeparatorPeriod" }
                        .also { decimalSeparatorGroup.add(it.component) }
                    cell(VariableLabelRadioButton(UIConstants.SIZE_TINY, MinMaxLengthDocumentFilter(1, 1)))
                        .also { it.component.name = "decimalSeparatorCustom" }
                        .also { decimalSeparatorGroup.add(it.component) }
                        .also { customDecimalSeparator = it.component }

                    decimalSeparatorGroup.setLabel(decimalSeparatorLabel)
                }
            }

            group(Bundle("decimal.ui.affixes.header")) {
                row(Bundle("decimal.ui.affixes.prefix_option")) {
                    textField()
                        .withFixedWidth(UIConstants.SIZE_SMALL)
                        .also { it.component.name = "prefix" }
                        .also { prefixInput = it.component }
                }

                row(Bundle("decimal.ui.affixes.suffix_option")) {
                    textField()
                        .withFixedWidth(UIConstants.SIZE_SMALL)
                        .also { it.component.name = "suffix" }
                        .also { suffixInput = it.component }
                }
            }

            row {
                arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
                cell(arrayDecoratorEditor.rootComponent).horizontalAlign(HorizontalAlign.FILL)
            }
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
