package com.fwdekker.randomness.uds

import com.fwdekker.randomness.SchemesPanel
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.SettingsComponentListener
import com.fwdekker.randomness.ValidationInfo
import com.fwdekker.randomness.uds.UDSSettings.Companion.default
import com.fwdekker.randomness.ui.PreviewPanel
import javax.swing.JPanel
import javax.swing.JTextField


/**
 * Component for settings of random UDS-based string generation.
 *
 * @param settings the settings to edit in the component
 *
 * @see UDSSettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class UDSSettingsComponent(settings: UDSSettings = default) : SettingsComponent<UDSSettings, UDSScheme>(settings) {
    override lateinit var unsavedSettings: UDSSettings
    override lateinit var schemesPanel: SchemesPanel<UDSScheme>

    private lateinit var contentPane: JPanel
    private lateinit var previewPanelHolder: PreviewPanel
    private lateinit var previewPanel: JPanel
    private lateinit var descriptor: JTextField

    override val rootPane get() = contentPane


    init {
        loadSettings()

        previewPanelHolder.updatePreviewOnUpdateOf(descriptor)
        previewPanelHolder.updatePreview()
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        unsavedSettings = UDSSettings()
        schemesPanel = UDSSchemesPanel(unsavedSettings)
            .also { panel -> panel.addListener(SettingsComponentListener(this)) }

        previewPanelHolder = PreviewPanel { UDSInsertAction(UDSScheme().also { saveScheme(it) }) }
        previewPanel = previewPanelHolder.rootPane
    }

    override fun loadScheme(scheme: UDSScheme) {
        descriptor.text = scheme.descriptor
    }

    override fun saveScheme(scheme: UDSScheme) {
        scheme.descriptor = descriptor.text
    }

    override fun doValidate(): ValidationInfo? = null


    /**
     * A panel to select schemes from.
     *
     * @param settings the settings model backing up the panel
     */
    private class UDSSchemesPanel(settings: UDSSettings) : SchemesPanel<UDSScheme>(settings) {
        override val type: Class<UDSScheme>
            get() = UDSScheme::class.java

        override fun createDefaultInstances() = UDSSettings.DEFAULT_SCHEMES
    }
}
