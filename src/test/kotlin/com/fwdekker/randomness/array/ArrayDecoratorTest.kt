package com.fwdekker.randomness.array

import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.shouldValidateAsBundle
import com.fwdekker.randomness.testhelpers.stateDeepCopyTestFactory
import com.fwdekker.randomness.testhelpers.stateSerializationTestFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.tuple
import io.kotest.datatest.withTests
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe


/**
 * Unit tests for [ArrayDecorator].
 */
object ArrayDecoratorTest : FunSpec({
    tags(Tags.PLAIN, Tags.SCHEME)


    context("generateStrings") {
        withTests(
            mapOf(
                "returns default input if disabled" to
                    tuple(
                        ArrayDecorator(enabled = false, minCount = 3),
                        "<i0>",
                    ),
                "returns a single value" to
                    tuple(
                        ArrayDecorator(enabled = true, minCount = 1, maxCount = 1),
                        "[<i0>]",
                    ),
                "returns a fixed number of values" to
                    tuple(
                        ArrayDecorator(enabled = true, minCount = 3, maxCount = 3),
                        "[<i0>, <i1>, <i2>]",
                    ),
                "returns array with multi-char separator" to
                    tuple(
                        ArrayDecorator(enabled = true, separator = ";;"),
                        "[<i0>;;<i1>;;<i2>]",
                    ),
                "retains leading whitespace in separator" to
                    tuple(
                        ArrayDecorator(enabled = true, separator = ",  "),
                        "[<i0>,  <i1>,  <i2>]",
                    ),
                "converts escaped 'n' in separator to newline" to
                    tuple(
                        ArrayDecorator(enabled = true, separator = """\n"""),
                        "[<i0>\n<i1>\n<i2>]",
                    ),
                "applies the element format to each element separately" to
                    tuple(
                        ArrayDecorator(enabled = true, elementFormat = "({val})"),
                        "[(<i0>), (<i1>), (<i2>)]",
                    ),
                "includes the element index if part of the element format" to
                    tuple(
                        ArrayDecorator(enabled = true, elementFormat = "{eid}={val}"),
                        "[0=<i0>, 1=<i1>, 2=<i2>]",
                    ),
                "ignores unknown brace-delimited keywords in the element format" to
                    tuple(
                        ArrayDecorator(enabled = true, elementFormat = "{val}={foo}"),
                        "[<i0>={foo}, <i1>={foo}, <i2>={foo}]",
                    ),
                "applies affix decorator" to
                    tuple(
                        ArrayDecorator(enabled = true, affixDecorator = AffixDecorator(enabled = true, "(@)")),
                        "(<i0>, <i1>, <i2>)",
                    ),
            )
        ) { (scheme, output) ->
            scheme.generator = { count -> List(count) { "<i$it>" } }

            scheme.generateStrings()[0] shouldBe output
        }

        test("generates the desired number of parts in each string") {
            val scheme = ArrayDecorator(enabled = true, minCount = 3, maxCount = 8)
            scheme.generator = { count -> List(count) { "<i$it>" } }

            scheme.generateStrings(count = 50)
                .map { string -> string.count { it == ',' } + 1 }
                .forEach { it shouldBeInRange 3..8 }
        }

        test("generates an independently random number of parts per string") {
            val scheme = ArrayDecorator(enabled = true, minCount = 1, maxCount = 8)
            scheme.generator = { count -> List(count) { "<i$it>" } }

            scheme.generateStrings(count = 50)
                .map { string -> string.count { it == ',' } + 1 }
                .distinct() shouldHaveAtLeastSize 2
        }

        test("appropriately splits parts into strings") {
            val scheme = ArrayDecorator(enabled = true)
            var partIdx = 0
            scheme.generator = { count -> List(count) { "<i${partIdx++}>" } }

            scheme.generateStrings(count = 2) shouldBe listOf("[<i0>, <i1>, <i2>]", "[<i3>, <i4>, <i5>]")
        }

        test("applies the element format to each element of each array") {
            val scheme = ArrayDecorator(enabled = true, elementFormat = "({val})")
            var partIdx = 0
            scheme.generator = { count -> List(count) { "<i${partIdx++}>" } }

            scheme.generateStrings(count = 2) shouldBe listOf("[(<i0>), (<i1>), (<i2>)]", "[(<i3>), (<i4>), (<i5>)]")
        }

        test("starts the element index at 0 for each array") {
            val scheme = ArrayDecorator(enabled = true, elementFormat = "{eid}={val}")
            var partIdx = 0
            scheme.generator = { count -> List(count) { "<i${partIdx++}>" } }

            scheme.generateStrings(count = 2) shouldBe listOf("[0=<i0>, 1=<i1>, 2=<i2>]", "[0=<i3>, 1=<i4>, 2=<i5>]")
        }

        test("includes the array index in each array") {
            val scheme = ArrayDecorator(enabled = true, elementFormat = "{aid}={val}")
            var partIdx = 0
            scheme.generator = { count -> List(count) { "<i${partIdx++}>" } }

            scheme.generateStrings(count = 2) shouldBe listOf("[0=<i0>, 0=<i1>, 0=<i2>]", "[1=<i3>, 1=<i4>, 1=<i5>]")
        }
    }

    context("doValidate") {
        withTests(
            mapOf(
                "succeeds for default state" to
                    tuple(ArrayDecorator(), null),
                "fails for zero min count" to
                    tuple(ArrayDecorator(enabled = true, minCount = 0), "array.error.min_count_too_low"),
                "fails for negative min count" to
                    tuple(ArrayDecorator(enabled = true, minCount = -23), "array.error.min_count_too_low"),
                "succeeds for min count equals max count" to
                    tuple(ArrayDecorator(enabled = true, minCount = 368, maxCount = 368), null),
                "fails for min count above max count" to
                    tuple(
                        ArrayDecorator(
                            enabled = true,
                            minCount = 14,
                            maxCount = 2
                        ),
                        "array.error.min_count_above_max"
                    ),
                "fails if affix decorator is invalid" to
                    tuple(
                        ArrayDecorator(
                            enabled = true,
                            affixDecorator = AffixDecorator(enabled = true, descriptor = """\"""),
                        ),
                        ""
                    ),
                "ignores invalid settings if disabled" to
                    tuple(ArrayDecorator(enabled = false, minCount = -23), null),
                "ignores invalid affix decorator if that decorator is disabled" to
                    tuple(ArrayDecorator(affixDecorator = AffixDecorator(enabled = false, descriptor = """\""")), null),
            )
        ) { (scheme, validation) ->
            scheme.generator = { count -> List(count) { "<i$it>" } }

            scheme shouldValidateAsBundle validation
        }
    }

    include(stateDeepCopyTestFactory { ArrayDecorator() })

    include(stateSerializationTestFactory { ArrayDecorator() })
})
