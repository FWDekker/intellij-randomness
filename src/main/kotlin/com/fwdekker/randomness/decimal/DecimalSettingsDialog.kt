package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.SettingsDialog
import com.fwdekker.randomness.ui.ButtonGroupHelper
import com.fwdekker.randomness.ui.JDoubleSpinner
import com.fwdekker.randomness.ui.JLongSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import javax.swing.ButtonGroup
import javax.swing.JPanel


/**
 * Dialog for settings of random decimal generation.
 *
 * @param settings the settings to manipulate with this dialog. Defaults to [DecimalSettings.default]
 */
class DecimalSettingsDialog(settings: DecimalSettings = DecimalSettings.default) :
    SettingsDialog<DecimalSettings>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var valueRange: JSpinnerRange
    private lateinit var minValue: JDoubleSpinner
    private lateinit var maxValue: JDoubleSpinner
    private lateinit var decimalCount: JLongSpinner
    private lateinit var groupingSeparatorGroup: ButtonGroup
    private lateinit var decimalSeparatorGroup: ButtonGroup


    init {
        init()
        loadSettings()
    }


    override fun createCenterPanel() = contentPane

    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    private fun createUIComponents() {
        minValue = JDoubleSpinner()
        maxValue = JDoubleSpinner()
        valueRange = JSpinnerRange(minValue, maxValue)
        decimalCount = JLongSpinner(0, 0, Integer.MAX_VALUE.toLong())
    }

    override fun doValidate() =
        null
            ?: minValue.validateValue()
            ?: maxValue.validateValue()
            ?: valueRange.validateValue()
            ?: decimalCount.validateValue()


    override fun loadSettings(settings: DecimalSettings) {
        minValue.value = settings.minValue
        maxValue.value = settings.maxValue
        decimalCount.setValue(settings.decimalCount)
        ButtonGroupHelper.setValue(groupingSeparatorGroup, settings.groupingSeparator.toString())
        ButtonGroupHelper.setValue(decimalSeparatorGroup, settings.decimalSeparator.toString())
    }

    override fun saveSettings(settings: DecimalSettings) {
        settings.minValue = minValue.value
        settings.maxValue = maxValue.value
        settings.decimalCount = Math.toIntExact(decimalCount.value)
        settings.setGroupingSeparator(ButtonGroupHelper.getValue(groupingSeparatorGroup)!!) // TODO Remove !!
        settings.setDecimalSeparator(ButtonGroupHelper.getValue(decimalSeparatorGroup)!!)
    }
}
