package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.decimal.DecimalSettings.Companion.default
import com.fwdekker.randomness.ui.JDoubleSpinner
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.event.ChangeEvent


/**
 * Component for settings of random decimal generation.
 *
 * @see DecimalSettings
 * @see DecimalSettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class DecimalSettingsComponent(settings: DecimalSettings = default) : SettingsComponent<DecimalSettings>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var previewPanelHolder: PreviewPanel<DecimalInsertAction>
    private lateinit var previewPanel: JPanel
    private lateinit var valueRange: JSpinnerRange
    private lateinit var minValue: JDoubleSpinner
    private lateinit var maxValue: JDoubleSpinner
    private lateinit var decimalCount: JIntSpinner
    private lateinit var showTrailingZeroesCheckBox: JCheckBox
    private lateinit var groupingSeparatorGroup: ButtonGroup
    private lateinit var decimalSeparatorGroup: ButtonGroup

    override val rootPane get() = contentPane


    init {
        loadSettings()

        previewPanelHolder.updatePreviewOnUpdateOf(
            minValue, maxValue, decimalCount, showTrailingZeroesCheckBox, groupingSeparatorGroup, decimalSeparatorGroup)
        previewPanelHolder.updatePreview()
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        previewPanelHolder = PreviewPanel { DecimalInsertAction(DecimalSettings().also { saveSettings(it) }) }
        previewPanel = previewPanelHolder.rootPanel

        minValue = JDoubleSpinner()
        maxValue = JDoubleSpinner()
        valueRange = JSpinnerRange(minValue, maxValue, name = "value")

        decimalCount = JIntSpinner(0, 0)
        decimalCount.addChangeListener { event: ChangeEvent ->
            val value = (event.source as JIntSpinner).value
            showTrailingZeroesCheckBox.isEnabled = value > 0
        }
    }

    override fun loadSettings(settings: DecimalSettings) {
        minValue.value = settings.minValue
        maxValue.value = settings.maxValue
        decimalCount.value = settings.decimalCount
        showTrailingZeroesCheckBox.isSelected = settings.showTrailingZeroes
        groupingSeparatorGroup.setValue(settings.groupingSeparator)
        decimalSeparatorGroup.setValue(settings.decimalSeparator)
    }

    override fun saveSettings(settings: DecimalSettings) {
        settings.minValue = minValue.value
        settings.maxValue = maxValue.value
        settings.decimalCount = decimalCount.value
        settings.showTrailingZeroes = showTrailingZeroesCheckBox.isSelected
        settings.safeSetGroupingSeparator(groupingSeparatorGroup.getValue())
        settings.safeSetDecimalSeparator(decimalSeparatorGroup.getValue())
    }

    override fun doValidate() =
        minValue.validateValue()
            ?: maxValue.validateValue()
            ?: valueRange.validateValue()
            ?: decimalCount.validateValue()
}
