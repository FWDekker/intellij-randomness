package com.fwdekker.randomness.affix

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.ui.StringComboBox
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.disableMnemonic
import com.fwdekker.randomness.ui.loadMnemonic
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.and
import com.intellij.ui.layout.selected
import java.util.Locale
import javax.swing.JCheckBox
import javax.swing.JPanel


/**
 * Component for settings of affixation decoration.
 *
 * @param settings the settings to edit in the component
 * @param enabledIf the predicate that determines whether the components in this editor are enabled
 * @param enableMnemonic whether to enable mnemonics
 * @param namePrefix the string to prepend to all component names
 */
class AffixDecoratorEditor(
    settings: AffixDecorator,
    enabledIf: ComponentPredicate? = null,
    enableMnemonic: Boolean = false,
    namePrefix: String = "",
) : StateEditor<AffixDecorator>(settings) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = enabledCheckBox

    private lateinit var enabledCheckBox: JCheckBox
    private lateinit var descriptorInput: ComboBox<String>


    init {
        rootComponent = panel {
            row {
                checkBox(Bundle("affix.ui.option"))
                    .let { if (enableMnemonic) it.loadMnemonic() else it.disableMnemonic() }
                    .also { it.component.name = camelConcat(namePrefix, "affixEnabled") }
                    .also { enabledCheckBox = it.component }

                cell(StringComboBox(listOf("@b", "$@", "0x@")))
                    .enabledIf(enabledIf?.and(enabledCheckBox.selected) ?: enabledCheckBox.selected)
                    .also { it.component.isEditable = true }
                    .also { it.component.name = camelConcat(namePrefix, "affixDescriptor") }
                    .also { descriptorInput = it.component }
                contextHelp(Bundle("affix.ui.comment"))
            }.also { if (enabledIf != null) it.enabledIf(enabledIf) }
        }

        loadState()
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
        if (prefix != "") "${prefix}${name[0].uppercase(Locale.getDefault())}${name.drop(1)}"
        else name


    override fun loadState(state: AffixDecorator) {
        super.loadState(state)

        enabledCheckBox.isSelected = state.enabled
        descriptorInput.item = state.descriptor
    }

    override fun readState() =
        AffixDecorator(
            enabled = enabledCheckBox.isSelected,
            descriptor = descriptorInput.item,
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(enabledCheckBox, descriptorInput, listener = listener)
}
