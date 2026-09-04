package com.fwdekker.randomness.uuid

import com.fasterxml.uuid.impl.UUIDUtil.extractTimestamp
import com.fwdekker.randomness.Timestamp
import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.shouldValidateAsBundle
import com.fwdekker.randomness.testhelpers.stateDeepCopyTestFactory
import com.fwdekker.randomness.testhelpers.stateSerializationTestFactory
import com.fwdekker.randomness.uuid.UuidScheme.Companion.MAX_MAX_DATE_TIME
import com.fwdekker.randomness.uuid.UuidScheme.Companion.MIN_MIN_DATE_TIME
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.tuple
import io.kotest.datatest.withContexts
import io.kotest.datatest.withTests
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.beLowerCase
import io.kotest.matchers.string.beUpperCase
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.util.UUID


/**
 * Unit tests for [UuidScheme].
 */
object UuidSchemeTest : FunSpec({
    tags(Tags.PLAIN, Tags.SCHEME)


    /**
     * Utility method for generating a single UUID.
     *
     * @param version the version of UUID to generate
     * @param min the minimum date-time to use, or `null` to not set a minimum
     * @param max the maximum date-time to use, or `null` to not set a minimum; defaults to the [min] value
     */
    fun generateString(version: Int, min: Timestamp? = null, max: Timestamp? = min): String =
        UuidScheme(version = version)
            .apply {
                if (min != null) minDateTime = min
                if (max != null) maxDateTime = max
            }
            .generateStrings()[0]

    /**
     * Returns the [UUID] generates by [generateString].
     */
    fun generateUuid(version: Int, min: Timestamp? = null, max: Timestamp? = min): UUID =
        UUID.fromString(generateString(version, min, max))


    context("generateStrings") {
        context("generates a UUID for all supported versions") {
            withTests(UuidScheme.SUPPORTED_VERSIONS) { version ->
                generateUuid(version).version() shouldBe version
            }
        }

        context("uses the specified date-time") {
            withTests(UuidScheme.TIME_BASED_VERSIONS) { version ->
                val timestamp = Timestamp("2023-04-07 08:36:29")

                extractTimestamp(generateUuid(version, timestamp)) shouldBe timestamp.epochMilli
            }
        }

        context("generates in the specified date-time range") {
            withTests(UuidScheme.TIME_BASED_VERSIONS) { version ->
                repeat(100) {
                    val min = Timestamp("1975-11-25 22:38:21")
                    val max = Timestamp("2084-03-22 19:10:44")

                    val epoch = extractTimestamp(generateUuid(version, min, max))
                    epoch shouldBeGreaterThanOrEqualTo min.epochMilli!!
                    epoch shouldBeLessThanOrEqualTo max.epochMilli!!
                }
            }
        }

        context("generates at the minimum datetime, in 1970") {
            withTests(UuidScheme.TIME_BASED_VERSIONS) { version ->
                extractTimestamp(generateUuid(version, MIN_MIN_DATE_TIME)) shouldBe MIN_MIN_DATE_TIME.epochMilli
            }
        }

        context("generates at the maximum datetime, in 5236") {
            withTests(UuidScheme.TIME_BASED_VERSIONS) { version ->
                extractTimestamp(generateUuid(version, MAX_MAX_DATE_TIME)) shouldBe MAX_MAX_DATE_TIME.epochMilli
            }
        }

        context("generates datetimes in all centuries strictly between 1970 and 5236") {
            withContexts(UuidScheme.TIME_BASED_VERSIONS) { version ->
                withTests((20..51).associateBy { "Century $it" }) { century ->
                    val timestamp = Timestamp("${century}92-10-24 15:01:16")
                    extractTimestamp(generateUuid(version, timestamp)) shouldBe timestamp.epochMilli
                }
            }
        }

        test("returns uppercase string") {
            UuidScheme(isUppercase = true).generateStrings()[0] should beUpperCase()
        }

        test("returns lowercase string") {
            UuidScheme(isUppercase = false).generateStrings()[0] should beLowerCase()
        }

        test("returns string with dashes") {
            UuidScheme(addDashes = true).generateStrings()[0] shouldContain "-"
        }

        test("returns string without dashes") {
            UuidScheme(addDashes = false).generateStrings()[0] shouldNotContain "-"
        }

        test("applies decorators in order affix, array") {
            UuidScheme(
                affixDecorator = AffixDecorator(enabled = true, descriptor = "#@"),
                arrayDecorator = ArrayDecorator(enabled = true, minCount = 3, maxCount = 3),
            ).generateStrings()[0].count { it == '#' } shouldBe 3
        }
    }

    context("doValidate") {
        context("general validation") {
            withTests(
                mapOf(
                    "succeeds for default state" to
                        tuple(UuidScheme(), null),
                    "fails for unsupported version" to
                        tuple(UuidScheme(version = 14), "uuid.error.unknown_version"),
                    "fails if affix decorator is invalid" to
                        tuple(UuidScheme(affixDecorator = AffixDecorator(enabled = true, descriptor = """\""")), ""),
                    "fails if array decorator is invalid" to
                        tuple(UuidScheme(arrayDecorator = ArrayDecorator(enabled = true, minCount = -539)), ""),
                )
            ) { (scheme, validation) -> scheme shouldValidateAsBundle validation }
        }

        context("time-based validation") {
            withContexts(
                mapOf(
                    "fails for invalid min date-time" to
                        tuple(UuidScheme(minDateTime = Timestamp("invalid")), "timestamp.error.parse"),
                    "fails for invalid max date-time" to
                        tuple(UuidScheme(minDateTime = Timestamp("invalid")), "timestamp.error.parse"),
                    "fails if min date-time is before 1970" to
                        tuple(UuidScheme(minDateTime = Timestamp("1960")), "timestamp.error.too_old"),
                    "fails if max date-time is after 5236" to
                        tuple(UuidScheme(maxDateTime = Timestamp("5258")), "timestamp.error.too_new"),
                    "fails if min date-time is above max date-time" to
                        tuple(
                            UuidScheme(minDateTime = Timestamp("4157"), maxDateTime = Timestamp("3376")),
                            "uuid.error.min_datetime_above_max"
                        ),
                )
            ) { (scheme, validation) ->
                test("Should fail if time-based") {
                    scheme.version = 1
                    scheme shouldValidateAsBundle validation
                }

                test("Should ignore failure if not time-based") {
                    scheme.version = 4
                    scheme shouldValidateAsBundle null
                }
            }
        }
    }

    include(stateDeepCopyTestFactory { UuidScheme() })

    include(stateSerializationTestFactory { UuidScheme() })
})
