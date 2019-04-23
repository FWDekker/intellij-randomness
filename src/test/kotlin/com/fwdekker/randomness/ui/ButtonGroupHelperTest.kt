package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.NoSuchElementException
import javax.swing.ButtonGroup
import javax.swing.JButton


/**
 * Unit tests for [ButtonGroupHelper].
 */
class ButtonGroupHelperTest {
    private lateinit var group: ButtonGroup


    @BeforeEach
    fun beforeEach() {
        group = ButtonGroup()
    }


    @Test
    fun testForEachEmpty() {
        val sum = intArrayOf(0)

        ButtonGroupHelper.forEach(group) { sum[0]++ }

        assertThat(sum[0]).isEqualTo(0)
    }

    @Test
    fun testForEach() {
        val buttonA = JButton()
        val buttonB = JButton()
        val buttonC = JButton()

        group.add(buttonA)
        group.add(buttonB)
        group.add(buttonC)

        val sum = intArrayOf(0)

        ButtonGroupHelper.forEach(group) { sum[0]++ }

        assertThat(sum[0]).isEqualTo(3)
    }


    @Test
    fun testGetValueEmpty() {
        assertThat(ButtonGroupHelper.getValue(group)).isNull()
    }

    @Test
    fun testGetValueNoneSelected() {
        val button = JButton()

        group.add(button)

        assertThat(ButtonGroupHelper.getValue(group)).isNull()
    }

    @Test
    fun testGetValue() {
        val buttonA = JButton()
        buttonA.actionCommand = "29zo4"
        val buttonB = JButton()
        buttonB.isSelected = true
        buttonB.actionCommand = "Y6ddy"

        group.add(buttonA)
        group.add(buttonB)

        assertThat(ButtonGroupHelper.getValue(group)).isEqualTo("Y6ddy")
    }


    @Test
    fun testSetValueEmpty() {
        assertThatThrownBy { ButtonGroupHelper.setValue(group, "syWR#") }
            .isInstanceOf(NoSuchElementException::class.java)
            .hasMessage("Could not find a button with action command `syWR#`.")
    }

    @Test
    fun testSetValueNotFound() {
        val buttonA = JButton()
        buttonA.actionCommand = "*VgyA"
        val buttonB = JButton()
        buttonB.actionCommand = "s8vOP"

        group.add(buttonA)
        group.add(buttonB)

        assertThatThrownBy { ButtonGroupHelper.setValue(group, "OD>5&") }
            .isInstanceOf(NoSuchElementException::class.java)
            .hasMessage("Could not find a button with action command `OD>5&`.")
    }

    @Test
    fun testSetValue() {
        val buttonA = JButton()
        buttonA.actionCommand = "TRUaN"
        val buttonB = JButton()
        buttonB.actionCommand = "2Y@2_"
        val buttonC = JButton()
        buttonC.actionCommand = "#Oq%n"

        group.add(buttonA)
        group.add(buttonB)
        group.add(buttonC)

        ButtonGroupHelper.setValue(group, "2Y@2_")

        assertThat(buttonB.isSelected).isTrue()
    }

    @Test
    fun testSetValueObject() {
        val buttonA = JButton()
        buttonA.actionCommand = "iqGfVwJDLd"
        val buttonB = JButton()
        buttonB.actionCommand = "ouzioKGsKi"
        val buttonC = JButton()
        buttonC.actionCommand = "pKVEAoQzmr"

        group.add(buttonA)
        group.add(buttonB)
        group.add(buttonC)

        ButtonGroupHelper.setValue(group, "ouzioKGsKi")

        assertThat(buttonB.isSelected).isTrue()
    }
}
