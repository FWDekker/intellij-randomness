package com.fwdekker.randomness.datetime

import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.ui.JDateTimeField
import com.github.sisyphsu.dateparser.DateParserUtils
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import java.time.format.DateTimeFormatter


/**
 * GUI tests for [DateTimeSchemeEditor].
 */
object DateTimeSchemeEditorTest : DescribeSpec({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture
    lateinit var scheme: DateTimeScheme
    lateinit var editor: DateTimeSchemeEditor

    val format = { epoch: Long ->
        DateTimeFormatter.ofPattern(JDateTimeField.DATE_TIME_FORMAT).format(epoch.toLocalDateTime())
    }

    val setMinDateTime = { string: String ->
        (frame.textBox("minDateTime").target() as JDateTimeField).value = DateParserUtils.parseDateTime(string)
    }

    val setMaxDateTime = { string: String ->
        (frame.textBox("maxDateTime").target() as JDateTimeField).value = DateParserUtils.parseDateTime(string)
    }


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = DateTimeScheme()
        editor = GuiActionRunner.execute<DateTimeSchemeEditor> { DateTimeSchemeEditor(scheme) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("input handling") {
        it("sets the maximum to the minimum if the minimum is greater than the maximum") {
            GuiActionRunner.execute {
                editor.loadState(DateTimeScheme(minDateTime = 286_267, maxDateTime = 319_740))
                setMinDateTime(format(856_028))
            }

            frame.textBox("maxDateTime").requireText(format(856_028))
        }

        it("sets the minimum to the maximum if the maximum is less than the minimum") {
            GuiActionRunner.execute {
                editor.loadState(DateTimeScheme(minDateTime = 728_982, maxDateTime = 778_579))
                setMaxDateTime(format(506_729))
            }

            frame.textBox("minDateTime").requireText(format(506_729))
        }
    }


    describe("loadState") {
        it("loads the scheme's minimum value") {
            GuiActionRunner.execute { editor.loadState(DateTimeScheme(minDateTime = 176_662, maxDateTime = 935_199)) }

            frame.textBox("minDateTime").requireText(format(176_662))
        }

        it("loads the scheme's maximum value") {
            GuiActionRunner.execute { editor.loadState(DateTimeScheme(minDateTime = 621_417, maxDateTime = 641_799)) }

            frame.textBox("maxDateTime").requireText(format(641_799))
        }

        it("loads the scheme's pattern") {
            GuiActionRunner.execute { editor.loadState(DateTimeScheme(pattern = "YY-mM")) }

            frame.textBox("pattern").requireText("YY-mM")
        }
    }

    describe("readState") {
        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                setMinDateTime("6678-01-20")
                setMaxDateTime("7310-06-27")
                frame.textBox("pattern").target().text = "YY Y M h"
            }

            val readScheme = editor.readState()
            assertThat(readScheme.minDateTime.toLocalDateTime()).isEqualTo(DateParserUtils.parseDateTime("6678-01-20"))
            assertThat(readScheme.maxDateTime.toLocalDateTime()).isEqualTo(DateParserUtils.parseDateTime("7310-06-27"))
            assertThat(readScheme.pattern).isEqualTo("YY Y M h")
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { setMinDateTime("2369-04-15") }
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

            GuiActionRunner.execute { frame.textBox("minDateTime").target().text = "4704" }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            GuiActionRunner.execute {
                editor.loadState(DateTimeScheme(arrayDecorator = ArrayDecorator(enabled = true)))
            }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("arrayMinCount").target().value = 940 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
