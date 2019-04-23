package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArraySettings
import com.fwdekker.randomness.ui.JBPopupHelper
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import java.awt.event.InputEvent


/**
 * A group of actions for a particular type of random data that can be generated.
 */
abstract class DataGroupAction : ActionGroup() {
    abstract val insertAction: DataInsertAction
    abstract val insertArrayAction: DataInsertArrayAction
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


/**
 * Inserts a randomly generated string at the positions of the event's editor's carets.
 */
abstract class DataInsertAction : AnAction() {
    /**
     * The name of the action to display.
     */
    abstract val name: String


    /**
     * Disables this action if no editor is currently opened.
     *
     * @param event the performed action
     */
    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        val editor = event.getData(CommonDataKeys.EDITOR)

        presentation.text = name
        presentation.isEnabled = editor != null
    }

    /**
     * Inserts the string generated by [.generateString] at the caret(s) in the editor.
     *
     * @param event the performed action
     */
    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR)
            ?: return
        val project = event.getData(CommonDataKeys.PROJECT)
            ?: return
        val document = editor.document
        val caretModel = editor.caretModel

        WriteCommandAction.runWriteCommandAction(project) {
            try {
                caretModel.allCarets.forEach { caret ->
                    val start = caret.selectionStart
                    val end = caret.selectionEnd

                    val string = generateString()
                    val newEnd = start + string.length

                    document.replaceString(start, end, string)
                    caret.setSelection(start, newEnd)
                }
            } catch (e: StringGenerationException) {
                JBPopupHelper.showMessagePopup(
                    "Randomness error",
                    e.message ?: "An unknown error occurred while generating a random string.",
                    "Please check your Randomness settings and try again."
                )
            }
        }
    }

    /**
     * Generates a random string.
     *
     * @return a random string
     */
    @Throws(StringGenerationException::class)
    abstract fun generateString(): String


    class StringGenerationException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
}


/**
 * Inserts a randomly generated array of strings at the positions of the event's editor's carets.
 *
 * @param dataInsertAction the action to generate data with
 */
abstract class DataInsertArrayAction(private val dataInsertAction: DataInsertAction) : DataInsertAction() {
    private val arraySettings: ArraySettings = ArraySettings.default


    /**
     * Generates a random array of strings.
     *
     * @return a random array of strings
     */
    @Throws(StringGenerationException::class)
    override fun generateString() =
        arraySettings.arrayify(List(arraySettings.count) { dataInsertAction.generateString() })
}


/**
 * Shows a modal dialog for changing settings.
 */
abstract class SettingsAction : AnAction() {
    /**
     * The title of the dialog to display.
     */
    protected abstract val title: String


    override fun update(event: AnActionEvent?) {
        super.update(event)

        if (event != null)
            event.presentation.text = title
    }

    /**
     * Creates and displays a modal dialog for changing settings.
     *
     * @param event the event that triggered the creation of this dialog
     */
    override fun actionPerformed(event: AnActionEvent) {
        createDialog()
            .also {
                it.title = title
                it.show()
            }
    }


    /**
     * Returns the dialog to display.
     *
     * @return the dialog to display
     */
    protected abstract fun createDialog(): SettingsDialog<*>
}