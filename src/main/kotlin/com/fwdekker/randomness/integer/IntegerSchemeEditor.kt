package com.fwdekker.randomness.integer

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SchemeEditor
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
class IntegerSchemeEditor(scheme: IntegerScheme = IntegerScheme()) : SchemeEditor<IntegerScheme>() {
    override lateinit var rootPane: JPanel private set
    private lateinit var valueRange: JSpinnerRange
    private lateinit var minValue: JLongSpinner
    private lateinit var maxValue: JLongSpinner
    private lateinit var base: JIntSpinner
    private lateinit var groupingSeparatorGroup: ButtonGroup
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var prefixInput: JTextField
    private lateinit var suffixInput: JTextField


    init {
        loadScheme(scheme)

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

    override fun loadScheme(scheme: IntegerScheme) =
        scheme.also {
            minValue.value = it.minValue
            maxValue.value = it.maxValue
            base.value = it.base
            groupingSeparatorGroup.setValue(it.groupingSeparator)
            capitalizationGroup.setValue(it.capitalization)
            prefixInput.text = it.prefix
            suffixInput.text = it.suffix
        }.let { }

    override fun saveScheme() =
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
