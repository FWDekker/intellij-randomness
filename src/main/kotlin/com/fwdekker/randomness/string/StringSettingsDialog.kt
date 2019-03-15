package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.SettingsDialog
import com.fwdekker.randomness.ValidationException
import com.fwdekker.randomness.ui.ButtonGroupHelper
import com.fwdekker.randomness.ui.JLongSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import com.intellij.openapi.ui.ValidationInfo
import java.util.HashSet
import javax.swing.ButtonGroup
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel


/**
 * Dialog for settings of random string generation.
 *
 * @param settings the settings to manipulate with this dialog. Defaults to [StringSettings.default]
 */
// TODO Kotlin-ify concatenation of alphabets
class StringSettingsDialog(settings: StringSettings = StringSettings.default) :
    SettingsDialog<StringSettings>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var lengthRange: JSpinnerRange
    private lateinit var minLength: JLongSpinner
    private lateinit var maxLength: JLongSpinner
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var alphabetList: JList<Alphabet>


    init {
        init()
        loadSettings()
    }


    override fun createCenterPanel() = contentPane

    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    private fun createUIComponents() {
        minLength = JLongSpinner(1, 1, Integer.MAX_VALUE.toLong())
        maxLength = JLongSpinner(1, 1, Integer.MAX_VALUE.toLong())
        lengthRange = JSpinnerRange(minLength, maxLength, Integer.MAX_VALUE.toDouble())

        alphabetList = JList(Alphabet.values())
        alphabetList.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        alphabetList.layoutOrientation = JList.HORIZONTAL_WRAP
    }


    override fun loadSettings(settings: StringSettings) {
        minLength.setValue(settings.minLength)
        maxLength.setValue(settings.maxLength)
        ButtonGroupHelper.setValue(enclosureGroup, settings.enclosure)
        ButtonGroupHelper.setValue(capitalizationGroup, settings.capitalization)

        for (i in 0 until Alphabet.values().size) {
            if (settings.alphabets.contains(Alphabet.values()[i])) {
                alphabetList.addSelectionInterval(i, i)
            }
        }
    }

    override fun saveSettings(settings: StringSettings) {
        settings.minLength = Math.toIntExact(minLength.value)
        settings.maxLength = Math.toIntExact(maxLength.value)
        settings.enclosure = ButtonGroupHelper.getValue(enclosureGroup)
        settings.capitalization = CapitalizationMode.getMode(ButtonGroupHelper.getValue(capitalizationGroup))
        settings.alphabets = HashSet(alphabetList.selectedValuesList)
    }

    override fun doValidate(): ValidationInfo? {
        try {
            minLength.validateValue()
            maxLength.validateValue()
            lengthRange.validate()
        } catch (e: ValidationException) {
            return ValidationInfo(e.message ?: "", e.component)
        }

        return if (alphabetList.selectedValuesList.isEmpty()) {
            ValidationInfo("Please select at least one option.", alphabetList)
        } else {
            null
        }
    }
}
