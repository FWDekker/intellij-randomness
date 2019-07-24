package com.fwdekker.randomness.string

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * Unit tests for [SymbolSet].
 */
object SymbolSetTest : Spek({
    describe("sum") {
        it("does nothing when a symbol set is added to itself") {
            assertThat(listOf(SymbolSet.SPECIAL, SymbolSet.SPECIAL).sum()).isEqualTo(SymbolSet.SPECIAL.symbols)
        }

        it("does nothing when a subset is added to a symbol set") {
            assertThat(listOf(SymbolSet.HEXADECIMAL, SymbolSet.DIGITS).sum()).isEqualTo(SymbolSet.HEXADECIMAL.symbols)
        }

        it("adds the symbols of three symbol sets together") {
            assertThat(listOf(SymbolSet.DIGITS, SymbolSet.MINUS, SymbolSet.SPECIAL).sum())
                .isEqualTo("0123456789-!@#\$%^&*")
        }
    }

    describe("description") {
        it("returns the description of a symbol set") {
            assertThat(SymbolSet.ALPHABET.description).isEqualTo("Alphabet (a, b, c, ...)")
        }
    }

    describe("symbols") {
        it("returns the symbols of a symbol set") {
            assertThat(SymbolSet.ALPHABET.symbols).isEqualTo("abcdefghijklmnopqrstuvwxyz")
        }
    }

    describe("toString") {
        it("returns the description as the string representation of a symbol set") {
            assertThat(SymbolSet.ALPHABET.toString()).isEqualTo("Alphabet (a, b, c, ...)")
        }
    }
})
