package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.SchemeEditor
import com.fwdekker.randomness.affix.AffixDecoratorEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.isEditable
import com.fwdekker.randomness.ui.loadMnemonic
import com.fwdekker.randomness.ui.withName
import com.fwdekker.randomness.uuid.UuidScheme.Companion.PRESET_AFFIX_DECORATOR_DESCRIPTORS
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import javax.swing.JList


/**
 * Component for editing a [UuidScheme].
 *
 * @param scheme the scheme to edit
 */
class UuidSchemeEditor(scheme: UuidScheme = UuidScheme()) : SchemeEditor<UuidScheme>(scheme) {
    override val rootComponent = panel {
        group(Bundle("uuid.ui.value.header")) {
            panel {
                row(Bundle("uuid.ui.value.version.option")) {
                    comboBox(UuidScheme.SUPPORTED_VERSIONS, UuidVersionRenderer())
                        .isEditable(false)
                        .withName("version")
                        .bindItem(scheme::version.toNullableProperty())
                }

                row {
                    checkBox(Bundle("uuid.ui.value.capitalization_option"))
                        .loadMnemonic()
                        .withName("isUppercase")
                        .bindSelected(scheme::isUppercase)
                }

                row {
                    checkBox(Bundle("uuid.add_dashes"))
                        .loadMnemonic()
                        .withName("addDashes")
                        .bindSelected(scheme::addDashes)
                }

                row {
                    AffixDecoratorEditor(scheme.affixDecorator, PRESET_AFFIX_DECORATOR_DESCRIPTORS)
                        .also { decoratorEditors += it }
                        .let { cell(it.rootComponent) }
                }
            }
        }

        row {
            ArrayDecoratorEditor(scheme.arrayDecorator)
                .also { decoratorEditors += it }
                .let { cell(it.rootComponent).align(AlignX.FILL) }
        }
    }


    init {
        reset()
    }


    /**
     * Renders a supported UUID version.
     */
    private class UuidVersionRenderer : ColoredListCellRenderer<Int>() {
        override fun customizeCellRenderer(
            list: JList<out Int>,
            value: Int?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean,
        ) {
            if (value == null) return

            append("$value")
            append("  ")
            append(Bundle("uuid.ui.value.version.$value"), SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
    }
}
