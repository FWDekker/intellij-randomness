package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import javax.swing.ButtonGroup


/**
 * GUI tests for [VariableLabelRadioButton].
 */
object VariableLabelRadioButtonTest : Spek({
    lateinit var variableButton: VariableLabelRadioButton
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        variableButton = GuiActionRunner.execute<VariableLabelRadioButton> { VariableLabelRadioButton() }
        frame = Containers.showInFrame(variableButton)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("label") {
        it("returns the text of the text field") {
            GuiActionRunner.execute { frame.textBox().target().text = "formal" }

            assertThat(variableButton.label).isEqualTo("formal")
        }

        it("sets the text of the text field") {
            GuiActionRunner.execute { variableButton.label = "paw" }

            assertThat(frame.textBox().target().text).isEqualTo("paw")
        }

        it("writes to the action command of the button") {
            GuiActionRunner.execute { variableButton.label = "fool" }

            assertThat(frame.radioButton().target().actionCommand).isEqualTo("fool")
        }

        it("writes changes to the text field to the action command of the button") {
            GuiActionRunner.execute { frame.textBox().target().text = "hold" }

            assertThat(frame.radioButton().target().actionCommand).isEqualTo("hold")
        }
    }


    describe("constructor") {
        it("sets the width of the text field") {
            frame.cleanUp()

            variableButton = GuiActionRunner.execute<VariableLabelRadioButton> { VariableLabelRadioButton(width = 398) }
            frame = Containers.showInFrame(variableButton)

            assertThat(frame.textBox().target().preferredSize.width).isEqualTo(398)
        }
    }


    describe("addToButtonGroup") {
        it("adds the button to the button group") {
            val group = ButtonGroup()

            variableButton.addToButtonGroup(group)

            assertThat(group.buttonCount).isEqualTo(1)
        }
    }

    describe("addChangeListener") {
        it("invokes the listener when the button is selected") {
            var invoked = false
            variableButton.addChangeListener { invoked = true }

            GuiActionRunner.execute { frame.radioButton().target().isSelected = true }

            assertThat(invoked).isTrue()
        }

        it("invokes the listener when the button is de-selected") {
            GuiActionRunner.execute { frame.radioButton().target().isSelected = true }

            var invoked = false
            variableButton.addChangeListener { invoked = true }

            GuiActionRunner.execute { frame.radioButton().target().isSelected = false }

            assertThat(invoked).isTrue()
        }
    }


    describe("setEnabled") {
        it("has elements enabled by default") {
            frame.radioButton().requireEnabled()
            frame.textBox().requireEnabled()
        }

        it("disables elements") {
            GuiActionRunner.execute { variableButton.isEnabled = false }

            frame.radioButton().requireDisabled()
            frame.textBox().requireDisabled()
        }

        it("re-enables elements") {
            GuiActionRunner.execute {
                variableButton.isEnabled = false
                variableButton.isEnabled = true
            }

            frame.radioButton().requireEnabled()
            frame.textBox().requireEnabled()
        }
    }
})
