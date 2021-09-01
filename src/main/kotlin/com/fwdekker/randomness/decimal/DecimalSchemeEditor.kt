package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArraySchemeDecoratorEditor
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.DEFAULT_DECIMAL_SEPARATOR
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.DEFAULT_GROUPING_SEPARATOR
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.MAX_VALUE_DIFFERENCE
import com.fwdekker.randomness.decimal.DecimalScheme.Companion.MIN_DECIMAL_COUNT
import com.fwdekker.randomness.ui.JDoubleSpinner
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.ChangeEvent


/**
 * Component for editing random decimal settings.
 *
 * @param scheme the scheme to edit in the component
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class DecimalSchemeEditor(scheme: DecimalScheme = DecimalScheme()) : StateEditor<DecimalScheme>(scheme) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent = minValue.editorComponent

    private lateinit var minValue: JDoubleSpinner
    private lateinit var maxValue: JDoubleSpinner
    private lateinit var decimalCount: JIntSpinner
    private lateinit var showTrailingZeroesCheckBox: JCheckBox
    private lateinit var groupingSeparatorGroup: ButtonGroup
    private lateinit var decimalSeparatorGroup: ButtonGroup
    private lateinit var prefixInput: JTextField
    private lateinit var suffixInput: JTextField
    private lateinit var arrayDecoratorPanel: JPanel
    private lateinit var arrayDecoratorEditor: ArraySchemeDecoratorEditor


    init {
        decimalCount.addChangeListener { showTrailingZeroesCheckBox.isEnabled = decimalCount.value > 0 }
        decimalCount.changeListeners.forEach { it.stateChanged(ChangeEvent(decimalCount)) }

        loadState()
    }

    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        minValue = JDoubleSpinner()
        maxValue = JDoubleSpinner()
        bindSpinners(minValue, maxValue, MAX_VALUE_DIFFERENCE)
        decimalCount = JIntSpinner(value = MIN_DECIMAL_COUNT, minValue = MIN_DECIMAL_COUNT)

        arrayDecoratorEditor = ArraySchemeDecoratorEditor(originalState.arrayDecorator)
        arrayDecoratorPanel = arrayDecoratorEditor.rootComponent
    }


    override fun loadState(state: DecimalScheme) {
        super.loadState(state)

        minValue.value = state.minValue
        maxValue.value = state.maxValue
        decimalCount.value = state.decimalCount
        showTrailingZeroesCheckBox.isSelected = state.showTrailingZeroes
        groupingSeparatorGroup.setValue(state.groupingSeparator)
        decimalSeparatorGroup.setValue(state.decimalSeparator)
        prefixInput.text = state.prefix
        suffixInput.text = state.suffix
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        DecimalScheme(
            minValue = minValue.value,
            maxValue = maxValue.value,
            decimalCount = decimalCount.value,
            showTrailingZeroes = showTrailingZeroesCheckBox.isSelected,
            groupingSeparator = groupingSeparatorGroup.getValue() ?: DEFAULT_GROUPING_SEPARATOR,
            decimalSeparator = decimalSeparatorGroup.getValue() ?: DEFAULT_DECIMAL_SEPARATOR,
            prefix = prefixInput.text,
            suffix = suffixInput.text,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minValue, maxValue, decimalCount, showTrailingZeroesCheckBox, groupingSeparatorGroup, decimalSeparatorGroup,
            prefixInput, suffixInput, arrayDecoratorEditor,
            listener = listener
        )
}
