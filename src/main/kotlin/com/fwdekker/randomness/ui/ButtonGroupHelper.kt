package com.fwdekker.randomness.ui

import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.JLabel


/**
 * Executes a consumer for each button in a group.
 *
 * @param consumer the function to apply to each button
 */
fun ButtonGroup.forEach(consumer: (AbstractButton) -> Unit) = buttons().forEach(consumer)

/**
 * Returns the action command of the currently selected button, or `null` if no button is selected.
 *
 * @return the action command of the currently selected button, or `null` if no button is selected
 */
fun ButtonGroup.getValue() = buttons().firstOrNull { it.isSelected }?.actionCommand

/**
 * Sets the currently selected button to the button with the given action command.
 *
 * If there is no button with the given action command, all buttons will be deselected. If there are multiple buttons
 * with the given action command, any of the matching buttons may be selected.
 *
 * @param value the [Object] of which [toString] returns an action command
 */
fun ButtonGroup.setValue(value: Any?) {
    clearSelection()
    buttons().firstOrNull { it.actionCommand == value?.toString() }?.isSelected = true
}

/**
 * Returns the buttons in this button group as a typed array.
 *
 * @return the buttons in this button group as a typed array
 */
fun ButtonGroup.buttons(): Array<AbstractButton> = elements.toList().toTypedArray()

/**
 * Sets the [JLabel] of this group, updating its [JLabel.labelFor] property whenever a different button is selected.
 *
 * @param label the label to change the [JLabel.labelFor] property of
 */
fun ButtonGroup.setLabel(label: JLabel) =
    buttons().forEach { button -> button.addItemListener { if (button.isSelected) label.labelFor = button } }