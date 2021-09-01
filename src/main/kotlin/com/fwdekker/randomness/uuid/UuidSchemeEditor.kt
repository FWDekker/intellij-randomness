package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArraySchemeDecoratorEditor
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.buttons
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
class UuidSchemeEditor(scheme: UuidScheme = UuidScheme()) : StateEditor<UuidScheme>(scheme) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = versionGroup.buttons().firstOrNull { it.isSelected }

    private lateinit var versionGroup: ButtonGroup
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var addDashesCheckBox: JCheckBox
    private lateinit var arrayDecoratorPanel: JPanel
    private lateinit var arrayDecoratorEditor: ArraySchemeDecoratorEditor


    init {
        loadState()
    }

    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        arrayDecoratorEditor = ArraySchemeDecoratorEditor(originalState.arrayDecorator)
        arrayDecoratorPanel = arrayDecoratorEditor.rootComponent
    }


    override fun loadState(state: UuidScheme) {
        super.loadState(state)

        versionGroup.setValue(state.version.toString())
        enclosureGroup.setValue(state.enclosure)
        capitalizationGroup.setValue(state.capitalization)
        addDashesCheckBox.isSelected = state.addDashes
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState(): UuidScheme =
        UuidScheme(
            version = versionGroup.getValue()?.toInt() ?: DEFAULT_VERSION,
            enclosure = enclosureGroup.getValue() ?: DEFAULT_ENCLOSURE,
            capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION,
            addDashes = addDashesCheckBox.isSelected,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            versionGroup, enclosureGroup, capitalizationGroup, addDashesCheckBox, arrayDecoratorEditor,
            listener = listener
        )
}
