package com.fwdekker.randomness.array

import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.schemeSerializationTestFactory
import com.fwdekker.randomness.stateDeepCopyTestFactory
import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.shouldValidateAsBundle
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe


/**
 * Unit tests for [ArrayDecorator].
 */
object ArrayDecoratorTest : FunSpec({
    tags(Tags.SCHEME)


    context("generateStrings") {
        withData(
            mapOf(
                "returns default input if disabled" to
                    row(
                        ArrayDecorator(enabled = false, minCount = 3),
                        "{i0}",
                    ),
                "returns a single value" to
                    row(
                        ArrayDecorator(enabled = true, minCount = 1, maxCount = 1),
                        "[{i0}]",
                    ),
                "returns a fixed number of values" to
                    row(
                        ArrayDecorator(enabled = true, minCount = 3, maxCount = 3),
                        "[{i0}, {i1}, {i2}]",
                    ),
                "returns array with multi-char separator" to
                    row(
                        ArrayDecorator(enabled = true, separator = ";;"),
                        "[{i0};;{i1};;{i2}]",
                    ),
                "retains leading whitespace in separator" to
                    row(
                        ArrayDecorator(enabled = true, separator = ",  "),
                        "[{i0},  {i1},  {i2}]",
                    ),
                "converts escaped 'n' in separator to newline" to
                    row(
                        ArrayDecorator(enabled = true, separator = """\n"""),
                        "[{i0}\n{i1}\n{i2}]",
                    ),
                "applies affix decorator" to
                    row(
                        ArrayDecorator(enabled = true, affixDecorator = AffixDecorator(enabled = true, "(@)")),
                        "({i0}, {i1}, {i2})",
                    ),
            )
        ) { (scheme, output) ->
            scheme.generator = { count -> List(count) { "{i$it}" } }

            scheme.generateStrings()[0] shouldBe output
        }

        test("generates the desired number of parts in each string") {
            val scheme = ArrayDecorator(enabled = true, minCount = 3, maxCount = 8)
            scheme.generator = { count -> List(count) { "{i$it}" } }

            scheme.generateStrings(count = 50)
                .map { string -> string.count { it == ',' } + 1 }
                .forEach { it shouldBeInRange 3..8 }
        }

        test("generates an independently random number of parts per string") {
            val scheme = ArrayDecorator(enabled = true, minCount = 1, maxCount = 8)
            scheme.generator = { count -> List(count) { "{i$it}" } }

            scheme.generateStrings(count = 50)
                .map { string -> string.count { it == ',' } + 1 }
                .distinct() shouldHaveAtLeastSize 2
        }

        test("appropriately splits parts into strings") {
            val scheme = ArrayDecorator(enabled = true)
            var partIdx = 0
            scheme.generator = { count -> List(count) { "{i${partIdx++}}" } }

            scheme.generateStrings(count = 2) shouldBe listOf("[{i0}, {i1}, {i2}]", "[{i3}, {i4}, {i5}]")
        }
    }

    context("doValidate") {
        withData(
            mapOf(
                "succeeds for default state" to
                    row(ArrayDecorator(), null),
                "fails for zero min count" to
                    row(ArrayDecorator(minCount = 0), "array.error.min_count_too_low"),
                "fails for negative min count" to
                    row(ArrayDecorator(minCount = -23), "array.error.min_count_too_low"),
                "succeeds for min count equals max count" to
                    row(ArrayDecorator(minCount = 368, maxCount = 368), null),
                "fails for min count above max count" to
                    row(ArrayDecorator(minCount = 14, maxCount = 2), "array.error.min_count_above_max"),
                "fails if affix decorator is invalid" to
                    row(ArrayDecorator(affixDecorator = AffixDecorator(descriptor = """\""")), ""),
            )
        ) { (scheme, validation) ->
            scheme.generator = { count -> List(count) { "{i$it}" } }

            scheme shouldValidateAsBundle validation
        }
    }

    include(stateDeepCopyTestFactory { ArrayDecorator() })

    include(schemeSerializationTestFactory { ArrayDecorator() })
})
