package com.fwdekker.randomness.array

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


/**
 * Unit tests for [ArraySettings].
 */
class ArraySettingsTest {
    private lateinit var arraySettings: ArraySettings


    @BeforeEach
    fun beforeEach() {
        arraySettings = ArraySettings()
    }


    @Test
    fun testGetLoadState() {
        arraySettings.count = 997
        arraySettings.brackets = "0fWx<i6jTJ"
        arraySettings.separator = "f3hu)Rxiz1"
        arraySettings.isSpaceAfterSeparator = false

        val newArraySettings = ArraySettings()
        newArraySettings.loadState(arraySettings.state)

        assertThat(newArraySettings.count).isEqualTo(997)
        assertThat(newArraySettings.brackets).isEqualTo("0fWx<i6jTJ")
        assertThat(newArraySettings.separator).isEqualTo("f3hu)Rxiz1")
        assertThat(newArraySettings.isSpaceAfterSeparator).isEqualTo(false)
    }

    @Test
    fun testGetSetCount() {
        arraySettings.count = 655

        assertThat(arraySettings.count).isEqualTo(655)
    }

    @Test
    fun testGetSetBrackets() {
        arraySettings.brackets = "RLevljrzf0"

        assertThat(arraySettings.brackets).isEqualTo("RLevljrzf0")
    }

    @Test
    fun testGetSetSeparator() {
        arraySettings.brackets = "d2[tlXkGf{"

        assertThat(arraySettings.brackets).isEqualTo("d2[tlXkGf{")
    }

    @Test
    fun testGetSetSpaceAfterSeparator() {
        arraySettings.isSpaceAfterSeparator = false

        assertThat(arraySettings.isSpaceAfterSeparator).isEqualTo(false)
    }


    @Test
    fun testArrayifyEmpty() {
        assertThat(arraySettings.arrayify(emptyList())).isEqualTo("[]")
    }

    @Test
    fun testArrayify() {
        arraySettings.count = 4
        arraySettings.brackets = "@#"
        arraySettings.separator = ";;"
        arraySettings.isSpaceAfterSeparator = true

        assertThat(arraySettings.arrayify(listOf("Garhwali", "Pattypan", "Troll")))
            .isEqualTo("@Garhwali;; Pattypan;; Troll#")
    }

    @Test
    fun testArrayifyNoBrackets() {
        arraySettings.count = 8
        arraySettings.brackets = ""
        arraySettings.separator = "h"
        arraySettings.isSpaceAfterSeparator = false

        assertThat(arraySettings.arrayify(listOf("Antheia", "Cowbinds", "Cotutor")))
            .isEqualTo("AntheiahCowbindshCotutor")
    }
}
