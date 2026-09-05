package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.shouldValidateAsBundle
import com.fwdekker.randomness.testhelpers.stateDeepCopyTestFactory
import com.fwdekker.randomness.testhelpers.stateSerializationTestFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.tuple
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe


/**
 * Unit tests for [DecimalScheme].
 */
object DecimalSchemeTest : FunSpec({
    tags(Tags.PLAIN, Tags.SCHEME)


    context("generateStrings") {
        withTests(
            mapOf(
                "returns 0" to
                    tuple(DecimalScheme().withValue(0.0), "0"),
                "returns 1" to
                    tuple(DecimalScheme().withValue(1.0), "1"),
                "truncates non-zero decimals" to
                    tuple(DecimalScheme(decimalCount = 1).withValue(203.54), "203.5"),
                "hides trailing zeroes" to
                    tuple(DecimalScheme(decimalCount = 3, showTrailingZeroes = false).withValue(409.82), "409.82"),
                "adds trailing zeroes" to
                    tuple(DecimalScheme(decimalCount = 3, showTrailingZeroes = true).withValue(702.78), "702.780"),
                "uses grouping separator" to
                    tuple(
                        DecimalScheme(groupingSeparatorEnabled = true, groupingSeparator = "#").withValue(311_752.11),
                        "311#752.11",
                    ),
                "uses no grouping separator if disabled" to
                    tuple(
                        DecimalScheme(groupingSeparatorEnabled = false, groupingSeparator = "#").withValue(499_935.29),
                        "499935.29",
                    ),
                "uses decimal separator" to
                    tuple(
                        DecimalScheme(decimalSeparator = "#").withValue(335_328.52),
                        "335328#52",
                    ),
                "applies decorators in order affix, array" to
                    tuple(
                        DecimalScheme(
                            affixDecorator = AffixDecorator(enabled = true, descriptor = "@f"),
                            arrayDecorator = ArrayDecorator(enabled = true),
                        ).withValue(735.77),
                        "[735.77f, 735.77f, 735.77f]",
                    ),
            )
        ) { (scheme, output) -> scheme.generateStrings()[0] shouldBe output }
    }

    context("doValidate") {
        withTests(
            mapOf(
                "succeeds for default state" to
                    tuple(DecimalScheme(), null),
                "fails if min value is above max value" to
                    tuple(DecimalScheme(minValue = 674.58, maxValue = 218.14), "decimal.error.min_value_above_max"),
                "fails if range size overflows" to
                    tuple(DecimalScheme(minValue = -1E53, maxValue = 1E53), "decimal.error.value_range"),
                "succeeds if decimal count is zero" to
                    tuple(DecimalScheme(decimalCount = 0), null),
                "fails if decimal count is negative" to
                    tuple(DecimalScheme(decimalCount = -3), "decimal.error.decimal_count_too_low"),
                "fails if decimal separator is empty" to
                    tuple(DecimalScheme(decimalSeparator = ""), "decimal.error.decimal_separator_length"),
                "fails if decimal separator is non-char" to
                    tuple(DecimalScheme(decimalSeparator = "long"), "decimal.error.decimal_separator_length"),
                "fails if grouping separator is empty" to
                    tuple(DecimalScheme(groupingSeparator = ""), "decimal.error.grouping_separator_length"),
                "fails if grouping separator is non-char" to
                    tuple(DecimalScheme(groupingSeparator = "long"), "decimal.error.grouping_separator_length"),
                "fails if affix decorator is invalid" to
                    tuple(DecimalScheme(affixDecorator = AffixDecorator(enabled = true, descriptor = """\""")), ""),
                "fails if array decorator is invalid" to
                    tuple(DecimalScheme(arrayDecorator = ArrayDecorator(enabled = true, minCount = -98)), ""),
            )
        ) { (scheme, validation) -> scheme shouldValidateAsBundle validation }
    }

    include(stateDeepCopyTestFactory { DecimalScheme() })

    include(stateSerializationTestFactory { DecimalScheme() })
})


/**
 * Sets the [DecimalScheme.minValue] and [DecimalScheme.maxValue] to [value].
 */
private fun DecimalScheme.withValue(value: Double): DecimalScheme {
    minValue = value
    maxValue = value
    return this
}
