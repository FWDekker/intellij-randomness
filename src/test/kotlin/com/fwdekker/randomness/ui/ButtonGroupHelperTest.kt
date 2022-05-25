package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JRadioButton


/**
 * Unit tests for the extension functions in `ButtonGroupKt`.
 */
object ButtonGroupHelperTest : Spek({
    lateinit var group: ButtonGroup


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        group = ButtonGroup()
    }


    describe("forEach") {
        it("iterates 0 times over an empty group") {
            var sum = 0
            group.forEach { sum++ }

            assertThat(sum).isEqualTo(0)
        }

        it("iterates once for each button in a group") {
            group.add(createJButton())
            group.add(createJButton())
            group.add(createJButton())

            var sum = 0
            group.forEach { sum++ }

            assertThat(sum).isEqualTo(3)
        }
    }

    describe("getValue") {
        it("returns null if the group is empty") {
            assertThat(group.getValue()).isNull()
        }

        it("returns null if no button is selected") {
            group.add(createJButton(actionCommand = "medicine"))

            assertThat(group.getValue()).isNull()
        }

        it("returns an empty string if the selected button has an empty action command") {
            group.add(createJButton(actionCommand = "", isSelected = true))

            assertThat(group.getValue()).isEmpty()
        }

        it("returns the action command of the selected button") {
            group.add(createJButton(actionCommand = "frequent"))
            group.add(createJButton(actionCommand = "umbrella", isSelected = true))

            assertThat(group.getValue()).isEqualTo("umbrella")
        }
    }

    describe("setSalue") {
        it("deselects the currently selected button if no button has the given action command") {
            val buttonA = createJButton(actionCommand = "plenty")
            val buttonB = createJButton(actionCommand = "date")
            val buttonC = createJButton(actionCommand = "bath", isSelected = true)

            group.add(buttonA)
            group.add(buttonB)
            group.add(buttonC)

            GuiActionRunner.execute { group.setValue("formal") }

            assertThat(buttonC.isSelected).isFalse()
        }

        it("selects the button with the given action command") {
            val buttonA = createJButton(actionCommand = "cape")
            val buttonB = createJButton(actionCommand = "another")
            val buttonC = createJButton(actionCommand = "empire")

            group.add(buttonA)
            group.add(buttonB)
            group.add(buttonC)

            GuiActionRunner.execute { group.setValue("another") }

            assertThat(buttonB.isSelected).isTrue()
        }

        it("selects exactly one button if multiple buttons have the given action command") {
            val buttonA = createJButton(actionCommand = "secrecy")
            val buttonB = createJButton(actionCommand = "secrecy")
            val buttonC = createJButton(actionCommand = "study")

            group.add(buttonA)
            group.add(buttonB)
            group.add(buttonC)

            GuiActionRunner.execute { group.setValue("secrecy") }

            assertThat(buttonA.isSelected).isNotEqualTo(buttonB.isSelected)
        }
    }

    describe("buttons") {
        it("returns nothing if the group is empty") {
            assertThat(group.buttons()).isEmpty()
        }

        it("returns the elements of the group") {
            val buttonA = createJButton(actionCommand = "whole")
            val buttonB = createJButton(actionCommand = "melt")
            val buttonC = createJButton(actionCommand = "envy")

            group.add(buttonA)
            group.add(buttonB)
            group.add(buttonC)

            assertThat(group.buttons()).containsExactly(buttonA, buttonB, buttonC)
        }
    }

    describe("setLabel") {
        it("sets the `labelFor` if another button is selected") {
            val button1 = GuiActionRunner.execute<JRadioButton> { JRadioButton() }
            val button2 = GuiActionRunner.execute<JRadioButton> { JRadioButton() }
            group.add(button1)
            group.add(button2)

            val label = GuiActionRunner.execute<JLabel> { JLabel() }
            group.setLabel(label)
            GuiActionRunner.execute { button2.isSelected = true }

            assertThat(label.labelFor).isEqualTo(button2)
        }
    }
})


private fun createJButton(actionCommand: String? = null, isSelected: Boolean = false) =
    GuiActionRunner.execute<JButton> {
        JButton().also {
            it.actionCommand = actionCommand
            it.isSelected = isSelected
        }
    }
