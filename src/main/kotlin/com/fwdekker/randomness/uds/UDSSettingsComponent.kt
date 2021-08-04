package com.fwdekker.randomness.uds

import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.ValidationInfo
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.decimal.DecimalSettingsComponent
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.integer.IntegerSettingsComponent
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.string.StringSettingsComponent
import com.fwdekker.randomness.uds.UDSSettings.Companion.default
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.addChangeListener
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.uuid.UuidSettingsComponent
import com.fwdekker.randomness.word.WordScheme
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
class UDSSettingsComponent(settings: UDSSettings = default) : SettingsComponent<UDSSettings>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var previewPanelHolder: PreviewPanel
    private lateinit var previewPanel: JPanel
    private lateinit var descriptor: JTextField
    private lateinit var descriptorEditorPanel: JPanel
    // TODO: Come up with something more elegant than these booleans
    // TODO: Docs
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
        previewPanelHolder = PreviewPanel { UDSInsertAction(UDSSettings().also { saveSettings(it) }) }
        previewPanel = previewPanelHolder.rootPane

        descriptorEditorPanel = JPanel()
    }

    override fun loadSettings(settings: UDSSettings) {
        descriptor.text = settings.descriptor
    }

    override fun saveSettings(settings: UDSSettings) {
        settings.descriptor = descriptor.text
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
            newDescriptor.startsWith("%Int") -> IntegerSettingsComponent(IntegerScheme())
            newDescriptor.startsWith("%Dec") -> DecimalSettingsComponent(DecimalScheme())
            newDescriptor.startsWith("%Str") -> StringSettingsComponent(StringScheme())
            newDescriptor.startsWith("%Word") -> WordSettingsComponent(WordScheme())
            newDescriptor.startsWith("%UUID") -> UuidSettingsComponent(UuidScheme())
            else -> null
        }?.also { component ->
            component.addChangeListener { onComponentChanged(component.saveScheme().descriptor) }
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
}
