package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SchemeEditor
import com.fwdekker.randomness.string.StringScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.string.StringScheme.Companion.DEFAULT_ENCLOSURE
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.jgoodies.forms.factories.DefaultComponentFactory
import java.awt.font.TextAttribute
import java.util.ResourceBundle
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel


/**
 * Component for editing random string settings.
 *
 * @param scheme the scheme to edit in the component
 *
 * @see SymbolSetTable
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class StringSchemeEditor(
    scheme: StringScheme = StringScheme(),
    private val symbolSetSettings: SymbolSetSettings = SymbolSetSettings.default
) : SchemeEditor<StringScheme>() {
    override lateinit var rootPane: JPanel private set
    private lateinit var lengthRange: JSpinnerRange
    private lateinit var minLength: JIntSpinner
    private lateinit var maxLength: JIntSpinner
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var symbolSetPanel: JPanel
    private lateinit var symbolSetSeparator: JComponent
    private lateinit var symbolSetTable: SymbolSetTable
    private lateinit var excludeLookAlikeSymbolsCheckBox: JCheckBox


    init {
        loadScheme(scheme)

        excludeLookAlikeSymbolsCheckBox.font = excludeLookAlikeSymbolsCheckBox.font.attributes.toMutableMap()
            .also { it[TextAttribute.UNDERLINE] = TextAttribute.UNDERLINE_LOW_DOTTED }
            .let { excludeLookAlikeSymbolsCheckBox.font.deriveFont(it) }
        excludeLookAlikeSymbolsCheckBox.toolTipText =
            "Excludes the following characters from all generated strings: ${SymbolSet.lookAlikeCharacters}"
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
        symbolSetTable = SymbolSetTable()
        symbolSetPanel = symbolSetTable.panel
        symbolSetSeparator = factory.createSeparator(bundle.getString("settings.symbol_sets"))
    }

    override fun loadScheme(scheme: StringScheme) =
        scheme.also {
            minLength.value = it.minLength
            maxLength.value = it.maxLength
            enclosureGroup.setValue(it.enclosure)
            capitalizationGroup.setValue(it.capitalization)
            excludeLookAlikeSymbolsCheckBox.isSelected = it.excludeLookAlikeSymbols

            symbolSetTable.data = symbolSetSettings.symbolSetList
            symbolSetTable.activeData =
                symbolSetSettings.symbolSetList.filter { symbolSet -> symbolSet.name in it.activeSymbolSets }
        }.let {}

    override fun saveScheme() =
        StringScheme().also {
            it.minLength = minLength.value
            it.maxLength = maxLength.value
            it.enclosure = enclosureGroup.getValue() ?: DEFAULT_ENCLOSURE
            it.capitalization = capitalizationGroup.getValue()?.let(::getMode) ?: DEFAULT_CAPITALIZATION
            it.excludeLookAlikeSymbols = excludeLookAlikeSymbolsCheckBox.isSelected

            symbolSetSettings.symbolSetList = symbolSetTable.data.toSet()
            it.activeSymbolSets = symbolSetTable.activeData.map { symbolSet -> symbolSet.name }.toSet()
        }

    override fun doValidate() =
        minLength.validateValue()
            ?: maxLength.validateValue()
            ?: lengthRange.validateValue()
            ?: symbolSetTable.doValidate(excludeLookAlikeSymbolsCheckBox.isSelected)

    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minLength, maxLength, enclosureGroup, capitalizationGroup, symbolSetTable,
            listener = listener
        )
}
