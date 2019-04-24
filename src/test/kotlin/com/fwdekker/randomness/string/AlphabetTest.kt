package com.fwdekker.randomness.string

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * Unit tests for [Alphabet].
 */
object AlphabetTest : Spek({
    describe("sum") {
        it("does nothing when an alphabet is added to itself") {
            assertThat(listOf(Alphabet.SPECIAL, Alphabet.SPECIAL).sum()).isEqualTo(Alphabet.SPECIAL.symbols)
        }

        it("does nothing when a subset is added to an alphabet") {
            assertThat(listOf(Alphabet.HEXADECIMAL, Alphabet.DIGITS).sum()).isEqualTo(Alphabet.HEXADECIMAL.symbols)
        }

        it("adds the symbols of three alphabets together") {
            assertThat(listOf(Alphabet.DIGITS, Alphabet.MINUS, Alphabet.SPECIAL).sum())
                .isEqualTo("0123456789-!@#\$%^&*")
        }
    }

    describe("description") {
        it("returns the description of an alphabet") {
            assertThat(Alphabet.ALPHABET.description).isEqualTo("Alphabet (a, b, c, ...)")
        }
    }

    describe("symbols") {
        it("returns the symbols of an alphabet") {
            assertThat(Alphabet.ALPHABET.symbols).isEqualTo("abcdefghijklmnopqrstuvwxyz")
        }
    }

    describe("toString") {
        it("returns the description as the string representation of an alphabet") {
            assertThat(Alphabet.ALPHABET.toString()).isEqualTo("Alphabet (a, b, c, ...)")
        }
    }
})
