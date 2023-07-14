package com.fwdekker.randomness.ui

import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [VariableLabelRadioButton].
 */
object VariableLabelRadioButtonTest : DescribeSpec({
    lateinit var variableButton: VariableLabelRadioButton
    lateinit var frame: FrameFixture


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        variableButton = GuiActionRunner.execute<VariableLabelRadioButton> { VariableLabelRadioButton() }
        frame = Containers.showInFrame(variableButton)
    }

    afterEach {
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

        it("focuses the text box when the radio button is selected") {
            assertThat(frame.textBox().target().hasFocus()).isFalse()

            frame.radioButton().click()

            frame.textBox().requireFocused()
        }

        it("selects the radio button when the text box is focused") {
            frame.radioButton().requireNotSelected()

            GuiActionRunner.execute { frame.textBox().target().grabFocus() }

            frame.radioButton().requireSelected()
        }

        it("enables the text box if the radio button is selected") {
            GuiActionRunner.execute { frame.radioButton().target().isSelected = true }

            frame.textBox().requireEnabled()
        }

        it("disables the text box if the radio button is not selected") {
            GuiActionRunner.execute { frame.radioButton().target().isSelected = false }

            frame.textBox().requireDisabled()
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
        it("disables elements") {
            GuiActionRunner.execute { variableButton.isEnabled = false }

            frame.radioButton().requireDisabled()
            frame.textBox().requireDisabled()
        }

        it("enables the radio button") {
            GuiActionRunner.execute { variableButton.isEnabled = true }

            frame.radioButton().requireEnabled()
        }

        it("enables the text box if the radio button is selected") {
            GuiActionRunner.execute {
                frame.radioButton().target().isSelected = true
                variableButton.isEnabled = true
            }

            frame.textBox().requireEnabled()
        }

        it("does not enable the text box if the radio button is not selected") {
            GuiActionRunner.execute {
                frame.radioButton().target().isSelected = false
                variableButton.isEnabled = true
            }

            frame.textBox().requireDisabled()
        }
    }
})
