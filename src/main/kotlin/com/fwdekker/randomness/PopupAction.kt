package com.fwdekker.randomness

import com.fwdekker.randomness.template.TemplateGroupAction
import com.fwdekker.randomness.template.TemplateSettings
import com.fwdekker.randomness.template.TemplateSettingsAction
import com.fwdekker.randomness.ui.registerModifierActions
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.popup.list.ListPopupImpl
import java.awt.event.ActionEvent


/**
 * Shows a popup for all available Randomness actions.
 */
class PopupAction : AnAction() {
    /**
     * `true` if and only if the user focused the editor when opening this popup.
     */
    private var hasEditor: Boolean = true


    /**
     * Sets the icon of this action.
     *
     * @param event carries contextual information
     */
    override fun update(event: AnActionEvent) {
        event.presentation.icon = RandomnessIcons.RANDOMNESS
        hasEditor = event.getData(CommonDataKeys.EDITOR) != null
    }

    /**
     * Displays a popup with all actions provided by Randomness.
     *
     * @param event carries contextual information
     */
    override fun actionPerformed(event: AnActionEvent) {
        val popupGroup = if (hasEditor) PopupGroup() else SettingsOnlyPopupGroup()
        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                Bundle("popup.title"), popupGroup, event.dataContext,
                JBPopupFactory.ActionSelectionAid.NUMBERING,
                true
            )
            as ListPopupImpl

        popup.speedSearch.setEnabled(false)
        if (hasEditor) {
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
     *
     * @param event the event on which the title should be based
     * @return the desired title for the popup given [event]
     */
    @Suppress("ComplexMethod") // Cannot be simplified
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
            TemplateSettings.default.state.templates.map { TemplateGroupAction(it) }.toTypedArray<AnAction>() +
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
            TemplateSettings.default.state.templates.map { TemplateSettingsAction(it) }.toTypedArray<AnAction>() +
                Separator() +
                TemplateSettingsAction()
    }
}
