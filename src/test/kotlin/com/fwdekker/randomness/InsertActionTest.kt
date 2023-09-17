package com.fwdekker.randomness

import com.fwdekker.randomness.testhelpers.DummyInsertAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.kotest.matchers.shouldBe


/**
 * Integration tests for [InsertAction].
 */
@Suppress("detekt:FunctionMaxLength", "detekt:FunctionName", "detekt:TooManyFunctions")
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
        insertAction = DummyInsertAction { "${insertValue++}" }
    }

    @Suppress("detekt:SwallowedException") // Intentional
    override fun tearDown() {
        try {
            super.tearDown()
        } catch (error: Error) {
            // Swallow errors about undisposed timers
        }
    }

    override fun getTestDataPath() = javaClass.classLoader.getResource("integration-project/")?.path


    fun `test that it inserts text into an empty document`() {
        myFixture.testAction(insertAction)

        document.text shouldBe "0"
    }

    fun `test that it inserts text in front of existing text`() {
        runWriteCommandAction(myFixture.project) { document.setText("contents") }

        setCaret(0)
        myFixture.testAction(insertAction)

        document.text shouldBe "0contents"
    }

    fun `test that it inserts text behind existing text`() {
        runWriteCommandAction(myFixture.project) { document.setText("contents") }

        setCaret(8)
        myFixture.testAction(insertAction)

        document.text shouldBe "contents0"
    }

    fun `test that it inserts text in the middle of existing text`() {
        runWriteCommandAction(myFixture.project) { document.setText("contents") }

        setCaret(3)
        myFixture.testAction(insertAction)

        document.text shouldBe "con0tents"
    }

    fun `test that it replaces the entire document if selected`() {
        runWriteCommandAction(myFixture.project) { document.setText("contents") }

        setSelection(0, 8)
        myFixture.testAction(insertAction)

        document.text shouldBe "0"
    }

    fun `test that it replaces a partial selection of text`() {
        runWriteCommandAction(myFixture.project) { document.setText("contents") }

        setSelection(2, 4)
        myFixture.testAction(insertAction)

        document.text shouldBe "co0ents"
    }

    fun `test that it inserts text at multiple carets`() {
        runWriteCommandAction(myFixture.project) { document.setText("line1\nline2\nline3") }

        setCaret(2)
        addCaret(7)
        addCaret(16)
        myFixture.testAction(insertAction)

        document.text shouldBe "li0ne1\nl1ine2\nline23"
    }

    fun `test that it replaces text at multiple carets`() {
        runWriteCommandAction(myFixture.project) { document.setText("line1\nline2\nline3") }

        setSelection(2, 7)
        addSelection(10, 15)
        myFixture.testAction(insertAction)

        document.text shouldBe "li0ine1e3"
    }

    fun `test that it simultaneously inserts at carets and replaces at selections`() {
        runWriteCommandAction(myFixture.project) { document.setText("line1\nline2\nline3") }

        setCaret(2)
        addSelection(4, 6)
        addCaret(8)
        addSelection(12, 15)
        myFixture.testAction(insertAction)

        document.text shouldBe "li0ne1li2ne2\n3e3"
    }

    fun `test that it inserts the same value at multiple carets`() {
        runWriteCommandAction(myFixture.project) { document.setText("line1\nline2\nline3") }

        setCaret(5)
        addCaret(10)
        addCaret(12)

        var insertValue = 0
        myFixture.testAction(DummyInsertAction(repeat = true) { "${insertValue++}" })

        document.text shouldBe "line10\nline02\n0line3"
    }

    fun `test that it inserts nothing if the scheme is invalid`() {
        myFixture.testAction(DummyInsertAction { throw DataGenerationException("Invalid input!") })

        document.text shouldBe ""
    }

    fun `test that it inserts nothing if the scheme is invalid with an empty message`() {
        myFixture.testAction(DummyInsertAction { throw DataGenerationException() })

        document.text shouldBe ""
    }


    fun `test that it inserts nothing if the project is null`() {
        val event = AnActionEvent.createFromDataContext("", null) {
            if (it == CommonDataKeys.PROJECT.name) myFixture.project
            else null
        }

        insertAction.actionPerformed(event)

        document.text shouldBe ""
    }

    fun `test that it inserts nothing if the editor is null`() {
        val event = AnActionEvent.createFromDataContext("", null) {
            if (it == CommonDataKeys.EDITOR.name) myFixture.editor
            else null
        }

        insertAction.actionPerformed(event)

        document.text shouldBe ""
    }

    fun `test that it disables the presentation if the editor is null`() {
        val presentation = Presentation()
        val event = AnActionEvent.createFromDataContext("", presentation) { null }

        insertAction.update(event)

        presentation.isEnabled shouldBe false
    }

    fun `test that it enables the presentation if the editor is not null`() {
        val presentation = Presentation()
        val event = AnActionEvent.createFromDataContext("", presentation) { myFixture.editor }

        insertAction.update(event)

        presentation.isEnabled shouldBe true
    }


    /**
     * Moves the primary caret to [offset] and returns the primary caret.
     */
    private fun setCaret(offset: Int) =
        caretModel.primaryCaret.also { it.moveToOffset(offset) }

    /**
     * Adds a caret at [offset] and returns the caret.
     */
    private fun addCaret(offset: Int) =
        caretModel.addCaret(myFixture.editor.offsetToVisualPosition(offset))!!

    /**
     * Selects the interval from [startOffset] (inclusive) to [endOffset] (exclusive) with the primary caret.
     */
    private fun setSelection(startOffset: Int, endOffset: Int) =
        caretModel.primaryCaret.setSelection(startOffset, endOffset)

    /**
     * Adds a caret that selects from [startOffset] (inclusive) to [endOffset] (exclusive).
     */
    private fun addSelection(startOffset: Int, endOffset: Int) =
        addCaret(startOffset).setSelection(startOffset, endOffset)
}
