package com.fwdekker.randomness.array

import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecorator.Companion.DEFAULT_BRACKETS
import com.fwdekker.randomness.array.ArrayDecorator.Companion.DEFAULT_SEPARATOR
import com.fwdekker.randomness.array.ArrayDecorator.Companion.MIN_COUNT
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.jgoodies.forms.factories.DefaultComponentFactory
import java.util.ResourceBundle
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.event.ChangeEvent


/**
 * Component for settings of random array generation.
 *
 * @param settings the settings to edit in the component
 * @param disablable true if and only if the user has the option of disabling the array scheme. If this is set to false,
 * [readState] will return a decorator which is always enabled
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class ArrayDecoratorEditor(settings: ArrayDecorator, disablable: Boolean = true) :
    StateEditor<ArrayDecorator>(settings) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent = minCountSpinner.editorComponent

    private lateinit var separator: JComponent
    private lateinit var enabledCheckBox: JCheckBox
    private lateinit var arrayDetailsPanel: JPanel
    private lateinit var minCountSpinner: JIntSpinner
    private lateinit var maxCountSpinner: JIntSpinner
    private lateinit var bracketsGroup: ButtonGroup
    private lateinit var separatorGroup: ButtonGroup
    private lateinit var newlineSeparatorButton: JRadioButton
    private lateinit var spaceAfterSeparatorCheckBox: JCheckBox


    init {
        if (disablable) {
            enabledCheckBox.addChangeListener { arrayDetailsPanel.isVisible = enabledCheckBox.isSelected }
            enabledCheckBox.changeListeners.forEach { it.stateChanged(ChangeEvent(enabledCheckBox)) }
        } else {
            enabledCheckBox.isVisible = false
        }

        newlineSeparatorButton.addChangeListener {
            spaceAfterSeparatorCheckBox.isEnabled = !newlineSeparatorButton.isSelected
        }
        newlineSeparatorButton.changeListeners.forEach { it.stateChanged(ChangeEvent(newlineSeparatorButton)) }

        loadState()
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

        minCountSpinner = JIntSpinner(value = MIN_COUNT, minValue = MIN_COUNT)
        maxCountSpinner = JIntSpinner(value = MIN_COUNT, minValue = MIN_COUNT)
        bindSpinners(minCountSpinner, maxCountSpinner)
        separator = factory.createSeparator(bundle.getString("settings.array"))
    }


    override fun loadState(state: ArrayDecorator) {
        super.loadState(state)

        enabledCheckBox.isSelected = state.enabled
        minCountSpinner.value = state.minCount
        maxCountSpinner.value = state.maxCount
        bracketsGroup.setValue(state.brackets)
        separatorGroup.setValue(state.separator)
        spaceAfterSeparatorCheckBox.isSelected = state.isSpaceAfterSeparator
    }

    override fun readState(): ArrayDecorator =
        ArrayDecorator(
            enabled = enabledCheckBox.isSelected,
            minCount = minCountSpinner.value,
            maxCount = maxCountSpinner.value,
            brackets = bracketsGroup.getValue() ?: DEFAULT_BRACKETS,
            separator = separatorGroup.getValue() ?: DEFAULT_SEPARATOR,
            isSpaceAfterSeparator = spaceAfterSeparatorCheckBox.isSelected
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            enabledCheckBox, minCountSpinner, maxCountSpinner, bracketsGroup, separatorGroup,
            spaceAfterSeparatorCheckBox,
            listener = listener
        )
}
