package com.fwdekker.randomness.integer

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.fixedlength.FixedLengthDecoratorEditor
import com.fwdekker.randomness.integer.IntegerScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.integer.IntegerScheme.Companion.DEFAULT_GROUPING_SEPARATOR
import com.fwdekker.randomness.ui.GridPanelBuilder
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JLongSpinner
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.forEach
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.uiDesigner.core.GridConstraints
import javax.swing.ButtonGroup
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.ChangeEvent


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
        rootComponent = GridPanelBuilder.panel {
            textSeparator(Bundle("integer.ui.value_separator"))

            panel {
                row {
                    lateinit var minValueLabel: JLabel

                    cell {
                        JBLabel(Bundle("integer.ui.min_value_option"))
                            .also { minValueLabel = it }
                    }

                    cell(constraints(fixedWidth = UIConstants.SIZE_LARGE)) {
                        JLongSpinner()
                            .withName("minValue")
                            .setLabel(minValueLabel)
                            .also { minValue = it }
                    }
                }

                row {
                    lateinit var maxValueLabel: JLabel

                    cell {
                        JBLabel(Bundle("integer.ui.max_value_option"))
                            .also { maxValueLabel = it }
                    }

                    cell(constraints(fixedWidth = UIConstants.SIZE_LARGE)) {
                        JLongSpinner()
                            .withName("maxValue")
                            .setLabel(maxValueLabel)
                            .also { maxValue = it }
                    }

                    run { bindSpinners(minValue, maxValue, maxRange = null) }
                }
            }

            vSeparator()

            panel {
                row {
                    lateinit var baseLabel: JLabel

                    cell {
                        JBLabel(Bundle("integer.ui.base_option"))
                            .also { baseLabel = it }
                    }

                    cell(constraints(fixedWidth = UIConstants.SIZE_SMALL)) {
                        JIntSpinner(IntegerScheme.DECIMAL_BASE, IntegerScheme.MIN_BASE, IntegerScheme.MAX_BASE)
                            .withName("base")
                            .setLabel(baseLabel)
                            .also { base = it }
                    }
                }

                row {
                    lateinit var groupingSeparatorLabel: JLabel

                    cell {
                        JBLabel(Bundle("integer.ui.grouping_separator.option"))
                            .loadMnemonic()
                            .also { groupingSeparatorLabel = it }
                    }

                    row {
                        run { groupingSeparatorGroup = ButtonGroup() }

                        cell {
                            JBRadioButton(Bundle("integer.ui.grouping_separator.none"))
                                .withName("groupingSeparatorNone")
                                .withActionCommand("")
                                .inGroup(groupingSeparatorGroup)
                        }

                        cell {
                            JBRadioButton(".")
                                .withName("groupingSeparatorPeriod")
                                .inGroup(groupingSeparatorGroup)
                        }

                        cell {
                            JBRadioButton(",")
                                .withName("groupingSeparatorComma")
                                .inGroup(groupingSeparatorGroup)
                        }

                        cell {
                            JBRadioButton("_")
                                .withName("groupingSeparatorUnderscore")
                                .inGroup(groupingSeparatorGroup)
                        }

                        cell {
                            VariableLabelRadioButton(UIConstants.SIZE_TINY, MaxLengthDocumentFilter(1))
                                .withName("groupingSeparatorCustom")
                                .also { it.addToButtonGroup(groupingSeparatorGroup) }
                                .also { customGroupingSeparator = it }
                        }

                        run { groupingSeparatorGroup.setLabel(groupingSeparatorLabel) }
                    }
                }

                row {
                    lateinit var capitalizationLabel: JLabel

                    cell {
                        JBLabel(Bundle("integer.ui.capitalization_option"))
                            .loadMnemonic()
                            .also { capitalizationLabel = it }
                    }

                    row {
                        run { capitalizationGroup = ButtonGroup() }

                        cell {
                            @Suppress("DialogTitleCapitalization") // Intentional
                            JBRadioButton(Bundle("shared.capitalization.lower"))
                                .withActionCommand("lower")
                                .withName("capitalizationLower")
                                .inGroup(capitalizationGroup)
                        }

                        cell {
                            JBRadioButton(Bundle("shared.capitalization.upper"))
                                .withActionCommand("upper")
                                .withName("capitalizationUpper")
                                .inGroup(capitalizationGroup)
                        }

                        run { capitalizationGroup.setLabel(capitalizationLabel) }

                        // TODO: Find way to add `run` elsewhere, to make relation clearer!
                        //  (Add listeners after creating panel?)
                        run {
                            base.addChangeListener(
                                { _: ChangeEvent? ->
                                    groupingSeparatorGroup.forEach {
                                        it.isEnabled = base.value == IntegerScheme.DECIMAL_BASE
                                    }
                                    customGroupingSeparator.isEnabled = base.value == IntegerScheme.DECIMAL_BASE

                                    capitalizationGroup.forEach {
                                        it.isEnabled = base.value > IntegerScheme.DECIMAL_BASE
                                    }
                                }.also { it(null) }
                            )
                        }
                    }
                }
            }

            vSeparator()

            panel {
                row {
                    lateinit var prefixLabel: JLabel

                    cell {
                        JBLabel(Bundle("integer.ui.prefix_option"))
                            .also { prefixLabel = it }
                    }

                    cell(constraints(fixedWidth = UIConstants.SIZE_SMALL)) {
                        JBTextField()
                            .withName("prefix")
                            .setLabel(prefixLabel)
                            .also { prefixInput = it }
                    }
                }

                row {
                    lateinit var suffixLabel: JLabel

                    cell {
                        JBLabel(Bundle("integer.ui.suffix_option"))
                            .also { suffixLabel = it }
                    }

                    cell(constraints(fixedWidth = UIConstants.SIZE_SMALL)) {
                        JBTextField()
                            .withName("suffix")
                            .setLabel(suffixLabel)
                            .also { suffixInput = it }
                    }
                }
            }

            vSeparator()

            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                FixedLengthDecoratorEditor(originalState.fixedLengthDecorator)
                    .also { fixedLengthDecoratorEditor = it }
                    .rootComponent
            }

            vSeparator()

            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                ArrayDecoratorEditor(originalState.arrayDecorator)
                    .also { arrayDecoratorEditor = it }
                    .rootComponent
            }

            vSpacer()
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
