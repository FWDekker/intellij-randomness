package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.GuiActionRunner
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import javax.swing.ButtonGroup
import javax.swing.JButton


/**
 * Unit tests for the extension functions in `ButtonGroupKt`.
 */
object ButtonGroupKtTest : Spek({
    lateinit var group: ButtonGroup


    beforeEachTest {
        group = ButtonGroup()
    }


    describe("for each") {
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

    describe("get value") {
        it("returns null if the group is empty") {
            assertThat(group.getValue()).isNull()
        }

        it("returns null if no button is selected") {
            group.add(createJButton(actionCommand = "shahid"))

            assertThat(group.getValue()).isNull()
        }

        it("returns an empty string if the selected button has an empty action command") {
            group.add(createJButton(actionCommand = "", isSelected = true))

            assertThat(group.getValue()).isEmpty()
        }

        it("returns the action command of the selected button") {
            group.add(createJButton(actionCommand = "causing"))
            group.add(createJButton(actionCommand = "spahees", isSelected = true))

            assertThat(group.getValue()).isEqualTo("spahees")
        }
    }

    describe("set value") {
        it("deselects the currently selected button if no button has the given action command") {
            val buttonA = createJButton(actionCommand = "windrode")
            val buttonB = createJButton(actionCommand = "carene")
            val buttonC = createJButton(actionCommand = "eringoes", isSelected = true)

            group.add(buttonA)
            group.add(buttonB)
            group.add(buttonC)

            GuiActionRunner.execute { group.setValue("staplers") }

            assertThat(buttonC.isSelected).isFalse()
        }

        it("selects the button with the given action command") {
            val buttonA = createJButton(actionCommand = "claps")
            val buttonB = createJButton(actionCommand = "delegati")
            val buttonC = createJButton(actionCommand = "slumming")

            group.add(buttonA)
            group.add(buttonB)
            group.add(buttonC)

            GuiActionRunner.execute { group.setValue("delegati") }

            assertThat(buttonB.isSelected).isTrue()
        }

        it("selects exactly one button if multiple buttons have the given action command") {
            val buttonA = createJButton(actionCommand = "launch")
            val buttonB = createJButton(actionCommand = "launch")
            val buttonC = createJButton(actionCommand = "chimakum")

            group.add(buttonA)
            group.add(buttonB)
            group.add(buttonC)

            GuiActionRunner.execute { group.setValue("launch") }

            assertThat(buttonA.isSelected).isNotEqualTo(buttonB.isSelected)
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
