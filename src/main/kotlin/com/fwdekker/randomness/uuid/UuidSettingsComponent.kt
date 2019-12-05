package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.uuid.UuidSettings.Companion.default
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.ButtonGroup
import javax.swing.JPanel


/**
 * Component for settings of random UUID generation.
 *
 * @see UuidSettings
 * @see UuidSettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class UuidSettingsComponent(settings: UuidSettings = default) : SettingsComponent<UuidSettings>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var enclosureGroup: ButtonGroup

    override val rootPane get() = contentPane


    init {
        loadSettings()
    }


    override fun loadSettings(settings: UuidSettings) {
        enclosureGroup.setValue(settings.enclosure)
    }

    override fun saveSettings(settings: UuidSettings) {
        settings.enclosure = enclosureGroup.getValue() ?: UuidSettings.DEFAULT_ENCLOSURE
    }

    override fun doValidate(): ValidationInfo? = null
}
