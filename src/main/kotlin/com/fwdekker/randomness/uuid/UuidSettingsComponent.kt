package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SchemesPanel
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.SettingsComponentListener
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_ENCLOSURE
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_VERSION
import com.fwdekker.randomness.uuid.UuidSettings.Companion.default
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel


/**
 * Component for settings of random UUID generation.
 *
 * @param settings the settings to edit in the component
 *
 * @see UuidSettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class UuidSettingsComponent(settings: UuidSettings = default) : SettingsComponent<UuidSettings, UuidScheme>(settings) {
    override lateinit var unsavedSettings: UuidSettings
    override lateinit var schemesPanel: SchemesPanel<UuidScheme>

    private lateinit var contentPane: JPanel
    private lateinit var previewPanelHolder: PreviewPanel
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
        unsavedSettings = UuidSettings()
        schemesPanel = UuidSchemesPanel(unsavedSettings)
            .also { it.addListener(SettingsComponentListener(this)) }

        previewPanelHolder = PreviewPanel { UuidInsertAction(UuidScheme().also { saveScheme(it) }) }
        previewPanel = previewPanelHolder.rootPane
    }


    override fun loadScheme(scheme: UuidScheme) {
        versionGroup.setValue(scheme.version.toString())
        enclosureGroup.setValue(scheme.enclosure)
        capitalizationGroup.setValue(scheme.capitalization)
        addDashesCheckBox.isSelected = scheme.addDashes
    }

    override fun saveScheme(scheme: UuidScheme) {
        scheme.version = versionGroup.getValue()?.toInt() ?: DEFAULT_VERSION
        scheme.enclosure = enclosureGroup.getValue() ?: DEFAULT_ENCLOSURE
        scheme.capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION
        scheme.addDashes = addDashesCheckBox.isSelected
    }

    override fun doValidate(): ValidationInfo? = null


    /**
     * A panel to select schemes from.
     *
     * @param settings the settings model backing up the panel
     */
    private class UuidSchemesPanel(settings: UuidSettings) : SchemesPanel<UuidScheme>(settings) {
        override val type: Class<UuidScheme>
            get() = UuidScheme::class.java

        override fun createDefaultInstance() = UuidScheme()
    }
}
