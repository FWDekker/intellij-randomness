package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.SettingsDialog
import com.fwdekker.randomness.ValidationException
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
import java.util.Objects
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
    private// Method used by scene builder
    fun createUIComponents() {
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

        dictionaries.setEntries(settings.validAllDictionaries)
        dictionaries.setActiveEntries(settings.validActiveDictionaries)
    }

    override fun saveSettings(settings: WordSettings) {
        settings.minLength = Math.toIntExact(minLength.value)
        settings.maxLength = Math.toIntExact(maxLength.value)
        settings.enclosure = ButtonGroupHelper.getValue(enclosureGroup)
        settings.capitalization = CapitalizationMode.getMode(ButtonGroupHelper.getValue(capitalizationGroup))

        settings.bundledDictionaries = dictionaries.entries
            .filter { Dictionary.BundledDictionary::class.java.isInstance(it) }
            .map { it.uid }
            .toSet()
        settings.activeBundledDictionaries = dictionaries.activeEntries
            .filter { Dictionary.BundledDictionary::class.java.isInstance(it) }
            .map { it.uid }
            .toSet()
        Dictionary.BundledDictionary.clearCache()

        settings.userDictionaries = dictionaries.entries
            .filter { Dictionary.UserDictionary::class.java.isInstance(it) }
            .map { it.uid }
            .toSet()
        settings.activeUserDictionaries = dictionaries.activeEntries
            .filter { Dictionary.UserDictionary::class.java.isInstance(it) }
            .map { it.uid }
            .toSet()
        Dictionary.UserDictionary.clearCache()
    }

    public override fun doValidate(): ValidationInfo? {
        if (dictionaries.activeEntries.isEmpty()) {
            return ValidationInfo("Select at least one dictionary.", dictionaries)
        }

        val invalidDictionary = dictionaries.activeEntries.stream()
            .map<ValidationInfo> { it.validate() }
            .filter { Objects.nonNull(it) }
            .findFirst()
        if (invalidDictionary.isPresent) {
            return ValidationInfo(invalidDictionary.get().message, dictionaries)
        }

        try {
            minLength.validateValue()
            maxLength.validateValue()
            lengthRange.validate()
        } catch (e: ValidationException) {
            return ValidationInfo(e.message ?: "", e.component)
        }

        return null
    }


    /**
     * Fires when a new `Dictionary` is added to the list.
     */
    private fun addDictionary() {
        FileChooser.chooseFiles(FileChooserDescriptorFactory.createSingleFileDescriptor("dic"), null, null) { files ->
            if (files.isEmpty()) {
                return@chooseFiles
            }

            val validationInfo = Dictionary.UserDictionary.validate(files[0].canonicalPath)
            if (validationInfo != null) {
                JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(validationInfo.message, MessageType.ERROR, null)
                    .createBalloon()
                    .show(RelativePoint.getSouthOf(dictionaryAddButton), Balloon.Position.below)
                return@chooseFiles
            }

            val newDictionary = Dictionary.UserDictionary.get(files[0].canonicalPath, false)
            dictionaries.addEntry(newDictionary)
        }
    }

    /**
     * Fires when the currently highlighted `Dictionary` should be removed the list.
     */
    private fun removeDictionary() {
        dictionaries.highlightedEntry.ifPresent { dictionary ->
            if (dictionary is Dictionary.UserDictionary) {
                dictionaries.removeEntry(dictionary)
            }
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
            val enable = highlightedDictionary.isPresent && highlightedDictionary.get() is Dictionary.UserDictionary
            dictionaryRemoveButton.isEnabled = enable
        }
    }

    /**
     * Fires when the user (de)activates a dictionary.
     */
    private fun onDictionaryActivityChange() {
        val dictionary = Dictionary.combine(dictionaries.activeEntries)

        if (dictionary.words.isEmpty()) {
            minLength.maxValue = 1
            maxLength.minValue = Integer.MAX_VALUE.toLong()
        } else {
            minLength.maxValue = dictionary.longestWord.length.toLong()
            maxLength.minValue = dictionary.shortestWord.length.toLong()
        }
    }
}
