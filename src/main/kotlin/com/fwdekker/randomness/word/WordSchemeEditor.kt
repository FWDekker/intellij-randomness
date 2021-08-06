package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.word.WordScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.word.WordScheme.Companion.DEFAULT_ENCLOSURE
import com.fwdekker.randomness.word.WordScheme.Companion.MAX_LENGTH_DIFFERENCE
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.UIUtil
import com.jgoodies.forms.factories.DefaultComponentFactory
import java.util.ResourceBundle
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea


/**
 * Component for editing random word settings.
 *
 * @param scheme the scheme to edit in the component
 *
 * @see DictionaryTable
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class WordSchemeEditor(
    scheme: WordScheme = WordScheme(),
    private val dictionarySettings: DictionarySettings = DictionarySettings.default
) : StateEditor<WordScheme>(scheme) {
    override lateinit var rootComponent: JPanel private set
    private lateinit var minLength: JIntSpinner
    private lateinit var maxLength: JIntSpinner
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var dictionaryPanel: JPanel
    private lateinit var dictionarySeparator: JComponent
    private lateinit var dictionaryTable: DictionaryTable
    private lateinit var dictionaryHelp: JTextArea


    init {
        loadState(scheme)

        dictionaryHelp.border = null
        dictionaryHelp.font = JBLabel().font.deriveFont(UIUtil.getFontSize(UIUtil.FontSize.SMALL))
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        val bundle = ResourceBundle.getBundle("randomness")
        val factory = DefaultComponentFactory.getInstance()

        minLength = JIntSpinner(1, 1, description = "minimum length")
        maxLength = JIntSpinner(1, 1, description = "maximum length")
        bindSpinners(minLength, maxLength, maxRange = MAX_LENGTH_DIFFERENCE)
        dictionaryTable = DictionaryTable()
        dictionaryPanel = dictionaryTable.panel
        dictionarySeparator = factory.createSeparator(bundle.getString("settings.dictionaries"))
    }

    override fun loadState(state: WordScheme) {
        super.loadState(state)

        minLength.value = state.minLength
        maxLength.value = state.maxLength
        enclosureGroup.setValue(state.enclosure)
        capitalizationGroup.setValue(state.capitalization)
        dictionaryTable.data = dictionarySettings.bundledDictionaries + dictionarySettings.userDictionaries
        dictionaryTable.activeData = state.activeBundledDictionaries + state.activeUserDictionaries
            .filter { dictionary -> dictionary in dictionaryTable.data }
    }

    override fun readState() =
        WordScheme(
            minLength = minLength.value,
            maxLength = maxLength.value,
            enclosure = enclosureGroup.getValue() ?: DEFAULT_ENCLOSURE,
            capitalization = capitalizationGroup.getValue()?.let(::getMode) ?: DEFAULT_CAPITALIZATION
        ).also {
            dictionarySettings.bundledDictionaries = dictionaryTable.data
                .filter { file -> file.isBundled }.toSet()
            it.activeBundledDictionaries = dictionaryTable.activeData
                .filter { file -> file.isBundled && file in dictionarySettings.bundledDictionaries }.toSet()
            dictionarySettings.userDictionaries = dictionaryTable.data
                .filter { file -> !file.isBundled }.toSet()
            it.activeUserDictionaries = dictionaryTable.activeData
                .filter { file -> !file.isBundled && file in dictionarySettings.userDictionaries }.toSet()

            BundledDictionary.cache.clear()
            UserDictionary.cache.clear()
        }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minLength, maxLength, capitalizationGroup, enclosureGroup, dictionaryTable,
            listener = listener
        )
}
