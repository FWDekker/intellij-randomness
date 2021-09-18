package com.fwdekker.randomness.literal

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.literal.LiteralScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TitledSeparator
import java.util.ResourceBundle
import javax.swing.ButtonGroup
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
    override val preferredFocusedComponent
        get() = literalInput

    private lateinit var titleSeparator: TitledSeparator
    private lateinit var literalInput: JTextField
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var arrayDecoratorPanel: JPanel
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


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
        val bundle = ResourceBundle.getBundle("randomness")
        titleSeparator = SeparatorFactory.createSeparator(bundle.getString("settings.literal_title"), null)

        arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
        arrayDecoratorPanel = arrayDecoratorEditor.rootComponent
    }


    override fun loadState(state: LiteralScheme) {
        super.loadState(state)

        literalInput.text = state.literal
        capitalizationGroup.setValue(state.capitalization)
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        LiteralScheme(
            literal = literalInput.text,
            capitalization = capitalizationGroup.getValue()?.let(::getMode) ?: DEFAULT_CAPITALIZATION,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(literalInput, arrayDecoratorEditor, listener = listener)
}
