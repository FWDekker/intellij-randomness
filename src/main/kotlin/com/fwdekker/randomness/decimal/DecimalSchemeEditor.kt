package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.SchemeEditor
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.DEFAULT_DECIMAL_SEPARATOR
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.DEFAULT_GROUPING_SEPARATOR
import com.fwdekker.randomness.ui.JDoubleSpinner
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
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
@Suppress("LateinitUsage") // Initialized by scene builder
class DecimalSchemeEditor(scheme: DecimalScheme = DecimalScheme()) : SchemeEditor<DecimalScheme>() {
    override lateinit var rootPane: JPanel private set
    private lateinit var valueRange: JSpinnerRange
    private lateinit var minValue: JDoubleSpinner
    private lateinit var maxValue: JDoubleSpinner
    private lateinit var decimalCount: JIntSpinner
    private lateinit var showTrailingZeroesCheckBox: JCheckBox
    private lateinit var groupingSeparatorGroup: ButtonGroup
    private lateinit var decimalSeparatorGroup: ButtonGroup
    private lateinit var prefixInput: JTextField
    private lateinit var suffixInput: JTextField


    init {
        loadScheme(scheme)

        decimalCount.addChangeListener { showTrailingZeroesCheckBox.isEnabled = decimalCount.value > 0 }
        decimalCount.changeListeners.forEach { it.stateChanged(ChangeEvent(decimalCount)) }
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        minValue = JDoubleSpinner(description = "minimum value")
        maxValue = JDoubleSpinner(description = "maximum value")
        valueRange = JSpinnerRange(minValue, maxValue, MAX_VALUE_RANGE, name = "value")
        decimalCount = JIntSpinner(0, 0, description = "decimal count")
    }

    override fun loadScheme(scheme: DecimalScheme) =
        scheme.also {
            minValue.value = it.minValue
            maxValue.value = it.maxValue
            decimalCount.value = it.decimalCount
            showTrailingZeroesCheckBox.isSelected = it.showTrailingZeroes
            groupingSeparatorGroup.setValue(it.groupingSeparator)
            decimalSeparatorGroup.setValue(it.decimalSeparator)
            prefixInput.text = it.prefix
            suffixInput.text = it.suffix
        }.let {}

    override fun saveScheme() =
        DecimalScheme(
            minValue = minValue.value,
            maxValue = maxValue.value,
            decimalCount = decimalCount.value,
            showTrailingZeroes = showTrailingZeroesCheckBox.isSelected,
            groupingSeparator = groupingSeparatorGroup.getValue() ?: DEFAULT_GROUPING_SEPARATOR,
            decimalSeparator = decimalSeparatorGroup.getValue() ?: DEFAULT_DECIMAL_SEPARATOR,
            prefix = prefixInput.text,
            suffix = suffixInput.text
        )

    override fun doValidate() =
        minValue.validateValue()
            ?: maxValue.validateValue()
            ?: valueRange.validateValue()
            ?: decimalCount.validateValue()


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minValue, maxValue, decimalCount, showTrailingZeroesCheckBox, groupingSeparatorGroup, decimalSeparatorGroup,
            prefixInput, suffixInput,
            listener = listener
        )


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The maximum difference between the minimum and maximum values that can be generated.
         */
        const val MAX_VALUE_RANGE = 1E53
    }
}
