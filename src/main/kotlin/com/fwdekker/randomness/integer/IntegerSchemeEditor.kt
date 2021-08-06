package com.fwdekker.randomness.integer

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.integer.IntegerScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.integer.IntegerScheme.Companion.DEFAULT_GROUPING_SEPARATOR
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JLongSpinner
import com.fwdekker.randomness.ui.JSpinnerRange
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.forEach
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.ChangeEvent


/**
 * Component for editing random integer settings.
 *
 * @param scheme the scheme to edit in the component
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class IntegerSchemeEditor(scheme: IntegerScheme = IntegerScheme()) : StateEditor<IntegerScheme>(scheme) {
    override lateinit var rootComponent: JPanel private set
    private lateinit var valueRange: JSpinnerRange
    private lateinit var minValue: JLongSpinner
    private lateinit var maxValue: JLongSpinner
    private lateinit var base: JIntSpinner
    private lateinit var groupingSeparatorGroup: ButtonGroup
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var prefixInput: JTextField
    private lateinit var suffixInput: JTextField


    init {
        loadState(scheme)

        base.addChangeListener {
            groupingSeparatorGroup.forEach { it.isEnabled = base.value == IntegerScheme.DECIMAL_BASE }
            capitalizationGroup.forEach { it.isEnabled = base.value > IntegerScheme.DECIMAL_BASE }
        }
        base.changeListeners.forEach { it.stateChanged(ChangeEvent(base)) }
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        minValue = JLongSpinner(description = "minimum value")
        maxValue = JLongSpinner(description = "maximum value")
        base = JIntSpinner(
            IntegerScheme.DECIMAL_BASE,
            IntegerScheme.MIN_BASE, IntegerScheme.MAX_BASE,
            description = "base"
        )
        valueRange = JSpinnerRange(minValue, maxValue, maxRange = null, "value")
    }

    override fun loadState(state: IntegerScheme) {
        super.loadState(state)

        minValue.value = state.minValue
        maxValue.value = state.maxValue
        base.value = state.base
        groupingSeparatorGroup.setValue(state.groupingSeparator)
        capitalizationGroup.setValue(state.capitalization)
        prefixInput.text = state.prefix
        suffixInput.text = state.suffix
    }

    override fun readState() =
        IntegerScheme(
            minValue = minValue.value,
            maxValue = maxValue.value,
            base = base.value,
            groupingSeparator = groupingSeparatorGroup.getValue() ?: DEFAULT_GROUPING_SEPARATOR,
            capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION,
            prefix = prefixInput.text,
            suffix = suffixInput.text
        )

    override fun doValidate() =
        minValue.validateValue()
            ?: maxValue.validateValue()
            ?: base.validateValue()
            ?: valueRange.validateValue()

    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minValue, maxValue, base, groupingSeparatorGroup, capitalizationGroup, prefixInput, suffixInput,
            listener = listener
        )
}
