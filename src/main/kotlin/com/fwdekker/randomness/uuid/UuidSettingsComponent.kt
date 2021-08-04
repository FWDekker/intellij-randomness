package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SchemeComponent
import com.fwdekker.randomness.ValidationInfo
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_ENCLOSURE
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_VERSION
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel


/**
 * Component for settings of random UUID generation.
 *
 * @param settings the settings to edit in the component
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class UuidSettingsComponent(settings: UuidScheme) : SchemeComponent<UuidScheme>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var versionGroup: ButtonGroup
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var addDashesCheckBox: JCheckBox

    override val rootPane get() = contentPane


    init {
        loadScheme()
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

    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            versionGroup, enclosureGroup, capitalizationGroup, addDashesCheckBox,
            listener = listener
        )

    override fun toUDSDescriptor() = "%UUID[]"
}
