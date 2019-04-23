package com.fwdekker.randomness.string

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


/**
 * Unit tests for [Alphabet].
 */
class AlphabetTest {
    @Test
    fun testConcatenate() {
        val alphabets = listOf(Alphabet.ALPHABET, Alphabet.MINUS, Alphabet.SPECIAL)

        assertThat(Alphabet.concatenate(alphabets)).isEqualTo("abcdefghijklmnopqrstuvwxyz-!@#$%^&*")
    }


    @Test
    fun testGetDescription() {
        assertThat(Alphabet.ALPHABET.description).isEqualTo("Alphabet (a, b, c, ...)")
    }

    @Test
    fun testGetSymbols() {
        assertThat(Alphabet.ALPHABET.symbols).isEqualTo("abcdefghijklmnopqrstuvwxyz")
    }

    @Test
    fun testToString() {
        assertThat(Alphabet.ALPHABET.toString()).isEqualTo("Alphabet (a, b, c, ...)")
    }
}
