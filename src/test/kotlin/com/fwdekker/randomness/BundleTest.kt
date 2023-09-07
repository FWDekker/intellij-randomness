package com.fwdekker.randomness

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.util.MissingFormatArgumentException
import java.util.MissingResourceException


/**
 * Unit tests for [Bundle].
 */
object BundleTest : FunSpec({
    context("get (no arguments)") {
        test("throws an exception if the key could not be found") {
            shouldThrow<MissingResourceException> { Bundle("this_key_does_not_exist") }
        }

        test("returns the string at the given key") {
            Bundle("string.title") shouldBe "String"
        }
    }

    context("get (vararg)") {
        test("ignores unnecessary arguments") {
            Bundle("string.title", "unnecessary", "unnecessary") shouldBe "String"
        }

        test("inserts the given arguments into the string") {
            Bundle("preview.invalid", "Argument") shouldBe "Settings are invalid:\nArgument"
        }
    }

    context("matchesFormat") {
        test("throws a MissingFormatArgumentException if insufficient arguments are given") {
            shouldThrow<MissingFormatArgumentException> { "anything".matchesFormat("%1\$s") }
        }

        withData(
            nameFn = { it.a },
            row("no args, match", "no args, match", arrayOf(), true),
            row("no args, mismatch", "no args, different", arrayOf(), false),
            row("one arg, match", "%1\$s arg, match", arrayOf("one"), true),
            row("one arg, mismatch in format", "%1\$s arg, different format", arrayOf("one"), false),
            row("one arg, mismatch in arg", "%1\$s arg, mismatch in arg", arrayOf("different"), false),
            row("two args, match", "%1\$s args, %2\$s", arrayOf("two", "match"), true),
            row("two args, mismatch in format", "%1\$s args, different %2\$s", arrayOf("two", "format"), false),
            row("two args, mismatch in arg", "%1\$s args, mismatch %2\$s", arrayOf("different", "arg"), false),
        ) { (input, format, args, matches) ->
            input.matchesFormat(format, *args) shouldBe matches
        }
    }
})
