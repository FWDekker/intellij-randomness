package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArraySettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction


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
            caretModel.allCarets.forEach { caret ->
                val start = caret.selectionStart
                val end = caret.selectionEnd

                val string = generateString()
                val newEnd = start + string.length

                document.replaceString(start, end, string)
                caret.setSelection(start, newEnd)
            }
        }
    }

    /**
     * Generates a random string.
     *
     * @return a random string
     */
    abstract fun generateString(): String


    /**
     * Inserts a randomly generated array of strings at the positions of the event's editor's carets.
     *
     * @param dataInsertAction the action to generate data with
     */
    abstract inner class ArrayAction(private val dataInsertAction: DataInsertAction) : DataInsertAction() {
        private val arraySettings: ArraySettings = ArraySettings.default


        /**
         * Generates a random array of strings.
         *
         * @return a random array of strings
         */
        override fun generateString() =
            arraySettings.arrayify(List(arraySettings.count) { dataInsertAction.generateString() })
    }
}
