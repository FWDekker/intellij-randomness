package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.word.WordSettings.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.word.WordSettings.Companion.default
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.ButtonGroup
import javax.swing.JPanel


/**
 * Component for settings of random word generation.
 *
 * @see WordSettings
 * @see WordSettingsAction
 * @see DictionaryTable
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class WordSettingsComponent(settings: WordSettings = default) : SettingsComponent<WordSettings>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var previewPanelHolder: PreviewPanel<WordInsertAction>
    private lateinit var previewPanel: JPanel
    private lateinit var lengthRange: JSpinnerRange
    private lateinit var minLength: JIntSpinner
    private lateinit var maxLength: JIntSpinner
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var dictionaryPanel: JPanel
    private lateinit var dictionaryTable: DictionaryTable

    override val rootPane get() = contentPane


    init {
        loadSettings()

        previewPanelHolder.updatePreviewOnUpdateOf(
            minLength, maxLength, capitalizationGroup, enclosureGroup, dictionaryTable)
        previewPanelHolder.updatePreview()
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        previewPanelHolder = PreviewPanel { WordInsertAction(WordSettings().also { saveSettings(it) }) }
        previewPanel = previewPanelHolder.rootPanel

        minLength = JIntSpinner(1, 1)
        maxLength = JIntSpinner(1, 1)
        lengthRange = JSpinnerRange(minLength, maxLength, Int.MAX_VALUE.toDouble(), "length")
        dictionaryTable = DictionaryTable()
        dictionaryPanel = dictionaryTable.createComponent()
    }

    override fun loadSettings(settings: WordSettings) {
        minLength.value = settings.minLength
        maxLength.value = settings.maxLength
        enclosureGroup.setValue(settings.enclosure)
        capitalizationGroup.setValue(settings.capitalization)
        dictionaryTable.data = settings.bundledDictionaries + settings.userDictionaries
        dictionaryTable.activeData = settings.activeBundledDictionaries + settings.activeUserDictionaries
    }

    override fun saveSettings(settings: WordSettings) {
        settings.minLength = minLength.value
        settings.maxLength = maxLength.value
        settings.enclosure = enclosureGroup.getValue() ?: WordSettings.DEFAULT_ENCLOSURE
        settings.capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION
        settings.bundledDictionaries = dictionaryTable.data.filterIsInstance<BundledDictionary>().toSet()
        settings.activeBundledDictionaries = dictionaryTable.activeData.filterIsInstance<BundledDictionary>().toSet()
        settings.userDictionaries = dictionaryTable.data.filterIsInstance<UserDictionary>().toSet()
        settings.activeUserDictionaries = dictionaryTable.activeData.filterIsInstance<UserDictionary>().toSet()

        BundledDictionary.cache.clear()
        UserDictionary.cache.clear()
    }

    override fun isModified(settings: WordSettings): Boolean {
        val tableDictionaries = dictionaryTable.data
        val settingsDictionaries = settings.bundledDictionaries + settings.userDictionaries

        return tableDictionaries.size != settingsDictionaries.size ||
            tableDictionaries.zip(settingsDictionaries).any { it.first != it.second }
    }

    override fun doValidate(): ValidationInfo? {
        BundledDictionary.cache.clear()
        UserDictionary.cache.clear()

        return validateDictionarySelection()
            ?: validateWordRange()
            ?: minLength.validateValue()
            ?: maxLength.validateValue()
            ?: lengthRange.validateValue()
    }

    /**
     * Returns `null` if a unique, non-empty selection of valid dictionaries has been made, or a `ValidationInfo` object
     * explaining which input should be changed.
     *
     * @return `null` if a unique, non-empty selection of valid dictionaries has been made, or a `ValidationInfo` object
     * explaining which input should be changed
     */
    @Suppress("ReturnCount") // Acceptable for validation functions
    private fun validateDictionarySelection(): ValidationInfo? {
        if (dictionaryTable.data.distinct().size != dictionaryTable.data.size)
            return ValidationInfo("Dictionaries must be unique.", dictionaryPanel)
        if (dictionaryTable.activeData.isEmpty())
            return ValidationInfo("Select at least one dictionary.", dictionaryPanel)

        dictionaryTable.data.forEach { dictionary ->
            try {
                dictionary.validate()
            } catch (e: InvalidDictionaryException) {
                return ValidationInfo("Dictionary `$dictionary` is invalid: ${e.message}", dictionaryPanel)
            }

            if (dictionary.words.isEmpty())
                return ValidationInfo("Dictionary `$dictionary` is empty.", dictionaryPanel)
        }

        return null
    }

    /**
     * Returns `null` if the selected word range overlaps with words in the chosen dictionaries, or a `ValidationInfo`
     * object explaining which input should be changed.
     *
     * @return `null` if the selected word range overlaps with words in the chosen dictionaries, or a `ValidationInfo`
     * object explaining which input should be changed
     */
    private fun validateWordRange(): ValidationInfo? {
        val words = dictionaryTable.activeData.filter { it.isValid() }.flatMap { it.words }
        val minWordLength = words.map { it.length }.min() ?: 1
        val maxWordLength = words.map { it.length }.max() ?: Integer.MAX_VALUE

        return when {
            minLength.value > maxWordLength ->
                ValidationInfo(
                    "The longest word in the selected dictionaries is $maxWordLength characters. " +
                        "Set the minimum length to a value less than or equal to $maxWordLength.",
                    minLength
                )
            maxLength.value < minWordLength ->
                ValidationInfo(
                    "The shortest word in the selected dictionaries is $minWordLength characters. " +
                        "Set the maximum length to a value less than or equal to $minWordLength.",
                    maxLength
                )
            else -> null
        }
    }
}
