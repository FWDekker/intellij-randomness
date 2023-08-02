package com.fwdekker.randomness.integer

import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [IntegerSchemeEditor].
 */
object IntegerSchemeEditorTest : DescribeSpec({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: IntegerScheme
    lateinit var editor: IntegerSchemeEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = IntegerScheme()
        editor = guiGet { IntegerSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }



    describe("input handling") {
        it("truncates decimals in the minimum value") {
            guiRun { frame.spinner("minValue").target().value = 285.21f }

            frame.spinner("minValue").requireValue(285L)
        }

        it("truncates decimals in the maximum value") {
            guiRun { frame.spinner("maxValue").target().value = 490.34f }

            frame.spinner("maxValue").requireValue(490L)
        }

        it("truncates decimals in the base") {
            guiRun { frame.spinner("base").target().value = 22.62f }

            frame.spinner("base").requireValue(22)
        }

        it("toggles the grouping separator depending on base value and checkbox state") {
            @Suppress("BooleanLiteralArgument") // Argument labels given in line with `headers`
            forAll(
                table(
                    headers("base", "checkbox checked", "expected checkbox state", "expected input state"),
                    row(8, false, false, false),
                    row(8, true, false, false),
                    row(10, false, true, false),
                    row(10, true, true, true),
                    row(12, false, false, false),
                    row(12, true, false, false),
                )
            ) { base, checkBoxChecked, expectedCheckBoxEnabled, expectedInputEnabled ->
                guiRun {
                    frame.spinner("base").target().value = base
                    frame.checkBox("groupingSeparatorEnabled").target().isSelected = checkBoxChecked
                }

                frame.checkBox("groupingSeparatorEnabled")
                    .let { if (expectedCheckBoxEnabled) it.requireEnabled() else it.requireDisabled() }
                frame.comboBox("groupingSeparator")
                    .let { if (expectedInputEnabled) it.requireEnabled() else it.requireDisabled() }
            }
        }
    }


    describe("loadState") {
        it("loads the scheme's minimum value") {
            guiRun { editor.loadState(IntegerScheme(minValue = 145L, maxValue = 341L)) }

            frame.spinner("minValue").requireValue(145L)
        }

        it("loads the scheme's maximum value") {
            guiRun { editor.loadState(IntegerScheme(minValue = 337L, maxValue = 614L)) }

            frame.spinner("maxValue").requireValue(614L)
        }

        it("loads the scheme's base value") {
            guiRun { editor.loadState(IntegerScheme(base = 25)) }

            frame.spinner("base").requireValue(25)
        }

        it("loads the scheme's grouping separator enabled state") {
            guiRun { editor.loadState(IntegerScheme(groupingSeparatorEnabled = true)) }

            frame.checkBox("groupingSeparatorEnabled").requireSelected()
        }

        it("loads the scheme's grouping separator") {
            guiRun { editor.loadState(IntegerScheme(groupingSeparator = "_")) }

            frame.comboBox("groupingSeparator").requireSelection("_")
        }

        it("loads the scheme's uppercase state") {
            guiRun { editor.loadState(IntegerScheme(isUppercase = true)) }

            frame.checkBox("isUppercase").requireSelected()
        }

        it("loads the scheme's prefix") {
            guiRun { editor.loadState(IntegerScheme(prefix = "wage")) }

            frame.textBox("prefix").requireText("wage")
        }

        it("loads the scheme's suffix") {
            guiRun { editor.loadState(IntegerScheme(suffix = "fat")) }

            frame.textBox("suffix").requireText("fat")
        }
    }

    describe("readState") {
        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            guiRun {
                frame.spinner("minValue").target().value = 2_147_483_648L
                frame.spinner("maxValue").target().value = 2_147_483_649L
                frame.spinner("base").target().value = 14
                frame.checkBox("groupingSeparatorEnabled").target().isSelected = true
                frame.comboBox("groupingSeparator").target().selectedItem = "."
                frame.checkBox("isUppercase").target().isSelected = true
                frame.textBox("prefix").target().text = "silent"
                frame.textBox("suffix").target().text = "pain"
            }

            val readScheme = editor.readState()
            assertThat(readScheme.minValue).isEqualTo(2_147_483_648L)
            assertThat(readScheme.maxValue).isEqualTo(2_147_483_649L)
            assertThat(readScheme.base).isEqualTo(14)
            assertThat(readScheme.groupingSeparatorEnabled).isTrue()
            assertThat(readScheme.groupingSeparator).isEqualTo(".")
            assertThat(readScheme.isUppercase).isTrue()
            assertThat(readScheme.prefix).isEqualTo("silent")
            assertThat(readScheme.suffix).isEqualTo("pain")
        }

        it("returns the loaded state if no editor changes are made") {
            guiRun { frame.spinner("minValue").target().value = 242L }
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

            guiRun { frame.spinner("minValue").target().value = 76L }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            guiRun { editor.loadState(IntegerScheme(arrayDecorator = ArrayDecorator(enabled = true))) }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            guiRun { frame.spinner("arrayMinCount").target().value = 528 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
