package com.fwdekker.randomness.string

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.CapitalizationComboBox
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.withFixedWidth
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.EMPTY_LABEL
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.util.ui.DialogUtil
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
    private lateinit var removeLookAlikeSymbolsCheckBox: JCheckBox
    private lateinit var capitalizationComboBox: ComboBox<CapitalizationMode>
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        rootComponent = panel {
            group(Bundle("string.ui.value.header")) {
                row(Bundle("string.ui.value.pattern_option")) {
                    textField()
                        .withFixedWidth(UIConstants.SIZE_VERY_LARGE)
                        .also { it.component.name = "pattern" }
                        .also { patternField = it.component }

                    browserLink(Bundle("string.ui.value.pattern_help"), Bundle("string.ui.value.pattern_help_url"))
                }

                row(EMPTY_LABEL) {
                    checkBox(Bundle("string.ui.value.is_regex_option"))
                        .also { DialogUtil.registerMnemonic(it.component, '&') }
                        .also { it.component.name = "isRegex" }
                        .also { isRegexCheckBox = it.component }
                }

                row(EMPTY_LABEL) {
                    checkBox(Bundle("string.ui.value.remove_look_alike"))
                        .also { DialogUtil.registerMnemonic(it.component, '&') }
                        .also { it.component.name = "removeLookAlikeCharacters" }
                        .also { removeLookAlikeSymbolsCheckBox = it.component }

                    contextHelp(Bundle("string.ui.value.remove_look_alike_help", StringScheme.LOOK_ALIKE_CHARACTERS))
                }.bottomGap(BottomGap.SMALL)

                row(Bundle("string.ui.value.capitalization_option")) {
                    cell(
                        CapitalizationComboBox(
                            listOf(
                                CapitalizationMode.RETAIN,
                                CapitalizationMode.LOWER,
                                CapitalizationMode.UPPER,
                                CapitalizationMode.RANDOM,
                            )
                        )
                    )
                        .also { it.component.name = "capitalization" }
                        .also { capitalizationComboBox = it.component }
                }
            }

            row {
                arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
                cell(arrayDecoratorEditor.rootComponent).horizontalAlign(HorizontalAlign.FILL)
            }
        }

        loadState()
    }


    override fun loadState(state: StringScheme) {
        super.loadState(state)

        patternField.text = state.pattern
        isRegexCheckBox.isSelected = state.isRegex
        removeLookAlikeSymbolsCheckBox.isSelected = state.removeLookAlikeSymbols
        capitalizationComboBox.item = state.capitalization

        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        StringScheme(
            pattern = patternField.text,
            isRegex = isRegexCheckBox.isSelected,
            removeLookAlikeSymbols = removeLookAlikeSymbolsCheckBox.isSelected,
            capitalization = capitalizationComboBox.item,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            patternField, isRegexCheckBox, removeLookAlikeSymbolsCheckBox, capitalizationComboBox, arrayDecoratorEditor,
            listener = listener
        )
}
