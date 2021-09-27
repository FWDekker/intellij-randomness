package com.fwdekker.randomness.array

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecorator.Companion.DEFAULT_BRACKETS
import com.fwdekker.randomness.array.ArrayDecorator.Companion.DEFAULT_SEPARATOR
import com.fwdekker.randomness.array.ArrayDecorator.Companion.MIN_COUNT
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.VariableLabelRadioButton
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.Component
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.event.ChangeEvent


/**
 * Component for settings of random array generation.
 *
 * @param settings the settings to edit in the component
 * @param disablable `true` if and only if the user has the option of disabling the array scheme. If this is set to
 * `false`, [readState] will return a decorator which is always enabled.
 * @param helpText the text displayed at the top
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class ArrayDecoratorEditor(settings: ArrayDecorator, disablable: Boolean = true, helpText: String? = null) :
    StateEditor<ArrayDecorator>(settings) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = minCountSpinner.editorComponent

    private lateinit var separator: TitledSeparator
    private lateinit var enabledCheckBox: JCheckBox
    private lateinit var helpLabel: JLabel
    private lateinit var controlPanel: JPanel
    private lateinit var minCountSpinner: JIntSpinner
    private lateinit var maxCountSpinner: JIntSpinner
    private lateinit var bracketsGroup: ButtonGroup
    private lateinit var customBrackets: VariableLabelRadioButton
    private lateinit var separatorGroup: ButtonGroup
    private lateinit var customSeparator: VariableLabelRadioButton
    private lateinit var newlineSeparatorButton: JRadioButton
    private lateinit var spaceAfterSeparatorCheckBox: JCheckBox


    init {
        if (disablable) {
            enabledCheckBox.addChangeListener {
                controlPanel.getChildren().forEach { it.isEnabled = enabledCheckBox.isSelected }
                spaceAfterSeparatorCheckBox.isEnabled = enabledCheckBox.isSelected && !newlineSeparatorButton.isSelected
            }
            enabledCheckBox.changeListeners.forEach { it.stateChanged(ChangeEvent(enabledCheckBox)) }
        } else {
            enabledCheckBox.isVisible = false
        }

        if (helpText != null) {
            helpLabel.text = "<html>$helpText"
            helpLabel.isVisible = true
        }

        customBrackets.addToButtonGroup(bracketsGroup)
        customSeparator.addToButtonGroup(separatorGroup)

        newlineSeparatorButton.addChangeListener {
            spaceAfterSeparatorCheckBox.isEnabled = enabledCheckBox.isSelected && !newlineSeparatorButton.isSelected
        }
        newlineSeparatorButton.changeListeners.forEach { it.stateChanged(ChangeEvent(newlineSeparatorButton)) }

        loadState()
    }

    /**
     * Initializes custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        separator = SeparatorFactory.createSeparator(Bundle("array.title"), null)
        helpLabel = JBLabel().also { it.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND }

        customBrackets = VariableLabelRadioButton(UIConstants.WIDTH_MEDIUM)
        customSeparator = VariableLabelRadioButton()

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
        customBrackets.label = state.customBrackets
        separatorGroup.setValue(state.separator)
        customSeparator.label = state.customSeparator
        spaceAfterSeparatorCheckBox.isSelected = state.isSpaceAfterSeparator
    }

    override fun readState(): ArrayDecorator =
        ArrayDecorator(
            enabled = enabledCheckBox.isSelected,
            minCount = minCountSpinner.value,
            maxCount = maxCountSpinner.value,
            brackets = bracketsGroup.getValue() ?: DEFAULT_BRACKETS,
            customBrackets = customBrackets.label,
            separator = separatorGroup.getValue() ?: DEFAULT_SEPARATOR,
            customSeparator = customSeparator.label,
            isSpaceAfterSeparator = spaceAfterSeparatorCheckBox.isSelected
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            enabledCheckBox, minCountSpinner, maxCountSpinner, bracketsGroup, customBrackets, separatorGroup,
            customSeparator, spaceAfterSeparatorCheckBox,
            listener = listener
        )
}


/**
 * Returns the [Component]s recursively contained in this [JPanel].
 *
 * @return the [Component]s recursively contained in this [JPanel]
 */
private fun JPanel.getChildren(): List<Component> =
    components.flatMap {
        if (it is JPanel) listOf(it) + it.getChildren()
        else listOf(it)
    }.toList()
