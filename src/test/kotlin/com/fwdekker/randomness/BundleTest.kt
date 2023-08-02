package com.fwdekker.randomness

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import java.util.MissingResourceException


/**
 * Unit tests for [Bundle].
 */
object BundleTest : FunSpec({
    test("get (no arguments)") {
        test("throws an exception if the key could not be found") {
            shouldThrow<MissingResourceException> { Bundle("this_key_does_not_exist") }
        }

        test("returns the string at the given key") {
            Bundle("string.title") shouldBe "string"
        }
    }

    test("get (vararg)") {
        test("ignores unnecessary arguments") {
            Bundle("string.title", "unnecessary", "unnecessary") shouldBe "String"
        }

        test("inserts the given arguments into the string") {
            Bundle("preview.invalid", "Argument") shouldBe "Settings are invalid:\nArgument"
        }
    }


    test("matchesFormat") {
        test("match without args in input") {
            forAll(
                table(
                    headers("input", "format", "matches"),
                    row("no args, matching", "no args, matching", true),
                    row("no args, mismatching", "different", false),
                    row("one arg, matching", "one %1\$s, matching", true),
                    row("one arg, mismatching", "one arg, but %1\$s", false),
                    row("multiple args, matching", "%1\$s args, %2\$s", true),
                    row("multiple args, mismatching", "multiple %1\$s, but %2\$s", false),
                )
            ) { input, format, matches -> input.matchesFormat(format) shouldBe matches }
        }

        test("match with args") {
            forAll(
                table(
                    headers("input", "format", "args", "matches"),
                    row("no args, matching", "no args, matching", arrayOf(), true),
                    row("no args, mismatching", "no args, different", arrayOf(), false),
                    row("one arg, matching", "%1\$s arg, matching", arrayOf("one"), true),
                    row("one arg, mismatching", "%1\$s arg, different format", arrayOf("one"), false),
                    row("one arg, mismatching", "%1\$s arg, mismatching", arrayOf("different"), false),
                    row("incomplete args, matching", "%1\$s args, %2\$s", arrayOf("incomplete"), true),
                    row("incomplete args, mismatching", "%1\$s format, %2\$s", arrayOf("incomplete"), false),
                    row("incomplete args, mismatching", "%1\$s args, %2\$s", arrayOf("different"), false),
                    row("complete args, matching", "%1\$s args, %2\$s", arrayOf("complete", "matching"), true),
                    row("complete args, mismatching", "%1\$s format, %2\$s", arrayOf("complete", "mismatching"), false),
                    row("complete args, mismatching", "%1\$s args, %2\$s", arrayOf("different", "mismatching"), false),
                )
            ) { input, format, args, matches -> input.matchesFormat(format, *args) shouldBe matches }
        }
    }
})
