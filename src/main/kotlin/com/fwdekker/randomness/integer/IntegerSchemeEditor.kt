package com.fwdekker.randomness.integer

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.affix.AffixDecoratorEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.fixedlength.FixedLengthDecoratorEditor
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JLongSpinner
import com.fwdekker.randomness.ui.MinMaxLengthDocumentFilter
import com.fwdekker.randomness.ui.StringComboBox
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.hasValue
import com.fwdekker.randomness.ui.loadMnemonic
import com.fwdekker.randomness.ui.withFixedWidth
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.and
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox
import javax.swing.JPanel


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
    private lateinit var groupingSeparatorEnabledCheckBox: JCheckBox
    private lateinit var groupingSeparatorComboBox: ComboBox<String>
    private lateinit var isUppercaseCheckBox: JCheckBox
    private lateinit var affixDecoratorEditor: AffixDecoratorEditor
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

                row {
                    checkBox(Bundle("integer.ui.format.grouping_separator_option"))
                        .loadMnemonic()
                        .also { it.component.name = "groupingSeparatorEnabled" }
                        .also { groupingSeparatorEnabledCheckBox = it.component }

                    cell(StringComboBox(listOf(".", ",", "_"), MinMaxLengthDocumentFilter(1, 1)))
                        .enabledIf(
                            base.hasValue { it == IntegerScheme.DECIMAL_BASE }
                                .and(groupingSeparatorEnabledCheckBox.selected)
                        )
                        .also { it.component.isEditable = true }
                        .also { it.component.name = "groupingSeparator" }
                        .also { groupingSeparatorComboBox = it.component }
                }.enabledIf(base.hasValue { it == IntegerScheme.DECIMAL_BASE })

                row {
                    checkBox(Bundle("integer.ui.format.uppercase_option"))
                        .loadMnemonic()
                        .also { it.component.name = "isUppercase" }
                        .also { isUppercaseCheckBox = it.component }
                }

                row {
                    // TODO: Check how alignment works
                    affixDecoratorEditor = AffixDecoratorEditor(originalState.affixDecorator, enableMnemonic = true)
                    cell(affixDecoratorEditor.rootComponent)
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
        groupingSeparatorEnabledCheckBox.isSelected = state.groupingSeparatorEnabled
        groupingSeparatorComboBox.item = state.groupingSeparator
        isUppercaseCheckBox.isSelected = state.isUppercase
        affixDecoratorEditor.loadState(state.affixDecorator)
        fixedLengthDecoratorEditor.loadState(state.fixedLengthDecorator)
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        IntegerScheme(
            minValue = minValue.value,
            maxValue = maxValue.value,
            base = base.value,
            groupingSeparatorEnabled = groupingSeparatorEnabledCheckBox.isSelected,
            groupingSeparator = groupingSeparatorComboBox.item,
            isUppercase = isUppercaseCheckBox.isSelected,
            affixDecorator = affixDecoratorEditor.readState(),
            fixedLengthDecorator = fixedLengthDecoratorEditor.readState(),
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }

    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minValue, maxValue, base, groupingSeparatorEnabledCheckBox, groupingSeparatorComboBox, isUppercaseCheckBox,
            affixDecoratorEditor, fixedLengthDecoratorEditor, arrayDecoratorEditor,
            listener = listener
        )
}
