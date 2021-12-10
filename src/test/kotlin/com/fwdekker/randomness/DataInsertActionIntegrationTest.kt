package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArrayScheme
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.assertThat


/**
 * Integration tests for [DataInsertAction].
 *
 * @see DataInsertAction
 */
class DataInsertActionIntegrationTest : BasePlatformTestCase() {
    private lateinit var insertAction: DummyInsertAction
    private lateinit var document: Document
    private lateinit var caretModel: CaretModel


    override fun setUp() {
        super.setUp()

        val file = myFixture.copyFileToProject("emptyFile.txt")
        myFixture.openFileInEditor(file)

        document = myFixture.editor.document
        caretModel = myFixture.editor.caretModel

        var insertValue = 0
        insertAction = DummyInsertAction { insertValue++.toString() }
    }

    override fun getTestDataPath() = javaClass.classLoader.getResource("integration-project/")?.path


    fun testInsertIntoEmpty() {
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("0")
    }

    fun testInsertBefore() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("RkpjkS9Itb") }

        caretModel.moveToOffset(0)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("${"0"}RkpjkS9Itb")
    }

    fun testInsertAfter() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("0aiMbK5hK5") }

        caretModel.moveToOffset(10)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("0aiMbK5hK5${"0"}")
    }

    fun testInsertBetween() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("U6jBDMh8Nq") }

        caretModel.moveToOffset(5)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("U6jBD${"0"}Mh8Nq")
    }

    fun testReplaceAll() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("fMhAajjDw6") }

        setSelection(0, 10)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("0")
    }

    fun testReplacePart() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("qZPGZDEcPS") }

        setSelection(3, 7)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("qZP${"0"}cPS")
    }

    fun testInsertMultiple() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) {
            document.setText("DCtD41lFOk\nOCnrdYk9gE\nn1HAPKotDq")
        }

        addCaret(11)
        addCaret(22)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("${"0"}DCtD41lFOk\n${"1"}OCnrdYk9gE\n${"2"}n1HAPKotDq")
    }

    fun testReplaceMultiple() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) {
            document.setText("YXSncq4FC9\nG31Ybbn1c4\nTNCqAhqPnh")
        }

        setSelection(2, 4)
        addSelection(18, 23)
        addSelection(29, 29)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("YX${"0"}cq4FC9\nG31Ybbn${"1"}NCqAhq${"2"}Pnh")
    }

    fun testInsertAndReplace() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) {
            document.setText("XOppzVdZTj\nZhAaVfQynW\nk3kWemkdAg")
        }

        caretModel.moveToOffset(5)
        addSelection(6, 9)
        addCaret(15)
        addSelection(24, 28)
        myFixture.testAction(insertAction)

        assertThat(document.text).isEqualTo("XOppz${"0"}V${"1"}j\nZhAa${"2"}VfQynW\nk3${"3"}kdAg")
    }

    fun testInsertArray() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("wizard\nsirens\nvanity") }

        setSelection(5, 9)

        var insertValue = 0
        myFixture.testAction(DummyInsertArrayAction({ ArrayScheme(count = 2) }) { insertValue++.toString() })

        assertThat(document.text).isEqualTo("wizar[${"0"}, ${"1"}]rens\nvanity")
    }

    fun testInsertRepeat() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("evening\nplease\nfew") }

        caretModel.moveToOffset(5)
        addCaret(10)
        addCaret(12)

        var insertValue = 0
        myFixture.testAction(DummyInsertRepeatAction { insertValue++.toString() })

        assertThat(document.text).isEqualTo("eveni${"0"}ng\npl${"0"}ea${"0"}se\nfew")
    }

    fun testInsertRepeatArray() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("heavy\nbegin\ncare") }

        caretModel.moveToOffset(2)
        addCaret(7)
        addCaret(13)

        var insertValue = 0
        myFixture.testAction(DummyInsertRepeatArrayAction({ ArrayScheme(count = 2) }) { insertValue++.toString() })

        assertThat(document.text).isEqualTo("he${"[0, 1]"}avy\nb${"[0, 1]"}egin\nc${"[0, 1]"}are")
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
     * Adds a caret at the given offset.
     *
     * @param offset an offset
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
