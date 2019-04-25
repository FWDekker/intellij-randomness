package com.fwdekker.randomness.ui

import java.util.NoSuchElementException
import javax.swing.AbstractButton
import javax.swing.ButtonGroup


/**
 * A helper class for using [ButtonGroup]s.
 */
object ButtonGroupHelper {
    /**
     * Executes a consumer for each button in a group.
     *
     * @param group the group of buttons
     * @param consumer the function to apply to each button
     */
    // TODO Inline this method once UI is in Kotlin
    fun forEach(group: ButtonGroup, consumer: (AbstractButton) -> Unit) = group.elements.toList().forEach(consumer)

    /**
     * Returns the action command of the currently selected button, or `null` if no button is selected.
     *
     * @param group a `ButtonGroup`
     * @return the `String` value of the currently selected button, or `null` if no button is selected
     */
    fun getValue(group: ButtonGroup) =
        group.elements.toList()
            .filter { it.isSelected }
            .map { it.actionCommand }
            .firstOrNull()

    /**
     * Changes the currently selected button to the button with the given action command.
     *
     * @param group a `ButtonGroup`
     * @param value an action command
     */
    fun setValue(group: ButtonGroup, value: String) {
        group.elements.toList()
            .firstOrNull { button -> button.actionCommand == if (value == "\u0000") "" else value }
            ?.also { it.isSelected = true }
            ?: throw NoSuchElementException("Could not find a button with action command `$value`.")
    }

    /**
     * Sets the currently selected button to the button with the given action command.
     *
     * @param group a `ButtonGroup`
     * @param value an `Object` of which [toString] returns an action command
     */
    fun setValue(group: ButtonGroup, value: Any) = setValue(group, value.toString())
}
