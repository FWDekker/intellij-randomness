package com.fwdekker.randomness

import com.fwdekker.randomness.DataInsertAction.Companion.GENERATOR_TIMEOUT
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.editor.Document
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icons.RandomnessIcons
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.math.roundToLong
import kotlin.random.Random


/**
 * Unit tests for [DataInsertAction].
 *
 * @see DataInsertActionIntegrationTest
 */
class DataInsertActionTest : BasePlatformTestCase() {
    private lateinit var dataInsertAction: DataInsertAction
    private lateinit var document: Document


    override fun setUp() {
        super.setUp()

        val file = myFixture.copyFileToProject("emptyFile.txt")
        myFixture.openFileInEditor(file)

        document = myFixture.editor.document

        dataInsertAction = DummyInsertAction { "random_value" }
    }

    override fun getTestDataPath() = javaClass.classLoader.getResource("integration-project/")?.path


    fun testActionPerformedNoFurtherActionsWhenEditorIsNull() {
        val event = AnActionEvent.createFromDataContext("", null) {
            if (it == CommonDataKeys.PROJECT.name) myFixture.project
            else null
        }

        dataInsertAction.actionPerformed(event)

        assertThat(document.text).isEqualTo("")
    }

    fun testActionPerformedNoFurtherActionsWhenProjectIsNull() {
        val event = AnActionEvent.createFromDataContext("", null) {
            if (it == CommonDataKeys.EDITOR.name) myFixture.editor
            else null
        }

        dataInsertAction.actionPerformed(event)

        assertThat(document.text).isEqualTo("")
    }

    fun testUpdateDisablePresentationWhenEditorIsNull() {
        val presentation = Presentation()
        val event = AnActionEvent.createFromDataContext("", presentation) { null }

        dataInsertAction.update(event)

        assertThat(presentation.isEnabled).isFalse()
    }

    fun testUpdateEnablePresentationWhenEditorIsNotNull() {
        val presentation = Presentation()
        val event = AnActionEvent.createFromDataContext("", presentation) { myFixture.editor }

        dataInsertAction.update(event)

        assertThat(presentation.isEnabled).isTrue()
    }
}


/**
 * Unit tests for timeliness of generators in [DataInsertAction].
 */
class DataInsertActionTimeoutTest : Spek({
    /**
     * Inserts a dummy value after a delay that is longer than [GENERATOR_TIMEOUT].
     *
     * @property dummySupplier generates dummy values to insert
     */
    class SlowDummyInsertAction(private val dummySupplier: (Random) -> String) :
        DataInsertAction(RandomnessIcons.Data.Base) {
        override val name = "Slow Random Dummy"

        override fun generateStrings(count: Int): List<String> {
            Thread.sleep((1.1 * GENERATOR_TIMEOUT).roundToLong())
            return List(count) { dummySupplier(random) }
        }
    }


    describe("timely generators") {
        lateinit var action: SlowDummyInsertAction


        beforeEachTest {
            action = SlowDummyInsertAction { "test string" }
        }


        it("generates a string normally") {
            assertThat(action.generateString()).isEqualTo("test string")
        }

        it("generates multiple strings normally") {
            assertThat(action.generateStrings(2)).isEqualTo(listOf("test string", "test string"))
        }

        it("fails to generate a single string in time") {
            assertThatThrownBy { action.generateStringTimely() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("Timed out while generating data.")
        }

        it("fails to generate multiple strings in time") {
            assertThatThrownBy { action.generateStringsTimely(2) }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("Timed out while generating data.")
        }
    }
})
