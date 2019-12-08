package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.uuid.UuidSettings.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.uuid.UuidSettings.Companion.DEFAULT_ENCLOSURE
import com.fwdekker.randomness.uuid.UuidSettings.Companion.DEFAULT_VERSION
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
    private lateinit var previewPanelHolder: PreviewPanel<UuidInsertAction>
    private lateinit var previewPanel: JPanel
    private lateinit var versionGroup: ButtonGroup
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var addDashesCheckBox: JCheckBox

    override val rootPane get() = contentPane


    init {
        loadSettings()

        previewPanelHolder.updatePreviewOnUpdateOf(versionGroup, enclosureGroup, capitalizationGroup, addDashesCheckBox)
        previewPanelHolder.updatePreview()
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        previewPanelHolder = PreviewPanel { UuidInsertAction(UuidSettings().also { saveSettings(it) }) }
        previewPanel = previewPanelHolder.rootPane
    }


    override fun loadSettings(settings: UuidSettings) {
        versionGroup.setValue(settings.version.toString())
        enclosureGroup.setValue(settings.enclosure)
        capitalizationGroup.setValue(settings.capitalization)
        addDashesCheckBox.isSelected = settings.addDashes
    }

    override fun saveSettings(settings: UuidSettings) {
        settings.version = versionGroup.getValue()?.toInt() ?: DEFAULT_VERSION
        settings.enclosure = enclosureGroup.getValue() ?: DEFAULT_ENCLOSURE
        settings.capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION
        settings.addDashes = addDashesCheckBox.isSelected
    }

    override fun doValidate(): ValidationInfo? = null
}
