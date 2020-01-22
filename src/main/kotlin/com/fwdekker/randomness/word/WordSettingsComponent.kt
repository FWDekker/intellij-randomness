package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SchemesPanel
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.SettingsComponentListener
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.fwdekker.randomness.word.WordScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.word.WordScheme.Companion.DEFAULT_ENCLOSURE
import com.fwdekker.randomness.word.WordSettings.Companion.DEFAULT_SCHEMES
import com.fwdekker.randomness.word.WordSettings.Companion.default
import com.intellij.openapi.ui.ValidationInfo
import com.jgoodies.forms.factories.DefaultComponentFactory
import java.util.ResourceBundle
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel


/**
 * Component for settings of random word generation.
 *
 * @param settings the settings to edit in the component
 *
 * @see WordSettingsAction
 * @see DictionaryTable
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class WordSettingsComponent(settings: WordSettings = default) : SettingsComponent<WordSettings, WordScheme>(settings) {
    override lateinit var unsavedSettings: WordSettings
    override lateinit var schemesPanel: SchemesPanel<WordScheme>

    private lateinit var contentPane: JPanel
    private lateinit var previewPanelHolder: PreviewPanel
    private lateinit var previewPanel: JPanel
    private lateinit var lengthRange: JSpinnerRange
    private lateinit var minLength: JIntSpinner
    private lateinit var maxLength: JIntSpinner
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var dictionaryPanel: JPanel
    private lateinit var dictionarySeparator: JComponent
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
        val bundle = ResourceBundle.getBundle("randomness")
        val factory = DefaultComponentFactory.getInstance()

        unsavedSettings = WordSettings()
        schemesPanel = WordSchemesPanel(unsavedSettings)
            .also { panel -> panel.addListener(SettingsComponentListener(this)) }

        previewPanelHolder = PreviewPanel { WordInsertAction(WordScheme().also { saveScheme(it) }) }
        previewPanel = previewPanelHolder.rootPane

        minLength = JIntSpinner(1, 1, description = "minimum length")
        maxLength = JIntSpinner(1, 1, description = "maximum length")
        lengthRange = JSpinnerRange(minLength, maxLength, Int.MAX_VALUE.toDouble(), "length")
        dictionaryTable = DictionaryTable()
        dictionaryPanel = dictionaryTable.panel

        dictionarySeparator = factory.createSeparator(bundle.getString("settings.dictionaries"))
    }

    override fun loadScheme(scheme: WordScheme) {
        minLength.value = scheme.minLength
        maxLength.value = scheme.maxLength
        enclosureGroup.setValue(scheme.enclosure)
        capitalizationGroup.setValue(scheme.capitalization)
        dictionaryTable.data = scheme.bundledDictionaries + scheme.userDictionaries
        dictionaryTable.activeData = scheme.activeBundledDictionaries + scheme.activeUserDictionaries
    }

    override fun saveScheme(scheme: WordScheme) {
        scheme.minLength = minLength.value
        scheme.maxLength = maxLength.value
        scheme.enclosure = enclosureGroup.getValue() ?: DEFAULT_ENCLOSURE
        scheme.capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION
        scheme.bundledDictionaries = dictionaryTable.data.filterIsInstance<BundledDictionary>().toSet()
        scheme.activeBundledDictionaries = dictionaryTable.activeData.filterIsInstance<BundledDictionary>().toSet()
        scheme.userDictionaries = dictionaryTable.data.filterIsInstance<UserDictionary>().toSet()
        scheme.activeUserDictionaries = dictionaryTable.activeData.filterIsInstance<UserDictionary>().toSet()

        BundledDictionary.cache.clear()
        UserDictionary.cache.clear()
    }

    /**
     * Returns true if any dictionaries have been reordered.
     *
     * @param settings the settings to check for modifications
     * @return true if any dictionaries have been reordered
     */
    override fun isModified(settings: WordSettings): Boolean {
        val tableDictionaries = dictionaryTable.data
        val settingsDictionaries = settings.currentScheme.bundledDictionaries + settings.currentScheme.userDictionaries

        return tableDictionaries.size != settingsDictionaries.size ||
            tableDictionaries.zip(settingsDictionaries).any { it.first != it.second }
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


    /**
     * A panel to select schemes from.
     *
     * @param settings the settings model backing up the panel
     */
    private class WordSchemesPanel(settings: WordSettings) : SchemesPanel<WordScheme>(settings) {
        override val type: Class<WordScheme>
            get() = WordScheme::class.java

        override fun createDefaultInstances() = DEFAULT_SCHEMES
    }
}
