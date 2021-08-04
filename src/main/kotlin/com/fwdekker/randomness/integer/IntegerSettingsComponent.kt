package com.fwdekker.randomness.integer

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.SchemeComponent
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
 * Component for settings of random integer generation.
 *
 * @param settings the settings to edit in the component
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class IntegerSettingsComponent(settings: IntegerScheme) :
    SchemeComponent<IntegerScheme>(settings) {
    private lateinit var contentPane: JPanel
    private lateinit var valueRange: JSpinnerRange
    private lateinit var minValue: JLongSpinner
    private lateinit var maxValue: JLongSpinner
    private lateinit var base: JIntSpinner
    private lateinit var groupingSeparatorGroup: ButtonGroup
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var prefixInput: JTextField
    private lateinit var suffixInput: JTextField

    override val rootPane get() = contentPane


    init {
        loadScheme()

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

    override fun loadScheme(scheme: IntegerScheme) {
        minValue.value = scheme.minValue
        maxValue.value = scheme.maxValue
        base.value = scheme.base
        groupingSeparatorGroup.setValue(scheme.groupingSeparator)
        capitalizationGroup.setValue(scheme.capitalization)
        prefixInput.text = scheme.prefix
        suffixInput.text = scheme.suffix
    }

    override fun saveScheme(scheme: IntegerScheme) {
        scheme.minValue = minValue.value
        scheme.maxValue = maxValue.value
        scheme.base = base.value
        scheme.groupingSeparator = groupingSeparatorGroup.getValue() ?: DEFAULT_GROUPING_SEPARATOR
        scheme.capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION
        scheme.prefix = prefixInput.text
        scheme.suffix = suffixInput.text
    }

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

    override fun toUDSDescriptor() = "%Int[]"
}
