package com.fwdekker.randomness

import com.fwdekker.randomness.template.TemplateGroupAction
import com.fwdekker.randomness.template.TemplateSettingsAction
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.popup.list.ListPopupImpl
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.util.Locale
import javax.swing.AbstractAction
import javax.swing.KeyStroke


/**
 * Shows a popup for all available Randomness actions.
 */
class PopupAction : AnAction(Icons.RANDOMNESS) {
    /**
     * `true` if and only if the user focused a non-viewer editor when opening this popup.
     */
    private var isEditable: Boolean = true


    /**
     * Specifies the thread in which [update] is invoked.
     */
    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    /**
     * Sets the icon of this action.
     *
     * @param event carries contextual information
     */
    override fun update(event: AnActionEvent) {
        event.presentation.icon = Icons.RANDOMNESS

        // Running this in `actionPerformed` always sets it to `true`
        isEditable = event.getData(CommonDataKeys.EDITOR)?.isViewer == false
    }

    /**
     * Displays a popup with all actions provided by Randomness.
     *
     * @param event carries contextual information
     */
    override fun actionPerformed(event: AnActionEvent) {
        val popupGroup = if (isEditable) PopupGroup() else SettingsOnlyPopupGroup()
        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                Bundle("popup.title"), popupGroup, event.dataContext,
                JBPopupFactory.ActionSelectionAid.NUMBERING,
                true
            )
            as ListPopupImpl

        if (isEditable) {
            popup.setCaption(Bundle("popup.title"))
            popup.setAdText(Bundle("popup.ad"))
            popup.registerModifierActions { this.captionModifier(it) }
        } else {
            popup.setCaption(Bundle("popup.title.ctrl"))
            popup.setAdText(Bundle("popup.ad.settings_only"))
            popup.registerModifierActions { Bundle("popup.title.ctrl") }
        }

        popup.showInBestPositionFor(event.dataContext)
    }


    /**
     * Returns the desired title for the popup given [event].
     */
    @Suppress("detekt:ComplexMethod") // Cannot be simplified
    private fun captionModifier(event: ActionEvent?): String {
        val modifiers = event?.modifiers ?: 0
        val altPressed = modifiers and ActionEvent.ALT_MASK != 0
        val ctrlPressed = modifiers and ActionEvent.CTRL_MASK != 0
        val shiftPressed = modifiers and ActionEvent.SHIFT_MASK != 0

        return when {
            altPressed && ctrlPressed && shiftPressed -> Bundle("popup.title.alt_ctrl_shift")
            altPressed && ctrlPressed -> Bundle("popup.title.alt_ctrl")
            altPressed && shiftPressed -> Bundle("popup.title.alt_shift")
            ctrlPressed && shiftPressed -> Bundle("popup.title.ctrl_shift")
            altPressed -> Bundle("popup.title.alt")
            ctrlPressed -> Bundle("popup.title.ctrl")
            shiftPressed -> Bundle("popup.title.shift")
            else -> Bundle("popup.title")
        }
    }


    /**
     * The [ActionGroup] containing all Randomness actions.
     */
    private class PopupGroup : ActionGroup() {
        /**
         * Returns all group actions.
         *
         * @param event carries contextual information
         */
        override fun getChildren(event: AnActionEvent?) =
            PersistentSettings.default.state.templates.map { TemplateGroupAction(it) }.toTypedArray<AnAction>() +
                Separator() +
                TemplateSettingsAction()
    }

    /**
     * The [ActionGroup] containing only settings-related actions.
     */
    private class SettingsOnlyPopupGroup : ActionGroup() {
        /**
         * Returns all settings actions.
         *
         * @param event carries contextual information
         */
        override fun getChildren(event: AnActionEvent?) =
            PersistentSettings.default.state.templates.map { TemplateSettingsAction(it) }.toTypedArray<AnAction>() +
                Separator() +
                TemplateSettingsAction()
    }
}


/**
 * An `AbstractAction` that uses [myActionPerformed] as the implementation of its [actionPerformed] method.
 *
 * @property myActionPerformed The code to execute in [actionPerformed].
 */
private class SimpleAbstractAction(private val myActionPerformed: (ActionEvent?) -> Unit) : AbstractAction() {
    /**
     * @see myActionPerformed
     */
    override fun actionPerformed(event: ActionEvent?) = myActionPerformed(event)
}

/**
 * Returns the cartesian product of [this] and [other].
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
 */
private operator fun <E> List<List<E>>.times(other: List<List<E>>) =
    this.flatMap { t1 -> other.map { t2 -> t1 + t2 } }

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
            SimpleAbstractAction { setCaption(captionModifier(it)) }
        )
        registerAction(
            "${a}${b}${c}Pressed",
            KeyStroke.getKeyStroke("$a $b $c pressed ${a.uppercase(Locale.getDefault())}"),
            SimpleAbstractAction { setCaption(captionModifier(it)) }
        )
        registerAction(
            "${a}${b}${c}invokeAction",
            KeyStroke.getKeyStroke("$a $b $c pressed ENTER"),
            SimpleAbstractAction { event ->
                event ?: return@SimpleAbstractAction

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

        @Suppress("detekt:MagicNumber") // Not worth a constant
        for (key in 0..9) {
            registerAction(
                "${a}${b}${c}invokeAction$key",
                KeyStroke.getKeyStroke("$a $b $c pressed $key"),
                SimpleAbstractAction { event ->
                    event ?: return@SimpleAbstractAction

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
