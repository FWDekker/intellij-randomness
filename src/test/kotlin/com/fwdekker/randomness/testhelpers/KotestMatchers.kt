package com.fwdekker.randomness.testhelpers

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.State
import com.fwdekker.randomness.matchesFormat
import io.kotest.assertions.withClue
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.beEmptyArray
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should


/**
 * @see beEmptyArray
 */
fun beEmptyIntArray() = Matcher<IntArray> { beEmptyArray<Int>().test(it.toTypedArray()) }

/**
 * @see shouldContainExactly
 */
infix fun IntArray.shouldContainExactly(collection: Array<Int>) =
    this.toTypedArray() shouldContainExactly collection


/**
 * Matches a nullable string against the [Bundle] entry at [key] formatted with [args].
 *
 * @see matchesFormat
 * @throws java.util.MissingFormatArgumentException if [args] has fewer arguments than required for [format]
 */
fun matchBundle(key: String, vararg args: String): Matcher<String?> =
    Matcher { string ->
        val format = Bundle(key)
        MatcherResult(
            string != null && (string == format || string.matchesFormat(format, *args)),
            { "string was '$string' but should have matched format '$format'" },
            { "string was '$string' and should not have matched format '$format'" },
        )
    }


/**
 * Matches [State.doValidate] against the [Bundle] entry at [key], based on [matchBundle] (without `args`).
 *
 * If [key] is `null`, the [State] should be valid. If [key] is the empty string, the [State] should be invalid for
 * any reason. Otherwise, the [State] should be invalid for the specified reason.
 *
 * @see matchesFormat
 */
fun validateAsBundle(key: String?): Matcher<State> =
    Matcher { scheme ->
        when (key) {
            null -> withClue("Should be valid") { beNull().test(scheme.doValidate()) }
            "" -> withClue("Should be invalid with any message") { beNull().invert().test(scheme.doValidate()) }
            else -> withClue("Should be invalid with specific message") { matchBundle(key).test(scheme.doValidate()) }
        }
    }

/**
 * Infix version of [validateAsBundle].
 */
infix fun State.shouldValidateAsBundle(key: String?): State {
    this should validateAsBundle(key)
    return this
}
