package com.fwdekker.randomness.array

import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArraySettings.Companion.DEFAULT_BRACKETS
import com.fwdekker.randomness.array.ArraySettings.Companion.DEFAULT_SEPARATOR
import com.fwdekker.randomness.array.ArraySettings.Companion.MIN_COUNT
import com.fwdekker.randomness.array.ArraySettings.Companion.default
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.event.ChangeEvent


/**
 * Component for settings of random array generation.
 *
 * @param settings the settings to edit in the component
 * @see ArraySettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class ArraySettingsEditor(settings: ArraySettings = default) : StateEditor<ArraySettings>(settings) {
    override lateinit var rootComponent: JPanel private set
    private lateinit var countSpinner: JIntSpinner
    private lateinit var bracketsGroup: ButtonGroup
    private lateinit var separatorGroup: ButtonGroup
    private lateinit var newlineSeparatorButton: JRadioButton
    private lateinit var spaceAfterSeparatorCheckBox: JCheckBox


    init {
        loadState()

        newlineSeparatorButton.addChangeListener {
            spaceAfterSeparatorCheckBox.isEnabled = !newlineSeparatorButton.isSelected
        }
        newlineSeparatorButton.changeListeners.forEach { it.stateChanged(ChangeEvent(newlineSeparatorButton)) }
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        countSpinner = JIntSpinner(value = 1, minValue = MIN_COUNT, description = "count")
    }

    override fun loadState(state: ArraySettings) {
        super.loadState(state)

        countSpinner.value = state.count
        bracketsGroup.setValue(state.brackets)
        separatorGroup.setValue(state.separator)
        spaceAfterSeparatorCheckBox.isSelected = state.isSpaceAfterSeparator
    }

    override fun readState(): ArraySettings =
        ArraySettings(
            count = countSpinner.value,
            brackets = bracketsGroup.getValue() ?: DEFAULT_BRACKETS,
            separator = separatorGroup.getValue() ?: DEFAULT_SEPARATOR,
            isSpaceAfterSeparator = spaceAfterSeparatorCheckBox.isSelected
        )


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            countSpinner, bracketsGroup, separatorGroup, spaceAfterSeparatorCheckBox,
            listener = listener
        )
}
