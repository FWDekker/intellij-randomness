package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArraySettings
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.assertj.core.api.Assertions.assertThat


/**
 * Integration tests for [DataInsertAction].
 *
 * Note that `LightPlatformCodeInsightFixtureTestCase` is a JUnit 3 test class.
 */
class DataInsertActionIntegrationTest : LightPlatformCodeInsightFixtureTestCase() {
    companion object {
        /**
         * The recognizable string that is inserted by the insertion action.
         */
        private const val RANDOM_STRING = "random_string"
    }

    private lateinit var insertRandomSimple: SimpleInsertAction
    private lateinit var document: Document
    private lateinit var caretModel: CaretModel


    override fun setUp() {
        super.setUp()

        val file = myFixture.copyFileToProject("emptyFile.txt")
        myFixture.openFileInEditor(file)

        document = myFixture.editor.document
        caretModel = myFixture.editor.caretModel
        insertRandomSimple = SimpleInsertAction()
    }

    override fun getTestDataPath() = javaClass.classLoader.getResource("integration-project/")?.path


    fun testInsertIntoEmpty() {
        myFixture.testAction(insertRandomSimple)

        assertThat(document.text).isEqualTo(RANDOM_STRING)
    }

    fun testInsertBefore() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("RkpjkS9Itb") }

        caretModel.moveToOffset(0)
        myFixture.testAction(insertRandomSimple)

        assertThat(document.text).isEqualTo("${RANDOM_STRING}RkpjkS9Itb")
    }

    fun testInsertAfter() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("0aiMbK5hK5") }

        caretModel.moveToOffset(10)
        myFixture.testAction(insertRandomSimple)

        assertThat(document.text).isEqualTo("0aiMbK5hK5$RANDOM_STRING")
    }

    fun testInsertBetween() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("U6jBDMh8Nq") }

        caretModel.moveToOffset(5)
        myFixture.testAction(insertRandomSimple)

        assertThat(document.text).isEqualTo("U6jBD${RANDOM_STRING}Mh8Nq")
    }

    fun testReplaceAll() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("fMhAajjDw6") }

        setSelection(0, 10)
        myFixture.testAction(insertRandomSimple)

        assertThat(document.text).isEqualTo(RANDOM_STRING)
    }

    fun testReplacePart() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("qZPGZDEcPS") }

        setSelection(3, 7)
        myFixture.testAction(insertRandomSimple)

        assertThat(document.text).isEqualTo("qZP${RANDOM_STRING}cPS")
    }

    fun testInsertMultiple() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) {
            document.setText("DCtD41lFOk\nOCnrdYk9gE\nn1HAPKotDq")
        }

        addCaret(11)
        addCaret(22)
        myFixture.testAction(insertRandomSimple)

        assertThat(document.text)
            .isEqualTo("${RANDOM_STRING}DCtD41lFOk\n${RANDOM_STRING}OCnrdYk9gE\n${RANDOM_STRING}n1HAPKotDq")
    }

    fun testReplaceMultiple() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) {
            document.setText("YXSncq4FC9\nG31Ybbn1c4\nTNCqAhqPnh")
        }

        setSelection(2, 4)
        addSelection(18, 23)
        addSelection(29, 29)
        myFixture.testAction(insertRandomSimple)

        assertThat(document.text)
            .isEqualTo("YX${RANDOM_STRING}cq4FC9\nG31Ybbn${RANDOM_STRING}NCqAhq${RANDOM_STRING}Pnh")
    }

    fun testInsertAndReplace() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) {
            document.setText("XOppzVdZTj\nZhAaVfQynW\nk3kWemkdAg")
        }

        caretModel.moveToOffset(5)
        addSelection(6, 9)
        addCaret(15)
        addSelection(24, 28)
        myFixture.testAction(insertRandomSimple)

        assertThat(document.text)
            .isEqualTo("XOppz${RANDOM_STRING}V${RANDOM_STRING}j\nZhAa${RANDOM_STRING}VfQynW\nk3${RANDOM_STRING}kdAg")
    }

    fun testInsertArray() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) { document.setText("wizard\nsirens\nvanity") }

        val arraySettings = ArraySettings()
        arraySettings.count = 2

        setSelection(5, 9)
        myFixture.testAction(SimplyArrayInsertAction(arraySettings))

        assertThat(document.text).isEqualTo("wizar[$RANDOM_STRING, $RANDOM_STRING]rens\nvanity")
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


    /**
     * Simple implementation of [DataInsertAction].
     */
    private class SimpleInsertAction : DataInsertAction() {
        override val name = "Insert Random Simple"


        override fun generateStrings(count: Int) = List(count) { RANDOM_STRING }
    }

    /**
     * Simple implementation of [DataInsertArrayAction].
     */
    private class SimplyArrayInsertAction(arraySettings: ArraySettings = ArraySettings.default) :
        DataInsertArrayAction(arraySettings, SimpleInsertAction()) {
        override val name = "Insert Random Simple Array"
    }
}
