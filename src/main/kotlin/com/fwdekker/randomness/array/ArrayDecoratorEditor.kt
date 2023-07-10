package com.fwdekker.randomness.array

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecorator.Companion.DEFAULT_BRACKETS
import com.fwdekker.randomness.array.ArrayDecorator.Companion.DEFAULT_SEPARATOR
import com.fwdekker.randomness.array.ArrayDecorator.Companion.MIN_MIN_COUNT
import com.fwdekker.randomness.ui.GridPanelBuilder
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.components.JBCheckBox
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.event.ChangeEvent


/**
 * Component for settings of random array generation.
 *
 * @param settings the settings to edit in the component
 * @property embedded `true` if the editor is embedded, which means that no titled separator is shown at the top, and
 * the user cannot disable the array scheme; [readState] always returns true
 */
class ArrayDecoratorEditor(
    settings: ArrayDecorator,
    private val embedded: Boolean = false,
) : StateEditor<ArrayDecorator>(settings) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = if (embedded) minCountSpinner.editorComponent else enabledCheckBox

    private lateinit var enabledCheckBox: JCheckBox
    private lateinit var minCountSpinner: JIntSpinner
    private lateinit var maxCountSpinner: JIntSpinner
    private lateinit var bracketsGroup: ButtonGroup
    private lateinit var customBrackets: VariableLabelRadioButton
    private lateinit var separatorGroup: ButtonGroup
    private lateinit var customSeparator: VariableLabelRadioButton
    private lateinit var newlineSeparatorButton: JRadioButton
    private lateinit var spaceAfterSeparatorCheckBox: JCheckBox


    init {
        rootComponent = GridPanelBuilder.panel {
            enabledCheckBox = JBCheckBox(Bundle("array.ui.enabled"))
            if (!embedded) {
                textSeparatorCell(Bundle("array.title"))

                // TODO: Make checkbox-based toggling of all components automatic
                cell { enabledCheckBox.withName("arrayEnabled") }
            }

            panel {
                row {
                    cell { label("arrayMinCountLabel", Bundle("array.ui.min_count_option")).toggledBy(enabledCheckBox) }

                    cell(constraints(fixedWidth = UIConstants.SIZE_SMALL)) {
                        JIntSpinner(value = MIN_MIN_COUNT, minValue = MIN_MIN_COUNT)
                            .withName("arrayMinCount")
                            .toggledBy(enabledCheckBox)
                            .also { minCountSpinner = it }
                    }
                }

                row {
                    cell { label("arrayMaxCountLabel", Bundle("array.ui.max_count_option")).toggledBy(enabledCheckBox) }

                    cell(constraints(fixedWidth = UIConstants.SIZE_SMALL)) {
                        JIntSpinner(value = MIN_MIN_COUNT, minValue = MIN_MIN_COUNT)
                            .withName("arrayMaxCount")
                            .toggledBy(enabledCheckBox)
                            .also { maxCountSpinner = it }
                    }
                }

                bindSpinners(minCountSpinner, maxCountSpinner)

                row {
                    cell { label("arrayBracketsLabel", Bundle("array.ui.brackets.option")).toggledBy(enabledCheckBox) }

                    row {
                        bracketsGroup = buttonGroup("arrayBrackets")

                        cell {
                            radioButton("arrayBracketsNone", Bundle("shared.option.none"), "")
                                .toggledBy(enabledCheckBox)
                        }
                        cell { radioButton("arrayBracketsSquare", "[]", "[@]").toggledBy(enabledCheckBox) }
                        cell { radioButton("arrayBracketsCurly", "{}", "{@}").toggledBy(enabledCheckBox) }
                        cell { radioButton("arrayBracketsRound", "()", "(@)").toggledBy(enabledCheckBox) }
                        cell {
                            VariableLabelRadioButton(UIConstants.SIZE_MEDIUM)
                                .withName("arrayBracketsCustom")
                                .toggledBy(enabledCheckBox)
                                .also { customBrackets = it }
                        }

                        cell { ContextHelpLabel.create(Bundle("array.ui.brackets.comment")).toggledBy(enabledCheckBox) }
                    }
                }

                row {
                    cell {
                        label("arraySeparatorLabel", Bundle("array.ui.separator.option"))
                            .toggledBy(enabledCheckBox)
                    }

                    row {
                        separatorGroup = buttonGroup("arraySeparator")

                        cell {
                            radioButton("arraySeparatorNone", Bundle("shared.option.none"), "")
                                .toggledBy(enabledCheckBox)
                        }
                        cell { radioButton("arraySeparatorComma", ",").toggledBy(enabledCheckBox) }
                        cell { radioButton("arraySeparatorSemicolon", ";").toggledBy(enabledCheckBox) }
                        cell {
                            radioButton("arraySeparatorNewLine", "\\n", "\n")
                                .toggledBy(enabledCheckBox)
                                .also { newlineSeparatorButton = it }
                        }
                        cell {
                            VariableLabelRadioButton()
                                .withName("arraySeparatorCustom")
                                .toggledBy(enabledCheckBox)
                                .also { customSeparator = it }
                        }
                    }
                }

                row {
                    skip()

                    cell {
                        JBCheckBox(Bundle("array.ui.space_after_separator"))
                            .withName("arraySpaceAfterSeparator")
                            .toggledBy(enabledCheckBox) { !newlineSeparatorButton.isSelected }
                            .also {
                                newlineSeparatorButton.addChangeListener(
                                    { _: ChangeEvent? ->
                                        it.isEnabled = enabledCheckBox.isSelected && !newlineSeparatorButton.isSelected
                                    }.also { it(null) }
                                )
                            }
                            .also { spaceAfterSeparatorCheckBox = it }
                    }
                }
            }
        }

        loadState()
    }


    override fun loadState(state: ArrayDecorator) {
        super.loadState(state)

        enabledCheckBox.isSelected = embedded || state.enabled
        minCountSpinner.value = state.minCount
        maxCountSpinner.value = state.maxCount
        customBrackets.label = state.customBrackets
        bracketsGroup.setValue(state.brackets)
        customSeparator.label = state.customSeparator
        separatorGroup.setValue(state.separator)
        spaceAfterSeparatorCheckBox.isSelected = state.isSpaceAfterSeparator
    }

    override fun readState(): ArrayDecorator =
        ArrayDecorator(
            enabled = !embedded && enabledCheckBox.isSelected,
            minCount = minCountSpinner.value,
            maxCount = maxCountSpinner.value,
            brackets = bracketsGroup.getValue() ?: DEFAULT_BRACKETS,
            customBrackets = customBrackets.label,
            separator = separatorGroup.getValue() ?: DEFAULT_SEPARATOR,
            customSeparator = customSeparator.label,
            isSpaceAfterSeparator = spaceAfterSeparatorCheckBox.isSelected
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            enabledCheckBox, minCountSpinner, maxCountSpinner, bracketsGroup, customBrackets, separatorGroup,
            customSeparator, spaceAfterSeparatorCheckBox,
            listener = listener
        )
}
