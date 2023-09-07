package com.fwdekker.randomness.datetime

import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.shouldValidateAsBundle
import com.fwdekker.randomness.stateDeepCopyTestFactory
import io.kotest.assertions.withClue
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe


/**
 * Unit tests for [DateTimeScheme].
 */
object DateTimeSchemeTest : FunSpec({
    tags(NamedTag("Scheme"))


    context("generateStrings") {
        withData(
            mapOf(
                "returns date at given timestamp" to
                    // TODO
                    row(DateTimeScheme().withDateTime(45_582L), "TODO"),
                "returns date with given format" to
                    // TODO
                    row(DateTimeScheme(pattern = "yyyy.MM").withDateTime(242_966_670L), "TODO.TD"),
            )
        ) { (scheme, output) -> scheme.generateStrings()[0] shouldBe output }

        test("correctly generates distinct values at maximum range size") {
            val scheme = IntegerScheme(minValue = Long.MIN_VALUE, maxValue = Long.MAX_VALUE)

            withClue("Should have distinct elements") { scheme.generateStrings(count = 50).toSet() shouldHaveSize 50 }
        }
    }

    context("doValidate") {
        withData(
            mapOf(
                "succeeds for default state" to
                    row(DateTimeScheme(), null),
                "fails if min date-time is above max date-time" to
                    row(DateTimeScheme(minDateTime = 531L, maxDateTime = 38L), "datetime.error.min_datetime_above_max"),
                "fails if pattern is invalid" to
                    row(DateTimeScheme(pattern = "yyyy-ffff"), "Unknown pattern letter: f"),
                "succeeds if invalid pattern is escaped" to
                    row(DateTimeScheme(pattern = "yyyy-'ffff'"), null),
            )
        ) { (scheme, validation) -> scheme shouldValidateAsBundle validation }
    }

    include(stateDeepCopyTestFactory { DateTimeScheme() })
})


/**
 * Sets the [DateTimeScheme.minDateTime] and [DateTimeScheme.maxDateTime] to [dateTime].
 */
private fun DateTimeScheme.withDateTime(dateTime: Long): DateTimeScheme {
    minDateTime = dateTime
    maxDateTime = dateTime
    return this
}
