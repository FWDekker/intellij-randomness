package com.fwdekker.randomness.integer

import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.integer.IntegerSettings.Companion.default
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JLongSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.forEach
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.event.ChangeEvent


/**
 * Component for settings of random integer generation.
 *
 * @see IntegerSettings
 * @see IntegerSettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class IntegerSettingsComponent(settings: IntegerSettings = default) : SettingsComponent<IntegerSettings>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var previewPanelHolder: PreviewPanel<IntegerInsertAction>
    private lateinit var previewPanel: JPanel
    private lateinit var valueRange: JSpinnerRange
    private lateinit var minValue: JLongSpinner
    private lateinit var maxValue: JLongSpinner
    private lateinit var base: JIntSpinner
    private lateinit var groupingSeparatorGroup: ButtonGroup

    override val rootPane get() = contentPane


    init {
        loadSettings()

        base.addChangeListener {
            groupingSeparatorGroup.forEach { it.isEnabled = base.value == IntegerSettings.DECIMAL_BASE }
        }
        base.changeListeners.forEach { it.stateChanged(ChangeEvent(base)) }

        previewPanelHolder.updatePreviewOnUpdateOf(minValue, maxValue, base, groupingSeparatorGroup)
        previewPanelHolder.updatePreview()
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        previewPanelHolder = PreviewPanel { IntegerInsertAction(IntegerSettings().also { saveSettings(it) }) }
        previewPanel = previewPanelHolder.rootPane

        minValue = JLongSpinner(description = "minimum value")
        maxValue = JLongSpinner(description = "maximum value")
        base = JIntSpinner(IntegerSettings.DECIMAL_BASE, IntegerSettings.MIN_BASE, IntegerSettings.MAX_BASE)
        valueRange = JSpinnerRange(minValue, maxValue, Long.MAX_VALUE.toDouble(), "value")
    }

    override fun loadSettings(settings: IntegerSettings) {
        minValue.value = settings.minValue
        maxValue.value = settings.maxValue
        base.value = settings.base
        groupingSeparatorGroup.setValue(settings.groupingSeparator)
    }

    override fun saveSettings(settings: IntegerSettings) {
        settings.minValue = minValue.value
        settings.maxValue = maxValue.value
        settings.base = base.value
        settings.safeSetGroupingSeparator(groupingSeparatorGroup.getValue())
    }

    override fun doValidate() = minValue.validateValue()
        ?: maxValue.validateValue()
        ?: base.validateValue()
        ?: valueRange.validateValue()
}
