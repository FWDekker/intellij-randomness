package com.fwdekker.randomness

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.assertThat


/**
 * Integration tests for [InsertAction].
 */
class InsertActionTest : BasePlatformTestCase() {
    private lateinit var document: Document
    private lateinit var caretModel: CaretModel

    private lateinit var insertAction: DummyInsertAction


    override fun setUp() {
        super.setUp()

        val file = myFixture.copyFileToProject("emptyFile.txt")
        myFixture.openFileInEditor(file)

        document = myFixture.editor.document
        caretModel = myFixture.editor.caretModel

        var insertValue = 0
        insertAction = DummyInsertAction { insertValue++.toString() }
    }

    override fun tearDown() {
        try {
            super.tearDown()
        } catch (e: Error) {
            // Swallow errors about undisposed timers
        }
    }

    override fun getTestDataPath() = javaClass.classLoader.getResource("integration-project/")?.path


    fun `test that it inserts text into an empty document`() {
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("0")
    }

    fun `test that it inserts text in front of existing text`() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("RkpjkS9Itb") }

        caretModel.moveToOffset(0)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("0RkpjkS9Itb")
    }

    fun `test that it inserts text behind existing text`() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("0aiMbK5hK5") }

        caretModel.moveToOffset(10)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("0aiMbK5hK50")
    }

    fun `test that it inserts text in the middle of existing text`() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("U6jBDMh8Nq") }

        caretModel.moveToOffset(5)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("U6jBD0Mh8Nq")
    }

    fun `test that it replaces the entire document if selected`() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("fMhAajjDw6") }

        setSelection(0, 10)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("0")
    }

    fun `test that it replaces an incomplete selection of text`() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("qZPGZDEcPS") }

        setSelection(3, 7)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("qZP0cPS")
    }

    fun `test that it inserts text at multiple carets`() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) {
            document.setText("DCtD41lFOk\nOCnrdYk9gE\nn1HAPKotDq")
        }

        addCaret(11)
        addCaret(22)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("0DCtD41lFOk\n1OCnrdYk9gE\n2n1HAPKotDq")
    }

    fun `test that it replaces text at multiple carets`() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) {
            document.setText("YXSncq4FC9\nG31Ybbn1c4\nTNCqAhqPnh")
        }

        setSelection(2, 4)
        addSelection(18, 23)
        addSelection(29, 29)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("YX0cq4FC9\nG31Ybbn1NCqAhq2Pnh")
    }

    fun `test that it simultaneously inserts at carets and replaces at selections`() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) {
            document.setText("XOppzVdZTj\nZhAaVfQynW\nk3kWemkdAg")
        }

        caretModel.moveToOffset(5)
        addSelection(6, 9)
        addCaret(15)
        addSelection(24, 28)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("XOppz0V1j\nZhAa2VfQynW\nk33kdAg")
    }

    fun `test that it inserts the same value at multiple carets`() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("evening\nplease\nfew") }

        caretModel.moveToOffset(5)
        addCaret(10)
        addCaret(12)

        var insertValue = 0
        myFixture.testAction(DummyInsertAction(repeat = true) { insertValue++.toString() })

        assertThat(document.text).isEqualTo("eveni0ng\npl0ea0se\nfew")
    }

    fun `test that it inserts nothing if the scheme is invalid`() {
        myFixture.testAction(DummyInsertAction { throw DataGenerationException("Invalid input!") })

        assertThat(document.text).isEqualTo("")
    }

    fun `test that it inserts nothing if the scheme is invalid with an empty message`() {
        myFixture.testAction(DummyInsertAction { throw DataGenerationException() })

        assertThat(document.text).isEqualTo("")
    }


    fun `test that it inserts nothing if the project is null`() {
        val event = AnActionEvent.createFromDataContext("", null) {
            if (it == CommonDataKeys.PROJECT.name) myFixture.project
            else null
        }

        insertAction.actionPerformed(event)

        assertThat(document.text).isEqualTo("")
    }

    fun `test that it inserts nothing if the editor is null`() {
        val event = AnActionEvent.createFromDataContext("", null) {
            if (it == CommonDataKeys.EDITOR.name) myFixture.editor
            else null
        }

        insertAction.actionPerformed(event)

        assertThat(document.text).isEqualTo("")
    }

    fun `test that it disables the presentation if the editor is null`() {
        val presentation = Presentation()
        val event = AnActionEvent.createFromDataContext("", presentation) { null }

        insertAction.update(event)

        assertThat(presentation.isEnabled).isFalse()
    }

    fun `test that it enables the presentation if the editor is not null`() {
        val presentation = Presentation()
        val event = AnActionEvent.createFromDataContext("", presentation) { myFixture.editor }

        insertAction.update(event)

        assertThat(presentation.isEnabled).isTrue()
    }


    /**
     * Causes the first caret to select the given interval.
     *
     * @param fromOffset the start of the selected interval
     * @param toOffset the end of the selected interval
     */
    private fun setSelection(fromOffset: Int, toOffset: Int) =
        caretModel.allCarets[0].setSelection(fromOffset, toOffset)

    /**
     * Adds a caret at [offset].
     *
     * @param offset the offset to add a caret at
     */
    private fun addCaret(offset: Int) {
        caretModel.addCaret(myFixture.editor.offsetToVisualPosition(offset))
    }

    /**
     * Adds a caret that selects the given interval.
     *
     * @param fromOffset the start of the selected interval
     * @param toOffset the end of the selected interval
     */
    private fun addSelection(fromOffset: Int, toOffset: Int) {
        caretModel.addCaret(myFixture.editor.offsetToVisualPosition(fromOffset))
        caretModel.allCarets[caretModel.caretCount - 1].setSelection(fromOffset, toOffset)
    }
}
