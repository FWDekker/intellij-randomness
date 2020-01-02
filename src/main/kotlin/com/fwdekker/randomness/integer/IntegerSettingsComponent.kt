package com.fwdekker.randomness.integer

import com.fwdekker.randomness.SchemesPanel
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.SettingsComponentListener
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
 * @param settings the settings to edit in the component
 *
 * @see IntegerSettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class IntegerSettingsComponent(settings: IntegerSettings = default) :
    SettingsComponent<IntegerSettings, IntegerScheme>(settings) {
    override lateinit var unsavedSettings: IntegerSettings
    override lateinit var schemesPanel: SchemesPanel<IntegerScheme>

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
            groupingSeparatorGroup.forEach { it.isEnabled = base.value == IntegerScheme.DECIMAL_BASE }
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
        unsavedSettings = IntegerSettings()
        schemesPanel = IntegerSchemesPanel(unsavedSettings)
            .also { it.addListener(SettingsComponentListener(this)) }

        previewPanelHolder = PreviewPanel { IntegerInsertAction(IntegerScheme().also { saveScheme(it) }) }
        previewPanel = previewPanelHolder.rootPane

        minValue = JLongSpinner(description = "minimum value")
        maxValue = JLongSpinner(description = "maximum value")
        base = JIntSpinner(
            IntegerScheme.DECIMAL_BASE,
            IntegerScheme.MIN_BASE, IntegerScheme.MAX_BASE,
            description = "base"
        )
        valueRange = JSpinnerRange(minValue, maxValue, Long.MAX_VALUE.toDouble(), "value")
    }

    override fun loadScheme(scheme: IntegerScheme) {
        minValue.value = scheme.minValue
        maxValue.value = scheme.maxValue
        base.value = scheme.base
        groupingSeparatorGroup.setValue(scheme.groupingSeparator)
    }

    override fun saveScheme(scheme: IntegerScheme) {
        scheme.minValue = minValue.value
        scheme.maxValue = maxValue.value
        scheme.base = base.value
        scheme.safeSetGroupingSeparator(groupingSeparatorGroup.getValue())
    }

    override fun doValidate() = minValue.validateValue()
        ?: maxValue.validateValue()
        ?: base.validateValue()
        ?: valueRange.validateValue()


    /**
     * A panel to select schemes from.
     *
     * @param settings the settings model backing up the panel
     */
    private class IntegerSchemesPanel(settings: IntegerSettings) : SchemesPanel<IntegerScheme>(settings) {
        override val type: Class<IntegerScheme>
            get() = IntegerScheme::class.java

        override fun createDefaultInstance() = IntegerScheme()
    }
}
