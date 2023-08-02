package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.shouldValidateAsBundle
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe


/**
 * Unit tests for [DecimalScheme].
 */
object DecimalSchemeTest : FunSpec({
    tags(NamedTag("Scheme"))


    test("generateStrings") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "output"),
                row("returns 0", DecimalScheme().withValue(0.0), "0"),
                row("returns 1", DecimalScheme().withValue(1.0), "1"),
                row("truncates non-zero decimals", DecimalScheme(decimalCount = 1).withValue(203.54), "203.5"),
                row("hides trailing zeroes", DecimalScheme(decimalCount = 3, showTrailingZeroes = false).withValue(409.82), "409.82"),
                row("adds trailing zeroes", DecimalScheme(decimalCount = 3, showTrailingZeroes = true).withValue(702.78), "702.780"),
                row("uses grouping separator", DecimalScheme(groupingSeparator = "#").withValue(311_752.11), "311#752.11"),
                row("uses no grouping separator if disabled", DecimalScheme(groupingSeparatorEnabled = false, groupingSeparator = "#").withValue(499_935.29), "499935.29"),
                row("uses decimal separator", DecimalScheme(decimalSeparator = "#").withValue(335_328.52), "335328#52"),
                row("applies decorators in order affix, array", DecimalScheme(affixDecorator = AffixDecorator(enabled = true, descriptor = "@f"), arrayDecorator = ArrayDecorator(enabled = true)).withValue(735.77), "[735.77f, 735.77f, 735.77f]"),
                //@formatter:on
            )
        ) { _, scheme, output -> scheme.generateStrings()[0] shouldBe output }
    }

    test("doValidate") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "validation"),
                row("succeeds for default state", DecimalScheme(), null),
                row("fails if min value is above max value", DecimalScheme(minValue = 674.58, maxValue = 218.14), "decimal.error.min_value_above_max"),
                row("fails if range size overflows", DecimalScheme(minValue = -1E53, maxValue = 1E53), "decimal.error.value_range"),
                row("succeeds if decimal count is zero", DecimalScheme(decimalCount = 0), null),
                row("fails if decimal count is negative", DecimalScheme(decimalCount = -3), "decimal.error.decimal_count_too_low"),
                row("fails if decimal separator is empty", DecimalScheme(decimalSeparator = ""), "decimal.error.decimal_separator_length"),
                row("fails if decimal separator is non-char", DecimalScheme(decimalSeparator = "long"), "decimal.error.decimal_separator_length"),
                row("fails if grouping separator is empty", DecimalScheme(groupingSeparator = ""), "decimal.error.grouping_separator_length"),
                row("fails if grouping separator is non-char", DecimalScheme(groupingSeparator = "long"), "decimal.error.grouping_separator_length"),
                row("fails if affix decorator is invalid", DecimalScheme(affixDecorator = AffixDecorator(descriptor = """\""")), ""),
                row("fails if array decorator is invalid", DecimalScheme(arrayDecorator = ArrayDecorator(minCount = -98)), ""),
                //@formatter:on
            )
        ) { _, scheme, validation -> scheme shouldValidateAsBundle validation }
    }

    test("deepCopy") {
        lateinit var scheme: DecimalScheme


        beforeEach {
            scheme = DecimalScheme()
        }


        test("equals old instance") {
            scheme.deepCopy() shouldBe scheme
        }

        test("is independent of old instance") {
            val copy = scheme.deepCopy()

            scheme.decimalSeparator = "other"

            copy.decimalSeparator shouldNotBe scheme.decimalSeparator
        }

        test("retains uuid if chosen") {
            scheme.deepCopy(true).uuid shouldBe scheme.uuid
        }

        test("replaces uuid if chosen") {
            scheme.deepCopy(false).uuid shouldNotBe scheme.uuid
        }
    }
})


/**
 * Sets the [DecimalScheme.minValue] and [DecimalScheme.maxValue] to [value].
 *
 * @receiver the scheme to set the minimum and maximum value on
 * @param value the value to set
 * @return `this`
 */
private fun DecimalScheme.withValue(value: Double): DecimalScheme {
    minValue = value
    maxValue = value
    return this
}
