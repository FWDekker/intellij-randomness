package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.uuid.UuidSettings.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.uuid.UuidSettings.Companion.DEFAULT_ENCLOSURE
import com.fwdekker.randomness.uuid.UuidSettings.Companion.default
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
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
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var addDashesCheckBox: JCheckBox

    override val rootPane get() = contentPane


    init {
        loadSettings()
    }


    override fun loadSettings(settings: UuidSettings) {
        enclosureGroup.setValue(settings.enclosure)
        capitalizationGroup.setValue(settings.capitalization)
        addDashesCheckBox.isSelected = settings.addDashes
    }

    override fun saveSettings(settings: UuidSettings) {
        settings.enclosure = enclosureGroup.getValue() ?: DEFAULT_ENCLOSURE
        settings.capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION
        settings.addDashes = addDashesCheckBox.isSelected
    }

    override fun doValidate(): ValidationInfo? = null
}
