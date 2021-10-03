package com.fwdekker.randomness.string

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.string.StringScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.UI
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField


/**
 * Component for editing random string settings.
 *
 * @param scheme the scheme to edit in the component
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class StringSchemeEditor(scheme: StringScheme = StringScheme()) : StateEditor<StringScheme>(scheme) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = patternField

    private lateinit var patternField: JTextField
    private lateinit var capitalizationLabel: JLabel
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var removeLookAlikeSymbolsPanel: JPanel
    private lateinit var removeLookAlikeSymbolsCheckBox: JCheckBox
    private lateinit var arrayDecoratorPanel: JPanel
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        nop() // Cannot use `lateinit` property as first statement in init

        capitalizationGroup.setLabel(capitalizationLabel)

        loadState()
    }

    /**
     * Initializes custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        removeLookAlikeSymbolsCheckBox = JBCheckBox(Bundle("string.ui.remove_look_alike"))
            .also { box ->
                box.name = "removeLookAlikeCharacters"
                box.setMnemonic(box.text.dropWhile { it != '&' }[1])
                box.text = box.text.filterNot { it == '&' }
            }
        removeLookAlikeSymbolsPanel = UI.PanelFactory.panel(removeLookAlikeSymbolsCheckBox)
            .withTooltip(Bundle("string.ui.remove_look_alike_help", StringScheme.LOOK_ALIKE_CHARACTERS))
            .createPanel()

        arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
        arrayDecoratorPanel = arrayDecoratorEditor.rootComponent
    }


    override fun loadState(state: StringScheme) {
        super.loadState(state)

        patternField.text = state.pattern
        capitalizationGroup.setValue(state.capitalization)
        removeLookAlikeSymbolsCheckBox.isSelected = state.removeLookAlikeSymbols

        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        StringScheme(
            pattern = patternField.text,
            capitalization = capitalizationGroup.getValue()?.let(::getMode) ?: DEFAULT_CAPITALIZATION,
            removeLookAlikeSymbols = removeLookAlikeSymbolsCheckBox.isSelected,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(patternField, capitalizationGroup, arrayDecoratorEditor, listener = listener)
}


/**
 * Null operation, does nothing.
 */
private fun nop() {
    // Does nothing
}
