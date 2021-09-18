package com.fwdekker.randomness.array

import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecorator.Companion.DEFAULT_BRACKETS
import com.fwdekker.randomness.array.ArrayDecorator.Companion.DEFAULT_SEPARATOR
import com.fwdekker.randomness.array.ArrayDecorator.Companion.MIN_COUNT
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.forEach
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.UIUtil
import java.util.ResourceBundle
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JTextArea
import javax.swing.event.ChangeEvent


/**
 * Component for settings of random array generation.
 *
 * @param settings the settings to edit in the component
 * @param disablable true if and only if the user has the option of disabling the array scheme. If this is set to false,
 * [readState] will return a decorator which is always enabled
 * @param helpText the text displayed right at the top
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class ArrayDecoratorEditor(settings: ArrayDecorator, disablable: Boolean = true, helpText: String? = null) :
    StateEditor<ArrayDecorator>(settings) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = minCountSpinner.editorComponent

    private lateinit var separator: TitledSeparator
    private lateinit var enabledCheckBox: JCheckBox
    private lateinit var helpTextArea: JTextArea
    private lateinit var minCountSpinner: JIntSpinner
    private lateinit var maxCountSpinner: JIntSpinner
    private lateinit var bracketsGroup: ButtonGroup
    private lateinit var separatorGroup: ButtonGroup
    private lateinit var newlineSeparatorButton: JRadioButton
    private lateinit var spaceAfterSeparatorCheckBox: JCheckBox


    init {
        if (disablable) {
            enabledCheckBox.addChangeListener {
                minCountSpinner.isEnabled = enabledCheckBox.isSelected
                maxCountSpinner.isEnabled = enabledCheckBox.isSelected
                bracketsGroup.forEach { it.isEnabled = enabledCheckBox.isSelected }
                separatorGroup.forEach { it.isEnabled = enabledCheckBox.isSelected }
                newlineSeparatorButton.isEnabled = enabledCheckBox.isSelected
                spaceAfterSeparatorCheckBox.isEnabled = enabledCheckBox.isSelected
            }
            enabledCheckBox.changeListeners.forEach { it.stateChanged(ChangeEvent(enabledCheckBox)) }
        } else {
            enabledCheckBox.isVisible = false
        }

        if (helpText != null) {
            helpTextArea.text = helpText
            helpTextArea.border = null
            helpTextArea.font = JBLabel().font.deriveFont(UIUtil.getFontSize(UIUtil.FontSize.SMALL))
            helpTextArea.isVisible = true
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
        separator = SeparatorFactory.createSeparator(bundle.getString("settings.array"), null)

        minCountSpinner = JIntSpinner(value = MIN_COUNT, minValue = MIN_COUNT)
        maxCountSpinner = JIntSpinner(value = MIN_COUNT, minValue = MIN_COUNT)
        bindSpinners(minCountSpinner, maxCountSpinner)
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
