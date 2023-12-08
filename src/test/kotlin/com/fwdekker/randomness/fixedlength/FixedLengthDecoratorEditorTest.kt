package com.fwdekker.randomness.fixedlength

import com.fwdekker.randomness.editorApplyTestFactory
import com.fwdekker.randomness.editorFieldsTestFactory
import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.guiRun
import com.fwdekker.randomness.testhelpers.isSelectedProp
import com.fwdekker.randomness.testhelpers.prop
import com.fwdekker.randomness.testhelpers.textProp
import com.fwdekker.randomness.testhelpers.valueProp
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * Unit tests for [FixedLengthDecoratorEditor].
 */
object FixedLengthDecoratorEditorTest : FunSpec({
    tags(Tags.EDITOR, Tags.IDEA_FIXTURE, Tags.SWING)


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: FixedLengthDecorator
    lateinit var editor: FixedLengthDecoratorEditor


    beforeSpec {
        FailOnThreadViolationRepaintManager.install()
    }

    afterSpec {
        FailOnThreadViolationRepaintManager.uninstall()
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = FixedLengthDecorator(enabled = true)
        editor = guiGet { FixedLengthDecoratorEditor(scheme) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterNonContainer {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    context("input handling") {
        context("fixedLengthEnabled") {
            test("disables inputs if deselected") {
                guiRun { frame.checkBox("fixedLengthEnabled").target().isSelected = false }

                frame.spinner("fixedLengthLength").requireDisabled()
                frame.textBox("fixedLengthFiller").requireDisabled()
            }

            test("enables inputs if (re)selected") {
                guiRun { frame.checkBox("fixedLengthEnabled").target().isSelected = false }
                guiRun { frame.checkBox("fixedLengthEnabled").target().isSelected = true }

                frame.spinner("fixedLengthLength").requireEnabled()
                frame.textBox("fixedLengthFiller").requireEnabled()
            }
        }

        context("filler") {
            test("enforces the filler's length filter") {
                guiRun { frame.textBox("fixedLengthFiller").target().text = "zAt" }

                frame.textBox("fixedLengthFiller").requireText("t")
            }
        }
    }


    include(editorApplyTestFactory { editor })

    include(
        editorFieldsTestFactory(
            { editor },
            mapOf(
                "enabled" to {
                    row(frame.checkBox("fixedLengthEnabled").isSelectedProp(), editor.scheme::enabled.prop(), false)
                },
                "length" to {
                    row(frame.spinner("fixedLengthLength").valueProp(), editor.scheme::length.prop(), 5L)
                },
                "filler" to {
                    row(frame.textBox("fixedLengthFiller").textProp(), editor.scheme::filler.prop(), ".")
                },
            )
        )
    )
})
