package com.fwdekker.randomness.string

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [SymbolSet].
 */
object SymbolSetTest : Spek({
    describe("name") {
        it("returns the name of a symbol set") {
            assertThat(SymbolSet.ALPHABET.name).isEqualTo("Alphabet (a, b, c, ...)")
        }
    }

    describe("symbols") {
        it("returns the symbols of a symbol set") {
            assertThat(SymbolSet.ALPHABET.symbols).isEqualTo("abcdefghijklmnopqrstuvwxyz")
        }
    }

    describe("toString") {
        it("returns the name as the string representation of a symbol set") {
            assertThat(SymbolSet.ALPHABET.toString()).isEqualTo("Alphabet (a, b, c, ...)")
        }
    }

    describe("utility methods") {
        describe("sum") {
            it("does nothing when a symbol set is added to itself") {
                assertThat(listOf(SymbolSet.SPECIAL, SymbolSet.SPECIAL).sum())
                    .isEqualTo(SymbolSet.SPECIAL.symbols.toList().map { it.toString() })
            }

            it("adds the symbols of three symbol sets together") {
                assertThat(listOf(SymbolSet.DIGITS, SymbolSet.MINUS, SymbolSet.SPECIAL).sum())
                    .isEqualTo("0123456789-!@#\$%^&*".toList().map { it.toString() })
            }

            it("does not add symbols that are already in the accumulator") {
                assertThat(listOf(SymbolSet("set1", "abc"), SymbolSet("set2", "cde")).sum())
                    .isEqualTo("abcde".toList().map { it.toString() })
            }

            it("does not add symbols that are duplicated in a symbol set") {
                assertThat(listOf(SymbolSet("set1", "abc"), SymbolSet("set2", "ddeef")).sum())
                    .isEqualTo("abcdef".toList().map { it.toString() })
            }

            it("removes look-alike symbols if the option is given") {
                assertThat(listOf(SymbolSet("set", "a" + SymbolSet.lookAlikeCharacters)).sum(true))
                    .isEqualTo("a".toList().map { it.toString() })
            }

            it("retains emoji with modifiers") {
                assertThat(listOf(SymbolSet("emoji", "a👨‍💼b")).sum())
                    .isEqualTo(listOf("👨‍💼", "a", "b"))
            }

            it("retains duplicate characters in emoji") {
                assertThat(listOf(SymbolSet("emoji", "🇦🇶🇦")).sum())
                    .isEqualTo(listOf("🇦🇶", "🇦"))
            }

            it("removes duplicate emoji") {
                assertThat(listOf(SymbolSet("emoji", "🇦🇶😀🇦🇶😀")).sum())
                    .isEqualTo(listOf("🇦🇶", "😀"))
            }
        }
    }
})
