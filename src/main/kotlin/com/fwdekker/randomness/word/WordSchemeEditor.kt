package com.fwdekker.randomness.word

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.word.WordScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.word.WordScheme.Companion.DEFAULT_QUOTATION
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TitledSeparator
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


/**
 * Component for editing random word settings.
 *
 * @param scheme the scheme to edit in the component
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class WordSchemeEditor(scheme: WordScheme = WordScheme()) : StateEditor<WordScheme>(scheme) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = capitalizationLabel.labelFor as JComponent

    private lateinit var capitalizationLabel: JLabel
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var customQuotation: VariableLabelRadioButton
    private lateinit var quotationLabel: JLabel
    private lateinit var quotationGroup: ButtonGroup
    private lateinit var dictionarySeparator: TitledSeparator
    private lateinit var dictionaryDocument: Document
    private lateinit var dictionaryEditor: Editor
    private lateinit var dictionaryComponent: JComponent
    private lateinit var arrayDecoratorPanel: JPanel
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor

    /**
     * The text currently displayed in [dictionaryComponent].
     */
    private var dictionaryText: String
        get() = dictionaryDocument.text
        set(value) = runWriteAction { dictionaryDocument.setText(value) }


    init {
        nop() // Cannot use `lateinit` property as first statement in init

        capitalizationGroup.setLabel(capitalizationLabel)

        customQuotation.addToButtonGroup(quotationGroup)
        quotationGroup.setLabel(quotationLabel)

        loadState()
    }

    /**
     * Initializes custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        customQuotation = VariableLabelRadioButton(UIConstants.WIDTH_TINY, MaxLengthDocumentFilter(2))

        dictionarySeparator = SeparatorFactory.createSeparator(Bundle("word.dictionary.title"), null)
        val factory = EditorFactory.getInstance()
        dictionaryDocument = factory.createDocument("")
        dictionaryEditor = factory.createEditor(dictionaryDocument)
        dictionaryComponent = dictionaryEditor.component

        arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
        arrayDecoratorPanel = arrayDecoratorEditor.rootComponent
    }

    /**
     * Disposes of this panel's resources, to be used when this panel is no longer used.
     */
    override fun dispose() {
        EditorFactory.getInstance().releaseEditor(dictionaryEditor)
    }


    override fun loadState(state: WordScheme) {
        super.loadState(state)

        customQuotation.label = state.customQuotation
        quotationGroup.setValue(state.quotation)
        capitalizationGroup.setValue(state.capitalization)
        dictionaryText = state.words.joinToString(separator = "\n")
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        WordScheme(
            quotation = quotationGroup.getValue() ?: DEFAULT_QUOTATION,
            customQuotation = customQuotation.label,
            capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION,
            words = dictionaryText.split('\n').filterNot { it.isBlank() },
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            capitalizationGroup, quotationGroup, customQuotation, dictionaryDocument, arrayDecoratorEditor,
            listener = listener
        )
}


/**
 * Null operation, does nothing.
 */
private fun nop() {
    // Does nothing
}
