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
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [DataInsertAction].
 *
 * @see DataInsertActionIntegrationTest
 */
object DataInsertActionTest : Spek({
    lateinit var dataInsertAction: DataInsertAction


    beforeEachTest {
        dataInsertAction = DummyInsertAction { "random_value" }
    }


    describe("actionPerformed") {
        it("takes no further actions when the editor is null") {
            val event = mock<AnActionEvent> {
                on { getData(CommonDataKeys.EDITOR) } doReturn null
            }

            dataInsertAction.actionPerformed(event)

            verify(event, times(1)).getData(CommonDataKeys.EDITOR)
            verifyNoMoreInteractions(event)
        }

        it("takes no further action when the project is null") {
            val event = mock<AnActionEvent> {
                on { getData(CommonDataKeys.EDITOR) } doReturn mock()
                on { getData(CommonDataKeys.PROJECT) } doReturn null
            }

            dataInsertAction.actionPerformed(event)

            verify(event, times(1)).getData(CommonDataKeys.EDITOR)
            verify(event, times(1)).getData(CommonDataKeys.PROJECT)
            verifyNoMoreInteractions(event)
        }
    }

    describe("update") {
        it("disables the presentation when the editor is null") {
            val presentation = Presentation()
            val event = mock<AnActionEvent> {
                on { getData(CommonDataKeys.EDITOR) } doReturn null
                on { it.presentation } doReturn presentation
            }

            dataInsertAction.update(event)

            assertThat(presentation.isEnabled).isFalse()
        }

        it("enables the presentation when the editor is not null") {
            val presentation = Presentation()
            val event = mock<AnActionEvent> {
                on { getData(CommonDataKeys.EDITOR) } doReturn mock()
                on { it.presentation } doReturn presentation
            }

            dataInsertAction.update(event)

            assertThat(presentation.isEnabled).isTrue()
        }
    }
})
