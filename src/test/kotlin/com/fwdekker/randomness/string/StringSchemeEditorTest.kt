package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.getComboBoxItem
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
 * GUI tests for [StringSchemeEditor].
 */
object StringSchemeEditorTest : DescribeSpec({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: StringScheme
    lateinit var editor: StringSchemeEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = StringScheme()
        editor = guiGet { StringSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("loadState") {
        it("loads the scheme's pattern") {
            guiRun { editor.loadState(StringScheme(pattern = "[0-9]{3}")) }

            frame.textBox("pattern").requireText("[0-9]{3}")
        }

        it("loads the scheme's capitalization") {
            guiRun { editor.loadState(StringScheme(capitalization = CapitalizationMode.RANDOM)) }

            assertThat(frame.getComboBoxItem<CapitalizationMode>("capitalization")).isEqualTo(CapitalizationMode.RANDOM)
        }

        it("loads the scheme's setting for removing look-alike symbols") {
            guiRun { editor.loadState(StringScheme(removeLookAlikeSymbols = true)) }

            frame.checkBox("removeLookAlikeCharacters").requireSelected()
        }
    }

    describe("readState") {
        describe("defaults") {
            it("returns default brackets if no capitalization is selected") {
                guiRun { editor.loadState(StringScheme(capitalization = CapitalizationMode.DUMMY)) }

                assertThat(editor.readState().capitalization).isEqualTo(StringScheme.DEFAULT_CAPITALIZATION)
            }
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            guiRun {
                frame.textBox("pattern").target().text = "AqqR"
                frame.comboBox("capitalization").target().selectedItem = CapitalizationMode.UPPER
                frame.checkBox("removeLookAlikeCharacters").target().isSelected = false
            }

            val readScheme = editor.readState()
            assertThat(readScheme.pattern).isEqualTo("AqqR")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.UPPER)
            assertThat(readScheme.removeLookAlikeSymbols).isFalse()
        }

        it("returns the loaded state if no editor changes are made") {
            guiRun { frame.textBox("pattern").target().text = "loyal" }
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

            guiRun { frame.textBox("pattern").target().text = "[ho]spital" }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            guiRun { editor.loadState(StringScheme(arrayDecorator = ArrayDecorator(enabled = true))) }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            guiRun { frame.spinner("arrayMinCount").target().value = 528 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
