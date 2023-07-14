package com.fwdekker.randomness.integer

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.fixedlength.FixedLengthDecoratorEditor
import com.fwdekker.randomness.integer.IntegerScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.integer.IntegerScheme.Companion.DEFAULT_GROUPING_SEPARATOR
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JLongSpinner
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.add
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.hasValue
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.ui.withFixedWidth
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.Label
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JTextField


/**
 * Component for editing random integer settings.
 *
 * @param scheme the scheme to edit in the component
 */
class IntegerSchemeEditor(scheme: IntegerScheme = IntegerScheme()) : StateEditor<IntegerScheme>(scheme) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = minValue.editorComponent

    private lateinit var minValue: JLongSpinner
    private lateinit var maxValue: JLongSpinner
    private lateinit var base: JIntSpinner
    private lateinit var groupingSeparatorGroup: ButtonGroup
    private lateinit var customGroupingSeparator: VariableLabelRadioButton
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var prefixInput: JTextField
    private lateinit var suffixInput: JTextField
    private lateinit var fixedLengthDecoratorEditor: FixedLengthDecoratorEditor
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        rootComponent = panel {
            group(Bundle("integer.ui.value.header")) {
                row(Bundle("integer.ui.value.min_option")) {
                    cell(JLongSpinner())
                        .withFixedWidth(UIConstants.SIZE_LARGE)
                        .also { it.component.name = "minValue" }
                        .also { minValue = it.component }
                }

                row(Bundle("integer.ui.value.max_option")) {
                    cell(JLongSpinner())
                        .withFixedWidth(UIConstants.SIZE_LARGE)
                        .also { it.component.name = "maxValue" }
                        .also { maxValue = it.component }
                }

                bindSpinners(minValue, maxValue, maxRange = null)
            }

            group(Bundle("integer.ui.format.header")) {
                row(Bundle("integer.ui.format.base_option")) {
                    cell(JIntSpinner(IntegerScheme.DECIMAL_BASE, IntegerScheme.MIN_BASE, IntegerScheme.MAX_BASE))
                        .withFixedWidth(UIConstants.SIZE_SMALL)
                        .also { it.component.name = "base" }
                        .also { base = it.component }
                }

                val groupingSeparatorLabel = Label(Bundle("integer.ui.format.grouping_separator_option"))
                row(groupingSeparatorLabel) {
                    groupingSeparatorGroup = ButtonGroup()

                    cell(JBRadioButton(Bundle("shared.option.none")))
                        .also { it.component.actionCommand = "" }
                        .also { it.component.name = "groupingSeparatorNone" }
                        .also { groupingSeparatorGroup.add(it.component) }
                    cell(JBRadioButton("."))
                        .also { it.component.name = "groupingSeparatorPeriod" }
                        .also { groupingSeparatorGroup.add(it.component) }
                    cell(JBRadioButton(","))
                        .also { it.component.name = "groupingSeparatorComma" }
                        .also { groupingSeparatorGroup.add(it.component) }
                    cell(JBRadioButton("_"))
                        .also { it.component.name = "groupingSeparatorUnderscore" }
                        .also { groupingSeparatorGroup.add(it.component) }
                    cell(VariableLabelRadioButton(UIConstants.SIZE_TINY, MaxLengthDocumentFilter(1)))
                        .also { it.component.name = "groupingSeparatorCustom" }
                        .also { groupingSeparatorGroup.add(it.component) }
                        .also { customGroupingSeparator = it.component }

                    groupingSeparatorGroup.setLabel(groupingSeparatorLabel)
                }.enabledIf(base.hasValue { it == IntegerScheme.DECIMAL_BASE })

                val capitalizationLabel = Label(Bundle("integer.ui.format.capitalization_option"))
                row(capitalizationLabel) {
                    capitalizationGroup = ButtonGroup()

                    @Suppress("DialogTitleCapitalization") // Intentional
                    cell(JBRadioButton(Bundle("shared.capitalization.lower")))
                        .also { it.component.actionCommand = "lower" }
                        .also { it.component.name = "capitalizationLower" }
                        .also { capitalizationGroup.add(it.component) }
                    cell(JBRadioButton(Bundle("shared.capitalization.upper")))
                        .also { it.component.actionCommand = "upper" }
                        .also { it.component.name = "capitalizationUpper" }
                        .also { capitalizationGroup.add(it.component) }

                    capitalizationGroup.setLabel(capitalizationLabel)
                }.enabledIf(base.hasValue { it > IntegerScheme.DECIMAL_BASE })
            }

            group(Bundle("integer.ui.affixes.header")) {
                row(Bundle("integer.ui.affixes.prefix_option")) {
                    textField()
                        .withFixedWidth(UIConstants.SIZE_SMALL)
                        .also { it.component.name = "prefix" }
                        .also { prefixInput = it.component }
                }

                row(Bundle("integer.ui.affixes.suffix_option")) {
                    textField()
                        .withFixedWidth(UIConstants.SIZE_SMALL)
                        .also { it.component.name = "suffix" }
                        .also { suffixInput = it.component }
                }
            }

            row {
                fixedLengthDecoratorEditor = FixedLengthDecoratorEditor(originalState.fixedLengthDecorator)
                cell(fixedLengthDecoratorEditor.rootComponent).horizontalAlign(HorizontalAlign.FILL)
            }

            row {
                arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
                cell(arrayDecoratorEditor.rootComponent).horizontalAlign(HorizontalAlign.FILL)
            }
        }

        loadState()
    }


    override fun loadState(state: IntegerScheme) {
        super.loadState(state)

        minValue.value = state.minValue
        maxValue.value = state.maxValue
        base.value = state.base
        customGroupingSeparator.label = state.customGroupingSeparator
        groupingSeparatorGroup.setValue(state.groupingSeparator)
        capitalizationGroup.setValue(state.capitalization)
        prefixInput.text = state.prefix
        suffixInput.text = state.suffix
        fixedLengthDecoratorEditor.loadState(state.fixedLengthDecorator)
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        IntegerScheme(
            minValue = minValue.value,
            maxValue = maxValue.value,
            base = base.value,
            groupingSeparator = groupingSeparatorGroup.getValue() ?: DEFAULT_GROUPING_SEPARATOR,
            customGroupingSeparator = customGroupingSeparator.label,
            capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION,
            prefix = prefixInput.text,
            suffix = suffixInput.text,
            fixedLengthDecorator = fixedLengthDecoratorEditor.readState(),
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }

    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minValue, maxValue, base, groupingSeparatorGroup, customGroupingSeparator, capitalizationGroup, prefixInput,
            suffixInput, fixedLengthDecoratorEditor, arrayDecoratorEditor,
            listener = listener
        )
}
