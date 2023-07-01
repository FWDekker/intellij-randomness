package com.fwdekker.randomness.fixedlength

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TitledSeparator
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.ChangeEvent
import javax.swing.text.PlainDocument


/**
 * Component for settings of fixed width decoration.
 *
 * @param settings the settings to edit in the component
 */
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
        enabledCheckBox.addChangeListener(
            { _: ChangeEvent? ->
                lengthLabel.isEnabled = enabledCheckBox.isSelected
                lengthInput.isEnabled = enabledCheckBox.isSelected
                fillerLabel.isEnabled = enabledCheckBox.isSelected
                fillerInput.isEnabled = enabledCheckBox.isSelected
            }.also { it(null) }
        )

        loadState()
    }

    /**
     * Initializes custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    private fun createUIComponents() {
        separator = SeparatorFactory.createSeparator(Bundle("fixed_length.title"), null)

        lengthInput = JIntSpinner(value = FixedLengthDecorator.MIN_LENGTH, minValue = FixedLengthDecorator.MIN_LENGTH)

        fillerInput = JTextField(PlainDocument().also { it.documentFilter = MaxLengthDocumentFilter(1) }, "", 0)
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
}
