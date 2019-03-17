package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.SettingsDialog
import com.fwdekker.randomness.ui.ButtonGroupHelper
import com.fwdekker.randomness.ui.JEditableList
import com.fwdekker.randomness.ui.JLongSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.event.ListSelectionEvent


/**
 * Dialog for settings of random word generation.
 *
 * @param settings the settings to manipulate with this dialog. Defaults to [WordSettings.default]
 */
// TODO Kotlin-ify this whole thing
class WordSettingsDialog(settings: WordSettings = WordSettings.default) : SettingsDialog<WordSettings>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var lengthRange: JSpinnerRange
    private lateinit var minLength: JLongSpinner
    private lateinit var maxLength: JLongSpinner
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var dictionaries: JEditableList<Dictionary>
    private lateinit var dictionaryAddButton: JButton
    private lateinit var dictionaryRemoveButton: JButton


    init {
        init()
        loadSettings()
    }


    public override fun createCenterPanel() = contentPane

    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    private fun createUIComponents() {
        minLength = JLongSpinner(1, 1, Integer.MAX_VALUE.toLong())
        maxLength = JLongSpinner(1, 1, Integer.MAX_VALUE.toLong())
        lengthRange = JSpinnerRange(minLength, maxLength, Integer.MAX_VALUE.toDouble())

        dictionaries = JEditableList()
        dictionaries.selectionModel.addListSelectionListener(this::onDictionaryHighlightChange)
        dictionaries.addEntryActivityChangeListener { onDictionaryActivityChange() }
        onDictionaryActivityChange()

        dictionaryAddButton = JButton()
        dictionaryAddButton.addActionListener { addDictionary() }
        dictionaryRemoveButton = JButton()
        dictionaryRemoveButton.addActionListener { removeDictionary() }
    }


    override fun loadSettings(settings: WordSettings) {
        minLength.setValue(settings.minLength)
        maxLength.setValue(settings.maxLength)
        ButtonGroupHelper.setValue(enclosureGroup, settings.enclosure)
        ButtonGroupHelper.setValue(capitalizationGroup, settings.capitalization)

        dictionaries.setEntries(settings.bundledDictionaries + settings.userDictionaries)
        dictionaries.setActiveEntries(settings.activeBundledDictionaries + settings.activeUserDictionaries)
    }

    override fun saveSettings(settings: WordSettings) {
        settings.minLength = Math.toIntExact(minLength.value)
        settings.maxLength = Math.toIntExact(maxLength.value)
        settings.enclosure = ButtonGroupHelper.getValue(enclosureGroup)!! // TODO Remove !!
        settings.capitalization = CapitalizationMode.getMode(ButtonGroupHelper.getValue(capitalizationGroup)!!)

        settings.bundledDictionaries = dictionaries.entries
            .filterIsInstance<BundledDictionary>()
            .toSet()
        settings.activeBundledDictionaries = dictionaries.activeEntries
            .filterIsInstance<BundledDictionary>()
            .toSet()
        BundledDictionary.cache.clear()

        settings.userDictionaries = dictionaries.entries
            .filterIsInstance<UserDictionary>()
            .toSet()
        settings.activeUserDictionaries = dictionaries.activeEntries
            .filterIsInstance<UserDictionary>()
            .toSet()
        UserDictionary.cache.clear()
    }

    public override fun doValidate(): ValidationInfo? {
        if (dictionaries.activeEntries.isEmpty())
            return ValidationInfo("Select at least one dictionary.", dictionaries)

        // TODO Improve error message
        if (dictionaries.activeEntries.any { !it.isValid() })
            return ValidationInfo("One of these dictionaries is not valid.", dictionaries)

        // TODO Add error message if there are no words in the meta-dictionary

        return null
            ?: minLength.validateValue()
            ?: maxLength.validateValue()
            ?: lengthRange.validateValue()
    }


    /**
     * Fires when a new `Dictionary` is added to the list.
     */
    private fun addDictionary() {
        FileChooser.chooseFiles(FileChooserDescriptorFactory.createSingleFileDescriptor("dic"), null, null) { files ->
            if (files.isEmpty())
                return@chooseFiles

            val canonicalPath = files.firstOrNull()?.canonicalPath
                ?: return@chooseFiles

            val newDictionary = UserDictionary.cache.get(canonicalPath, false)
            // TODO Can this check be moved elsewhere?
            if (!newDictionary.isValid()) {
                JBPopupFactory.getInstance()
                    // TODO Improve error message
                    .createHtmlTextBalloonBuilder("Failed to read the dictionary file.", MessageType.ERROR, null)
                    .createBalloon()
                    .show(RelativePoint.getSouthOf(dictionaryAddButton), Balloon.Position.below)
                return@chooseFiles
            }

            // TODO Add message if dictionary is empty

            dictionaries.addEntry(newDictionary)
        }
    }

    /**
     * Fires when the currently highlighted `Dictionary` should be removed the list.
     */
    private fun removeDictionary() {
        dictionaries.highlightedEntry?.let { dictionary ->
            if (dictionary is UserDictionary)
                dictionaries.removeEntry(dictionary)
        }
    }

    /**
     * Fires when the user (un)highlights a dictionary.
     *
     * @param event the triggering event
     */
    private fun onDictionaryHighlightChange(event: ListSelectionEvent) {
        if (!event.valueIsAdjusting) {
            val highlightedDictionary = dictionaries.highlightedEntry
            val enable = highlightedDictionary is UserDictionary
            dictionaryRemoveButton.isEnabled = enable
        }
    }

    /**
     * Fires when the user (de)activates a dictionary.
     */
    private fun onDictionaryActivityChange() {
        val words = dictionaries.activeEntries
            .fold(emptySet<String>()) { acc, dictionary -> (acc + dictionary.words).toSet() }

        if (words.isEmpty()) {
            minLength.maxValue = 1
            maxLength.minValue = Integer.MAX_VALUE.toLong()
        } else {
            minLength.maxValue = words.maxBy { it.length }!!.length.toLong() // TODO Should be safe right?
            maxLength.minValue = words.minBy { it.length }!!.length.toLong()
        }
    }
}
