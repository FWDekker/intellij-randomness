package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemesPanel
import com.fwdekker.randomness.SettingsComponent
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
 * @see UuidSettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class UuidSettingsComponent(settings: UuidSettings = default) : SettingsComponent<UuidSettings, UuidScheme>(settings) {
    @Suppress("UNCHECKED_CAST") // Guaranteed by implementation
    override val schemesPanel: SchemesPanel<UuidScheme>
        get() = schemesPanelImpl as SchemesPanel<UuidScheme>
    override lateinit var unsavedSettings: UuidSettings

    private lateinit var contentPane: JPanel
    private lateinit var schemesPanelImpl: JPanel
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
        unsavedSettings = UuidSettings()
        schemesPanelImpl = UuidSchemesPanel(unsavedSettings)
            .also { panel ->
                panel.addListener(object : SchemesPanel.Listener<UuidScheme> {
                    override fun onCurrentSchemeWillChange(scheme: UuidScheme) = saveScheme(scheme)

                    override fun onCurrentSchemeHasChanged(scheme: UuidScheme) = loadScheme(scheme)
                })
            }

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


    private class UuidSchemesPanel(settings: UuidSettings) : SchemesPanel<UuidScheme>(settings, Scheme.DEFAULT_NAME) {
        override val type: Class<UuidScheme>
            get() = UuidScheme::class.java

        override fun createDefaultInstance() = UuidScheme()
    }
}
