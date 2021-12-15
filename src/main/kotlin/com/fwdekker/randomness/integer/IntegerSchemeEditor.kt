package com.fwdekker.randomness.integer

import com.fwdekker.randomness.CapitalizationMode.Companion.getMode
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.fixedlength.FixedLengthDecoratorEditor
import com.fwdekker.randomness.integer.IntegerScheme.Companion.DEFAULT_CAPITALIZATION
import com.fwdekker.randomness.integer.IntegerScheme.Companion.DEFAULT_GROUPING_SEPARATOR
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.JLongSpinner
import com.fwdekker.randomness.ui.MaxLengthDocumentFilter
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.forEach
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setLabel
import com.fwdekker.randomness.ui.setValue
import javax.swing.ButtonGroup
import javax.swing.JLabel
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
    override val preferredFocusedComponent
        get() = minValue.editorComponent

    private lateinit var minValue: JLongSpinner
    private lateinit var maxValue: JLongSpinner
    private lateinit var base: JIntSpinner
    private lateinit var groupingSeparatorLabel: JLabel
    private lateinit var groupingSeparatorGroup: ButtonGroup
    private lateinit var customGroupingSeparator: VariableLabelRadioButton
    private lateinit var capitalizationLabel: JLabel
    private lateinit var capitalizationGroup: ButtonGroup
    private lateinit var prefixInput: JTextField
    private lateinit var suffixInput: JTextField
    private lateinit var fixedLengthDecoratorPanel: JPanel
    private lateinit var fixedLengthDecoratorEditor: FixedLengthDecoratorEditor
    private lateinit var arrayDecoratorPanel: JPanel
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        base.addChangeListener(
            { _: ChangeEvent? ->
                groupingSeparatorGroup.forEach { it.isEnabled = base.value == IntegerScheme.DECIMAL_BASE }
                customGroupingSeparator.isEnabled = base.value == IntegerScheme.DECIMAL_BASE

                capitalizationGroup.forEach { it.isEnabled = base.value > IntegerScheme.DECIMAL_BASE }
            }.also { it(null) }
        )

        customGroupingSeparator.addToButtonGroup(groupingSeparatorGroup)
        groupingSeparatorGroup.setLabel(groupingSeparatorLabel)

        capitalizationGroup.setLabel(capitalizationLabel)

        loadState()
    }

    /**
     * Initializes custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        minValue = JLongSpinner()
        maxValue = JLongSpinner()
        bindSpinners(minValue, maxValue, maxRange = null)
        base = JIntSpinner(IntegerScheme.DECIMAL_BASE, IntegerScheme.MIN_BASE, IntegerScheme.MAX_BASE)

        customGroupingSeparator = VariableLabelRadioButton(UIConstants.WIDTH_TINY, MaxLengthDocumentFilter(1))

        fixedLengthDecoratorEditor = FixedLengthDecoratorEditor(originalState.fixedLengthDecorator)
        fixedLengthDecoratorPanel = fixedLengthDecoratorEditor.rootComponent

        arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
        arrayDecoratorPanel = arrayDecoratorEditor.rootComponent
    }


    override fun loadState(state: IntegerScheme) {
        super.loadState(state)

        minValue.value = state.minValue
        maxValue.value = state.maxValue
        base.value = state.base
        customGroupingSeparator.label = state.customGroupingSeparator
        groupingSeparatorGroup.setValue(state.groupingSeparator)
        capitalizationGroup.setValue(state.capitalization)
        prefixInput.text = state.prefix
        suffixInput.text = state.suffix
        fixedLengthDecoratorEditor.loadState(state.fixedLengthDecorator)
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        IntegerScheme(
            minValue = minValue.value,
            maxValue = maxValue.value,
            base = base.value,
            groupingSeparator = groupingSeparatorGroup.getValue() ?: DEFAULT_GROUPING_SEPARATOR,
            customGroupingSeparator = customGroupingSeparator.label,
            capitalization = capitalizationGroup.getValue()?.let { getMode(it) } ?: DEFAULT_CAPITALIZATION,
            prefix = prefixInput.text,
            suffix = suffixInput.text,
            fixedLengthDecorator = fixedLengthDecoratorEditor.readState(),
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }

    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minValue, maxValue, base, groupingSeparatorGroup, customGroupingSeparator, capitalizationGroup, prefixInput,
            suffixInput, fixedLengthDecoratorEditor, arrayDecoratorEditor,
            listener = listener
        )
}
