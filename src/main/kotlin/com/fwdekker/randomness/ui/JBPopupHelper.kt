package com.fwdekker.randomness.ui

import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.popup.list.ListPopupImpl
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.util.Locale
import javax.swing.AbstractAction
import javax.swing.KeyStroke


/**
 * A collection of helper methods for dealing with `JBPopup`s.
 */
object JBPopupHelper {
    private const val NINE = 9


    /**
     * Disables speed search.
     *
     * @param popup the popup to disable speed search for
     */
    fun disableSpeedSearch(popup: ListPopupImpl) {
        val speedSearch = popup.speedSearch
        speedSearch.setEnabled(false)
        speedSearch.addChangeListener { speedSearch.updatePattern("") }
    }

    /**
     * Registers actions such that actions can be selected while holding `Shift`.
     *
     * @param popup the popup to enable selecting with `Shift` for
     * @param normalTitle the title of the popup while the `Shift` key is not pressed
     * @param shiftTitle the title of the popup while the `Shift` key is pressed
     */
    fun registerShiftActions(popup: ListPopupImpl, normalTitle: String, shiftTitle: String) =
        registerModifierActions(popup, ModifierKey.SHIFT, normalTitle, shiftTitle)

    /**
     * Registers actions such that actions can be selected while holding `Ctrl` or `Cmd`.
     *
     * @param popup the popup to enable selecting with `Ctrl`/`Cmd` for
     * @param normalTitle the title of the popup while the `Ctrl`/`Cmd` key is not pressed
     * @param ctrlTitle the title of the popup while the `Ctrl`/`Cmd` key is pressed
     */
    fun registerCtrlActions(popup: ListPopupImpl, normalTitle: String, ctrlTitle: String) =
        registerModifierActions(popup, ModifierKey.CTRL, normalTitle, ctrlTitle)

    /**
     * Registers actions such that actions can be selected while holding `Ctrl` or `Cmd`.
     *
     * @param popup the popup to enable selecting with the modifier key for
     * @param modifierKey the modifier key for which actions should be registered
     * @param normalTitle the title of the popup while the modifier key is not pressed
     * @param modifierTitle the title of the popup while the modifier key is pressed
     */
    private fun registerModifierActions(
        popup: ListPopupImpl,
        modifierKey: ModifierKey,
        normalTitle: String,
        modifierTitle: String
    ) {
        val lcShortModifierName = modifierKey.shortName.toLowerCase(Locale.getDefault())
        val lcLongModifierName = modifierKey.longName.toLowerCase(Locale.getDefault())
        val ucLongModifierName = modifierKey.longName.toUpperCase(Locale.getDefault())

        popup.registerAction(
            lcShortModifierName + "Released",
            KeyStroke.getKeyStroke("released $ucLongModifierName"),
            object : AbstractAction() {
                override fun actionPerformed(event: ActionEvent) {
                    popup.setCaption(normalTitle)
                }
            }
        )
        popup.registerAction(
            lcShortModifierName + "Pressed",
            KeyStroke.getKeyStroke("$lcLongModifierName pressed $ucLongModifierName"),
            object : AbstractAction() {
                override fun actionPerformed(event: ActionEvent) {
                    popup.setCaption(modifierTitle)
                }
            }
        )
        popup.registerAction(
            lcShortModifierName + "InvokeAction",
            KeyStroke.getKeyStroke("$lcLongModifierName ENTER"),
            object : AbstractAction() {
                override fun actionPerformed(event: ActionEvent) {
                    val keyEvent = KeyEvent(
                        popup.component, event.id, event.getWhen(),
                        event.modifiers, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED,
                        KeyEvent.KEY_LOCATION_UNKNOWN
                    )
                    popup.handleSelect(true, keyEvent)
                }
            }
        )

        1.rangeTo(NINE).forEach { key ->
            popup
                .registerAction(
                    lcShortModifierName + "Invoke" + key,
                    KeyStroke.getKeyStroke("$lcLongModifierName $key"),
                    object : AbstractAction() {
                        override fun actionPerformed(event: ActionEvent) {
                            val keyEvent = KeyEvent(
                                popup.component, event.id,
                                event.getWhen(), event.modifiers,
                                KeyEvent.VK_ENTER,
                                KeyEvent.CHAR_UNDEFINED,
                                KeyEvent.KEY_LOCATION_UNKNOWN
                            )
                            popup.list.addSelectionInterval(key - 1, key - 1)
                            popup.handleSelect(true, keyEvent)
                        }
                    }
                )
        }
    }

    /**
     * Displays a popup with a title and two messages.
     *
     * @param title the title of the popup
     * @param messageA the first message
     * @param messageB the second message
     */
    fun showMessagePopup(title: String, messageA: String, messageB: String) =
        JBPopupFactory.getInstance().createConfirmation(
            title,
            messageA,
            messageB,
            { },
            1
        ).showInFocusCenter()


    /**
     * Pairs the short and long name of a modifier key together.
     *
     * @param shortName the short name of the modifier key
     * @param longName the long name of the modifier key
     */
    private enum class ModifierKey(val shortName: String, val longName: String) {
        ALT("alt", "alt"),
        CTRL("ctrl", "control"),
        SHIFT("shift", "shift")
    }
}
