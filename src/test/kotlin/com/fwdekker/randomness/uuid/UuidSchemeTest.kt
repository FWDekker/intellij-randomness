package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.random.Random


/**
 * Unit tests for [UuidScheme].
 */
object UuidSchemeTest : Spek({
    lateinit var uuidScheme: UuidScheme


    beforeEachTest {
        uuidScheme = UuidScheme()
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            uuidScheme.version = 9

            assertThatThrownBy { uuidScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }


        describe("format") {
            data class Param(
                val version: Int,
                val quotation: String,
                val capitalization: CapitalizationMode,
                val addDashes: Boolean
            )

            listOf(
                Param(1, "", CapitalizationMode.LOWER, true),
                Param(4, "'", CapitalizationMode.LOWER, true),
                Param(1, "E", CapitalizationMode.LOWER, true),
                Param(4, "", CapitalizationMode.UPPER, true),
                Param(1, "'", CapitalizationMode.UPPER, false)
            ).forEach { (version, quotation, capitalization, addDashes) ->
                it("generates a formatted UUID") {
                    uuidScheme.version = version
                    uuidScheme.quotation = quotation
                    uuidScheme.capitalization = capitalization
                    uuidScheme.addDashes = addDashes

                    val alphabet = capitalization.transform("0-9a-fA-F")
                    val dash = if (addDashes) "-" else ""

                    assertThat(
                        Regex(
                            "" +
                                "^$quotation" +
                                "[$alphabet]{8}$dash" +
                                "[$alphabet]{4}$dash" +
                                "[$alphabet]{4}$dash" +
                                "[$alphabet]{4}$dash" +
                                "[$alphabet]{12}" +
                                "$quotation$"
                        ).matches(uuidScheme.generateStrings().single())
                    ).isTrue()
                }
            }
        }

        describe("quotation") {
            it("adds no quotations if the quotations are an empty string") {
                uuidScheme.quotation = ""
                uuidScheme.random = Random(512_615)
                val uuid = uuidScheme.generateStrings().single()

                uuidScheme.quotation = ""
                uuidScheme.random = Random(512_615)
                assertThat(uuidScheme.generateStrings().single()).isEqualTo(uuid)
            }

            it("repeats the first character of the quotations on both ends") {
                uuidScheme.quotation = ""
                uuidScheme.random = Random(47_334)
                val uuid = uuidScheme.generateStrings().single()

                uuidScheme.quotation = "r"
                uuidScheme.random = Random(47_334)
                assertThat(uuidScheme.generateStrings().single()).isEqualTo("r${uuid}r")
            }

            it("surrounds the output with the respective characters of the quotation string") {
                uuidScheme.quotation = ""
                uuidScheme.random = Random(908_309)
                val uuid = uuidScheme.generateStrings().single()

                uuidScheme.quotation = "bv"
                uuidScheme.random = Random(908_309)
                assertThat(uuidScheme.generateStrings().single()).isEqualTo("b${uuid}v")
            }
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(UuidScheme().doValidate()).isNull()
        }

        it("fails if the decorator is invalid") {
            uuidScheme.arrayDecorator.maxCount = -671

            assertThat(uuidScheme.doValidate()).isNotNull()
        }

        it("fails for an unsupported UUID version") {
            uuidScheme.version = 2

            assertThat(uuidScheme.doValidate()).isEqualTo("Unknown UUID version '2'.")
        }

        it("fails for a quotation string that has more than two characters") {
            uuidScheme.quotation = "police"

            assertThat(uuidScheme.doValidate()).isEqualTo("Quotation must be at most 2 characters.")
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            uuidScheme.version = 4
            uuidScheme.arrayDecorator.maxCount = 754

            val copy = uuidScheme.deepCopy()
            copy.version = 1
            copy.arrayDecorator.maxCount = 640

            assertThat(uuidScheme.version).isEqualTo(4)
            assertThat(uuidScheme.arrayDecorator.maxCount).isEqualTo(754)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            uuidScheme.version = 4
            uuidScheme.quotation = "nv"
            uuidScheme.customQuotation = "to"
            uuidScheme.capitalization = CapitalizationMode.FIRST_LETTER
            uuidScheme.addDashes = true
            uuidScheme.arrayDecorator.minCount = 264

            val newScheme = UuidScheme()
            newScheme.copyFrom(uuidScheme)

            assertThat(newScheme)
                .isEqualTo(uuidScheme)
                .isNotSameAs(uuidScheme)
            assertThat(newScheme.arrayDecorator)
                .isEqualTo(uuidScheme.arrayDecorator)
                .isNotSameAs(uuidScheme.arrayDecorator)
        }
    }
})
