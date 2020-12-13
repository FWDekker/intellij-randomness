package com.fwdekker.randomness

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.editor.Document
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.assertThat


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
