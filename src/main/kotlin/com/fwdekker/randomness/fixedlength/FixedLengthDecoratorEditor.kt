package com.fwdekker.randomness.fixedlength

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TitledSeparator
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.ChangeEvent
import javax.swing.text.AttributeSet
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.DocumentFilter


/**
 * Component for settings of fixed width decoration.
 *
 * @param settings the settings to edit in the component
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class FixedLengthDecoratorEditor(settings: FixedLengthDecorator) : StateEditor<FixedLengthDecorator>(settings) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = enabledCheckBox

    private lateinit var separator: TitledSeparator
    private lateinit var enabledCheckBox: JCheckBox
    private lateinit var lengthLabel: JLabel
    private lateinit var lengthInput: JIntSpinner
    private lateinit var fillerLabel: JLabel
    private lateinit var fillerInput: JTextField


    init {
        enabledCheckBox.addChangeListener {
            lengthLabel.isEnabled = enabledCheckBox.isSelected
            lengthInput.isEnabled = enabledCheckBox.isSelected
            fillerLabel.isEnabled = enabledCheckBox.isSelected
            fillerInput.isEnabled = enabledCheckBox.isSelected
        }
        enabledCheckBox.changeListeners.forEach { it.stateChanged(ChangeEvent(enabledCheckBox)) }

        loadState()
    }

    /**
     * Initializes custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        separator = SeparatorFactory.createSeparator(Bundle("fixed_length.title"), null)

        lengthInput = JIntSpinner(value = FixedLengthDecorator.MIN_LENGTH, minValue = FixedLengthDecorator.MIN_LENGTH)

        fillerInput = JTextField(DefaultStyledDocument().also { it.documentFilter = OneByteDocumentFilter() }, "", 0)
    }


    override fun loadState(state: FixedLengthDecorator) {
        super.loadState(state)

        enabledCheckBox.isSelected = state.enabled
        lengthInput.value = state.length
        fillerInput.text = state.filler
    }

    override fun readState() =
        FixedLengthDecorator(
            enabled = enabledCheckBox.isSelected,
            length = lengthInput.value,
            filler = fillerInput.text
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(enabledCheckBox, lengthInput, fillerInput, listener = listener)


    /**
     * A document that can contain exactly one character.
     */
    private class OneByteDocumentFilter : DocumentFilter() {
        /**
         * Replaces the document's contents with the last character in [text].
         *
         * @param fb bypass to mutate the document
         * @param offset ignored
         * @param text the text of which to insert the last character
         * @param attr the attributes to associate with the inserted content
         */
        override fun insertString(fb: FilterBypass, offset: Int, text: String?, attr: AttributeSet?) =
            super.replace(fb, 0, fb.document.length, text?.takeLast(1), attr)

        /**
         * Replaces the document's contents with the last character in [text].
         *
         * @param fb bypass to mutate the document
         * @param offset ignored
         * @param length ignored
         * @param text the text of which to insert the last character
         * @param attr the attributes to associate with the inserted content
         */
        override fun replace(fb: FilterBypass, offset: Int, length: Int, text: String?, attr: AttributeSet?) =
            super.replace(fb, 0, fb.document.length, text?.takeLast(1), attr)
    }
}
