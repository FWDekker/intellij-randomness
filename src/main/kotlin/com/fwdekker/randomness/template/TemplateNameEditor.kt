package com.fwdekker.randomness.template

import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel


/**
 * Component for editing only the name and decorator of [Template]s.
 *
 * @param template the template to edit
 */
class TemplateNameEditor(template: Template) : StateEditor<Template>(template) {
    override val rootComponent = JPanel(BorderLayout())
    override val preferredFocusedComponent by lazy { nameInput }

    private val nameInput = JBTextField().also { it.name = "templateName" }
    private val arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        val namePanel = JPanel(BorderLayout())
        namePanel.add(JLabel("Name:"), BorderLayout.WEST)
        namePanel.add(nameInput)
        rootComponent.add(namePanel, BorderLayout.NORTH)

        arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator, disablable = false)
        rootComponent.add(arrayDecoratorEditor.rootComponent, BorderLayout.CENTER)

        loadState()
    }


    override fun loadState(state: Template) {
        super.loadState(state)

        nameInput.text = state.name.trim()
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        Template(
            name = nameInput.text.trim(),
            schemes = originalState.schemes.map { it.deepCopy(retainUuid = true) },
            arrayDecorator = arrayDecoratorEditor.readState().also { it.enabled = false }
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(nameInput, arrayDecoratorEditor, listener = listener)
}
