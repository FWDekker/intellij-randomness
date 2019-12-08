package com.fwdekker.randomness.array

import com.fwdekker.randomness.DummyInsertArrayAction
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.array.ArraySettings.Companion.default
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel


/**
 * Component for settings of random array generation.
 *
 * @see ArraySettings
 * @see ArraySettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class ArraySettingsComponent(settings: ArraySettings = default) : SettingsComponent<ArraySettings>(settings) {
    companion object {
        private const val previewPlaceholder = "17"
    }


    private lateinit var contentPane: JPanel
    private lateinit var previewPanelHolder: PreviewPanel<DummyInsertArrayAction>
    private lateinit var previewPanel: JPanel
    private lateinit var countSpinner: JIntSpinner
    private lateinit var bracketsGroup: ButtonGroup
    private lateinit var separatorGroup: ButtonGroup
    private lateinit var spaceAfterSeparatorCheckBox: JCheckBox

    override val rootPane get() = contentPane


    init {
        loadSettings()

        previewPanelHolder.updatePreviewOnUpdateOf(
            countSpinner, bracketsGroup, separatorGroup, spaceAfterSeparatorCheckBox)
        previewPanelHolder.updatePreview()
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        previewPanelHolder = PreviewPanel {
            DummyInsertArrayAction(ArraySettings().also { saveSettings(it) }, previewPlaceholder)
        }
        previewPanel = previewPanelHolder.rootPane

        countSpinner = JIntSpinner(value = 1, minValue = 1)
    }

    override fun loadSettings(settings: ArraySettings) {
        countSpinner.value = settings.count
        bracketsGroup.setValue(settings.brackets)
        separatorGroup.setValue(settings.separator)
        spaceAfterSeparatorCheckBox.isSelected = settings.isSpaceAfterSeparator
    }

    override fun saveSettings(settings: ArraySettings) {
        settings.count = countSpinner.value
        settings.brackets = bracketsGroup.getValue() ?: ArraySettings.DEFAULT_BRACKETS
        settings.separator = separatorGroup.getValue() ?: ArraySettings.DEFAULT_SEPARATOR
        settings.isSpaceAfterSeparator = spaceAfterSeparatorCheckBox.isSelected
    }

    override fun doValidate() = countSpinner.validateValue()
}
