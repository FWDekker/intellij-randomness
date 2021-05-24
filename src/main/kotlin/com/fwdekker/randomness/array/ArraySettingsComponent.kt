package com.fwdekker.randomness.array

import com.fwdekker.randomness.DummyInsertArrayAction
import com.fwdekker.randomness.SchemesPanel
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.SettingsComponentListener
import com.fwdekker.randomness.array.ArrayScheme.Companion.DEFAULT_BRACKETS
import com.fwdekker.randomness.array.ArrayScheme.Companion.DEFAULT_SEPARATOR
import com.fwdekker.randomness.array.ArraySettings.Companion.DEFAULT_SCHEMES
import com.fwdekker.randomness.array.ArraySettings.Companion.default
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.event.ChangeEvent


/**
 * Component for settings of random array generation.
 *
 * @param settings the settings to edit in the component
 *
 * @see ArraySettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class ArraySettingsComponent(settings: ArraySettings = default) :
    SettingsComponent<ArraySettings, ArrayScheme>(settings) {
    override lateinit var unsavedSettings: ArraySettings
    override lateinit var schemesPanel: SchemesPanel<ArrayScheme>

    private lateinit var contentPane: JPanel
    private lateinit var previewPanelHolder: PreviewPanel
    private lateinit var previewPanel: JPanel
    private lateinit var countSpinner: JIntSpinner
    private lateinit var bracketsGroup: ButtonGroup
    private lateinit var separatorGroup: ButtonGroup
    private lateinit var newlineSeparatorButton: JRadioButton
    private lateinit var spaceAfterSeparatorCheckBox: JCheckBox

    override val rootPane get() = contentPane


    init {
        loadSettings()

        newlineSeparatorButton.addChangeListener {
            spaceAfterSeparatorCheckBox.isEnabled = !newlineSeparatorButton.isSelected
        }
        newlineSeparatorButton.changeListeners.forEach { it.stateChanged(ChangeEvent(newlineSeparatorButton)) }

        previewPanelHolder.updatePreviewOnUpdateOf(countSpinner, bracketsGroup, separatorGroup)
        previewPanelHolder.updatePreviewOnUpdateOf(spaceAfterSeparatorCheckBox)
        previewPanelHolder.updatePreview()
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        unsavedSettings = ArraySettings()
        schemesPanel = ArraySchemesPanel(unsavedSettings)
            .also { it.addListener(SettingsComponentListener(this)) }

        previewPanelHolder = PreviewPanel {
            val scheme = ArrayScheme().also { saveScheme(it) }
            DummyInsertArrayAction(scheme) { it.nextInt(previewMin, previewMax + 1).toString() }
        }
        previewPanel = previewPanelHolder.rootPane

        countSpinner = JIntSpinner(value = 1, minValue = 1, description = "count")
    }

    override fun loadScheme(scheme: ArrayScheme) {
        countSpinner.value = scheme.count
        bracketsGroup.setValue(scheme.brackets)
        separatorGroup.setValue(scheme.separator)
        spaceAfterSeparatorCheckBox.isSelected = scheme.isSpaceAfterSeparator
    }

    override fun saveScheme(scheme: ArrayScheme) {
        scheme.count = countSpinner.value
        scheme.brackets = bracketsGroup.getValue() ?: DEFAULT_BRACKETS
        scheme.separator = separatorGroup.getValue() ?: DEFAULT_SEPARATOR
        scheme.isSpaceAfterSeparator = spaceAfterSeparatorCheckBox.isSelected
    }

    override fun doValidate() = countSpinner.validateValue()


    /**
     * A panel to select schemes from.
     *
     * @param settings the settings model backing up the panel
     */
    private class ArraySchemesPanel(settings: ArraySettings) : SchemesPanel<ArrayScheme>(settings) {
        override val type: Class<ArrayScheme>
            get() = ArrayScheme::class.java

        override fun createDefaultInstances() = DEFAULT_SCHEMES
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The minimal value in the preview (inclusive).
         */
        const val previewMin = 1

        /**
         * The maximal value in the preview (inclusive).
         */
        const val previewMax = 20
    }
}
