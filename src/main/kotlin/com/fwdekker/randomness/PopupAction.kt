package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArraySettingsAction
import com.fwdekker.randomness.decimal.DecimalGroupAction
import com.fwdekker.randomness.integer.IntegerGroupAction
import com.fwdekker.randomness.string.StringGroupAction
import com.fwdekker.randomness.ui.ModifierKey
import com.fwdekker.randomness.ui.disableSpeedSearch
import com.fwdekker.randomness.ui.registerModifierActions
import com.fwdekker.randomness.uuid.UuidGroupAction
import com.fwdekker.randomness.word.WordGroupAction
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.popup.list.ListPopupImpl
import icons.RandomnessIcons


/**
 * Shows a popup for all available Randomness actions.
 */
class PopupAction : AnAction() {
    companion object {
        private const val TITLE = "Insert Data"
        private const val ALT_TITLE = "Insert Repeated Data"
        private const val CTRL_TITLE = "Change Settings"
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
        popup.registerModifierActions(ModifierKey.ALT, TITLE, ALT_TITLE)
        popup.registerModifierActions(ModifierKey.CTRL, TITLE, CTRL_TITLE)
        popup.registerModifierActions(ModifierKey.SHIFT, TITLE, SHIFT_TITLE)

        popup.setAdText(AD_TEXT)
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
