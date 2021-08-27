package com.fwdekker.randomness.template

import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel


/**
 * Component for editing only the name of [Template]s.
 *
 * @param template the template to edit
 */
class TemplateNameEditor(template: Template) : StateEditor<Template>(template) {
    override val rootComponent = JPanel(BorderLayout())
    private val nameInput = JBTextField().also { it.name = "templateName" }


    init {
        val namePanel = JPanel(BorderLayout())
        namePanel.add(JLabel("Name:"), BorderLayout.WEST)
        namePanel.add(nameInput)
        rootComponent.add(namePanel, BorderLayout.NORTH)

        loadState()
    }


    override fun loadState(state: Template) = super.loadState(state).also { nameInput.text = state.name.trim() }

    override fun readState() =
        Template(
            name = nameInput.text.trim(),
            schemes = originalState.schemes.map { it.deepCopy(retainUuid = true) }.toMutableList(),
            decorator = originalState.decorator
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) = addChangeListenerTo(nameInput, listener = listener)
}
