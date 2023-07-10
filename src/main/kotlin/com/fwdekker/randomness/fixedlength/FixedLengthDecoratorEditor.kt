package com.fwdekker.randomness.fixedlength

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.ui.GridPanelBuilder
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.ui.components.JBCheckBox
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.text.PlainDocument


/**
 * Component for settings of fixed width decoration.
 *
 * @param settings the settings to edit in the component
 */
class FixedLengthDecoratorEditor(settings: FixedLengthDecorator) : StateEditor<FixedLengthDecorator>(settings) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = enabledCheckBox

    private lateinit var enabledCheckBox: JCheckBox
    private lateinit var lengthInput: JIntSpinner
    private lateinit var fillerInput: JTextField


    init {
        rootComponent = GridPanelBuilder.panel {
            textSeparatorCell(Bundle("fixed_length.title"))

            cell {
                JBCheckBox(Bundle("fixed_length.ui.enabled"))
                    .withName("fixedLengthEnabled")
                    .also { enabledCheckBox = it }
            }

            panel {
                row {
                    cell {
                        label("fixedLengthLengthLabel", Bundle("fixed_length.ui.length_option"))
                            .toggledBy(enabledCheckBox)
                    }

                    cell(constraints(fixedWidth = UIConstants.SIZE_SMALL)) {
                        JIntSpinner(value = FixedLengthDecorator.MIN_LENGTH, minValue = FixedLengthDecorator.MIN_LENGTH)
                            .withName("fixedLengthLength")
                            .toggledBy(enabledCheckBox)
                            .also { lengthInput = it }
                    }
                }

                row {
                    cell {
                        label("fixedLengthFillerLabel", Bundle("fixed_length.ui.filler_option"))
                            .toggledBy(enabledCheckBox)
                    }

                    cell(constraints(fixedWidth = UIConstants.SIZE_SMALL)) {
                        JTextField(PlainDocument().also { it.documentFilter = MaxLengthDocumentFilter(1) }, "", 0)
                            .withName("fixedLengthFiller")
                            .toggledBy(enabledCheckBox)
                            .also { fillerInput = it }
                    }
                }
            }
        }

        loadState()
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
            filler = fillerInput.text,
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(enabledCheckBox, lengthInput, fillerInput, listener = listener)
}
