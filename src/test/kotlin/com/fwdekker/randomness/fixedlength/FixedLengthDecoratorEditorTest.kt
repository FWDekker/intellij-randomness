package com.fwdekker.randomness.fixedlength

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [FixedLengthDecoratorEditor].
 */
object FixedLengthDecoratorEditorTest : DescribeSpec({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: FixedLengthDecorator
    lateinit var editor: FixedLengthDecoratorEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = FixedLengthDecorator(enabled = true)
        editor = GuiActionRunner.execute<FixedLengthDecoratorEditor> { FixedLengthDecoratorEditor(scheme) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("event handling") {
        it("keeps only the last input to the filler") {
            GuiActionRunner.execute { frame.textBox("fixedLengthFiller").target().text = "zAt" }

            frame.textBox("fixedLengthFiller").requireText("t")
        }

        it("disables inputs when the scheme is disabled") {
            GuiActionRunner.execute { frame.checkBox("fixedLengthEnabled").target().isSelected = false }

            frame.spinner("fixedLengthLength").requireDisabled()
            frame.textBox("fixedLengthFiller").requireDisabled()
        }

        it("enables inputs when the scheme is re-enabled") {
            GuiActionRunner.execute {
                frame.checkBox("fixedLengthEnabled").target().isSelected = false
                frame.checkBox("fixedLengthEnabled").target().isSelected = true
            }

            frame.spinner("fixedLengthLength").requireEnabled()
            frame.textBox("fixedLengthFiller").requireEnabled()
        }
    }


    describe("loadState") {
        it("loads the scheme's enabled state") {
            GuiActionRunner.execute { editor.loadState(FixedLengthDecorator(enabled = true)) }

            frame.checkBox("fixedLengthEnabled").requireEnabled()
        }

        it("loads the scheme's length") {
            GuiActionRunner.execute { editor.loadState(FixedLengthDecorator(enabled = true, length = 808)) }

            frame.spinner("fixedLengthLength").requireValue(808)
        }

        it("loads the scheme's filler") {
            GuiActionRunner.execute { editor.loadState(FixedLengthDecorator(enabled = true, filler = "k")) }

            frame.textBox("fixedLengthFiller").requireText("k")
        }
    }

    describe("readState") {
        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.checkBox("fixedLengthEnabled").target().isSelected = true
                frame.spinner("fixedLengthLength").target().value = 410
                frame.textBox("fixedLengthFiller").target().text = "h"
            }

            val readScheme = editor.readState()
            assertThat(readScheme.enabled).isTrue()
            assertThat(readScheme.length).isEqualTo(410)
            assertThat(readScheme.filler).isEqualTo("h")
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.checkBox("fixedLengthEnabled").target().isSelected = false }
            assertThat(editor.isModified()).isTrue()

            GuiActionRunner.execute { editor.loadState(editor.readState()) }
            assertThat(editor.isModified()).isFalse()

            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns a different instance from the loaded scheme") {
            assertThat(editor.readState())
                .isEqualTo(editor.originalState)
                .isNotSameAs(editor.originalState)
        }

        it("retains the scheme's UUID") {
            assertThat(editor.readState().uuid).isEqualTo(editor.originalState.uuid)
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("fixedLengthLength").target().value = 274 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
