package com.fwdekker.randomness

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


/**
 * Unit tests for [DataInsertAction].
 */
class DataInsertActionTest {
    companion object {
        /**
         * The recognizable string that is inserted by the insertion action.
         */
        private const val RANDOM_STRING = "random_string"
    }

    private lateinit var dataInsertAction: DataInsertAction


    @BeforeEach
    fun beforeEach() {
        dataInsertAction = SimpleInsertAction()
    }


    /**
     * Tests that no further actions are taken when the editor is `null`.
     */
    @Test
    fun testActionPerformedNullEditor() {
        val event = mock<AnActionEvent> {
            on { getData(CommonDataKeys.EDITOR) } doReturn null
        }

        dataInsertAction.actionPerformed(event)

        verify(event, times(1)).getData(CommonDataKeys.EDITOR)
        verifyNoMoreInteractions(event)
    }

    @Test
    fun testActionPerformedNullProject() {
        val event = mock<AnActionEvent> {
            on { getData(CommonDataKeys.EDITOR) } doReturn mock()
            on { getData(CommonDataKeys.PROJECT) } doReturn null
        }

        dataInsertAction.actionPerformed(event)

        verify(event, times(1)).getData(CommonDataKeys.EDITOR)
        verify(event, times(1)).getData(CommonDataKeys.PROJECT)
        verifyNoMoreInteractions(event)
    }


    /**
     * Tests that the action's presentation is disabled when the editor is null.
     */
    @Test
    fun testUpdateDisabled() {
        val presentation = Presentation()
        val event = mock<AnActionEvent> {
            on { getData(CommonDataKeys.EDITOR) } doReturn null
            on { it.presentation } doReturn presentation
        }

        dataInsertAction.update(event)

        assertThat(presentation.isEnabled).isFalse()
    }

    /**
     * Tests that the action's presentation is enabled when the editor is not null.
     */
    @Test
    fun testUpdateEnabled() {
        val presentation = Presentation()
        val event = mock<AnActionEvent> {
            on { getData(CommonDataKeys.EDITOR) } doReturn mock()
            on { it.presentation } doReturn presentation
        }

        dataInsertAction.update(event)

        assertThat(presentation.isEnabled).isTrue()
    }


    /**
     * Simple implementation of [DataInsertAction].
     */
    private class SimpleInsertAction : DataInsertAction() {
        override val name = "Insert Random Simple"


        override fun generateStrings(count: Int) = List(count) { RANDOM_STRING }
    }
}
