package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
            group.add(JButton())
            group.add(JButton())
            group.add(JButton())

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
            val button = JButton()
            button.actionCommand = "shahid"

            group.add(JButton())

            assertThat(ButtonGroupHelper.getValue(group)).isNull()
        }

        it("returns an empty string if the selected button does not have an action command") {
            val button = JButton()
            button.isSelected = true

            group.add(button)

            assertThat(ButtonGroupHelper.getValue(group)).isEmpty()
        }

        it("returns the action command of the selected button") {
            val buttonA = JButton()
            buttonA.actionCommand = "causing"
            val buttonB = JButton()
            buttonB.isSelected = true
            buttonB.actionCommand = "spahees"

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
            val buttonA = JButton()
            buttonA.actionCommand = "phocean"
            val buttonB = JButton()
            buttonB.actionCommand = "pouffe"

            group.add(buttonA)
            group.add(buttonB)

            assertThatThrownBy { ButtonGroupHelper.setValue(group, "cherty") }
                .isInstanceOf(NoSuchElementException::class.java)
                .hasMessage("Could not find a button with action command `cherty`.")
        }

        it("selects the button with the given action command") {
            val buttonA = JButton()
            buttonA.actionCommand = "claps"
            val buttonB = JButton()
            buttonB.actionCommand = "delegati"
            val buttonC = JButton()
            buttonC.actionCommand = "slumming"

            group.add(buttonA)
            group.add(buttonB)
            group.add(buttonC)

            ButtonGroupHelper.setValue(group, "delegati")

            assertThat(buttonB.isSelected).isTrue()
        }
    }
})
