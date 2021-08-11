package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SchemeEditor
import com.fwdekker.randomness.array.ArraySchemeDecoratorEditor
import com.fwdekker.randomness.string.StringScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.string.StringScheme.Companion.DEFAULT_ENCLOSURE
import com.fwdekker.randomness.string.StringScheme.Companion.MAX_LENGTH_DIFFERENCE
import com.fwdekker.randomness.string.StringScheme.Companion.MIN_LENGTH
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
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
class StringSchemeEditor(scheme: StringScheme = StringScheme()) : SchemeEditor<StringScheme>(scheme) {
    override lateinit var rootComponent: JPanel private set
    private lateinit var minLength: JIntSpinner
    private lateinit var maxLength: JIntSpinner
    private lateinit var enclosureGroup: ButtonGroup
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var symbolSetPanel: JPanel
    private lateinit var symbolSetSeparator: JComponent
    private lateinit var symbolSetTable: SymbolSetTable
    private lateinit var excludeLookAlikeSymbolsCheckBox: JCheckBox
    private lateinit var arrayDecoratorPanel: JPanel
    private lateinit var arrayDecoratorEditor: ArraySchemeDecoratorEditor


    init {
        excludeLookAlikeSymbolsCheckBox.font = excludeLookAlikeSymbolsCheckBox.font.attributes.toMutableMap()
            .also { it[TextAttribute.UNDERLINE] = TextAttribute.UNDERLINE_LOW_DOTTED }
            .let { excludeLookAlikeSymbolsCheckBox.font.deriveFont(it) }
        excludeLookAlikeSymbolsCheckBox.toolTipText =
            "Excludes the following characters from all generated strings: ${SymbolSet.lookAlikeCharacters}"

        loadScheme(scheme)
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

        minLength = JIntSpinner(value = MIN_LENGTH, minValue = MIN_LENGTH)
        maxLength = JIntSpinner(value = MIN_LENGTH, minValue = MIN_LENGTH)
        bindSpinners(minLength, maxLength, maxRange = MAX_LENGTH_DIFFERENCE)

        symbolSetSeparator = factory.createSeparator(bundle.getString("settings.symbol_sets"))
        symbolSetTable = SymbolSetTable()
        symbolSetPanel = symbolSetTable.panel

        arrayDecoratorEditor = ArraySchemeDecoratorEditor(originalScheme.decorator)
        arrayDecoratorPanel = arrayDecoratorEditor.rootComponent
    }


    override fun loadScheme(scheme: StringScheme) {
        super.loadScheme(scheme)

        minLength.value = scheme.minLength
        maxLength.value = scheme.maxLength
        enclosureGroup.setValue(scheme.enclosure)
        capitalizationGroup.setValue(scheme.capitalization)
        excludeLookAlikeSymbolsCheckBox.isSelected = scheme.excludeLookAlikeSymbols

        symbolSetTable.data = scheme.symbolSetSettings.symbolSetList
        symbolSetTable.activeData =
            scheme.symbolSetSettings.symbolSetList.filter { symbolSet -> symbolSet.name in scheme.activeSymbolSets }

        arrayDecoratorEditor.loadScheme(scheme.decorator)
    }

    override fun readScheme() =
        StringScheme(
            minLength = minLength.value,
            maxLength = maxLength.value,
            enclosure = enclosureGroup.getValue() ?: DEFAULT_ENCLOSURE,
            capitalization = capitalizationGroup.getValue()?.let(::getMode) ?: DEFAULT_CAPITALIZATION,
            excludeLookAlikeSymbols = excludeLookAlikeSymbolsCheckBox.isSelected,
            decorator = arrayDecoratorEditor.readScheme()
        ).also {
            it.symbolSetSettings.symbolSetList = symbolSetTable.data.toSet()
            it.activeSymbolSets = symbolSetTable.activeData.map { symbolSet -> symbolSet.name }.toSet()
        }

    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minLength, maxLength, enclosureGroup, capitalizationGroup, symbolSetTable, arrayDecoratorEditor,
            listener = listener
        )
}
