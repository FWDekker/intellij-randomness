package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupActionTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [UuidInsertAction].
 */
class UuidInsertActionTest : Spek({
    describe("generateString") {
        data class Param(
            val version: Int,
            val enclosure: String,
            val capitalization: CapitalizationMode,
            val addDashes: Boolean
        )

        listOf(
            Param(1, "", CapitalizationMode.LOWER, true),
            Param(4, "'", CapitalizationMode.LOWER, true),
            Param(1, "Eglzfpf5", CapitalizationMode.LOWER, true),
            Param(4, "", CapitalizationMode.UPPER, true),
            Param(1, "'", CapitalizationMode.UPPER, false)
        ).forEach { (version, enclosure, capitalization, addDashes) ->
            it("generates a formatted UUID") {
                val uuidScheme = UuidScheme(
                    version = version,
                    enclosure = enclosure,
                    capitalization = capitalization,
                    addDashes = addDashes
                )

                val insertRandomUuid = UuidInsertAction(uuidScheme)
                val generatedString = insertRandomUuid.generateString()

                val alphabet = capitalization.transform("0-9a-fA-F")
                val dash = if (addDashes) "-" else ""

                assertThat(
                    Regex(
                        "" +
                            "^$enclosure" +
                            "[$alphabet]{8}$dash" +
                            "[$alphabet]{4}$dash" +
                            "[$alphabet]{4}$dash" +
                            "[$alphabet]{4}$dash" +
                            "[$alphabet]{12}" +
                            "$enclosure$"
                    ).matches(generatedString)
                ).isTrue()
            }
        }

        it("throws an exception if an invalid UUID version is given") {
            Assertions.assertThatThrownBy { UuidInsertAction(UuidScheme(version = 9)).generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("Unknown UUID version `9`.")
        }
    }
})


/**
 * Unit tests for [UuidGroupAction].
 */
class UuidGroupActionTest : DataGroupActionTest({ UuidGroupAction() })
