package com.fwdekker.randomness.word

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.GridPanelBuilder
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.SimpleJBDocumentListener
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
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
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.uiDesigner.core.GridConstraints
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
        rootComponent = GridPanelBuilder.panel {
            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                panel {
                    row {
                        cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                            SeparatorFactory.createSeparator(Bundle("word.ui.word_list"), null)
                        }

                        cell(constraints(hSizePolicy = 0)) {
                            ComboBox(arrayOf(PRESET_ITEM) + DefaultWordList.WORD_LISTS)
                                .withName("wordListBox")
                                .also { it.setRenderer { _, value, _, _, _ -> JBLabel(value.name) } }
                                .also {
                                    it.addItemListener { event ->
                                        if (event.stateChange == ItemEvent.SELECTED) {
                                            val item = event.item as DefaultWordList
                                            if (item != PRESET_ITEM) wordList = item.words
                                        }
                                    }
                                }
                                .also { wordListBox = it }
                        }
                    }

                    cell(
                        constraints(
                            colSpan = 2,
                            fill = GridConstraints.FILL_HORIZONTAL
                        ).withHeight(UIConstants.WIDTH_VERY_LARGE)
                    ) {
                        val factory = EditorFactory.getInstance()

                        factory.createDocument("")
                            .also {
                                it.addDocumentListener(
                                    SimpleJBDocumentListener {
                                        if (wordListBox.selectedIndex != 0 && wordList != wordListBox.item.words)
                                            wordListBox.selectedIndex = 0
                                    }
                                )
                            }
                            .also { wordListDocument = it }

                        factory.createEditor(wordListDocument)
                            .also { wordListEditor = it }
                            .component
                    }
                }
            }

            // TODO: Use constant instant of magic number
            vspacer(height = 15)

            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                SeparatorFactory.createSeparator(Bundle("word.ui.appearance"), null)
            }

            panel {
                row {
                    lateinit var quotationLabel: JLabel

                    cell {
                        JBLabel(Bundle("word.ui.quotation_marks.option"))
                            .loadMnemonic()
                            .also { quotationLabel = it }
                    }

                    row {
                        run { quotationGroup = ButtonGroup() }

                        cell {
                            JBRadioButton(Bundle("word.ui.quotation_marks.none"))
                                .withName("quotationNone")
                                .withActionCommand("")
                                .inGroup(quotationGroup)
                        }

                        cell {
                            JBRadioButton("'")
                                .withName("quotationSingle")
                                .inGroup(quotationGroup)
                        }

                        cell {
                            JBRadioButton(""""""")
                                .withName("quotationDouble")
                                .inGroup(quotationGroup)
                        }

                        cell {
                            JBRadioButton("`")
                                .withName("quotationBacktick")
                                .inGroup(quotationGroup)
                        }

                        cell {
                            VariableLabelRadioButton(UIConstants.WIDTH_TINY, MaxLengthDocumentFilter(2))
                                .withName("quotationCustom")
                                .also { it.addToButtonGroup(quotationGroup) }
                                .also { customQuotation = it }
                        }

                        run { quotationGroup.setLabel(quotationLabel) }
                    }
                }

                row {
                    cell {
                        JBLabel(Bundle("word.ui.capitalization_option"))
                            .loadMnemonic()
                            .also { capitalizationLabel = it }
                    }

                    row {
                        run { capitalizationGroup = ButtonGroup() }

                        cell {
                            JBRadioButton(Bundle("shared.capitalization.retain"))
                                .withActionCommand("retain")
                                .withName("capitalizationRetain")
                                .inGroup(capitalizationGroup)
                        }

                        cell {
                            @Suppress("DialogTitleCapitalization") // Intentional
                            JBRadioButton(Bundle("shared.capitalization.lower"))
                                .withActionCommand("lower")
                                .withName("capitalizationLower")
                                .inGroup(capitalizationGroup)
                        }

                        cell {
                            JBRadioButton(Bundle("shared.capitalization.upper"))
                                .withActionCommand("upper")
                                .withName("capitalizationUpper")
                                .inGroup(capitalizationGroup)
                        }

                        cell {
                            JBRadioButton(Bundle("shared.capitalization.random"))
                                .withActionCommand("random")
                                .withName("capitalizationRandom")
                                .inGroup(capitalizationGroup)
                        }

                        cell {
                            JBRadioButton(Bundle("shared.capitalization.sentence"))
                                .withActionCommand("sentence")
                                .withName("capitalizationSentence")
                                .inGroup(capitalizationGroup)
                        }

                        cell {
                            @Suppress("DialogTitleCapitalization") // Intentional
                            JBRadioButton(Bundle("shared.capitalization.first_letter"))
                                .withActionCommand("first letter")
                                .withName("capitalizationFirstLetter")
                                .inGroup(capitalizationGroup)
                        }

                        run { capitalizationGroup.setLabel(capitalizationLabel) }
                    }
                }
            }

            vspacer(height = 15)

            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                ArrayDecoratorEditor(originalState.arrayDecorator)
                    .also { arrayDecoratorEditor = it }
                    .rootComponent
            }

            vspacer()
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
