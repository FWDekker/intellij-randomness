package com.fwdekker.randomness.array

import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.shouldValidateAsBundle
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe


/**
 * Unit tests for [ArrayDecorator].
 */
object ArrayDecoratorTest : FunSpec({
    tags(NamedTag("Scheme"))


    test("generateStrings") {
        test("parameterized") {
            forAll(
                table(
                    //@formatter:off
                    headers("description", "scheme", "output"),
                    row("returns default input if disabled", ArrayDecorator(enabled = false, minCount = 3), "[i0]"),
                    row("returns a single value", ArrayDecorator(enabled = true, minCount = 1, maxCount = 1), "[[i0]]"),
                    row("returns a fixed number of values", ArrayDecorator(enabled = true, minCount = 3, maxCount = 3), "[[i0], [i1], [i2]]"),
                    row("returns array with multi-char separator", ArrayDecorator(enabled = true, separator = ";;"), "[[i0];;[i1];;[i2]]"),
                    row("retains leading whitespace in separator", ArrayDecorator(enabled = true, separator = ",  "), "[[i0],  [i1],  [i2]]"),
                    row("converts escaped 'n' to newline", ArrayDecorator(enabled = true, separator = """\n"""), """[[i0]\n[i1]\n[i2]]"""),
                    row("applies affix decorator", ArrayDecorator(enabled = true, affixDecorator = AffixDecorator(enabled = true, descriptor = "(@)")), "([i0], [i1], [i2])"),
                    //@formatter:on
                )
            ) { _, scheme, output ->
                scheme.generator = { count -> List(count) { "[i$it]" } }

                scheme.generateStrings()[0] shouldBe output
            }
        }

        test("generates the desired number of entries") {
            val scheme = ArrayDecorator(enabled = true, minCount = 3, maxCount = 8)
            scheme.generator = { count -> List(count) { "[i$it]" } }

            scheme.generateStrings(count = 50).map { string -> string.count { it == ',' } + 1 }
                .forEach { it shouldBeInRange 3..8 }
        }

        test("appropriately chunks generator outputs") {
            val scheme = ArrayDecorator(enabled = true)
            var i = 0
            scheme.generator = { count -> List(count) { "[i${i++}]" } }

            scheme.generateStrings(count = 3) shouldBe listOf("[[i0], [i1], [i2]]", "[[i3], [i4], [i5]]")
        }
    }

    test("doValidate") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "validation"),
                row("succeeds for default state", ArrayDecorator(), null),
                row("fails for zero min count", ArrayDecorator(minCount = 0), "array.error.min_count_too_low"),
                row("fails for negative min count", ArrayDecorator(minCount = -23), "array.error.min_count_too_low"),
                row("fails for min count equals max count", ArrayDecorator(minCount = 368, maxCount = 368), "array.error.min_count_above_max"),
                row("fails for min count above max count", ArrayDecorator(minCount = 14, maxCount = 2), "array.error.min_count_above_max"),
                row("fails if affix decorator is invalid", ArrayDecorator(affixDecorator = AffixDecorator(descriptor = """\""")), ""),
                //@formatter:on
            )
        ) { _, scheme, validation ->
            scheme.generator = { List(it) { "[in]" } }

            scheme shouldValidateAsBundle validation
        }
    }

    test("deepCopy") {
        lateinit var scheme: ArrayDecorator


        beforeEach {
            scheme = ArrayDecorator()
        }


        test("equals old instance") {
            scheme.deepCopy() shouldBe scheme
        }

        test("is independent of old instance") {
            val copy = scheme.deepCopy()

            scheme.separator = "other"

            copy.separator shouldNotBe scheme.separator
        }

        test("retains uuid if chosen") {
            scheme.deepCopy(true).uuid shouldBe scheme.uuid
        }

        test("replaces uuid if chosen") {
            scheme.deepCopy(false).uuid shouldNotBe scheme.uuid
        }
    }
})
