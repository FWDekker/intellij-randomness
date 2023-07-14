package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.withFixedWidth
import com.intellij.ui.dsl.builder.panel
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
        rootComponent = panel {
            row(Bundle("template.ui.name_option")) {
                textField()
                    .withFixedWidth(UIConstants.SIZE_LARGE)
                    .also { it.component.name = "templateName" }
                    .also { nameInput = it.component }
            }
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
