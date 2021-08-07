package com.fwdekker.randomness.ui

import javax.swing.AbstractButton
import javax.swing.ButtonGroup


/**
 * Executes a consumer for each button in a group.
 *
 * @param consumer the function to apply to each button
 */
fun ButtonGroup.forEach(consumer: (AbstractButton) -> Unit) = elements.toList().forEach(consumer)

/**
 * Returns the action command of the currently selected button, or `null` if no button is selected.
 *
 * @return the `String` value of the currently selected button, or `null` if no button is selected
 */
fun ButtonGroup.getValue() = elements.toList().firstOrNull { it.isSelected }?.actionCommand

/**
 * Sets the currently selected button to the button with the given action command.
 *
 * If there is no button with the given action command, all buttons will be deselected.
 * If there are multiple buttons with the given action command, any of the matching buttons may be selected.
 *
 * @param value an `Object` of which [toString] returns an action command
 */
fun ButtonGroup.setValue(value: Any?) {
    selection?.isSelected = false
    elements.toList().firstOrNull { it.actionCommand == value?.toString() }?.isSelected = true
}

/**
 * Enables or disables all buttons.
 *
 * @param enabled true if all buttons should be enabled, false if all buttons should be disabled
 */
fun ButtonGroup.setEnabled(enabled: Boolean) = forEach { it.isEnabled = enabled }

/**
 * Returns the buttons in this button group as a typed array.
 *
 * @return the buttons in this button group as a typed array
 */
fun ButtonGroup.buttons() = elements.toList().toTypedArray()
