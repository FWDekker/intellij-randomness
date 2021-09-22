package com.fwdekker.randomness.string

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.string.StringScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.string.StringScheme.Companion.DEFAULT_QUOTATION
import com.fwdekker.randomness.string.StringScheme.Companion.MAX_LENGTH_DIFFERENCE
import com.fwdekker.randomness.string.StringScheme.Companion.MIN_LENGTH
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TitledSeparator
import java.awt.font.TextAttribute
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel


/**
 * Component for editing random string settings.
 *
 * @param scheme the scheme to edit in the component
 * @see SymbolSetTable
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class StringSchemeEditor(scheme: StringScheme = StringScheme()) : StateEditor<StringScheme>(scheme) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = minLength.editorComponent

    private lateinit var titleSeparator: TitledSeparator
    private lateinit var minLength: JIntSpinner
    private lateinit var maxLength: JIntSpinner
    private lateinit var quotationGroup: ButtonGroup
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var symbolSetPanel: JPanel
    private lateinit var symbolSetSeparator: TitledSeparator
    private lateinit var symbolSetTable: SymbolSetTable
    private lateinit var excludeLookAlikeSymbolsCheckBox: JCheckBox
    private lateinit var arrayDecoratorPanel: JPanel
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        excludeLookAlikeSymbolsCheckBox.font = excludeLookAlikeSymbolsCheckBox.font.attributes.toMutableMap()
            .also { it[TextAttribute.UNDERLINE] = TextAttribute.UNDERLINE_LOW_DOTTED }
            .let { excludeLookAlikeSymbolsCheckBox.font.deriveFont(it) }
        excludeLookAlikeSymbolsCheckBox.toolTipText =
            Bundle("string.ui.exclude_look_alike_help", SymbolSet.lookAlikeCharacters)

        loadState()
    }

    /**
     * Initializes custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        titleSeparator = SeparatorFactory.createSeparator(Bundle("string.title"), null)

        minLength = JIntSpinner(value = MIN_LENGTH, minValue = MIN_LENGTH)
        maxLength = JIntSpinner(value = MIN_LENGTH, minValue = MIN_LENGTH)
        bindSpinners(minLength, maxLength, maxRange = MAX_LENGTH_DIFFERENCE.toDouble())

        symbolSetSeparator = SeparatorFactory.createSeparator(Bundle("string.symbol_sets.title"), null)
        symbolSetTable = SymbolSetTable()
        symbolSetPanel = symbolSetTable.panel

        arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
        arrayDecoratorPanel = arrayDecoratorEditor.rootComponent
    }


    override fun loadState(state: StringScheme) {
        super.loadState(state)

        minLength.value = state.minLength
        maxLength.value = state.maxLength
        quotationGroup.setValue(state.quotation)
        capitalizationGroup.setValue(state.capitalization)
        excludeLookAlikeSymbolsCheckBox.isSelected = state.excludeLookAlikeSymbols

        symbolSetTable.data = (+state.symbolSetSettings).symbolSets
        symbolSetTable.activeData = (+state.symbolSetSettings).symbolSets.filter { it.name in state.activeSymbolSets }

        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        StringScheme(
            minLength = minLength.value,
            maxLength = maxLength.value,
            quotation = quotationGroup.getValue() ?: DEFAULT_QUOTATION,
            capitalization = capitalizationGroup.getValue()?.let(::getMode) ?: DEFAULT_CAPITALIZATION,
            excludeLookAlikeSymbols = excludeLookAlikeSymbolsCheckBox.isSelected,
            activeSymbolSets = symbolSetTable.activeData.map { symbolSet -> symbolSet.name }.toSet(),
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also {
            it.uuid = originalState.uuid

            it.symbolSetSettings += (+originalState.symbolSetSettings).deepCopy(retainUuid = true)
            (+it.symbolSetSettings).symbolSets = symbolSetTable.data
        }

    override fun applyState() {
        super.applyState()
        (+originalState.symbolSetSettings).copyFrom(+readState().symbolSetSettings)
    }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minLength, maxLength, quotationGroup, capitalizationGroup, symbolSetTable, arrayDecoratorEditor,
            listener = listener
        )
}
