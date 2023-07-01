package com.fwdekker.randomness.template

import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.ui.addChangeListenerTo
import javax.swing.JPanel
import javax.swing.JTextField


/**
 * Component for editing non-children-related aspects of [Template]s.
 *
 * @param template the template to edit
 */
class TemplateEditor(template: Template) : StateEditor<Template>(template) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = nameInput

    private lateinit var nameInput: JTextField


    init {
        loadState()
    }


    override fun loadState(state: Template) {
        super.loadState(state)

        nameInput.text = state.name.trim()
    }

    override fun readState() =
        Template(
            name = nameInput.text.trim(),
            schemes = originalState.schemes.map { it.deepCopy(retainUuid = true) },
            arrayDecorator = originalState.arrayDecorator.deepCopy(retainUuid = true).also { it.enabled = false }
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) = addChangeListenerTo(nameInput, listener = listener)
}
