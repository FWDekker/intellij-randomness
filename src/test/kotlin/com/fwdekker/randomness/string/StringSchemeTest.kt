package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
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
 * Unit tests for [StringScheme].
 */
object StringSchemeTest : FunSpec({
    tags(NamedTag("Scheme"))


    test("isSimple") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "isSimple"),
                row("false if invalid", StringScheme(pattern = "{}"), false),
                row("false if pattern uses quantifier", StringScheme(pattern = "[u]{4}"), false),
                row("false if pattern uses grouping", StringScheme(pattern = "(a|b)"), false),
                row("true if pattern is plain string, as non-regex", StringScheme(pattern = "text", isRegex = false), true),
                row("true if pattern is plain string, as regex", StringScheme(pattern = "text", isRegex = true), true),
                row("true if pattern escapes character, as non-regex", StringScheme(pattern = """te\[xt""", isRegex = false), true),
                row("true if pattern escapes character, as regex", StringScheme(pattern = """te\[xt""", isRegex = true), true),
                row("true if pattern escapes backslash, as non-regex", StringScheme(pattern = """te\\xt""", isRegex = false), true),
                row("true if pattern escapes backslash, as regex", StringScheme(pattern = """te\\xt""", isRegex = true), true),
                row("true if pattern uses quantifier, as non-regex", StringScheme(pattern = "[u]{4}", isRegex = false), true),
                //@formatter:on
            )
        ) { _, scheme, isSimple -> scheme.isSimple() shouldBe isSimple }
    }


    test("generateStrings") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "output"),
                row("returns empty string", StringScheme(pattern = ""), ""),
                row("returns plain string", StringScheme(pattern = "text"), "text"),
                row("removes look-alike characters", StringScheme(pattern = "boiled", removeLookAlikeSymbols = true), "bed"),
                row("removes look-alike characters after interpreting regex", StringScheme(pattern = "[xi]{4}", removeLookAlikeSymbols = true), "xxxx"),
                row("returns capitalized string", StringScheme(pattern = "text", capitalization = CapitalizationMode.UPPER), "TEXT"),
                row("returns pattern literally if regex disabled", StringScheme(pattern = "a[bc]d", isRegex = false), "a[bc]d"),
                row("returns reverse-regexed string", StringScheme(pattern = "[x]{4}"), "xxx"),
                //@formatter:on
            )
        ) { _, scheme, output -> scheme.generateStrings()[0] shouldBe output }
    }

    test("doValidate") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "validation"),
                row("succeeds for default state", StringScheme(), null),
                row("fails if pattern is invalid", StringScheme(pattern = "{x"), ""),
                row("fails if pattern is empty curly braces", StringScheme(pattern = "{}"), "string.error.empty_curly"),
                row("fails if pattern has empty curly braces", StringScheme(pattern = "a{}b"), "string.error.empty_curly"),
                row("succeeds if empty curly braces are escaped", StringScheme(pattern = """\{}"""), null),
                row("fails if pattern is empty square braces", StringScheme(pattern = "[]"), "string.error.empty_square"),
                row("fails if pattern has empty square braces", StringScheme(pattern = "a[]b"), "string.error.empty_square"),
                row("succeeds if empty square braces are escaped", StringScheme(pattern = """\[]"""), null),
                row("fails if pattern has single trailing backslash", StringScheme(pattern = """text\"""), "string.error.trailing_backslash"),
                row("succeeds if pattern has double trailing backslash", StringScheme(pattern = """text\\"""), null),
                row("fails if pattern has triple trailing backslash", StringScheme(pattern = """text\\\"""), "string.error.trailing_backslash"),
                row("succeeds if non-regex pattern has single trailing backslash", StringScheme(pattern = """text\""", isRegex = false), null),
                row("fails if array decorator is invalid", StringScheme(arrayDecorator = ArrayDecorator(minCount = -328)), ""),
                //@formatter:on
            )
        ) { _, scheme, validation -> scheme shouldValidateAsBundle validation }
    }

    test("deepCopy") {
        lateinit var scheme: StringScheme


        beforeEach {
            scheme = StringScheme()
        }


        test("equals old instance") {
            scheme.deepCopy() shouldBe scheme
        }

        test("is independent of old instance") {
            val copy = scheme.deepCopy()

            scheme.pattern = "other"

            copy.pattern shouldNotBe scheme.pattern
        }

        test("retains uuid if chosen") {
            scheme.deepCopy(true).uuid shouldBe scheme.uuid
        }

        test("replaces uuid if chosen") {
            scheme.deepCopy(false).uuid shouldNotBe scheme.uuid
        }
    }
})
