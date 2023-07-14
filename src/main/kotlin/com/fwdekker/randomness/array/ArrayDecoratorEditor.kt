package com.fwdekker.randomness.array

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecorator.Companion.DEFAULT_BRACKETS
import com.fwdekker.randomness.array.ArrayDecorator.Companion.DEFAULT_SEPARATOR
import com.fwdekker.randomness.array.ArrayDecorator.Companion.MIN_MIN_COUNT
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.add
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.indentedIf
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.ui.withFixedWidth
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.Label
import com.intellij.ui.dsl.builder.EMPTY_LABEL
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.intellij.util.ui.DialogUtil
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JRadioButton


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
        rootComponent = panel {
            separator(Bundle("array.title"))
                .topGap(TopGap.MEDIUM)
                .visible(!embedded)

            indentedIf(!embedded) {
                row {
                    checkBox(Bundle("array.ui.enabled"))
                        .also { DialogUtil.registerMnemonic(it.component, '&') }
                        .also { it.component.name = "arrayEnabled" }
                        .also { enabledCheckBox = it.component }
                }.visible(!embedded)

                indentedIf(!embedded) {
                    row(Bundle("array.ui.min_count_option")) {
                        cell(JIntSpinner(value = MIN_MIN_COUNT, minValue = MIN_MIN_COUNT))
                            .withFixedWidth(UIConstants.SIZE_SMALL)
                            .also { it.component.name = "arrayMinCount" }
                            .also { minCountSpinner = it.component }
                    }

                    row(Bundle("array.ui.max_count_option")) {
                        cell(JIntSpinner(value = MIN_MIN_COUNT, minValue = MIN_MIN_COUNT))
                            .withFixedWidth(UIConstants.SIZE_SMALL)
                            .also { it.component.name = "arrayMaxCount" }
                            .also { maxCountSpinner = it.component }
                    }

                    bindSpinners(minCountSpinner, maxCountSpinner)

                    val bracketsLabel = Label(Bundle("array.ui.brackets.option"))
                    row(bracketsLabel) {
                        bracketsGroup = ButtonGroup()

                        cell(JBRadioButton(Bundle("shared.option.none")))
                            .also { it.component.actionCommand = "" }
                            .also { it.component.name = "arrayBracketsNone" }
                            .also { bracketsGroup.add(it.component) }
                        cell(JBRadioButton("[]"))
                            .also { it.component.actionCommand = "[@]" }
                            .also { it.component.name = "arrayBracketsSquare" }
                            .also { bracketsGroup.add(it.component) }
                        cell(JBRadioButton("{}"))
                            .also { it.component.actionCommand = "{@}" }
                            .also { it.component.name = "arrayBracketsCurly" }
                            .also { bracketsGroup.add(it.component) }
                        cell(JBRadioButton("()"))
                            .also { it.component.actionCommand = "(@)" }
                            .also { it.component.name = "arrayBracketsRound" }
                            .also { bracketsGroup.add(it.component) }
                        cell(VariableLabelRadioButton(UIConstants.SIZE_MEDIUM))
                            .also { it.component.name = "arrayBracketsCustom" }
                            .also { bracketsGroup.add(it.component) }
                            .also { customBrackets = it.component }
                        contextHelp(Bundle("array.ui.brackets.comment"))

                        bracketsGroup.setLabel(bracketsLabel)
                    }

                    val separatorLabel = Label(Bundle("array.ui.separator.option"))
                    row(separatorLabel) {
                        separatorGroup = ButtonGroup()

                        cell(JBRadioButton(Bundle("shared.option.none")))
                            .also { it.component.actionCommand = "" }
                            .also { it.component.name = "arraySeparatorNone" }
                            .also { separatorGroup.add(it.component) }
                        cell(JBRadioButton(","))
                            .also { it.component.name = "arraySeparatorComma" }
                            .also { separatorGroup.add(it.component) }
                        cell(JBRadioButton(";"))
                            .also { it.component.name = "arraySeparatorSemicolon" }
                            .also { separatorGroup.add(it.component) }
                        cell(JBRadioButton("\\n"))
                            .also { it.component.actionCommand = "\n" }
                            .also { it.component.name = "arraySeparatorNewline" }
                            .also { separatorGroup.add(it.component) }
                            .also { newlineSeparatorButton = it.component }
                        cell(VariableLabelRadioButton())
                            .also { it.component.name = "arraySeparatorCustom" }
                            .also { separatorGroup.add(it.component) }
                            .also { customSeparator = it.component }

                        separatorGroup.setLabel(separatorLabel)
                    }

                    row(EMPTY_LABEL) {
                        checkBox(Bundle("array.ui.space_after_separator"))
                            .enabledIf(newlineSeparatorButton.selected.not())
                            .also { it.component.name = "arraySpaceAfterSeparator" }
                            .also { spaceAfterSeparatorCheckBox = it.component }
                    }
                }.enabledIf(enabledCheckBox.selected)
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
