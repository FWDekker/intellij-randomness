package com.fwdekker.randomness.string

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.string.StringScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.ui.GridPanelBuilder
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.components.BrowserLink
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.util.ui.UI
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField


/**
 * Component for editing random string settings.
 *
 * @param scheme the scheme to edit in the component
 */
class StringSchemeEditor(scheme: StringScheme = StringScheme()) : StateEditor<StringScheme>(scheme) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = patternField

    private lateinit var patternField: JTextField
    private lateinit var isRegexCheckBox: JCheckBox
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var removeLookAlikeSymbolsCheckBox: JCheckBox
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        rootComponent = GridPanelBuilder.panel {
            textSeparator(Bundle("string.ui.value_separator"))

            panel {
                row {
                    lateinit var patternLabel: JLabel

                    cell {
                        JBLabel(Bundle("string.ui.pattern_option"))
                            .loadMnemonic()
                            .also { patternLabel = it }
                    }

                    row {
                        cell(constraints(fixedWidth = UIConstants.SIZE_VERY_LARGE)) {
                            JBTextField()
                                .withName("pattern")
                                .setLabel(patternLabel)
                                .also { patternField = it }
                        }

                        cell {
                            BrowserLink(
                                Bundle("string.ui.pattern_help"),
                                "https://github.com/curious-odd-man/RgxGen/tree/1.4#supported-syntax"
                            )
                        }
                    }
                }

                row {
                    skip()

                    cell {
                        JBCheckBox(Bundle("string.ui.is_regex_option"))
                            .withName("isRegex")
                            .loadMnemonic()
                            .also { isRegexCheckBox = it }
                    }
                }

                row {
                    lateinit var capitalizationLabel: JLabel

                    cell {
                        JBLabel(Bundle("string.ui.capitalization_option"))
                            .loadMnemonic()
                            .also { capitalizationLabel = it }
                    }

                    row {
                        run { capitalizationGroup = ButtonGroup() }

                        cell {
                            JBRadioButton(Bundle("shared.capitalization.retain"))
                                .withActionCommand("retain")
                                .withName("capitalizationRetain")
                                .inGroup(capitalizationGroup)
                        }

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

                        cell {
                            JBRadioButton(Bundle("shared.capitalization.random"))
                                .withActionCommand("random")
                                .withName("capitalizationRandom")
                                .inGroup(capitalizationGroup)
                        }

                        run { capitalizationGroup.setLabel(capitalizationLabel) }
                    }
                }

                row {
                    skip()

                    cell {
                        removeLookAlikeSymbolsCheckBox = JBCheckBox(Bundle("string.ui.remove_look_alike"))
                            .withName("removeLookAlikeCharacters")
                            .loadMnemonic()

                        UI.PanelFactory.panel(removeLookAlikeSymbolsCheckBox)
                            .withTooltip(
                                Bundle(
                                    "string.ui.remove_look_alike_help",
                                    StringScheme.LOOK_ALIKE_CHARACTERS
                                )
                            )
                            .createPanel()
                    }
                }
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


    override fun loadState(state: StringScheme) {
        super.loadState(state)

        patternField.text = state.pattern
        isRegexCheckBox.isSelected = state.isRegex
        capitalizationGroup.setValue(state.capitalization)
        removeLookAlikeSymbolsCheckBox.isSelected = state.removeLookAlikeSymbols

        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        StringScheme(
            pattern = patternField.text,
            isRegex = isRegexCheckBox.isSelected,
            capitalization = capitalizationGroup.getValue()?.let(::getMode) ?: DEFAULT_CAPITALIZATION,
            removeLookAlikeSymbols = removeLookAlikeSymbolsCheckBox.isSelected,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            patternField, isRegexCheckBox, capitalizationGroup, arrayDecoratorEditor,
            listener = listener
        )
}
