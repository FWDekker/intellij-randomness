package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SchemeComponent
import com.fwdekker.randomness.ValidationInfo
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.word.WordScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.word.WordScheme.Companion.DEFAULT_ENCLOSURE
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
class WordSettingsComponent(scheme: WordScheme) : SchemeComponent<WordScheme>() {
    override lateinit var rootPane: JPanel private set
    private lateinit var lengthRange: JSpinnerRange
    private lateinit var minLength: JIntSpinner
    private lateinit var maxLength: JIntSpinner
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var dictionaryPanel: JPanel
    private lateinit var dictionarySeparator: JComponent
    private lateinit var dictionaryTable: DictionaryTable
    private lateinit var dictionaryHelp: JTextArea


    init {
        loadScheme(scheme)

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
        lengthRange = JSpinnerRange(minLength, maxLength, Int.MAX_VALUE.toDouble(), "length")
        dictionaryTable = DictionaryTable()
        dictionaryPanel = dictionaryTable.panel
        dictionarySeparator = factory.createSeparator(bundle.getString("settings.dictionaries"))
    }

    override fun loadScheme(scheme: WordScheme) =
        scheme.also {
            minLength.value = it.minLength
            maxLength.value = it.maxLength
            enclosureGroup.setValue(it.enclosure)
            capitalizationGroup.setValue(it.capitalization)
            dictionaryTable.data = it.bundledDictionaries + it.userDictionaries
            dictionaryTable.activeData = it.activeBundledDictionaries + it.activeUserDictionaries
        }.let {}

    override fun saveScheme(): WordScheme =
        WordScheme(
            minLength = minLength.value,
            maxLength = maxLength.value,
            enclosure = enclosureGroup.getValue() ?: DEFAULT_ENCLOSURE,
            capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION,
            bundledDictionaries = dictionaryTable.data.filter { it.isBundled }.toSet(),
            activeBundledDictionaries = dictionaryTable.activeData.filter { it.isBundled }.toSet(),
            userDictionaries = dictionaryTable.data.filter { !it.isBundled }.toSet(),
            activeUserDictionaries = dictionaryTable.activeData.filter { !it.isBundled }.toSet()
        ).also {
            BundledDictionary.cache.clear()
            UserDictionary.cache.clear()
        }


    override fun doValidate(): ValidationInfo? {
        BundledDictionary.cache.clear()
        UserDictionary.cache.clear()

        return dictionaryTable.doValidate()
            ?: validateWordRange()
            ?: minLength.validateValue()
            ?: maxLength.validateValue()
            ?: lengthRange.validateValue()
    }

    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minLength, maxLength, capitalizationGroup, enclosureGroup, dictionaryTable,
            listener = listener
        )


    /**
     * Returns `null` if the selected word range overlaps with words in the chosen dictionaries, or a `ValidationInfo`
     * object explaining which input should be changed.
     *
     * @return `null` if the selected word range overlaps with words in the chosen dictionaries, or a `ValidationInfo`
     * object explaining which input should be changed
     */
    private fun validateWordRange(): ValidationInfo? {
        val words = dictionaryTable.activeData.filter { it.isValid() }.flatMap { it.words }
        val minWordLength = words.map { it.length }.minOrNull() ?: 1
        val maxWordLength = words.map { it.length }.maxOrNull() ?: Integer.MAX_VALUE

        return when {
            minLength.value > maxWordLength ->
                ValidationInfo(
                    "The longest word in the selected dictionaries is $maxWordLength characters. " +
                        "Set the minimum length to a value less than or equal to $maxWordLength.",
                    minLength
                ) { minLength.value = maxWordLength }
            maxLength.value < minWordLength ->
                ValidationInfo(
                    "The shortest word in the selected dictionaries is $minWordLength characters. " +
                        "Set the maximum length to a value less than or equal to $minWordLength.",
                    maxLength
                ) { maxLength.value = minWordLength }
            else -> null
        }
    }
}
