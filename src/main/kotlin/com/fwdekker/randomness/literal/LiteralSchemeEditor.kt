package com.fwdekker.randomness.literal

import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArraySchemeDecoratorEditor
import com.fwdekker.randomness.ui.addChangeListenerTo
import javax.swing.JPanel
import javax.swing.JTextField


/**
 * Component for editing fixed literal settings.
 *
 * @param scheme the scheme to edit in the component
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class LiteralSchemeEditor(scheme: LiteralScheme = LiteralScheme()) : StateEditor<LiteralScheme>(scheme) {
    override lateinit var rootComponent: JPanel private set
    private lateinit var literalInput: JTextField
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
        arrayDecoratorEditor = ArraySchemeDecoratorEditor(originalState.decorator)
        arrayDecoratorPanel = arrayDecoratorEditor.rootComponent
    }


    override fun loadState(state: LiteralScheme) {
        super.loadState(state)

        literalInput.text = state.literal
        arrayDecoratorEditor.loadState(state.decorator)
    }

    override fun readState() =
        LiteralScheme(
            literal = literalInput.text,
            decorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(literalInput, arrayDecoratorEditor, listener = listener)
}
