package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.array.ArrayDecorator
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
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
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: UuidScheme
    lateinit var editor: UuidSchemeEditor


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = UuidScheme()
        editor = GuiActionRunner.execute<UuidSchemeEditor> { UuidSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("loadState") {
        it("loads the scheme's type") {
            GuiActionRunner.execute { editor.loadState(UuidScheme(type = 4)) }

            frame.radioButton("type1").requireSelected(false)
            frame.radioButton("type4").requireSelected(true)
        }

        it("loads the scheme's quotation") {
            GuiActionRunner.execute { editor.loadState(UuidScheme(quotation = "'")) }

            frame.radioButton("quotationNone").requireSelected(false)
            frame.radioButton("quotationSingle").requireSelected(true)
            frame.radioButton("quotationDouble").requireSelected(false)
            frame.radioButton("quotationBacktick").requireSelected(false)
            frame.panel("quotationCustom").radioButton().requireSelected(false)
        }

        it("loads the scheme's custom separator") {
            GuiActionRunner.execute { editor.loadState(UuidScheme(customQuotation = "bi")) }

            frame.panel("quotationCustom").textBox().requireText("bi")
        }

        it("selects the scheme's custom separator") {
            GuiActionRunner.execute { editor.loadState(UuidScheme(quotation = "pl", customQuotation = "pl")) }

            frame.panel("quotationCustom").radioButton().requireSelected()
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
            it("returns default typetype if no type is selected") {
                GuiActionRunner.execute { editor.loadState(UuidScheme(type = 967)) }

                assertThat(editor.readState().type).isEqualTo(UuidScheme.DEFAULT_TYPE)
            }

            it("returns default quotation if no quotation is selected") {
                GuiActionRunner.execute { editor.loadState(UuidScheme(quotation = "unsupported")) }

                assertThat(editor.readState().quotation).isEqualTo(UuidScheme.DEFAULT_QUOTATION)
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
            GuiActionRunner.execute {
                frame.radioButton("type1").target().isSelected = true
                frame.radioButton("quotationBacktick").target().isSelected = true
                frame.panel("quotationCustom").textBox().target().text = "yl"
                frame.radioButton("capitalizationUpper").target().isSelected = true
                frame.checkBox("addDashesCheckBox").target().isSelected = true
            }

            val readScheme = editor.readState()
            assertThat(readScheme.type).isEqualTo(1)
            assertThat(readScheme.quotation).isEqualTo("`")
            assertThat(readScheme.customQuotation).isEqualTo("yl")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.UPPER)
            assertThat(readScheme.addDashes).isTrue()
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.radioButton("quotationBacktick").target().isSelected = true }
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

            GuiActionRunner.execute { frame.radioButton("quotationBacktick").target().isSelected = true }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            GuiActionRunner.execute {
                editor.loadState(UuidScheme(arrayDecorator = ArrayDecorator(enabled = true)))
            }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("arrayMinCount").target().value = 528 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
