package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.SchemesPanel
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.SettingsComponentListener
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
 * @param settings the settings to edit in the component
 *
 * @see DecimalSettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class DecimalSettingsComponent(settings: DecimalSettings = default) :
    SettingsComponent<DecimalSettings, DecimalScheme>(settings) {
    override lateinit var unsavedSettings: DecimalSettings
    override lateinit var schemesPanel: SchemesPanel<DecimalScheme>

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

        decimalCount.addChangeListener { showTrailingZeroesCheckBox.isEnabled = decimalCount.value > 0 }
        decimalCount.changeListeners.forEach { it.stateChanged(ChangeEvent(decimalCount)) }

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
        unsavedSettings = DecimalSettings()
        schemesPanel = DecimalSchemesPanel(unsavedSettings)
            .also { it.addListener(SettingsComponentListener(this)) }

        previewPanelHolder = PreviewPanel { DecimalInsertAction(DecimalScheme().also { saveScheme(it) }) }
        previewPanel = previewPanelHolder.rootPane

        minValue = JDoubleSpinner(description = "minimum value")
        maxValue = JDoubleSpinner(description = "maximum value")
        valueRange = JSpinnerRange(minValue, maxValue, name = "value")

        decimalCount = JIntSpinner(0, 0, description = "decimal count")
    }

    override fun loadScheme(scheme: DecimalScheme) {
        minValue.value = scheme.minValue
        maxValue.value = scheme.maxValue
        decimalCount.value = scheme.decimalCount
        showTrailingZeroesCheckBox.isSelected = scheme.showTrailingZeroes
        groupingSeparatorGroup.setValue(scheme.groupingSeparator)
        decimalSeparatorGroup.setValue(scheme.decimalSeparator)
    }

    override fun saveScheme(scheme: DecimalScheme) {
        scheme.minValue = minValue.value
        scheme.maxValue = maxValue.value
        scheme.decimalCount = decimalCount.value
        scheme.showTrailingZeroes = showTrailingZeroesCheckBox.isSelected
        scheme.safeSetGroupingSeparator(groupingSeparatorGroup.getValue())
        scheme.safeSetDecimalSeparator(decimalSeparatorGroup.getValue())
    }

    override fun doValidate() =
        minValue.validateValue()
            ?: maxValue.validateValue()
            ?: valueRange.validateValue()
            ?: decimalCount.validateValue()


    /**
     * A panel to select schemes from.
     *
     * @param settings the settings model backing up the panel
     */
    private class DecimalSchemesPanel(settings: DecimalSettings) : SchemesPanel<DecimalScheme>(settings) {
        override val type: Class<DecimalScheme>
            get() = DecimalScheme::class.java

        override fun createDefaultInstance() = DecimalScheme()
    }
}
