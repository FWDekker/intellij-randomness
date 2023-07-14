package com.fwdekker.randomness.word

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.SimpleJBDocumentListener
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.add
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.ui.withFixedHeight
import com.fwdekker.randomness.word.WordScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.word.WordScheme.Companion.DEFAULT_QUOTATION
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.impl.UndoManagerImpl
import com.intellij.openapi.command.undo.DocumentReferenceManager
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.Label
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import java.awt.event.ItemEvent
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


/**
 * Component for editing random word settings.
 *
 * @param scheme the scheme to edit in the component
 */
class WordSchemeEditor(scheme: WordScheme = WordScheme()) : StateEditor<WordScheme>(scheme) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = capitalizationLabel.labelFor as JComponent

    private lateinit var wordListBox: ComboBox<DefaultWordList>
    private lateinit var wordListDocument: Document
    private lateinit var wordListEditor: Editor
    private lateinit var capitalizationLabel: JLabel
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var customQuotation: VariableLabelRadioButton
    private lateinit var quotationGroup: ButtonGroup
    private lateinit var arrayDecoratorPanel: JPanel
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor

    /**
     * The words currently displayed in [wordListDocument].
     */
    private var wordList: List<String>
        get() = wordListDocument.text.split('\n').filterNot { it.isBlank() }
        set(value) = runWriteAction { wordListDocument.setText(value.joinToString(separator = "\n", postfix = "\n")) }


    init {
        rootComponent = panel {
            group(Bundle("word.ui.words.header")) {
                row(Bundle("word.ui.words.insert_option")) {
                    cell(ComboBox(arrayOf(PRESET_ITEM) + DefaultWordList.WORD_LISTS))
                        .also { it.component.name = "wordListBox" }
                        .also { it.component.setRenderer { _, value, _, _, _ -> JBLabel(value.name) } }
                        .also {
                            it.component.addItemListener { event ->
                                if (event.stateChange == ItemEvent.SELECTED) {
                                    val item = event.item as DefaultWordList
                                    if (item != PRESET_ITEM) wordList = item.words
                                }
                            }
                        }
                        .also { wordListBox = it.component }
                }

                row {
                    val factory = EditorFactory.getInstance()
                    wordListDocument = factory.createDocument("")
                        .also {
                            it.addDocumentListener(
                                SimpleJBDocumentListener {
                                    if (wordListBox.selectedIndex != 0 && wordList != wordListBox.item.words)
                                        wordListBox.selectedIndex = 0
                                }
                            )
                        }
                    wordListEditor = factory.createEditor(wordListDocument)

                    cell(wordListEditor.component)
                        .horizontalAlign(HorizontalAlign.FILL)
                        .withFixedHeight(UIConstants.SIZE_VERY_LARGE)
                }.bottomGap(BottomGap.MEDIUM)
            }

            group(Bundle("word.ui.format.header")) {
                val quotationLabel = Label(Bundle("word.ui.format.quotation_marks_option"))
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

                val capitalizationLabel = Label(Bundle("word.ui.format.capitalization_option"))
                row(capitalizationLabel) {
                    capitalizationGroup = ButtonGroup()

                    cell(JBRadioButton(Bundle("shared.capitalization.retain")))
                        .also { it.component.actionCommand = "retain" }
                        .also { it.component.name = "capitalizationRetain" }
                        .also { capitalizationGroup.add(it.component) }
                    @Suppress("DialogTitleCapitalization") // Intentional
                    cell(JBRadioButton(Bundle("shared.capitalization.lower")))
                        .also { it.component.actionCommand = "lower" }
                        .also { it.component.name = "capitalizationLower" }
                        .also { capitalizationGroup.add(it.component) }
                    cell(JBRadioButton(Bundle("shared.capitalization.upper")))
                        .also { it.component.actionCommand = "upper" }
                        .also { it.component.name = "capitalizationUpper" }
                        .also { capitalizationGroup.add(it.component) }
                    cell(JBRadioButton(Bundle("shared.capitalization.random")))
                        .also { it.component.actionCommand = "random" }
                        .also { it.component.name = "capitalizationRandom" }
                        .also { capitalizationGroup.add(it.component) }
                    cell(JBRadioButton(Bundle("shared.capitalization.sentence")))
                        .also { it.component.actionCommand = "sentence" }
                        .also { it.component.name = "capitalizationSentence" }
                        .also { capitalizationGroup.add(it.component) }
                    @Suppress("DialogTitleCapitalization") // Intentional
                    cell(JBRadioButton(Bundle("shared.capitalization.first_letter")))
                        .also { it.component.actionCommand = "first letter" }
                        .also { it.component.name = "capitalizationFirstLetter" }
                        .also { capitalizationGroup.add(it.component) }

                    capitalizationGroup.setLabel(capitalizationLabel)
                }
            }

            row {
                arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
                cell(arrayDecoratorEditor.rootComponent).horizontalAlign(HorizontalAlign.FILL)
            }
        }

        loadState()

        // Reset undo history
        (UndoManager.getGlobalInstance() as UndoManagerImpl)
            .invalidateActionsFor(DocumentReferenceManager.getInstance().create(wordListDocument))
    }

    /**
     * Initializes custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    private fun createUIComponents() {
        arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
        arrayDecoratorPanel = arrayDecoratorEditor.rootComponent
    }

    /**
     * Disposes of this panel's resources, to be used when this panel is no longer used.
     */
    override fun dispose() {
        EditorFactory.getInstance().releaseEditor(wordListEditor)
    }


    override fun loadState(state: WordScheme) {
        super.loadState(state)

        wordListBox.selectedIndex = DefaultWordList.WORD_LISTS.map { it.words }.indexOf(state.words) + 1
        wordList = state.words
        customQuotation.label = state.customQuotation
        quotationGroup.setValue(state.quotation)
        capitalizationGroup.setValue(state.capitalization)
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        WordScheme(
            words = wordList,
            quotation = quotationGroup.getValue() ?: DEFAULT_QUOTATION,
            customQuotation = customQuotation.label,
            capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            wordListDocument, capitalizationGroup, quotationGroup, customQuotation, arrayDecoratorEditor,
            listener = listener
        )


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The "header" item inserted at the top of the [wordListBox].
         */
        val PRESET_ITEM
            get() = DefaultWordList("<html><i>${Bundle("word_list.ui.select_preset")}</i>", "")
    }
}
