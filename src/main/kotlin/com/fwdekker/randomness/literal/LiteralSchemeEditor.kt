package com.fwdekker.randomness.literal

import com.fwdekker.randomness.StateEditor
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


    init {
        loadState(scheme)
    }


    override fun loadState(state: LiteralScheme) {
        super.loadState(state)

        literalInput.text = state.literal
    }

    override fun readState() = LiteralScheme(literal = literalInput.text)


    override fun addChangeListener(listener: () -> Unit) = addChangeListenerTo(literalInput, listener = listener)
}
