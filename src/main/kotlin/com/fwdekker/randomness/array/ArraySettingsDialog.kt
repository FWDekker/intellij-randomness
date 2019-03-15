package com.fwdekker.randomness.array

import com.fwdekker.randomness.SettingsDialog
import com.fwdekker.randomness.ValidationException
import com.fwdekker.randomness.ui.ButtonGroupHelper
import com.fwdekker.randomness.ui.JLongSpinner
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel


/**
 * Dialog for settings of random array generation.
 *
 * @param settings the settings to manipulate with this dialog; defaults to [ArraySettings.instance]
 */
class ArraySettingsDialog(settings: ArraySettings = ArraySettings.instance) : SettingsDialog<ArraySettings>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var countSpinner: JLongSpinner
    private lateinit var bracketsGroup: ButtonGroup
    private lateinit var separatorGroup: ButtonGroup
    private lateinit var spaceAfterSeparatorCheckBox: JCheckBox


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
        countSpinner = JLongSpinner(1, 1, Integer.MAX_VALUE.toLong())
    }


    override fun loadSettings(settings: ArraySettings) {
        countSpinner.setValue(settings.count)
        ButtonGroupHelper.setValue(bracketsGroup, settings.brackets)
        ButtonGroupHelper.setValue(separatorGroup, settings.separator)
        spaceAfterSeparatorCheckBox.isSelected = settings.isSpaceAfterSeparator
    }

    override fun saveSettings(settings: ArraySettings) {
        settings.count = Math.toIntExact(countSpinner.value)
        settings.brackets = ButtonGroupHelper.getValue(bracketsGroup) // TODO use extension functions
        settings.separator = ButtonGroupHelper.getValue(separatorGroup)
        settings.isSpaceAfterSeparator = spaceAfterSeparatorCheckBox.isSelected
    }

    override fun doValidate(): ValidationInfo? {
        try {
            countSpinner.validateValue()
        } catch (e: ValidationException) {
            return ValidationInfo(e.message ?: "", e.component) // TODO remove null check
        }

        return null
    }
}
