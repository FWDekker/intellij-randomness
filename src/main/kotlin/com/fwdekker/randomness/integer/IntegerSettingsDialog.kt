package com.fwdekker.randomness.integer

import com.fwdekker.randomness.SettingsDialog
import com.fwdekker.randomness.ui.ButtonGroupHelper
import com.fwdekker.randomness.ui.JLongSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import java.util.function.Consumer
import javax.swing.ButtonGroup
import javax.swing.JPanel


/**
 * Dialog for settings of random integer generation.
 */
class IntegerSettingsDialog(settings: IntegerSettings = IntegerSettings.default) :
    SettingsDialog<IntegerSettings>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var valueRange: JSpinnerRange
    private lateinit var minValue: JLongSpinner
    private lateinit var maxValue: JLongSpinner
    private lateinit var base: JLongSpinner
    private lateinit var groupingSeparatorGroup: ButtonGroup


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
        minValue = JLongSpinner()
        maxValue = JLongSpinner()
        base = JLongSpinner(
            IntegerSettings.DECIMAL_BASE.toLong(),
            IntegerSettings.MIN_BASE.toLong(),
            IntegerSettings.MAX_BASE.toLong()
        )
        valueRange = JSpinnerRange(minValue, maxValue, Long.MAX_VALUE.toDouble())

        base.addChangeListener { event ->
            val value = (event.source as JLongSpinner).value
            val enabled = value == IntegerSettings.DECIMAL_BASE.toLong()
            ButtonGroupHelper.forEach(groupingSeparatorGroup, Consumer { button -> button.isEnabled = enabled })
        }
    }

    override fun doValidate() =
        null
            ?: minValue.validateValue()
            ?: maxValue.validateValue()
            ?: base.validateValue()
            ?: valueRange.validateValue()


    override fun loadSettings(settings: IntegerSettings) {
        minValue.value = settings.minValue
        maxValue.value = settings.maxValue
        base.value = settings.base.toLong()
        ButtonGroupHelper.setValue(groupingSeparatorGroup, settings.groupingSeparator.toString())
    }

    override fun saveSettings(settings: IntegerSettings) {
        settings.minValue = minValue.value
        settings.maxValue = maxValue.value
        settings.base = base.value.toInt()
        settings.setGroupingSeparator(ButtonGroupHelper.getValue(groupingSeparatorGroup)!!)
    }
}
