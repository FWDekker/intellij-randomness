package com.fwdekker.randomness.string

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.SchemeEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.string.StringScheme.Companion.PRESET_CAPITALIZATION
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.loadMnemonic
import com.fwdekker.randomness.ui.withFixedWidth
import com.fwdekker.randomness.ui.withName
import com.fwdekker.randomness.ui.withSimpleRenderer
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.ui.dsl.gridLayout.HorizontalAlign


/**
 * Component for editing a [StringScheme].
 *
 * @param scheme the scheme to edit
 */
class StringSchemeEditor(scheme: StringScheme = StringScheme()) : SchemeEditor<StringScheme>(scheme) {
    override val rootComponent = panel {
        group(Bundle("string.ui.value.header")) {
            row(Bundle("string.ui.value.pattern_option")) {
                textField()
                    .withFixedWidth(UIConstants.SIZE_VERY_LARGE)
                    .withName("pattern")
                    .bindText(scheme::pattern)

                browserLink(Bundle("string.ui.value.pattern_help"), Bundle("string.ui.value.pattern_help_url"))
            }

            row("") {
                checkBox(Bundle("string.ui.value.is_regex_option"))
                    .loadMnemonic()
                    .withName("isRegex")
                    .bindSelected(scheme::isRegex)
            }

            row("") {
                checkBox(Bundle("string.ui.value.remove_look_alike"))
                    .loadMnemonic()
                    .withName("removeLookAlikeCharacters")
                    .bindSelected(scheme::removeLookAlikeSymbols)

                contextHelp(Bundle("string.ui.value.remove_look_alike_help", StringScheme.LOOK_ALIKE_CHARACTERS))
            }.bottomGap(BottomGap.SMALL)

            row(Bundle("string.ui.value.capitalization_option")) {
                cell(ComboBox(PRESET_CAPITALIZATION))
                    .withSimpleRenderer(CapitalizationMode::toLocalizedString)
                    .withName("capitalization")
                    .bindItem(scheme::capitalization.toNullableProperty())
            }
        }

        row {
            ArrayDecoratorEditor(scheme.arrayDecorator)
                .also { decoratorEditors += it }
                .let { cell(it.rootComponent).horizontalAlign(HorizontalAlign.FILL) }
        }
    }


    init {
        reset()
    }
}
