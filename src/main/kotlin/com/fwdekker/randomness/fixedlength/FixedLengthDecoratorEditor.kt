package com.fwdekker.randomness.fixedlength

import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.addChangeListenerTo
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
    override val preferredFocusedComponent = enabledCheckBox

    private lateinit var enabledCheckBox: JCheckBox
    private lateinit var lengthInput: JIntSpinner
    private lateinit var fillerLabel: JLabel
    private lateinit var fillerInput: JTextField


    init {
        enabledCheckBox.addChangeListener {
            lengthInput.isEnabled = enabledCheckBox.isSelected
            fillerLabel.isEnabled = enabledCheckBox.isSelected
            fillerInput.isEnabled = enabledCheckBox.isSelected
        }
        enabledCheckBox.changeListeners.forEach { it.stateChanged(ChangeEvent(enabledCheckBox)) }

        loadState()
    }

    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        lengthInput = JIntSpinner(value = FixedLengthDecorator.MIN_LENGTH, minValue = FixedLengthDecorator.MIN_LENGTH)

        fillerInput = JTextField(
            DefaultStyledDocument().also { it.documentFilter = DocumentSizeFilter(maxLength = 1) },
            "",
            0
        )
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
     * A document that has an enforced maximum size.
     *
     * @property maxLength The maximum allowable length of the document.
     */
    private class DocumentSizeFilter(private val maxLength: Int) : DocumentFilter() {
        override fun insertString(fb: FilterBypass, offset: Int, text: String, attr: AttributeSet?) {
            if (fb.document.length < maxLength)
                super.insertString(fb, offset, text.take(maxLength - fb.document.length), attr)
        }

        override fun replace(fb: FilterBypass, offset: Int, length: Int, text: String, attrs: AttributeSet?) {
            if (fb.document.length - length <= maxLength)
                super.replace(fb, offset, length, text.take(maxLength - (fb.document.length - length)), attrs)
        }
    }
}
