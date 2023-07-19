package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.affix.AffixDecoratorEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.MIN_DECIMAL_COUNT
import com.fwdekker.randomness.ui.JDoubleSpinner
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.MinMaxLengthDocumentFilter
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.getCurrentText
import com.fwdekker.randomness.ui.hasValue
import com.fwdekker.randomness.ui.loadMnemonic
import com.fwdekker.randomness.ui.setFilter
import com.fwdekker.randomness.ui.withFixedWidth
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox
import javax.swing.JPanel


/**
 * Component for editing random decimal settings.
 *
 * @param scheme the scheme to edit in the component
 */
class DecimalSchemeEditor(scheme: DecimalScheme = DecimalScheme()) : StateEditor<DecimalScheme>(scheme) {
    override val rootComponent: JPanel
    override val stateComponents
        get() = super.stateComponents + affixDecoratorEditor + arrayDecoratorEditor
    override val preferredFocusedComponent
        get() = minValue.editorComponent

    private lateinit var minValue: JDoubleSpinner
    private lateinit var maxValue: JDoubleSpinner
    private lateinit var decimalCount: JIntSpinner
    private lateinit var showTrailingZeroesCheckBox: JCheckBox
    private lateinit var decimalSeparatorComboBox: ComboBox<String>
    private lateinit var groupingSeparatorEnabledCheckBox: JCheckBox
    private lateinit var groupingSeparatorComboBox: ComboBox<String>
    private lateinit var affixDecoratorEditor: AffixDecoratorEditor
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

                    checkBox(Bundle("decimal.ui.format.show_trailing_zeroes"))
                        .loadMnemonic()
                        .enabledIf(decimalCount.hasValue { it > 0 })
                        .also { it.component.name = "showTrailingZeroes" }
                        .also { showTrailingZeroesCheckBox = it.component }
                }

                row(Bundle("decimal.ui.format.decimal_separator_option")) {
                    cell(ComboBox(arrayOf(",", ".")))
                        .also { it.component.setFilter(MinMaxLengthDocumentFilter(1, 1)) }
                        .also { it.component.isEditable = true }
                        .also { it.component.name = "decimalSeparator" }
                        .also { decimalSeparatorComboBox = it.component }
                }

                row {
                    checkBox(Bundle("decimal.ui.format.grouping_separator_option"))
                        .loadMnemonic()
                        .also { it.component.name = "groupingSeparatorEnabled" }
                        .also { groupingSeparatorEnabledCheckBox = it.component }

                    cell(ComboBox(arrayOf(".", ",", "_")))
                        .enabledIf(groupingSeparatorEnabledCheckBox.selected)
                        .also { it.component.setFilter(MinMaxLengthDocumentFilter(1, 1)) }
                        .also { it.component.isEditable = true }
                        .also { it.component.name = "groupingSeparator" }
                        .also { groupingSeparatorComboBox = it.component }
                }

                row {
                    affixDecoratorEditor = AffixDecoratorEditor(
                        originalState.affixDecorator,
                        listOf("@f", "@d"),
                        enableMnemonic = true,
                    )
                    cell(affixDecoratorEditor.rootComponent)
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
        decimalSeparatorComboBox.item = state.decimalSeparator
        groupingSeparatorEnabledCheckBox.isSelected = state.groupingSeparatorEnabled
        groupingSeparatorComboBox.item = state.groupingSeparator
        affixDecoratorEditor.loadState(state.affixDecorator)
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        DecimalScheme(
            minValue = minValue.value,
            maxValue = maxValue.value,
            decimalCount = decimalCount.value,
            showTrailingZeroes = showTrailingZeroesCheckBox.isSelected,
            decimalSeparator = decimalSeparatorComboBox.getCurrentText(),
            groupingSeparatorEnabled = groupingSeparatorEnabledCheckBox.isSelected,
            groupingSeparator = groupingSeparatorComboBox.getCurrentText(),
            affixDecorator = affixDecoratorEditor.readState(),
            arrayDecorator = arrayDecoratorEditor.readState(),
        ).also { it.uuid = originalState.uuid }
}
