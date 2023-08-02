package com.fwdekker.randomness.fixedlength

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
 * Unit tests for [FixedLengthDecorator].
 */
object FixedLengthDecoratorTest : FunSpec({
    tags(NamedTag("Scheme"))


    test("generateStrings") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "output"),
                row("returns default input if disabled", FixedLengthDecorator(enabled = false, length = 1), "[i0]"),
                row("returns shortened string", FixedLengthDecorator(enabled = true, length = 3), "[i0"),
                row("returns padded string", FixedLengthDecorator(enabled = true, length = 5, filler = "f"), "[i0]f"),
                row("returns default input if correct length", FixedLengthDecorator(enabled = true, length = 4), "[i0]"),
                //@formatter:on
            )
        ) { _, scheme, output ->
            scheme.generator = { count -> List(count) { "[i$it]" } }

            scheme.generateStrings()[0] shouldBe output
        }
    }

    test("doValidate") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "validation"),
                row("succeeds for default state", FixedLengthDecorator(), null),
                row("succeeds for one length", FixedLengthDecorator(length = 1), null),
                row("fails for zero length", FixedLengthDecorator(length = 0), "fixed_length.error.length_too_low"),
                row("fails for negative length", FixedLengthDecorator(length = -4), "fixed_length.error.length_too_low"),
                row("fails for empty filler", FixedLengthDecorator(filler = ""), "fixed_length.error.filler_length"),
                row("fails for non-char filler", FixedLengthDecorator(filler = "long"), "fixed_length.error.filler_length"),
                //@formatter:on
            )
        ) { _, scheme, validation ->
            scheme.generator = { List(it) { "[in]" } }

            scheme shouldValidateAsBundle validation
        }
    }

    test("deepCopy") {
        lateinit var scheme: FixedLengthDecorator


        beforeEach {
            scheme = FixedLengthDecorator()
        }


        test("equals old instance") {
            scheme.deepCopy() shouldBe scheme
        }

        test("is independent of old instance") {
            val copy = scheme.deepCopy()

            scheme.filler = "other"

            copy.filler shouldNotBe scheme.filler
        }

        test("retains uuid if chosen") {
            scheme.deepCopy(true).uuid shouldBe scheme.uuid
        }

        test("replaces uuid if chosen") {
            scheme.deepCopy(false).uuid shouldNotBe scheme.uuid
        }
    }
})
