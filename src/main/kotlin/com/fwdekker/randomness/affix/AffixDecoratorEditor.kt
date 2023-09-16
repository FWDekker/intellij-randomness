package com.fwdekker.randomness.affix

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.SchemeEditor
import com.fwdekker.randomness.ui.bindCurrentText
import com.fwdekker.randomness.ui.disableMnemonic
import com.fwdekker.randomness.ui.isEditable
import com.fwdekker.randomness.ui.loadMnemonic
import com.fwdekker.randomness.ui.withName
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.and
import java.util.Locale
import javax.swing.JCheckBox


/**
 * Component for editing an [AffixDecorator].
 *
 * @param scheme the scheme to edit
 * @param presets the default affixation options available to the user in the editor
 * @param enabledIf the predicate that determines whether the components in this editor are enabled
 * @param enableMnemonic whether to enable mnemonics
 * @param namePrefix the string to prepend to all component names
 */
class AffixDecoratorEditor(
    scheme: AffixDecorator,
    presets: Collection<String>,
    enabledIf: ComponentPredicate? = null,
    enableMnemonic: Boolean = true,
    namePrefix: String = "",
) : SchemeEditor<AffixDecorator>(scheme) {
    override val rootComponent = panel {
        row {
            lateinit var enabledCheckBox: Cell<JCheckBox>

            checkBox(Bundle("affix.ui.option"))
                .let { if (enableMnemonic) it.loadMnemonic() else it.disableMnemonic() }
                .withName(camelConcat(namePrefix, "affixEnabled"))
                .bindSelected(scheme::enabled)
                .also { enabledCheckBox = it }

            cell(ComboBox(presets.toTypedArray()))
                .enabledIf(enabledIf?.and(enabledCheckBox.selected) ?: enabledCheckBox.selected)
                .isEditable(true)
                .withName(camelConcat(namePrefix, "affixDescriptor"))
                .bindCurrentText(scheme::descriptor)
            contextHelp(Bundle("affix.ui.comment"))
        }.also { if (enabledIf != null) it.enabledIf(enabledIf) }
    }
}


/**
 * Prefixes [name] with [prefix], and replaces the first character of [name] with its uppercase variant if [prefix]
 * is not the empty string.
 *
 * @param prefix the string to prepend to [name]
 * @param name the string to prepend by [name]
 * @return the camelCase concatenation of [prefix] and [name]
 */
private fun camelConcat(prefix: String, name: String) =
    if (prefix == "") name
    else "${prefix}${name[0].uppercase(Locale.getDefault())}${name.drop(1)}"
