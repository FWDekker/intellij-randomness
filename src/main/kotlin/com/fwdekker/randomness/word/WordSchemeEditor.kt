package com.fwdekker.randomness.word

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.word.WordScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.word.WordScheme.Companion.DEFAULT_ENCLOSURE
import com.fwdekker.randomness.word.WordScheme.Companion.MAX_LENGTH_DIFFERENCE
import com.fwdekker.randomness.word.WordScheme.Companion.MIN_LENGTH
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.UIUtil
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.SwingUtilities


/**
 * Component for editing random word settings.
 *
 * @param scheme the scheme to edit in the component
 * @see DictionaryTable
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class WordSchemeEditor(scheme: WordScheme = WordScheme()) : StateEditor<WordScheme>(scheme) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = minLength.editorComponent

    private lateinit var titleSeparator: TitledSeparator
    private lateinit var minLength: JIntSpinner
    private lateinit var maxLength: JIntSpinner
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var dictionaryPanel: JPanel
    private lateinit var dictionarySeparator: TitledSeparator
    private lateinit var dictionaryTable: DictionaryTable
    private lateinit var dictionaryHelp: JTextArea
    private lateinit var arrayDecoratorPanel: JPanel
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        dictionaryHelp.border = null
        dictionaryHelp.font = JBLabel().font.deriveFont(UIUtil.getFontSize(UIUtil.FontSize.SMALL))

        loadState()
        SwingUtilities.invokeLater { dictionaryHelp.isVisible = true } // Required to disable auto-scrolling
    }

    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        titleSeparator = SeparatorFactory.createSeparator(Bundle("word.title"), null)

        minLength = JIntSpinner(value = MIN_LENGTH, minValue = MIN_LENGTH)
        maxLength = JIntSpinner(value = MIN_LENGTH, minValue = MIN_LENGTH)
        bindSpinners(minLength, maxLength, maxRange = MAX_LENGTH_DIFFERENCE.toDouble())

        dictionarySeparator = SeparatorFactory.createSeparator(Bundle("word.dictionary.title"), null)
        dictionaryTable = DictionaryTable()
        dictionaryPanel = dictionaryTable.panel

        arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
        arrayDecoratorPanel = arrayDecoratorEditor.rootComponent
    }


    override fun loadState(state: WordScheme) {
        super.loadState(state)

        minLength.value = state.minLength
        maxLength.value = state.maxLength
        enclosureGroup.setValue(state.enclosure)
        capitalizationGroup.setValue(state.capitalization)
        dictionaryTable.data = (+state.dictionarySettings).dictionaries
        dictionaryTable.activeData = state.activeDictionaries.filter { it in dictionaryTable.data }
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        WordScheme(
            minLength = minLength.value,
            maxLength = maxLength.value,
            enclosure = enclosureGroup.getValue() ?: DEFAULT_ENCLOSURE,
            capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION,
            activeDictionaries = dictionaryTable.activeData.toSet(),
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also {
            it.uuid = originalState.uuid

            it.dictionarySettings += (+originalState.dictionarySettings).deepCopy(retainUuid = true)
            (+it.dictionarySettings).dictionaries = dictionaryTable.data

            UserDictionary.clearCache()
        }

    override fun applyState() {
        super.applyState()
        (+originalState.dictionarySettings).copyFrom(+readState().dictionarySettings)
    }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minLength, maxLength, capitalizationGroup, enclosureGroup, dictionaryTable, arrayDecoratorEditor,
            listener = listener
        )
}
