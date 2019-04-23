package com.fwdekker.randomness

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.editor.Editor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*


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
        val event = mock(AnActionEvent::class.java)
        `when`<Editor>(event.getData(CommonDataKeys.EDITOR)).thenReturn(null)

        dataInsertAction.actionPerformed(event)

        verify(event, times(1)).getData(CommonDataKeys.EDITOR)
        verifyNoMoreInteractions(event)
    }

    /**
     * Tests that the action's presentation is disabled when the editor is null.
     */
    @Test
    fun testUpdateDisabled() {
        val event = mock(AnActionEvent::class.java)
        val presentation = spy(Presentation::class.java)
        `when`<Editor>(event.getData(CommonDataKeys.EDITOR)).thenReturn(null)
        `when`(event.presentation).thenReturn(presentation)

        dataInsertAction.update(event)

        assertThat(presentation.isEnabled).isFalse()
    }

    /**
     * Tests that the action's presentation is enabled when the editor is not null.
     */
    @Test
    fun testUpdateEnabled() {
        val event = mock(AnActionEvent::class.java)
        val presentation = spy(Presentation::class.java)
        `when`<Editor>(event.getData(CommonDataKeys.EDITOR)).thenReturn(mock(Editor::class.java))
        `when`(event.presentation).thenReturn(presentation)

        dataInsertAction.update(event)

        assertThat(presentation.isEnabled).isTrue()
    }


    /**
     * Simple implementation of [DataInsertAction].
     */
    private class SimpleInsertAction : DataInsertAction() {
        override val name = "Insert Simple"


        override fun generateString() = RANDOM_STRING
    }
}
