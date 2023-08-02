package com.fwdekker.randomness.datetime

import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.shouldValidateAsBundle
import io.kotest.assertions.withClue
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe


/**
 * Unit tests for [DateTimeScheme].
 */
object DateTimeSchemeTest : FunSpec({
    tags(NamedTag("Scheme"))


    test("generateStrings") {
        test("parameterized") {
            forAll(
                table(
                    //@formatter:off
                    headers("description", "scheme", "output"),
                    // TODO
                    row("returns date at given timestamp", DateTimeScheme(minDateTime = 45_582L, maxDateTime = 45_582L), "TODO"),
                    // TODO
                    row("returns date with given format", DateTimeScheme(minDateTime = 242_966_670L, maxDateTime = 242_966_670L, pattern = "yyyy.MM"), "TODO"),
                    //@formatter:on
                )
            ) { _, scheme, output -> scheme.generateStrings()[0] shouldBe output }
        }

        test("correctly generates distinct values at maximum range size") {
            val scheme = IntegerScheme(minValue = Long.MIN_VALUE, maxValue = Long.MAX_VALUE)

            withClue("Should have distinct elements") { scheme.generateStrings(count = 50).toSet() shouldHaveSize 50 }
        }
    }

    test("doValidate") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "validation"),
                row("succeeds for default state", DateTimeScheme(), null),
                row("fails if min date-time is above max date-time", DateTimeScheme(minDateTime = 541L, maxDateTime = 318L), "datetime.error.min_datetime_above_max"),
                row("fails if pattern is invalid", DateTimeScheme(pattern = "yyyy-ffff"), "Unknown pattern letter: f"),
                row("succeeds if invalid pattern is escaped", DateTimeScheme(pattern = "yyyy-'ffff'"), null),
                //@formatter:on
            )
        ) { _, scheme, validation -> scheme shouldValidateAsBundle validation }
    }

    test("deepCopy") {
        lateinit var scheme: DateTimeScheme


        beforeEach {
            scheme = DateTimeScheme()
        }


        test("equals old instance") {
            scheme.deepCopy() shouldBe scheme
        }

        test("is independent of old instance") {
            val copy = scheme.deepCopy()

            scheme.pattern = "other"

            copy.pattern shouldNotBe scheme.pattern
        }

        test("retains uuid if chosen") {
            scheme.deepCopy(true).uuid shouldBe scheme.uuid
        }

        test("replaces uuid if chosen") {
            scheme.deepCopy(false).uuid shouldNotBe scheme.uuid
        }
    }
})
