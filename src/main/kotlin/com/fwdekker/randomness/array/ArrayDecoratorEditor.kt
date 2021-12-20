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
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setLabel
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
 * @param showSeparator `true` if and only if a titled separator should be shown at the top
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class ArrayDecoratorEditor(
    settings: ArrayDecorator,
    disablable: Boolean = true,
    helpText: String? = null,
    showSeparator: Boolean = true
) : StateEditor<ArrayDecorator>(settings) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = countSpinner.editorComponent

    private lateinit var separator: TitledSeparator
    private lateinit var enabledCheckBox: JCheckBox
    private lateinit var helpLabel: JLabel
    private lateinit var controlPanel: JPanel
    private lateinit var countSpinner: JIntSpinner
    private lateinit var bracketsLabel: JLabel
    private lateinit var bracketsGroup: ButtonGroup
    private lateinit var customBrackets: VariableLabelRadioButton
    private lateinit var separatorLabel: JLabel
    private lateinit var separatorGroup: ButtonGroup
    private lateinit var customSeparator: VariableLabelRadioButton
    private lateinit var newlineSeparatorButton: JRadioButton
    private lateinit var spaceAfterSeparatorCheckBox: JCheckBox


    init {
        if (disablable) {
            enabledCheckBox.addChangeListener(
                { _: ChangeEvent? ->
                    controlPanel.getChildren().forEach { it.isEnabled = enabledCheckBox.isSelected }
                    spaceAfterSeparatorCheckBox.isEnabled =
                        enabledCheckBox.isSelected && !newlineSeparatorButton.isSelected
                }.also { it(null) }
            )
        } else {
            enabledCheckBox.isVisible = false
        }

        if (helpText != null) {
            helpLabel.text = "<html>$helpText"
            helpLabel.isVisible = true
        }

        if (!showSeparator) separator.isVisible = false

        customBrackets.addToButtonGroup(bracketsGroup)
        bracketsGroup.setLabel(bracketsLabel)

        customSeparator.addToButtonGroup(separatorGroup)
        separatorGroup.setLabel(separatorLabel)

        newlineSeparatorButton.addChangeListener(
            { _: ChangeEvent? ->
                spaceAfterSeparatorCheckBox.isEnabled =
                    (!disablable || enabledCheckBox.isSelected) && !newlineSeparatorButton.isSelected
            }.also { it(null) }
        )

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

        countSpinner = JIntSpinner(value = MIN_COUNT, minValue = MIN_COUNT)
    }


    override fun loadState(state: ArrayDecorator) {
        super.loadState(state)

        enabledCheckBox.isSelected = state.enabled
        countSpinner.value = state.count
        customBrackets.label = state.customBrackets
        bracketsGroup.setValue(state.brackets)
        customSeparator.label = state.customSeparator
        separatorGroup.setValue(state.separator)
        spaceAfterSeparatorCheckBox.isSelected = state.isSpaceAfterSeparator
    }

    override fun readState(): ArrayDecorator =
        ArrayDecorator(
            enabled = enabledCheckBox.isSelected,
            count = countSpinner.value,
            brackets = bracketsGroup.getValue() ?: DEFAULT_BRACKETS,
            customBrackets = customBrackets.label,
            separator = separatorGroup.getValue() ?: DEFAULT_SEPARATOR,
            customSeparator = customSeparator.label,
            isSpaceAfterSeparator = spaceAfterSeparatorCheckBox.isSelected
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            enabledCheckBox, countSpinner, bracketsGroup, customBrackets, separatorGroup, customSeparator,
            spaceAfterSeparatorCheckBox,
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
        when (it) {
            is VariableLabelRadioButton -> listOf(it)
            is JPanel -> listOf(it) + it.getChildren()
            else -> listOf(it)
        }
    }.toList()
