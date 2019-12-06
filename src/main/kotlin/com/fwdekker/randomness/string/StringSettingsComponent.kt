package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.string.StringSettings.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.string.StringSettings.Companion.DEFAULT_ENCLOSURE
import com.fwdekker.randomness.string.StringSettings.Companion.default
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.intellij.openapi.ui.ValidationInfo
import java.util.ArrayList
import javax.swing.ButtonGroup
import javax.swing.JPanel


/**
 * Component for settings of random string generation.
 *
 * @see StringSettings
 * @see StringSettingsAction
 * @see SymbolSetTable
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class StringSettingsComponent(settings: StringSettings = default) : SettingsComponent<StringSettings>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var lengthRange: JSpinnerRange
    private lateinit var minLength: JIntSpinner
    private lateinit var maxLength: JIntSpinner
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var symbolSetPanel: JPanel
    private lateinit var symbolSetTable: SymbolSetTable

    override val rootPane get() = contentPane


    init {
        loadSettings()
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        minLength = JIntSpinner(1, 1)
        maxLength = JIntSpinner(1, 1)
        lengthRange = JSpinnerRange(minLength, maxLength, Int.MAX_VALUE.toDouble(), "length")
        symbolSetTable = SymbolSetTable()
        symbolSetPanel = symbolSetTable.createComponent()
    }

    override fun loadSettings(settings: StringSettings) {
        minLength.value = settings.minLength
        maxLength.value = settings.maxLength
        enclosureGroup.setValue(settings.enclosure)
        capitalizationGroup.setValue(settings.capitalization)
        symbolSetTable.data = settings.symbolSetList
        symbolSetTable.activeData = settings.activeSymbolSetList
    }

    override fun saveSettings(settings: StringSettings) {
        settings.minLength = minLength.value
        settings.maxLength = maxLength.value
        settings.enclosure = enclosureGroup.getValue() ?: DEFAULT_ENCLOSURE
        settings.capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION
        settings.symbolSetList = symbolSetTable.data
        settings.activeSymbolSetList = symbolSetTable.activeData
    }

    override fun isModified(settings: StringSettings): Boolean {
        val tableSymbolSets: List<SymbolSet> = ArrayList(symbolSetTable.data)
        val settingsSymbolSets: List<SymbolSet> = ArrayList(settings.symbolSetList)

        return tableSymbolSets.size != settingsSymbolSets.size ||
            tableSymbolSets.zip(settingsSymbolSets).any { it.first != it.second }
    }

    override fun doValidate() =
        when {
            symbolSetTable.data.any { it.name.isEmpty() } ->
                ValidationInfo("All symbol sets must have a name.", symbolSetPanel)
            symbolSetTable.data.map { it.name }.distinct().size != symbolSetTable.data.size ->
                ValidationInfo("Symbol sets must have unique names.", symbolSetPanel)
            symbolSetTable.data.any { it.symbols.isEmpty() } ->
                ValidationInfo("Symbol sets must have at least one symbol each.", symbolSetPanel)
            symbolSetTable.activeData.isEmpty() ->
                ValidationInfo("Activate at least one symbol set.", symbolSetPanel)
            else ->
                minLength.validateValue()
                    ?: maxLength.validateValue()
                    ?: lengthRange.validateValue()
        }
}
