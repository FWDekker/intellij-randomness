package com.fwdekker.randomness.uds

import com.fwdekker.randomness.SchemesPanel
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.SettingsComponentListener
import com.fwdekker.randomness.ValidationInfo
import com.fwdekker.randomness.decimal.DecimalSettingsComponent
import com.fwdekker.randomness.integer.IntegerSettingsComponent
import com.fwdekker.randomness.string.StringSettingsComponent
import com.fwdekker.randomness.uds.UDSSettings.Companion.default
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.addChangeListener
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.uuid.UuidSettingsComponent
import com.fwdekker.randomness.word.WordSettingsComponent
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
    private lateinit var descriptorEditorPanel: JPanel
    private var isEditingDescriptor = false
    private var isEditingComponent = false

    override val rootPane get() = contentPane


    init {
        loadSettings()

        descriptor.addChangeListener { onDescriptorChanged(it.text) }

        addChangeListener { previewPanelHolder.updatePreview() }
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

        descriptorEditorPanel = JPanel()
    }

    override fun loadScheme(scheme: UDSScheme) {
        descriptor.text = scheme.descriptor
    }

    override fun saveScheme(scheme: UDSScheme) {
        scheme.descriptor = descriptor.text
    }

    override fun doValidate(): ValidationInfo? = null

    override fun addChangeListener(listener: () -> Unit) = addChangeListenerTo(descriptor, listener = listener)


    // TODO: Docs
    private fun onDescriptorChanged(newDescriptor: String) {
        // TODO: Extract the descriptor's scheme and load it into the component
        // TODO: Do not reload entire panel every time, just load it into the component if it's already there

        if (isEditingComponent) return

        isEditingDescriptor = true
        descriptorEditorPanel.removeAll()
        when {
            newDescriptor.startsWith("%Int") -> IntegerSettingsComponent()
            newDescriptor.startsWith("%Dec") -> DecimalSettingsComponent()
            newDescriptor.startsWith("%Str") -> StringSettingsComponent()
            newDescriptor.startsWith("%Word") -> WordSettingsComponent()
            newDescriptor.startsWith("%UUID") -> UuidSettingsComponent()
            else -> null
        }?.also { component ->
            component.addChangeListener { onComponentChanged(component.toUDSDescriptor()) }
            descriptorEditorPanel.add(component.rootPane)
        }
        isEditingDescriptor = false
    }

    // TODO: Docs
    private fun onComponentChanged(newDescriptor: String) {
        if (isEditingDescriptor) return

        isEditingComponent = true
        descriptor.text = newDescriptor
        isEditingComponent = false
    }

    override fun toUDSDescriptor(): String = descriptor.text


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
