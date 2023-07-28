package com.fwdekker.randomness.fixedlength

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.loadMnemonic
import com.fwdekker.randomness.ui.withFixedWidth
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
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
    override val preferredFocusedComponent get() = enabledCheckBox

    private lateinit var enabledCheckBox: JCheckBox
    private lateinit var lengthInput: JIntSpinner
    private lateinit var fillerInput: JTextField


    init {
        rootComponent = panel {
            group(Bundle("fixed_length.title")) {
                row {
                    checkBox(Bundle("fixed_length.ui.enabled"))
                        .loadMnemonic()
                        .also { it.component.name = "fixedLengthEnabled" }
                        .also { enabledCheckBox = it.component }
                }

                indent {
                    row(Bundle("fixed_length.ui.length_option")) {
                        cell(
                            JIntSpinner(
                                value = FixedLengthDecorator.MIN_LENGTH,
                                minValue = FixedLengthDecorator.MIN_LENGTH
                            )
                        )
                            .withFixedWidth(UIConstants.SIZE_SMALL)
                            .also { it.component.name = "fixedLengthLength" }
                            .also { lengthInput = it.component }
                    }

                    row(Bundle("fixed_length.ui.filler_option")) {
                        val document = PlainDocument().also { it.documentFilter = MaxLengthDocumentFilter(1) }
                        textField()
                            .withFixedWidth(UIConstants.SIZE_SMALL)
                            .also { it.component.document = document }
                            .also { it.component.name = "fixedLengthFiller" }
                            .also { fillerInput = it.component }
                    }
                }.enabledIf(enabledCheckBox.selected)
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
}
