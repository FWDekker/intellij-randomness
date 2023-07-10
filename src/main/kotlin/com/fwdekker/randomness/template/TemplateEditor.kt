package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.ui.GridPanelBuilder
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField


/**
 * Component for editing non-children-related aspects of [Template]s.
 *
 * @param template the template to edit
 */
class TemplateEditor(template: Template) : StateEditor<Template>(template) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = nameInput

    private lateinit var nameInput: JTextField


    init {
        rootComponent = GridPanelBuilder.panel {
            row {
                lateinit var nameLabel: JLabel

                cell {
                    JBLabel(Bundle("template.ui.name_option"))
                        .also { nameLabel = it }
                }

                cell(constraints(fixedWidth = UIConstants.SIZE_LARGE)) {
                    JBTextField()
                        .withName("templateName")
                        .setLabel(nameLabel)
                        .also { nameInput = it }
                }

                hSpacer()
            }

            vSpacer()
        }

        loadState()
    }


    override fun loadState(state: Template) {
        super.loadState(state)

        nameInput.text = state.name.trim()
    }

    override fun readState() =
        originalState.deepCopy(retainUuid = true)
            .also { it.name = nameInput.text.trim() }
            .also { it.arrayDecorator.enabled = false }


    override fun addChangeListener(listener: () -> Unit) = addChangeListenerTo(nameInput, listener = listener)
}
