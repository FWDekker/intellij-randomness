package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArraySettingsAction
import com.fwdekker.randomness.decimal.DecimalGroupAction
import com.fwdekker.randomness.integer.IntegerGroupAction
import com.fwdekker.randomness.string.StringGroupAction
import com.fwdekker.randomness.ui.disableSpeedSearch
import com.fwdekker.randomness.ui.registerModifierActions
import com.fwdekker.randomness.uuid.UuidGroupAction
import com.fwdekker.randomness.word.WordGroupAction
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.popup.list.ListPopupImpl
import icons.RandomnessIcons
import java.awt.event.InputEvent


/**
 * Shows a popup for all available Randomness actions.
 */
class PopupAction : AnAction() {
    companion object {
        private const val TITLE = "Insert Data"
        private const val ALT_TITLE = "Insert Repeated Data"
        private const val ALT_SHIFT_TITLE = "Insert Repeated Array"
        private const val CTRL_TITLE = "Change Settings"
        private const val CTRL_SHIFT_TITLE = "Change Array Settings"
        private const val SHIFT_TITLE = "Insert Array"
        private const val AD_TEXT = "Shift = Array. Ctrl = Settings. Alt = Repeat."
    }


    /**
     * Sets the icon of this action.
     *
     * @param event carries information on the invocation place
     */
    override fun update(event: AnActionEvent) {
        event.presentation.icon = RandomnessIcons.Data.Base
        event.presentation.isEnabled = event.getData(CommonDataKeys.EDITOR) != null
    }

    /**
     * Displays a popup with all actions provided by Randomness.
     *
     * @param event carries information on the invocation place
     */
    override fun actionPerformed(event: AnActionEvent) {
        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                TITLE, PopupGroup(), event.dataContext,
                JBPopupFactory.ActionSelectionAid.NUMBERING, true
            )
            as ListPopupImpl

        popup.disableSpeedSearch()
        popup.setCaption(TITLE)
        popup.setAdText(AD_TEXT)
        popup.registerModifierActions { captionEvent ->
            val modifiers = captionEvent?.modifiers ?: 0
            val altPressed = modifiers and (InputEvent.ALT_MASK or InputEvent.ALT_DOWN_MASK) != 0
            val ctrlPressed = modifiers and (InputEvent.CTRL_MASK or InputEvent.CTRL_DOWN_MASK) != 0
            val shiftPressed = modifiers and (InputEvent.SHIFT_MASK or InputEvent.SHIFT_DOWN_MASK) != 0

            when {
                ctrlPressed && shiftPressed -> CTRL_SHIFT_TITLE
                ctrlPressed -> CTRL_TITLE
                altPressed && shiftPressed -> ALT_SHIFT_TITLE
                altPressed -> ALT_TITLE
                shiftPressed -> SHIFT_TITLE
                else -> TITLE
            }
        }

        popup.showInBestPositionFor(event.dataContext)
    }


    /**
     * The `ActionGroup` containing the popup's actions.
     */
    private class PopupGroup : ActionGroup() {
        /**
         * Returns all group actions.
         *
         * @param event carries information on the invocation place
         */
        override fun getChildren(event: AnActionEvent?) =
            arrayOf(
                IntegerGroupAction(),
                DecimalGroupAction(),
                StringGroupAction(),
                WordGroupAction(),
                UuidGroupAction(),
                Separator(),
                ArraySettingsAction()
            )
    }
}
