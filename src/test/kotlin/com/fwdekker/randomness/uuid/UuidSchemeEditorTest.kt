package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.array.ArraySchemeDecorator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [UuidSchemeEditor].
 */
object UuidSchemeEditorTest : Spek({
    lateinit var scheme: UuidScheme
    lateinit var editor: UuidSchemeEditor
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        scheme = UuidScheme()
        editor = GuiActionRunner.execute<UuidSchemeEditor> { UuidSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loadState") {
        it("loads the scheme's version") {
            GuiActionRunner.execute { editor.loadState(UuidScheme(version = 4)) }

            frame.radioButton("version1").requireSelected(false)
            frame.radioButton("version4").requireSelected(true)
        }

        it("loads the scheme's enclosure") {
            GuiActionRunner.execute { editor.loadState(UuidScheme(enclosure = "'")) }

            frame.radioButton("enclosureNone").requireSelected(false)
            frame.radioButton("enclosureSingle").requireSelected(true)
            frame.radioButton("enclosureDouble").requireSelected(false)
            frame.radioButton("enclosureBacktick").requireSelected(false)
        }

        it("loads the scheme's capitalization mode") {
            GuiActionRunner.execute { editor.loadState(UuidScheme(capitalization = CapitalizationMode.UPPER)) }

            frame.radioButton("capitalizationLower").requireSelected(false)
            frame.radioButton("capitalizationUpper").requireSelected(true)
        }

        it("loads the scheme's add dashes option") {
            GuiActionRunner.execute { editor.loadState(UuidScheme(addDashes = false)) }

            frame.checkBox("addDashesCheckBox").requireSelected(false)
        }
    }

    describe("readState") {
        describe("defaults") {
            it("returns default version if no version is selected") {
                GuiActionRunner.execute { editor.loadState(UuidScheme(version = 967)) }

                assertThat(editor.readState().version).isEqualTo(UuidScheme.DEFAULT_VERSION)
            }

            it("returns default enclosure if no enclosure is selected") {
                GuiActionRunner.execute { editor.loadState(UuidScheme(enclosure = "unsupported")) }

                assertThat(editor.readState().enclosure).isEqualTo(UuidScheme.DEFAULT_ENCLOSURE)
            }

            it("returns default capitalization if no capitalization is selected") {
                GuiActionRunner.execute { editor.loadState(UuidScheme(capitalization = CapitalizationMode.DUMMY)) }

                assertThat(editor.readState().capitalization).isEqualTo(UuidScheme.DEFAULT_CAPITALIZATION)
            }
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute { frame.radioButton("version1").target().isSelected = true }
            GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }
            GuiActionRunner.execute { frame.radioButton("capitalizationUpper").target().isSelected = true }
            GuiActionRunner.execute { frame.checkBox("addDashesCheckBox").target().isSelected = true }

            val readScheme = editor.readState()
            assertThat(readScheme.version).isEqualTo(1)
            assertThat(readScheme.enclosure).isEqualTo("`")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.UPPER)
            assertThat(readScheme.addDashes).isTrue()
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }
            assertThat(editor.isModified()).isTrue()

            GuiActionRunner.execute { editor.loadState(editor.readState()) }
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

            GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            GuiActionRunner.execute {
                editor.loadState(UuidScheme(arrayDecorator = ArraySchemeDecorator(enabled = true)))
            }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("arrayMinCount").target().value = 528 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
