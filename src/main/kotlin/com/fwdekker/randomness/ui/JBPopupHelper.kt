package com.fwdekker.randomness.ui

import com.intellij.ui.popup.list.ListPopupImpl
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.AbstractAction
import javax.swing.KeyStroke


/**
 * Disables speed search.
 */
fun ListPopupImpl.disableSpeedSearch() {
    speedSearch.setEnabled(false)
    speedSearch.addChangeListener { speedSearch.updatePattern("") }
}

/**
 * Registers actions such that actions can be selected while holding `Ctrl` or `Cmd`.
 *
 * @param modifierKey the modifier key for which actions should be registered
 * @param normalTitle the title of the popup while the modifier key is not pressed
 * @param modifierTitle the title of the popup while the modifier key is pressed
 */
fun ListPopupImpl.registerModifierActions(
    modifierKey: ModifierKey,
    normalTitle: String,
    modifierTitle: String
) {
    registerAction(
        "${modifierKey.shortName.toLowerCase()}Released",
        KeyStroke.getKeyStroke("released ${modifierKey.longName.toUpperCase()}"),
        actionListener { setCaption(normalTitle) }
    )
    registerAction(
        "${modifierKey.shortName.toLowerCase()}Pressed",
        KeyStroke.getKeyStroke("${modifierKey.longName.toLowerCase()} pressed ${modifierKey.longName.toUpperCase()}"),
        actionListener { setCaption(modifierTitle) }
    )
    registerAction(
        "${modifierKey.shortName.toLowerCase()}InvokeAction",
        KeyStroke.getKeyStroke("${modifierKey.longName.toLowerCase()} ENTER"),
        actionListener { event ->
            event ?: return@actionListener

            handleSelect(true, KeyEvent(
                component, event.id, event.getWhen(),
                event.modifiers, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED,
                KeyEvent.KEY_LOCATION_UNKNOWN
            ))
        }
    )

    (1..9).forEach { key ->
        registerAction(
            "${modifierKey.shortName.toLowerCase()}Invoke$key",
            KeyStroke.getKeyStroke("${modifierKey.longName.toLowerCase()} $key"),
            actionListener { event ->
                event ?: return@actionListener

                list.addSelectionInterval(key - 1, key - 1)
                handleSelect(true, KeyEvent(
                    component, event.id, event.getWhen(),
                    event.modifiers, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED,
                    KeyEvent.KEY_LOCATION_UNKNOWN
                ))
            }
        )
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
        override fun actionPerformed(event: ActionEvent?) {
            actionPerformed(event)
        }
    }


/**
 * Pairs the short and long name of a modifier key together.
 *
 * @param shortName the short name of the modifier key
 * @param longName the long name of the modifier key
 */
enum class ModifierKey(val shortName: String, val longName: String) {
    /**
     * The ⎇ (Alt or Opt) key.
     */
    ALT("alt", "alt"),
    /**
     * The control (Ctrl or ⌘) key.
     */
    CTRL("ctrl", "control"),
    /**
     * The ⇧ (Shift) key).
     */
    SHIFT("shift", "shift")
}
