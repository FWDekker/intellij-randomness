package com.fwdekker.randomness.ui

import com.intellij.ui.popup.list.ListPopupImpl
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.util.Locale
import javax.swing.AbstractAction
import javax.swing.KeyStroke


/**
 * Registers actions such that actions can be selected while holding (combinations of) modifier keys.
 *
 * All combinations of modifier keys are registered for events. Additionally, the [captionModifier] function is invoked
 * every time the user presses or releases any modifier key, even while holding other modifier keys.
 *
 * Events are also registered for pressing the Enter key (with or without modifier keys) to invoke the action that is
 * currently highlighted, and for pressing one of the numbers 1-9 (with or without modifier keys) to invoke the action
 * at that index in the popup.
 *
 * @param captionModifier returns the caption to set based on the event
 */
fun ListPopupImpl.registerModifierActions(captionModifier: (ActionEvent?) -> String) {
    val modifiers = listOf(listOf("alt"), listOf("control"), listOf("shift"))
    val optionalModifiers = listOf(listOf("")) + modifiers

    (modifiers * optionalModifiers * optionalModifiers).forEach { (a, b, c) ->
        registerAction(
            "${a}${b}${c}Released",
            KeyStroke.getKeyStroke("$b $c released ${a.uppercase(Locale.getDefault())}"),
            actionListener { setCaption(captionModifier(it)) }
        )
        registerAction(
            "${a}${b}${c}Pressed",
            KeyStroke.getKeyStroke("$a $b $c pressed ${a.uppercase(Locale.getDefault())}"),
            actionListener { setCaption(captionModifier(it)) }
        )
        registerAction(
            "${a}${b}${c}invokeAction",
            KeyStroke.getKeyStroke("$a $b $c pressed ENTER"),
            actionListener { event ->
                event ?: return@actionListener

                handleSelect(
                    true,
                    KeyEvent(
                        component,
                        event.id, event.getWhen(), event.modifiers,
                        KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN
                    )
                )
            }
        )

        @Suppress("MagicNumber") // Not worth a constant
        for (key in 0..9) {
            registerAction(
                "${a}${b}${c}invokeAction$key",
                KeyStroke.getKeyStroke("$a $b $c pressed $key"),
                actionListener { event ->
                    event ?: return@actionListener

                    val targetRow = if (key == 0) 9 else key - 1
                    list.addSelectionInterval(targetRow, targetRow)
                    handleSelect(
                        true,
                        KeyEvent(
                            component,
                            event.id, event.getWhen(), event.modifiers,
                            KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN
                        )
                    )
                }
            )
        }
    }
}

/**
 * Returns an `AbstractAction` that uses [actionPerformed] as the implementation of its `actionPerformed` method.
 *
 * @param actionPerformed the code to execute in `actionPerformed`
 * @return an `AbstractAction` that uses [actionPerformed] as the implementation of its `actionPerformed` method
 */
private fun actionListener(actionPerformed: (ActionEvent?) -> Unit) =
    object : AbstractAction() {
        override fun actionPerformed(event: ActionEvent?) = actionPerformed(event)
    }

/**
 * Returns the cartesian product of two lists.
 *
 * By requiring both lists to actually be lists of lists, this method can be chained.
 *
 * Consider the following examples, using a simplified notation for lists for readability:
 * ```
 * $ [[1, 2]] * [[3, 4]]
 * [[1, 3], [1, 4], [2, 3], [2, 4]]
 *
 * $ [[1, 2]] * [[3, 4]] * [[5, 6]]
 * [[1, 3, 5], [1, 3, 6], [1, 4, 5], [1, 4, 6], [2, 3, 5], [2, 3, 6], [2, 4, 5], [2, 4, 6]]
 * ```
 *
 * @param E the type of inner element
 * @param other the list to multiply with
 * @return the cartesian product of `this` and [other]
 */
@Suppress("UnusedPrivateMember") // False positive: Used as operator `*`
private operator fun <E> List<List<E>>.times(other: List<List<E>>) =
    this.flatMap { t1 -> other.map { t2 -> t1 + t2 } }
