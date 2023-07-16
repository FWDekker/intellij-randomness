package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.StringComboBox
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.buttons
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.loadMnemonic
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.uuid.UuidScheme.Companion.DEFAULT_TYPE
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.Label
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
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
    private lateinit var quotationComboBox: ComboBox<String>
    private lateinit var isUppercaseCheckBox: JCheckBox
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

                    row(Bundle("uuid.ui.value.quotation_marks.option")) {
                        cell(StringComboBox(listOf("", "'", "\"", "`"), MaxLengthDocumentFilter(2)))
                            .also { it.component.isEditable = true }
                            .also { it.component.name = "quotation" }
                            .also { quotationComboBox = it.component }
                    }

                    row {
                        checkBox(Bundle("uuid.ui.value.capitalization_option"))
                            .loadMnemonic()
                            .also { it.component.name = "isUppercase" }
                            .also { isUppercaseCheckBox = it.component }
                    }

                    row {
                        checkBox(Bundle("uuid.add_dashes"))
                            .loadMnemonic()
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
        quotationComboBox.item = state.quotation
        isUppercaseCheckBox.isSelected = state.isUppercase
        addDashesCheckBox.isSelected = state.addDashes
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState(): UuidScheme =
        UuidScheme(
            type = typeGroup.getValue()?.toInt() ?: DEFAULT_TYPE,
            quotation = quotationComboBox.item,
            isUppercase = isUppercaseCheckBox.isSelected,
            addDashes = addDashesCheckBox.isSelected,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            typeGroup, quotationComboBox, isUppercaseCheckBox, addDashesCheckBox, arrayDecoratorEditor,
            listener = listener
        )
}
