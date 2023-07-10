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
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.components.BrowserLink
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.util.ui.UI
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
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
            textSeparatorCell(Bundle("string.ui.value_separator"))

            panel {
                row {
                    cell { label("patternLabel", Bundle("string.ui.pattern_option")) }

                    row {
                        cell(constraints(fixedWidth = UIConstants.SIZE_VERY_LARGE)) {
                            JBTextField()
                                .withName("pattern")
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
                            .also { isRegexCheckBox = it }
                    }
                }

                row {
                    cell { label("capitalizationLabel", Bundle("string.ui.capitalization_option")) }

                    row {
                        capitalizationGroup = ButtonGroup()

                        cell { radioButton("capitalizationRetain", Bundle("shared.capitalization.retain"), "retain") }
                        cell { radioButton("capitalizationLower", Bundle("shared.capitalization.lower"), "lower") }
                        cell { radioButton("capitalizationUpper", Bundle("shared.capitalization.upper"), "upper") }
                        cell { radioButton("capitalizationRandom", Bundle("shared.capitalization.random"), "random") }
                    }
                }

                row {
                    skip()

                    cell {
                        JBCheckBox(Bundle("string.ui.remove_look_alike"))
                            .withName("removeLookAlikeCharacters")
                            .also { removeLookAlikeSymbolsCheckBox = it }

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

            vSeparatorCell()

            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                ArrayDecoratorEditor(originalState.arrayDecorator)
                    .also { arrayDecoratorEditor = it }
                    .rootComponent
            }

            vSpacerCell()
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
