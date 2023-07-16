package com.fwdekker.randomness.word

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.CapitalizationComboBox
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.SimpleJBDocumentListener
import com.fwdekker.randomness.ui.StringComboBox
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.withFixedHeight
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.impl.UndoManagerImpl
import com.intellij.openapi.command.undo.DocumentReferenceManager
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import java.awt.event.ItemEvent
import javax.swing.JComponent
import javax.swing.JPanel


/**
 * Component for editing random word settings.
 *
 * @param scheme the scheme to edit in the component
 */
class WordSchemeEditor(scheme: WordScheme = WordScheme()) : StateEditor<WordScheme>(scheme) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = capitalizationComboBox as JComponent

    private lateinit var presetComboBox: ComboBox<DefaultWordList>
    private lateinit var wordListDocument: Document
    private lateinit var wordListEditor: Editor
    private lateinit var capitalizationComboBox: ComboBox<CapitalizationMode>
    private lateinit var quotationComboBox: ComboBox<String>
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
                        .also { it.component.name = "presets" }
                        .also { it.component.setRenderer { _, value, _, _, _ -> JBLabel(value.name) } }
                        .also {
                            it.component.addItemListener { event ->
                                if (event.stateChange == ItemEvent.SELECTED) {
                                    val item = event.item as DefaultWordList
                                    if (item != PRESET_ITEM) wordList = item.words
                                }
                            }
                        }
                        .also { presetComboBox = it.component }
                }

                row {
                    val factory = EditorFactory.getInstance()
                    wordListDocument = factory.createDocument("")
                        .also {
                            it.addDocumentListener(
                                SimpleJBDocumentListener {
                                    if (presetComboBox.selectedIndex != 0 && wordList != presetComboBox.item.words)
                                        presetComboBox.selectedIndex = 0
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
                row(Bundle("word.ui.format.quotation_marks_option")) {
                    cell(StringComboBox(listOf("", "'", "\"", "`"), MaxLengthDocumentFilter(2)))
                        .also { it.component.isEditable = true }
                        .also { it.component.name = "quotation" }
                        .also { quotationComboBox = it.component }
                }

                row(Bundle("word.ui.format.capitalization_option")) {
                    cell(
                        CapitalizationComboBox(
                            listOf(
                                CapitalizationMode.RETAIN,
                                CapitalizationMode.LOWER,
                                CapitalizationMode.UPPER,
                                CapitalizationMode.RANDOM,
                                CapitalizationMode.SENTENCE,
                                CapitalizationMode.FIRST_LETTER,
                            )
                        )
                    )
                        .also { it.component.name = "capitalization" }
                        .also { capitalizationComboBox = it.component }
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
     * Disposes of this panel's resources, to be used when this panel is no longer used.
     */
    override fun dispose() {
        EditorFactory.getInstance().releaseEditor(wordListEditor)
    }


    override fun loadState(state: WordScheme) {
        super.loadState(state)

        presetComboBox.selectedIndex = DefaultWordList.WORD_LISTS.map { it.words }.indexOf(state.words) + 1
        wordList = state.words
        quotationComboBox.item = state.quotation
        capitalizationComboBox.item = state.capitalization
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        WordScheme(
            words = wordList,
            quotation = quotationComboBox.item,
            capitalization = capitalizationComboBox.item,
            arrayDecorator = arrayDecoratorEditor.readState(),
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            wordListDocument, capitalizationComboBox, quotationComboBox, arrayDecoratorEditor,
            listener = listener
        )


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The "header" item inserted at the top of the [presetComboBox].
         */
        val PRESET_ITEM
            get() = DefaultWordList("<html><i>${Bundle("word_list.ui.select_preset")}</i>", "")
    }
}
