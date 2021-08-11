package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
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


    describe("loadScheme") {
        it("loads the settings' version") {
            GuiActionRunner.execute { editor.loadScheme(UuidScheme(version = 4)) }

            frame.radioButton("version1").requireSelected(false)
            frame.radioButton("version4").requireSelected(true)
        }

        it("loads the settings' enclosure") {
            GuiActionRunner.execute { editor.loadScheme(UuidScheme(enclosure = "'")) }

            frame.radioButton("enclosureNone").requireSelected(false)
            frame.radioButton("enclosureSingle").requireSelected(true)
            frame.radioButton("enclosureDouble").requireSelected(false)
            frame.radioButton("enclosureBacktick").requireSelected(false)
        }

        it("loads the settings' capitalization mode") {
            GuiActionRunner.execute { editor.loadScheme(UuidScheme(capitalization = CapitalizationMode.UPPER)) }

            frame.radioButton("capitalizationLower").requireSelected(false)
            frame.radioButton("capitalizationUpper").requireSelected(true)
        }

        it("loads the settings' add dashes option") {
            GuiActionRunner.execute { editor.loadScheme(UuidScheme(addDashes = false)) }

            frame.checkBox("addDashesCheckBox").requireSelected(false)
        }
    }

    describe("readScheme") {
        it("returns the original state if no editor changes are made") {
            assertThat(editor.readScheme()).isEqualTo(editor.originalScheme)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute { frame.radioButton("version1").target().isSelected = true }
            GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }
            GuiActionRunner.execute { frame.radioButton("capitalizationUpper").target().isSelected = true }
            GuiActionRunner.execute { frame.checkBox("addDashesCheckBox").target().isSelected = true }

            val readScheme = editor.readScheme()
            assertThat(readScheme.version).isEqualTo(1)
            assertThat(readScheme.enclosure).isEqualTo("`")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.UPPER)
            assertThat(readScheme.addDashes).isTrue()
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }
            assertThat(editor.isModified()).isTrue()

            GuiActionRunner.execute { editor.loadScheme(editor.readScheme()) }
            assertThat(editor.isModified()).isFalse()

            assertThat(editor.readScheme()).isEqualTo(editor.originalScheme)
        }
    }
})
