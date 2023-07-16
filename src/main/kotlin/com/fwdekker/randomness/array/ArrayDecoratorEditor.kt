package com.fwdekker.randomness.array

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecorator.Companion.MIN_MIN_COUNT
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.StringComboBox
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.hasItem
import com.fwdekker.randomness.ui.indentedIf
import com.fwdekker.randomness.ui.loadMnemonic
import com.fwdekker.randomness.ui.withFixedWidth
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.EMPTY_LABEL
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.and
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox
import javax.swing.JPanel


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
    private lateinit var bracketsComboBox: ComboBox<String>
    private lateinit var separatorComboBox: ComboBox<String>
    private lateinit var spaceAfterSeparatorCheckBox: JCheckBox


    init {
        rootComponent = panel {
            separator(Bundle("array.title"))
                .topGap(TopGap.MEDIUM)
                .visible(!embedded)

            indentedIf(!embedded) {
                row {
                    checkBox(Bundle("array.ui.enabled"))
                        .loadMnemonic()
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

                    row(Bundle("array.ui.brackets.option")) {
                        cell(StringComboBox(listOf("", "[@]", "{@}", "(@)")))
                            .also { it.component.isEditable = true }
                            .also { it.component.name = "arrayBrackets" }
                            .also { bracketsComboBox = it.component }
                        contextHelp(Bundle("array.ui.brackets.comment"))
                    }

                    row(Bundle("array.ui.separator.option")) {
                        cell(StringComboBox(listOf("", ",", ";", "\\n")))
                            .also { it.component.isEditable = true }
                            .also { it.component.name = "arraySeparator" }
                            .also { separatorComboBox = it.component }
                    }

                    row(EMPTY_LABEL) {
                        checkBox(Bundle("array.ui.space_after_separator"))
                            .enabledIf(enabledCheckBox.selected.and(separatorComboBox.hasItem { it == "\\n" }.not()))
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
        bracketsComboBox.item = state.brackets
        separatorComboBox.item = state.separator
        spaceAfterSeparatorCheckBox.isSelected = state.isSpaceAfterSeparator
    }

    override fun readState(): ArrayDecorator =
        ArrayDecorator(
            enabled = !embedded && enabledCheckBox.isSelected,
            minCount = minCountSpinner.value,
            maxCount = maxCountSpinner.value,
            brackets = bracketsComboBox.item,
            separator = separatorComboBox.item,
            isSpaceAfterSeparator = spaceAfterSeparatorCheckBox.isSelected
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            enabledCheckBox, minCountSpinner, maxCountSpinner, bracketsComboBox, separatorComboBox,
            spaceAfterSeparatorCheckBox,
            listener = listener
        )
}
