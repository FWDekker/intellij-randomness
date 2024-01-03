package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.SchemeEditor
import com.fwdekker.randomness.affix.AffixDecoratorEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.bindItemNotNull
import com.fwdekker.randomness.ui.isEditable
import com.fwdekker.randomness.ui.loadMnemonic
import com.fwdekker.randomness.ui.withName
import com.fwdekker.randomness.ui.withSimpleRenderer
import com.fwdekker.randomness.uuid.UuidScheme.Companion.PRESET_AFFIX_DECORATOR_DESCRIPTORS
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel


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
                    cell(ComboBox(UuidScheme.SUPPORTED_VERSIONS.toTypedArray()))
                        .isEditable(false)
                        .withSimpleRenderer { Bundle("uuid.ui.value.version.$it") }
                        .withName("version")
                        .bindItemNotNull(scheme::type)
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
}
