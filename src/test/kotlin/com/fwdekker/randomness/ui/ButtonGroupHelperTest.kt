package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.GuiActionRunner
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.NoSuchElementException
import javax.swing.ButtonGroup
import javax.swing.JButton


/**
 * Unit tests for [ButtonGroupHelper].
 */
object ButtonGroupHelperTest : Spek({
    lateinit var group: ButtonGroup


    beforeEachTest {
        group = ButtonGroup()
    }


    describe("for each") {
        it("iterates 0 times over an empty group") {
            var sum = 0
            ButtonGroupHelper.forEach(group) { sum++ }

            assertThat(sum).isEqualTo(0)
        }

        it("iterates once for each button in a group") {
            group.add(createJButton())
            group.add(createJButton())
            group.add(createJButton())

            var sum = 0
            ButtonGroupHelper.forEach(group) { sum++ }

            assertThat(sum).isEqualTo(3)
        }
    }

    describe("get value") {
        it("returns null if the group is empty") {
            assertThat(ButtonGroupHelper.getValue(group)).isNull()
        }

        it("returns null if no button is selected") {
            val button = createJButton(actionCommand = "shahid")

            group.add(button)

            assertThat(ButtonGroupHelper.getValue(group)).isNull()
        }

        it("returns an empty string if the selected button does not have an action command") {
            val button = createJButton(isSelected = true)

            group.add(button)

            assertThat(ButtonGroupHelper.getValue(group)).isEmpty()
        }

        it("returns the action command of the selected button") {
            val buttonA = createJButton(actionCommand = "causing")
            val buttonB = createJButton(actionCommand = "spahees", isSelected = true)

            group.add(buttonA)
            group.add(buttonB)

            assertThat(ButtonGroupHelper.getValue(group)).isEqualTo("spahees")
        }
    }

    describe("set value") {
        it("throws an exception if the group is empty") {
            assertThatThrownBy { ButtonGroupHelper.setValue(group, "gobbin") }
                .isInstanceOf(NoSuchElementException::class.java)
                .hasMessage("Could not find a button with action command `gobbin`.")
        }

        it("throws an exception if there is no button with the action command") {
            val buttonA = createJButton(actionCommand = "phocean")
            val buttonB = createJButton(actionCommand = "pouffe")

            group.add(buttonA)
            group.add(buttonB)

            assertThatThrownBy { ButtonGroupHelper.setValue(group, "cherty") }
                .isInstanceOf(NoSuchElementException::class.java)
                .hasMessage("Could not find a button with action command `cherty`.")
        }

        it("selects the button with the given action command") {
            val buttonA = createJButton(actionCommand = "claps")
            val buttonB = createJButton(actionCommand = "delegati")
            val buttonC = createJButton(actionCommand = "slumming")

            group.add(buttonA)
            group.add(buttonB)
            group.add(buttonC)

            GuiActionRunner.execute { ButtonGroupHelper.setValue(group, "delegati") }

            assertThat(buttonB.isSelected).isTrue()
        }
    }
})


private fun createJButton(actionCommand: String? = null, isSelected: Boolean = false) =
    GuiActionRunner.execute<JButton> {
        val button = JButton()
        button.actionCommand = actionCommand
        button.isSelected = isSelected
        button
    }
