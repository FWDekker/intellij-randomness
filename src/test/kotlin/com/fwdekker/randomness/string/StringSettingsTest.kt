package com.fwdekker.randomness.string

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


/**
 * Unit tests for [StringSettings].
 */
internal class StringSettingsTest {
    private lateinit var stringSettings: StringSettings


    @BeforeEach
    fun beforeEach() {
        stringSettings = StringSettings()
    }


    @Test
    fun testGetLoadState() {
        val alphabets = mutableSetOf<Alphabet>()

        stringSettings.minLength = 730
        stringSettings.maxLength = 891
        stringSettings.enclosure = "Qh7"
        stringSettings.alphabets = alphabets

        val newStringSettings = StringSettings()
        newStringSettings.loadState(stringSettings.state)

        assertThat(newStringSettings.minLength).isEqualTo(730)
        assertThat(newStringSettings.maxLength).isEqualTo(891)
        assertThat(newStringSettings.enclosure).isEqualTo("Qh7")
        assertThat(newStringSettings.alphabets).isEqualTo(alphabets)
    }

    @Test
    fun testGetSetMinLength() {
        stringSettings.minLength = 173

        assertThat(stringSettings.minLength).isEqualTo(173)
    }

    @Test
    fun testGetSetMaxLength() {
        stringSettings.maxLength = 421

        assertThat(stringSettings.maxLength).isEqualTo(421)
    }

    @Test
    fun testGetSetEnclosure() {
        stringSettings.enclosure = "hWD"

        assertThat(stringSettings.enclosure).isEqualTo("hWD")
    }

    @Test
    fun testGetSetAlphabets() {
        val alphabets = mutableSetOf(Alphabet.ALPHABET, Alphabet.BRACKETS, Alphabet.MINUS)

        stringSettings.alphabets = alphabets

        assertThat(stringSettings.alphabets).isEqualTo(alphabets)
    }
}
