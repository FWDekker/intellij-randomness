package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArraySettingsAction
import com.fwdekker.randomness.template.TemplateGroupAction
import com.fwdekker.randomness.template.TemplateSettingsAction
import com.fwdekker.randomness.ui.registerModifierActions
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.popup.list.ListPopupImpl
import icons.RandomnessIcons
import java.awt.event.ActionEvent


/**
 * Shows a popup for all available Randomness actions.
 */
class PopupAction : AnAction() {
    /**
     * Whether the user focused the editor when opening this popup.
     */
    private var hasEditor: Boolean = true


    /**
     * Sets the icon of this action.
     *
     * @param event carries information on the invocation place
     */
    override fun update(event: AnActionEvent) {
        event.presentation.icon = RandomnessIcons.Data.Base
        hasEditor = event.getData(CommonDataKeys.EDITOR) != null
    }

    /**
     * Displays a popup with all actions provided by Randomness.
     *
     * @param event carries information on the invocation place
     */
    override fun actionPerformed(event: AnActionEvent) {
        val popupGroup = if (hasEditor) PopupGroup() else SettingsOnlyPopupGroup()
        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                TITLE, popupGroup, event.dataContext,
                JBPopupFactory.ActionSelectionAid.NUMBERING,
                true
            )
            as ListPopupImpl

        popup.speedSearch.setEnabled(false)
        if (hasEditor) {
            popup.setCaption(TITLE)
            popup.setAdText(AD_TEXT)
            popup.registerModifierActions { this.captionModifier(it) }
        } else {
            popup.setCaption(CTRL_TITLE)
            popup.setAdText("Editor is not selected. Displaying settings only.")
            popup.registerModifierActions { CTRL_TITLE }
        }

        popup.showInBestPositionFor(event.dataContext)
    }


    /**
     * Returns the desired title for the popup given an event.
     *
     * @param event the event on which the title should be based
     * @return the desired title for the popup given an event
     */
    @Suppress("ComplexMethod") // Cannot be simplified
    private fun captionModifier(event: ActionEvent?): String {
        val modifiers = event?.modifiers ?: 0
        val altPressed = modifiers and ActionEvent.ALT_MASK != 0
        val ctrlPressed = modifiers and ActionEvent.CTRL_MASK != 0
        val shiftPressed = modifiers and ActionEvent.SHIFT_MASK != 0

        return when {
            altPressed && ctrlPressed && shiftPressed -> ALT_CTRL_SHIFT_TITLE
            altPressed && ctrlPressed -> ALT_CTRL_TITLE
            altPressed && shiftPressed -> ALT_SHIFT_TITLE
            ctrlPressed && shiftPressed -> CTRL_SHIFT_TITLE
            altPressed -> ALT_TITLE
            ctrlPressed -> CTRL_TITLE
            shiftPressed -> SHIFT_TITLE
            else -> TITLE
        }
    }


    /**
     * The `ActionGroup` containing all Randomness actions.
     */
    private class PopupGroup : ActionGroup() {
        /**
         * Returns all group actions.
         *
         * @param event carries information on the invocation place
         */
        override fun getChildren(event: AnActionEvent?) =
            arrayOf(
                TemplateGroupAction(),
                Separator(),
                ArraySettingsAction()
            )
    }

    /**
     * The `ActionGroup` containing only settings-related actions.
     */
    private class SettingsOnlyPopupGroup : ActionGroup() {
        /**
         * Returns all settings actions.
         *
         * @param event carries information on the invocation place
         */
        override fun getChildren(event: AnActionEvent?) =
            arrayOf(
                TemplateSettingsAction(),
                Separator(),
                ArraySettingsAction()
            )
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default popup title.
         */
        const val TITLE = "Insert Data"

        /**
         * The popup title while the alt key is held down.
         */
        const val ALT_TITLE = "Insert Repeated Data"

        /**
         * The popup title when the alt and control keys are held down.
         */
        const val ALT_CTRL_TITLE = "Quick Switch Scheme"

        /**
         * The popup title when the alt, control, and shift keys are held down.
         */
        const val ALT_CTRL_SHIFT_TITLE = "Quick Switch Array Scheme"

        /**
         * The popup title when the alt and shift keys are held down.
         */
        const val ALT_SHIFT_TITLE = "Insert Repeated Array"

        /**
         * The popup title when the control key is held down.
         */
        const val CTRL_TITLE = "Change Settings"

        /**
         * The popup title when the control and shift keys are held down.
         */
        const val CTRL_SHIFT_TITLE = "Change Array Settings"

        /**
         * The popup title when the shift key is held down.
         */
        const val SHIFT_TITLE = "Insert Array"

        /**
         * The text shown at the bottom of the popup.
         */
        const val AD_TEXT = "Shift = Array. Ctrl = Settings. Alt = Repeat."
    }
}
