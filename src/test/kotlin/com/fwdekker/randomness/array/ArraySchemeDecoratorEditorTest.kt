package com.fwdekker.randomness.array

import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [ArraySchemeDecoratorEditor].
 */
object ArraySchemeDecoratorEditorTest : Spek({
    lateinit var scheme: ArraySchemeDecorator
    lateinit var editor: ArraySchemeDecoratorEditor
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        scheme = ArraySchemeDecorator(enabled = true)
        editor = GuiActionRunner.execute<ArraySchemeDecoratorEditor> { ArraySchemeDecoratorEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("event handling") {
        it("truncates decimals in the count") {
            GuiActionRunner.execute { frame.spinner("count").target().value = 983.24f }

            frame.spinner("count").requireValue(983)
        }
    }


    describe("loadScheme") {
        it("loads the settings' enabled state") {
            GuiActionRunner.execute { editor.loadScheme(ArraySchemeDecorator(enabled = true)) }

            frame.checkBox("enabled").requireEnabled()
        }

        it("loads the settings' count") {
            GuiActionRunner.execute { editor.loadScheme(ArraySchemeDecorator(enabled = true, count = 14)) }

            frame.spinner("count").requireValue(14)
        }

        it("loads the settings' brackets") {
            GuiActionRunner.execute { editor.loadScheme(ArraySchemeDecorator(enabled = true, brackets = "{}")) }

            frame.radioButton("bracketsNone").requireSelected(false)
            frame.radioButton("bracketsSquare").requireSelected(false)
            frame.radioButton("bracketsCurly").requireSelected(true)
            frame.radioButton("bracketsRound").requireSelected(false)
        }

        it("loads the settings' separator") {
            GuiActionRunner.execute { editor.loadScheme(ArraySchemeDecorator(enabled = true, separator = ";")) }

            frame.radioButton("separatorComma").requireSelected(false)
            frame.radioButton("separatorSemicolon").requireSelected(true)
            frame.radioButton("separatorNewline").requireSelected(false)
        }

        it("loads the settings' settings for using a space after separator") {
            GuiActionRunner.execute {
                editor.loadScheme(ArraySchemeDecorator(enabled = true, isSpaceAfterSeparator = false))
            }

            frame.checkBox("spaceAfterSeparator").requireSelected(false)
        }
    }

    describe("readScheme") {
        it("returns the original state if no editor changes are made") {
            assertThat(editor.readScheme()).isEqualTo(editor.originalScheme)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.checkBox("enabled").target().isSelected = true
                frame.spinner("count").target().value = 642
                frame.radioButton("bracketsCurly").target().isSelected = true
                frame.radioButton("separatorSemicolon").target().isSelected = true
                frame.checkBox("spaceAfterSeparator").target().isSelected = false
            }

            val readScheme = editor.readScheme()
            assertThat(readScheme.enabled).isEqualTo(true)
            assertThat(readScheme.count).isEqualTo(642)
            assertThat(readScheme.brackets).isEqualTo("{}")
            assertThat(readScheme.separator).isEqualTo(";")
            assertThat(readScheme.isSpaceAfterSeparator).isEqualTo(false)
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.checkBox("enabled").target().isSelected = true }
            assertThat(editor.isModified()).isEqualTo(true)

            editor.loadScheme(editor.readScheme())
            assertThat(editor.isModified()).isEqualTo(false)

            assertThat(editor.readScheme()).isEqualTo(editor.originalScheme)
        }
    }

    describe("addChangeListener") {
        mapOf(
            "enabled" to { frame.checkBox("enabled").target().isEnabled = true },
            "count" to { frame.spinner("count").target().value = 9 },
            "brackets" to { frame.radioButton("bracketsRound").target().isSelected = true },
            "separator" to { frame.radioButton("separatorNewline").target().isSelected = true },
            "isSpaceAfterSeparator" to { frame.checkBox("spaceAfterSeparator").target().isSelected = false }
        ).forEach { (title, updater) ->
            it("invokes the listener one when changing $title") {
                var invokedCount = 0
                editor.addChangeListener { invokedCount++ }

                GuiActionRunner.execute(updater)

                assertThat(invokedCount).isEqualTo(1)
            }
        }
    }
})
