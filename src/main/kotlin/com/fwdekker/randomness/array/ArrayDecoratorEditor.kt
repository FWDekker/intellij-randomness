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
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JLabel
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
    private lateinit var customBracketsHelpLabel: JLabel
    private lateinit var separatorGroup: ButtonGroup
    private lateinit var customSeparator: VariableLabelRadioButton
    private lateinit var newlineSeparatorButton: JRadioButton
    private lateinit var spaceAfterSeparatorCheckBox: JCheckBox


    init {
        rootComponent = GridPanelBuilder.panel {
            enabledCheckBox = JBCheckBox(Bundle("array.ui.enabled"))
            if (!embedded) {
                textSeparator(Bundle("array.title"))

                cell {
                    enabledCheckBox
                        .withName("arrayEnabled")
                        .loadMnemonic()
                }
            }

            panel {
                row {
                    lateinit var minCountLabel: JLabel

                    cell {
                        JBLabel(Bundle("array.ui.min_count_option"))
                            .toggledBy(enabledCheckBox)
                            .also { minCountLabel = it }
                    }

                    cell(constraints(fixedWidth = UIConstants.SIZE_SMALL)) {
                        JIntSpinner(value = MIN_MIN_COUNT, minValue = MIN_MIN_COUNT)
                            .withName("arrayMinCount")
                            .setLabel(minCountLabel)
                            .toggledBy(enabledCheckBox)
                            .also { minCountSpinner = it }
                    }
                }

                row {
                    lateinit var maxCountLabel: JLabel

                    cell {
                        JBLabel(Bundle("array.ui.max_count_option"))
                            .toggledBy(enabledCheckBox)
                            .also { maxCountLabel = it }
                    }

                    cell(constraints(fixedWidth = UIConstants.SIZE_SMALL)) {
                        JIntSpinner(value = MIN_MIN_COUNT, minValue = MIN_MIN_COUNT)
                            .withName("arrayMaxCount")
                            .setLabel(maxCountLabel)
                            .toggledBy(enabledCheckBox)
                            .also { maxCountSpinner = it }
                    }

                    run { bindSpinners(minCountSpinner, maxCountSpinner) }
                }

                row {
                    lateinit var bracketsLabel: JLabel

                    cell {
                        JBLabel(Bundle("array.ui.brackets.option"))
                            .toggledBy(enabledCheckBox)
                            .also { bracketsLabel = it }
                    }

                    row {
                        run { bracketsGroup = ButtonGroup() }

                        cell {
                            JBRadioButton(Bundle("array.ui.brackets.none"))
                                .withName("arrayBracketsNone")
                                .withActionCommand("")
                                .inGroup(bracketsGroup)
                                .toggledBy(enabledCheckBox)
                        }

                        cell {
                            JBRadioButton("[]")
                                .withName("arrayBracketsSquare")
                                .withActionCommand("[@]")
                                .inGroup(bracketsGroup)
                                .toggledBy(enabledCheckBox)
                        }

                        cell {
                            JBRadioButton("{}")
                                .withName("arrayBracketsCurly")
                                .withActionCommand("{@}")
                                .inGroup(bracketsGroup)
                                .toggledBy(enabledCheckBox)
                        }

                        cell {
                            JBRadioButton("()")
                                .withName("arrayBracketsRound")
                                .withActionCommand("(@)")
                                .inGroup(bracketsGroup)
                                .toggledBy(enabledCheckBox)
                        }

                        cell {
                            VariableLabelRadioButton(UIConstants.SIZE_MEDIUM)
                                .withName("arrayBracketsCustom")
                                .also { it.addToButtonGroup(bracketsGroup) }
                                .toggledBy(enabledCheckBox)
                                .also { customBrackets = it }
                        }

                        cell {
                            ContextHelpLabel.create(Bundle("array.ui.brackets.comment"))
                                .toggledBy(enabledCheckBox)
                                .also { customBracketsHelpLabel = it }
                        }

                        run { bracketsGroup.setLabel(bracketsLabel) }
                    }
                }

                row {
                    lateinit var separatorLabel: JLabel

                    cell {
                        JBLabel(Bundle("array.ui.separator.option"))
                            .toggledBy(enabledCheckBox)
                            .also { separatorLabel = it }
                    }

                    row {
                        run { separatorGroup = ButtonGroup() }

                        cell {
                            JBRadioButton(Bundle("array.ui.separator.none"))
                                .withName("arraySeparatorNone")
                                .withActionCommand("")
                                .inGroup(separatorGroup)
                                .toggledBy(enabledCheckBox)
                        }

                        cell {
                            JBRadioButton(",")
                                .withName("arraySeparatorComma")
                                .inGroup(separatorGroup)
                                .toggledBy(enabledCheckBox)
                        }

                        cell {
                            JBRadioButton(";")
                                .withName("arraySeparatorSemicolon")
                                .inGroup(separatorGroup)
                                .toggledBy(enabledCheckBox)
                        }

                        cell {
                            JBRadioButton("""\n""")
                                .withName("arraySeparatorNewline")
                                .withActionCommand("\n")
                                .inGroup(separatorGroup)
                                .toggledBy(enabledCheckBox)
                                .also { newlineSeparatorButton = it }
                        }

                        cell {
                            VariableLabelRadioButton()
                                .withName("arraySeparatorCustom")
                                .also { it.addToButtonGroup(separatorGroup) }
                                .toggledBy(enabledCheckBox)
                                .also { customSeparator = it }
                        }

                        run { separatorGroup.setLabel(separatorLabel) }
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
