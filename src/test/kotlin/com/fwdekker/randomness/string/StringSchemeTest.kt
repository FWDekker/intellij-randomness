package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.stateDeepCopyTestFactory
import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.shouldValidateAsBundle
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe


/**
 * Unit tests for [StringScheme].
 */
object StringSchemeTest : FunSpec({
    tags(Tags.SCHEME)


    context("isSimple") {
        withData(
            mapOf(
                "false if invalid" to
                    row(StringScheme(pattern = "{}"), false),
                "false if pattern uses quantifier" to
                    row(StringScheme(pattern = "[u]{4}"), false),
                "false if pattern uses grouping" to
                    row(StringScheme(pattern = "(a|b)"), false),
                "true if pattern is plain string, as non-regex" to
                    row(StringScheme(pattern = "text", isRegex = false), true),
                "true if pattern is plain string, as regex" to
                    row(StringScheme(pattern = "text", isRegex = true), true),
                "true if pattern escapes character, as non-regex" to
                    row(StringScheme(pattern = """te\[xt""", isRegex = false), true),
                "true if pattern escapes character, as regex" to
                    row(StringScheme(pattern = """te\[xt""", isRegex = true), true),
                "true if pattern escapes backslash, as non-regex" to
                    row(StringScheme(pattern = """te\\xt""", isRegex = false), true),
                "true if pattern escapes backslash, as regex" to
                    row(StringScheme(pattern = """te\\xt""", isRegex = true), true),
                "true if pattern uses quantifier, as non-regex" to
                    row(StringScheme(pattern = "[u]{4}", isRegex = false), true),
            )
        ) { (scheme, isSimple) -> scheme.isSimple() shouldBe isSimple }
    }


    context("generateStrings") {
        withData(
            mapOf(
                "returns empty string" to
                    row(StringScheme(pattern = ""), ""),
                "returns plain string" to
                    row(StringScheme(pattern = "text"), "text"),
                "removes look-alike characters" to
                    row(StringScheme(pattern = "boiled", removeLookAlikeSymbols = true), "bed"),
                "removes look-alike characters after interpreting regex" to
                    row(StringScheme(pattern = "[x]{4}[i]{4}", removeLookAlikeSymbols = true), "xxxx"),
                "returns capitalized string" to
                    row(StringScheme(pattern = "text", capitalization = CapitalizationMode.UPPER), "TEXT"),
                "returns pattern literally if regex disabled" to
                    row(StringScheme(pattern = "a[bc]d", isRegex = false), "a[bc]d"),
                "returns reverse-regexed string" to
                    row(StringScheme(pattern = "[x]{4}"), "xxxx"),
            )
        ) { (scheme, output) -> scheme.generateStrings()[0] shouldBe output }
    }

    context("doValidate") {
        withData(
            mapOf(
                "succeeds for default state" to
                    row(StringScheme(), null),
                "fails if pattern is invalid" to
                    row(StringScheme(pattern = "{x"), ""),
                "fails if pattern is empty curly braces" to
                    row(StringScheme(pattern = "{}"), "string.error.empty_curly"),
                "fails if pattern has empty curly braces" to
                    row(StringScheme(pattern = "a{}b"), "string.error.empty_curly"),
                "succeeds if empty curly braces are escaped" to
                    row(StringScheme(pattern = """\{}"""), null),
                "fails if pattern is empty square braces" to
                    row(StringScheme(pattern = "[]"), "string.error.empty_square"),
                "fails if pattern has empty square braces" to
                    row(StringScheme(pattern = "a[]b"), "string.error.empty_square"),
                "succeeds if empty square braces are escaped" to
                    row(StringScheme(pattern = """\[]"""), null),
                "fails if pattern has single trailing backslash" to
                    row(StringScheme(pattern = """text\"""), "string.error.trailing_backslash"),
                "succeeds if pattern has double trailing backslash" to
                    row(StringScheme(pattern = """text\\"""), null),
                "fails if pattern has triple trailing backslash" to
                    row(StringScheme(pattern = """text\\\"""), "string.error.trailing_backslash"),
                "succeeds if non-regex pattern has single trailing backslash" to
                    row(StringScheme(pattern = """text\""", isRegex = false), null),
                "fails if array decorator is invalid" to
                    row(StringScheme(arrayDecorator = ArrayDecorator(minCount = -328)), ""),
            )
        ) { (scheme, validation) -> scheme shouldValidateAsBundle validation }
    }

    include(stateDeepCopyTestFactory { StringScheme() })
})
