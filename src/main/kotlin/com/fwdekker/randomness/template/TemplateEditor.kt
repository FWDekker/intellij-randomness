package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TitledSeparator
import javax.swing.JPanel
import javax.swing.JTextField


/**
 * Component for editing non-children-related aspects of [Template]s.
 *
 * @param template the template to edit
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class TemplateEditor(template: Template) : StateEditor<Template>(template) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = nameInput

    private lateinit var titleSeparator: TitledSeparator
    private lateinit var nameInput: JTextField
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor
    private lateinit var arrayDecoratorEditorPanel: JPanel


    init {
        loadState()
    }

    /**
     * Initializes custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        titleSeparator = SeparatorFactory.createSeparator(Bundle("template.title"), null)

        arrayDecoratorEditor = ArrayDecoratorEditor(
            originalState.arrayDecorator,
            disablable = false,
            helpText = Bundle("template.array_help")
        )
        arrayDecoratorEditorPanel = arrayDecoratorEditor.rootComponent
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
