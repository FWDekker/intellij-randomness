package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.SchemeEditor
import com.fwdekker.randomness.affix.AffixDecoratorEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.template.TemplateReference.Companion.PRESET_AFFIX_DECORATOR_DESCRIPTORS
import com.fwdekker.randomness.template.TemplateReference.Companion.PRESET_CAPITALIZATION
import com.fwdekker.randomness.ui.onResetThis
import com.fwdekker.randomness.ui.withName
import com.fwdekker.randomness.ui.withSimpleRenderer
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.JList


/**
 * Component for editing a [TemplateReference].
 *
 * @param scheme the scheme to edit
 */
class TemplateReferenceEditor(scheme: TemplateReference) : SchemeEditor<TemplateReference>(scheme) {
    override val rootComponent = panel {
        group(Bundle("reference.ui.value.header")) {
            row(Bundle("reference.ui.value.template_option")) {
                comboBox(emptyList(), TemplateCellRenderer())
                    .onResetThis { repopulateTemplateComboBox(it.component) }
                    .withName("template")
                    .bindItem(scheme::template.toNullableProperty())
            }

            row(Bundle("reference.ui.value.capitalization_option")) {
                cell(ComboBox(PRESET_CAPITALIZATION))
                    .withSimpleRenderer(CapitalizationMode::toLocalizedString)
                    .withName("capitalization")
                    .bindItem(scheme::capitalization.toNullableProperty())
            }

            row {
                AffixDecoratorEditor(scheme.affixDecorator, PRESET_AFFIX_DECORATOR_DESCRIPTORS)
                    .also { decoratorEditors += it }
                    .let { cell(it.rootComponent) }
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


    // TODO: Document this
    private fun repopulateTemplateComboBox(templateComboBox: ComboBox<Template>) {
        // TODO: Move this logic into `TemplateList`?
        val contextCopy = (+scheme.context).deepCopy(retainUuid = true)
        val referenceCopy =
            contextCopy.templates
                .flatMap { it.schemes }
                .filterIsInstance<TemplateReference>()
                .single { it.uuid == scheme.uuid }
        val validTemplates =
            contextCopy.templates
                .filter { template ->
                    referenceCopy.template = template
                    contextCopy.templateList.findRecursionFrom(referenceCopy) == null
                }

        templateComboBox.removeAllItems()
        validTemplates.forEach { templateComboBox.addItem(it) }
    }

    // TODO: Document this
    private class TemplateCellRenderer : ColoredListCellRenderer<Template>() {
        override fun customizeCellRenderer(
            list: JList<out Template>,
            value: Template?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean,
        ) {
            icon = value?.icon ?: Template.DEFAULT_ICON
            append(value?.name ?: Bundle("template.name.unknown"))
        }
    }
}
