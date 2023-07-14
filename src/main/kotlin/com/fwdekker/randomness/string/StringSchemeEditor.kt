package com.fwdekker.randomness.string

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.string.StringScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.ui.withFixedWidth
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.Label
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.EMPTY_LABEL
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.util.ui.DialogUtil
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
    private lateinit var removeLookAlikeSymbolsCheckBox: JCheckBox
    private lateinit var capitalizationGroup: ButtonGroup
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

                val capitalizationLabel = Label(Bundle("string.ui.value.capitalization_option"))
                row(capitalizationLabel) {
                    capitalizationGroup = ButtonGroup()

                    cell(JBRadioButton(Bundle("shared.capitalization.retain")))
                        .also { it.component.actionCommand = "retain" }
                        .also { it.component.name = "capitalizationRetain" }
                        .also { capitalizationGroup.add(it.component) }
                    @Suppress("DialogTitleCapitalization") // Intentional
                    cell(JBRadioButton(Bundle("shared.capitalization.lower")))
                        .also { it.component.actionCommand = "lower" }
                        .also { it.component.name = "capitalizationLower" }
                        .also { capitalizationGroup.add(it.component) }
                    cell(JBRadioButton(Bundle("shared.capitalization.upper")))
                        .also { it.component.actionCommand = "upper" }
                        .also { it.component.name = "capitalizationUpper" }
                        .also { capitalizationGroup.add(it.component) }
                    cell(JBRadioButton(Bundle("shared.capitalization.random")))
                        .also { it.component.actionCommand = "random" }
                        .also { it.component.name = "capitalizationRandom" }
                        .also { capitalizationGroup.add(it.component) }

                    capitalizationGroup.setLabel(capitalizationLabel)
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
        capitalizationGroup.setValue(state.capitalization)

        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        StringScheme(
            pattern = patternField.text,
            isRegex = isRegexCheckBox.isSelected,
            removeLookAlikeSymbols = removeLookAlikeSymbolsCheckBox.isSelected,
            capitalization = capitalizationGroup.getValue()?.let(::getMode) ?: DEFAULT_CAPITALIZATION,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            patternField, isRegexCheckBox, removeLookAlikeSymbolsCheckBox, capitalizationGroup, arrayDecoratorEditor,
            listener = listener
        )
}
