package com.fwdekker.randomness.integer

import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.fixedlength.FixedLengthDecorator
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
 * Unit tests for [IntegerScheme].
 */
object IntegerSchemeTest : FunSpec({
    tags(NamedTag("Scheme"))


    test("generateStrings") {
        test("parameterized") {
            forAll(
                table(
                    //@formatter:off
                    headers("description", "scheme", "output"),
                    row("returns zero", IntegerScheme().withValue(0L), "0"),
                    row("returns one", IntegerScheme().withValue(1L), "1"),
                    row("returns a negative value", IntegerScheme().withValue(-660L), "-660"),
                    row("returns a positive value", IntegerScheme().withValue(708L), "708"),
                    row("returns Long.MIN_VALUE", IntegerScheme().withValue(Long.MIN_VALUE), "-9223372036854775808"),
                    row("returns Long.MAX_VALUE", IntegerScheme().withValue(Long.MAX_VALUE), "9223372036854775807"),
                    row("converts to base <10", IntegerScheme(base = 2).withValue(141L), "10001101"),
                    row("converts to base >10", IntegerScheme(base = 12).withValue(248L), "188"),
                    row("uses separator", IntegerScheme(groupingSeparator = "#").withValue(949_442L), "949#442"),
                    row("uses no separator if disabled", IntegerScheme(groupingSeparatorEnabled = false, groupingSeparator = "#").withValue(179_644L), "179644"),
                    row("uses no separator in base <10", IntegerScheme(groupingSeparatorEnabled = true, base = 8, groupingSeparator = "#").withValue(731_942L), "2625446"),
                    row("uses no separator in base >10", IntegerScheme(groupingSeparatorEnabled = true, base = 12, groupingSeparator = "#").withValue(586_394L), "243422"),
                    row("retains lowercase in base >10 if disabled", IntegerScheme(base = 14, isUppercase = false).withValue(829_960L), "17866c"),
                    row("converts to uppercase in base >10 if enabled", IntegerScheme(base = 13, isUppercase = true).withValue(451_922L), "12A913"),
                    row("applies decorators in order affix, fixed length, array", IntegerScheme(affixDecorator = AffixDecorator(enabled = true, descriptor = "@L"), fixedLengthDecorator = FixedLengthDecorator(enabled = true), arrayDecorator = ArrayDecorator(enabled = true)).withValue(53L), "[530L, 530L, 530L]"),
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
                row("succeeds for default state", IntegerScheme(), null),
                row("fails if min value is above max value", IntegerScheme(minValue = 671L, maxValue = 93L), "integer.error.min_value_above_max"),
                row("fails if base is negative", IntegerScheme(base = -48), "integer.error.base_range"),
                row("fails if base is zero", IntegerScheme(base = -0), "integer.error.base_range"),
                row("fails if base is one", IntegerScheme(base = 1), "integer.error.base_range"),
                row("fails if base is greater than 30", IntegerScheme(base = 704), "integer.error.base_range"),
                row("fails if grouping separator is empty", IntegerScheme(groupingSeparator = ""), "integer.error.grouping_separator_length"),
                row("fails if grouping separator is non-char", IntegerScheme(groupingSeparator = "long"), "integer.error.grouping_separator_length"),
                row("fails if fixed-length decorator is invalid", IntegerScheme(fixedLengthDecorator = FixedLengthDecorator(length = -3)), ""),
                row("fails if affix decorator is invalid", IntegerScheme(affixDecorator = AffixDecorator(descriptor = """\""")), ""),
                row("fails if array decorator is invalid", IntegerScheme(arrayDecorator = ArrayDecorator(minCount = -24)), ""),
                //@formatter:on
            )
        ) { _, scheme, validation -> scheme shouldValidateAsBundle validation }
    }

    test("deepCopy") {
        lateinit var scheme: IntegerScheme


        beforeEach {
            scheme = IntegerScheme()
        }


        test("equals old instance") {
            scheme.deepCopy() shouldBe scheme
        }

        test("is independent of old instance") {
            val copy = scheme.deepCopy()

            scheme.groupingSeparator = "other"

            copy.groupingSeparator shouldNotBe scheme.groupingSeparator
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
 * Sets the [IntegerScheme.minValue] and [IntegerScheme.maxValue] to [value].
 *
 * @receiver the scheme to set the minimum and maximum value on
 * @param value the value to set
 * @return `this`
 */
private fun IntegerScheme.withValue(value: Long): IntegerScheme {
    minValue = value
    maxValue = value
    return this
}
