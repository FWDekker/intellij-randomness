package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.SettingsDialog
import com.fwdekker.randomness.ui.ButtonGroupHelper
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.ButtonGroup
import javax.swing.JPanel


/**
 * Dialog for settings of random UUID generation.
 *
 * @param settings the settings to manipulate with this dialog. Defaults to [UuidSettings.default]
 */
class UuidSettingsDialog(settings: UuidSettings = UuidSettings.default) : SettingsDialog<UuidSettings>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var enclosureGroup: ButtonGroup


    init {
        init()
        loadSettings()
    }


    override fun createCenterPanel() = contentPane

    override fun doValidate(): ValidationInfo? = null


    override fun loadSettings(settings: UuidSettings) =
        ButtonGroupHelper.setValue(enclosureGroup, settings.enclosure)

    override fun saveSettings(settings: UuidSettings) {
        settings.enclosure = ButtonGroupHelper.getValue(enclosureGroup)!! // TODO Remove !!
    }
}
