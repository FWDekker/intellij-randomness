package com.fwdekker.randomness.string

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


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
        describe("toMap") {
            it("converts an empty list to an empty map") {
                val symbolSets = emptyList<SymbolSet>()

                assertThat(symbolSets.toMap()).isEmpty()
            }

            it("converts a single symbol set to a single map entry") {
                val symbolSets = listOf(SymbolSet("name", "abc"))

                assertThat(symbolSets.toMap())
                    .hasSize(1)
                    .containsAllEntriesOf(mapOf("name" to "abc"))
            }

            it("collapses symbol sets with the same name") {
                val symbolSets = listOf(SymbolSet("name", "abc"), SymbolSet("name", "def"))

                assertThat(symbolSets.toMap()).hasSize(1)
            }

            it("retains the order of the symbol sets") {
                val symbolSets = listOf(SymbolSet.ALPHABET, SymbolSet.DIGITS)

                assertThat(symbolSets.toMap().toSymbolSets())
                    .containsExactlyElementsOf(symbolSets.reversed().toMap().toSymbolSets().reversed())
            }
        }

        describe("toSymbolSets") {
            it("converts an empty map to an empty list") {
                val map = emptyMap<String, String>()

                assertThat(map.toSymbolSets()).isEmpty()
            }

            it("converts a single map entry to a single symbol set") {
                val map = mapOf("name" to "abc")

                assertThat(map.toSymbolSets()).containsExactly(SymbolSet("name", "abc"))
            }
        }

        describe("sum") {
            it("does nothing when a symbol set is added to itself") {
                assertThat(listOf(SymbolSet.SPECIAL, SymbolSet.SPECIAL).sum()).isEqualTo(SymbolSet.SPECIAL.symbols)
            }

            it("adds the symbols of three symbol sets together") {
                assertThat(listOf(SymbolSet.DIGITS, SymbolSet.MINUS, SymbolSet.SPECIAL).sum())
                    .isEqualTo("0123456789-!@#\$%^&*")
            }

            it("does not add symbols that are already in the accumulator") {
                assertThat(listOf(SymbolSet("set1", "abc"), SymbolSet("set2", "cde")).sum()).isEqualTo("abcde")
            }

            it("does not add symbols that are duplicated in a symbol set") {
                assertThat(listOf(SymbolSet("set1", "abc"), SymbolSet("set2", "ddeef")).sum()).isEqualTo("abcdef")
            }

            it("removes look-alike symbols if the option is given") {
                assertThat(listOf(SymbolSet("set", "a" + SymbolSet.lookAlikeCharacters)).sum(true)).isEqualTo("a")
            }
        }
    }
})
