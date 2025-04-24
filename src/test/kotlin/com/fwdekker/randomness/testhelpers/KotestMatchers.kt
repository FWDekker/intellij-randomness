package com.fwdekker.randomness.testhelpers

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.IconDescriptor
import com.fwdekker.randomness.State
import com.fwdekker.randomness.matchesFormat
import io.kotest.assertions.withClue
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.beEmptyArray
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import javax.swing.Icon


/**
 * @see beEmptyArray
 */
fun beEmptyIntArray() = Matcher<IntArray> { self -> beEmptyArray<Int>().test(self.toTypedArray()) }

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


/**
 * Matches two [Icon]s against each other based on how they render, regardless of internal structure.
 */
fun beSameIconAs(other: Icon?): Matcher<Icon?> =
    Matcher { self ->
        if (self == other)
            return@Matcher MatcherResult(true, { "icons are both null" }, { "either icon is not null" })
        if (self == null || other == null)
            return@Matcher MatcherResult(false, { "actual icon is not null" }, { "actual icon is null" })
        if (self.iconWidth != other.iconWidth || self.iconHeight != other.iconHeight)
            return@Matcher MatcherResult(false, { "icons have different sizes" }, { "icons have the same size" })

        val image1 = self.render()
        val image2 = other.render()

        val rgb1 = image1.getRGB(0, 0, image1.width, image1.height, null, 0, image1.height)
        val rgb2 = image2.getRGB(0, 0, image2.width, image2.height, null, 0, image2.height)

        MatcherResult(
            rgb1.contentEquals(rgb2),
            { "icons do not render as the same image" },
            { "icons render as the same image" },
        )
    }

/**
 * Infix version of [beSameIconAs].
 */
infix fun <I : Icon?> I.shouldBeSameIconAs(other: Icon?): I {
    this should beSameIconAs(other)
    return this
}

/**
 * Alternative version for [IconDescriptor]s.
 */
fun beSameIconAs(other: IconDescriptor?): Matcher<IconDescriptor?> =
    Matcher { self -> beSameIconAs(other?.get()).test(self?.get()) }

/**
 * Infix version of [beSameIconAs].
 */
infix fun <I : IconDescriptor?> I.shouldBeSameIconAs(other: IconDescriptor?): I {
    this should beSameIconAs(other)
    return this
}
