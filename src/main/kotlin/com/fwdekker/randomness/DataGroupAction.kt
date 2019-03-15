package com.fwdekker.randomness

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import java.awt.event.InputEvent


/**
 * A group of actions for a particular type of random data that can be generated.
 */
abstract class DataGroupAction : ActionGroup() {
    abstract val insertAction: DataInsertAction
    abstract val insertArrayAction: DataInsertAction.ArrayAction
    abstract val settingsAction: SettingsAction


    override fun getChildren(event: AnActionEvent?) = arrayOf(insertAction, insertArrayAction, settingsAction)

    override fun canBePerformed(context: DataContext?) = true

    override fun actionPerformed(event: AnActionEvent) {
        super.actionPerformed(event)

        val shiftPressed = event.modifiers and (InputEvent.SHIFT_MASK or InputEvent.SHIFT_DOWN_MASK) != 0
        val ctrlPressed = event.modifiers and (InputEvent.CTRL_MASK or InputEvent.CTRL_DOWN_MASK) != 0

        when {
            shiftPressed -> insertArrayAction.actionPerformed(event)
            ctrlPressed -> settingsAction.actionPerformed(event)
            else -> insertAction.actionPerformed(event)
        }
    }

    override fun update(event: AnActionEvent?) {
        super.update(event)

        if (event != null)
            event.presentation.text = insertAction.name
    }

    override fun isPopup() = true
}
