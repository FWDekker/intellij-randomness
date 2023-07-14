package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.add
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.buttons
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_QUOTATION
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_TYPE
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.Label
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.util.ui.DialogUtil
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel


/**
 * Component for editing random UUID settings.
 *
 * @param scheme the scheme to edit in the component
 */
class UuidSchemeEditor(scheme: UuidScheme = UuidScheme()) : StateEditor<UuidScheme>(scheme) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = typeGroup.buttons().firstOrNull { it.isSelected }

    private lateinit var typeGroup: ButtonGroup
    private lateinit var quotationGroup: ButtonGroup
    private lateinit var customQuotation: VariableLabelRadioButton
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var addDashesCheckBox: JCheckBox
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        rootComponent = panel {
            group(Bundle("uuid.ui.value.header")) {
                panel {
                    val typeLabel = Label(Bundle("uuid.ui.value.type.option"))
                    row(typeLabel) {
                        typeGroup = ButtonGroup()

                        cell(JBRadioButton(Bundle("uuid.ui.value.type.1")))
                            .also { it.component.actionCommand = "1" }
                            .also { it.component.name = "type1" }
                            .also { typeGroup.add(it.component) }
                        cell(JBRadioButton(Bundle("uuid.ui.value.type.4")))
                            .also { it.component.actionCommand = "4" }
                            .also { it.component.name = "type4" }
                            .also { typeGroup.add(it.component) }

                        typeGroup.setLabel(typeLabel)
                    }

                    val quotationLabel = Label(Bundle("uuid.ui.value.quotation_marks.option"))
                    row(quotationLabel) {
                        quotationGroup = ButtonGroup()

                        cell(JBRadioButton(Bundle("shared.option.none")))
                            .also { it.component.actionCommand = "" }
                            .also { it.component.name = "quotationNone" }
                            .also { quotationGroup.add(it.component) }
                        cell(JBRadioButton("'"))
                            .also { it.component.name = "quotationSingle" }
                            .also { quotationGroup.add(it.component) }
                        cell(JBRadioButton("\""))
                            .also { it.component.name = "quotationDouble" }
                            .also { quotationGroup.add(it.component) }
                        cell(JBRadioButton("`"))
                            .also { it.component.name = "quotationBacktick" }
                            .also { quotationGroup.add(it.component) }
                        cell(VariableLabelRadioButton(UIConstants.SIZE_TINY, MaxLengthDocumentFilter(2)))
                            .also { it.component.name = "quotationCustom" }
                            .also { quotationGroup.add(it.component) }
                            .also { customQuotation = it.component }

                        quotationGroup.setLabel(quotationLabel)
                    }

                    val capitalizationLabel = Label(Bundle("uuid.ui.value.capitalization_option"))
                    row(capitalizationLabel) {
                        capitalizationGroup = ButtonGroup()

                        @Suppress("DialogTitleCapitalization") // Intentional
                        cell(JBRadioButton(Bundle("shared.capitalization.lower")))
                            .also { it.component.actionCommand = "lower" }
                            .also { it.component.name = "capitalizationLower" }
                            .also { capitalizationGroup.add(it.component) }
                        cell(JBRadioButton(Bundle("shared.capitalization.upper")))
                            .also { it.component.actionCommand = "upper" }
                            .also { it.component.name = "capitalizationUpper" }
                            .also { capitalizationGroup.add(it.component) }

                        capitalizationGroup.setLabel(capitalizationLabel)
                    }

                    row {
                        checkBox(Bundle("uuid.add_dashes"))
                            .also { DialogUtil.registerMnemonic(it.component, '&') }
                            .also { it.component.name = "addDashesCheckBox" }
                            .also { addDashesCheckBox = it.component }
                    }
                }
            }

            row {
                arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
                cell(arrayDecoratorEditor.rootComponent).horizontalAlign(HorizontalAlign.FILL)
            }
        }

        loadState()
    }


    override fun loadState(state: UuidScheme) {
        super.loadState(state)

        typeGroup.setValue(state.type.toString())
        customQuotation.label = state.customQuotation
        quotationGroup.setValue(state.quotation)
        capitalizationGroup.setValue(state.capitalization)
        addDashesCheckBox.isSelected = state.addDashes
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState(): UuidScheme =
        UuidScheme(
            type = typeGroup.getValue()?.toInt() ?: DEFAULT_TYPE,
            quotation = quotationGroup.getValue() ?: DEFAULT_QUOTATION,
            customQuotation = customQuotation.label,
            capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION,
            addDashes = addDashesCheckBox.isSelected,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            typeGroup, quotationGroup, customQuotation, capitalizationGroup, addDashesCheckBox, arrayDecoratorEditor,
            listener = listener
        )
}
