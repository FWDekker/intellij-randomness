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
 * Component for editing random UUID settings.
 *
 * @param scheme the scheme to edit in the component
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class UuidSchemeEditor(scheme: UuidScheme) : SchemeComponent<UuidScheme>() {
    override lateinit var rootPane: JPanel private set
    private lateinit var versionGroup: ButtonGroup
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var addDashesCheckBox: JCheckBox


    init {
        loadScheme(scheme)
    }


    override fun loadScheme(scheme: UuidScheme) =
        scheme.also {
            versionGroup.setValue(it.version.toString())
            enclosureGroup.setValue(it.enclosure)
            capitalizationGroup.setValue(it.capitalization)
            addDashesCheckBox.isSelected = it.addDashes
        }.let {}

    override fun saveScheme(): UuidScheme =
        UuidScheme(
            version = versionGroup.getValue()?.toInt() ?: DEFAULT_VERSION,
            enclosure = enclosureGroup.getValue() ?: DEFAULT_ENCLOSURE,
            capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION,
            addDashes = addDashesCheckBox.isSelected
        )

    override fun doValidate(): ValidationInfo? = null

    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            versionGroup, enclosureGroup, capitalizationGroup, addDashesCheckBox,
            listener = listener
        )
}
