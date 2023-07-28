package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.StateContext
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.affix.AffixDecoratorEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.setSimpleRenderer
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.ComboBoxModel
import javax.swing.JList
import javax.swing.JPanel


/**
 * Component for editing [TemplateReference]s.
 *
 * @param reference the reference to edit
 */
class TemplateReferenceEditor(reference: TemplateReference) : StateEditor<TemplateReference>(reference) {
    override val rootComponent: JPanel
    override val stateComponents
        get() = super.stateComponents + affixDecoratorEditor + arrayDecoratorEditor
    override val preferredFocusedComponent
        get() = templateComboBox

    private lateinit var templateComboBoxModel: ComboBoxModel<Template>
    private lateinit var templateComboBox: ComboBox<Template>
    private lateinit var capitalizationComboBox: ComboBox<CapitalizationMode>
    private lateinit var affixDecoratorEditor: AffixDecoratorEditor
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        rootComponent = panel {
            group(Bundle("reference.ui.value.header")) {
                row(Bundle("reference.ui.value.template_option")) {
                    comboBox(
                        emptyList(),
                        object : ColoredListCellRenderer<Template>() {
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
                    )
                        .also { it.component.name = "template" }
                        .also { templateComboBoxModel = it.component.model }
                        .also { templateComboBox = it.component }
                }

                row(Bundle("reference.ui.value.capitalization_option")) {
                    cell(
                        ComboBox(
                            arrayOf(
                                CapitalizationMode.RETAIN,
                                CapitalizationMode.LOWER,
                                CapitalizationMode.UPPER,
                                CapitalizationMode.RANDOM,
                                CapitalizationMode.SENTENCE,
                                CapitalizationMode.FIRST_LETTER,
                            )
                        )
                    ).also {
                        it.component.setSimpleRenderer(CapitalizationMode::toLocalizedString)
                        it.component.name = "capitalization"
                        capitalizationComboBox = it.component
                    }
                }

                row {
                    affixDecoratorEditor = AffixDecoratorEditor(
                        originalState.affixDecorator,
                        listOf("'", "\"", "`"),
                        enableMnemonic = true
                    )
                    cell(affixDecoratorEditor.rootComponent)
                }
            }

            row {
                arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
                cell(arrayDecoratorEditor.rootComponent).horizontalAlign(HorizontalAlign.FILL)
            }
        }

        loadState()
    }


    override fun loadState(state: TemplateReference) {
        super.loadState(state)

        // Find templates that would not cause recursion if selected
        val listCopy = (+state.templateList).deepCopy(retainUuid = true)
            .also { it.applySettingsState(StateContext(it)) }
        val referenceCopy =
            listCopy.templates.flatMap { it.schemes }.single { it.uuid == originalState.uuid } as TemplateReference
        val validTemplates =
            (+state.templateList).templates
                .filter { template ->
                    referenceCopy.template = template
                    listCopy.findRecursionFrom(referenceCopy) == null
                }

        templateComboBox.removeAllItems()
        validTemplates.forEach { templateComboBox.addItem(it) }
        templateComboBox.selectedItem = state.template

        capitalizationComboBox.item = state.capitalization
        affixDecoratorEditor.loadState(state.affixDecorator)
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        TemplateReference(
            templateUuid = (templateComboBox.selectedItem as? Template)?.uuid,
            capitalization = capitalizationComboBox.item,
            affixDecorator = affixDecoratorEditor.readState(),
            arrayDecorator = arrayDecoratorEditor.readState(),
        ).also {
            it.uuid = originalState.uuid
            it.templateList = originalState.templateList.copy()
        }
}
