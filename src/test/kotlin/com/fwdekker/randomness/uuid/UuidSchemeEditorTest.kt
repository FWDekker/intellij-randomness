package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [UuidSchemeEditor].
 */
object UuidSchemeEditorTest : DescribeSpec({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: UuidScheme
    lateinit var editor: UuidSchemeEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = UuidScheme()
        editor = guiGet { UuidSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("loadState") {
        it("loads the scheme's type") {
            guiRun { editor.loadState(UuidScheme(type = 4)) }

            frame.radioButton("type1").requireSelected(false)
            frame.radioButton("type4").requireSelected(true)
        }

        it("loads the scheme's quotation") {
            guiRun { editor.loadState(UuidScheme(quotation = "'")) }

            frame.comboBox("quotation").requireSelection("'")
        }

        it("loads the scheme's capitalization mode") {
            guiRun { editor.loadState(UuidScheme(isUppercase = true)) }

            frame.checkBox("isUppercase").requireSelected()
        }

        it("loads the scheme's add dashes option") {
            guiRun { editor.loadState(UuidScheme(addDashes = false)) }

            frame.checkBox("addDashes").requireSelected(false)
        }
    }

    describe("readState") {
        describe("defaults") {
            it("returns default type if no type is selected") {
                guiRun { editor.loadState(UuidScheme(type = 967)) }

                assertThat(editor.readState().type).isEqualTo(UuidScheme.DEFAULT_TYPE)
            }
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            guiRun {
                frame.radioButton("type1").target().isSelected = true
                frame.comboBox("quotation").target().selectedItem = "`"
                frame.checkBox("isUppercase").target().isSelected = true
                frame.checkBox("addDashes").target().isSelected = true
            }

            val readScheme = editor.readState()
            assertThat(readScheme.type).isEqualTo(1)
            assertThat(readScheme.quotation).isEqualTo("`")
            assertThat(readScheme.isUppercase).isTrue()
            assertThat(readScheme.addDashes).isTrue()
        }

        it("returns the loaded state if no editor changes are made") {
            guiRun { frame.comboBox("quotation").target().selectedItem = "`" }
            assertThat(editor.isModified()).isTrue()

            guiRun { editor.loadState(editor.readState()) }
            assertThat(editor.isModified()).isFalse()

            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns a different instance from the loaded scheme") {
            val readState = editor.readState()

            assertThat(readState)
                .isEqualTo(editor.originalState)
                .isNotSameAs(editor.originalState)
            assertThat(readState.arrayDecorator)
                .isEqualTo(editor.originalState.arrayDecorator)
                .isNotSameAs(editor.originalState.arrayDecorator)
        }

        it("retains the scheme's UUID") {
            assertThat(editor.readState().uuid).isEqualTo(editor.originalState.uuid)
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            guiRun { frame.comboBox("quotation").target().selectedItem = "`" }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            guiRun { editor.loadState(UuidScheme(arrayDecorator = ArrayDecorator(enabled = true))) }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            guiRun { frame.spinner("arrayMinCount").target().value = 528 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
