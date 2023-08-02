package com.fwdekker.randomness.affix

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
 * Unit tests for [AffixDecorator].
 */
object AffixDecoratorTest : FunSpec({
    tags(NamedTag("Scheme"))


    test("generateStrings") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "output"),
                row("returns default input if disabled", AffixDecorator(enabled = false, descriptor = """<@>"""), """[i0]"""),
                row("appends and prepends if no '@'", AffixDecorator(enabled = true, descriptor = """*"""), "*[i0]*"),
                row("replaces 'at' with input", AffixDecorator(enabled = true, descriptor = "(@)"), "([i0])"),
                row("replaces multiple 'at' with input", AffixDecorator(enabled = true, descriptor = "(@|@)"), "([i0]|[i0])"),
                row("interprets escaped 'at' as literal", AffixDecorator(enabled = true, descriptor = """(\@)"""), """(@)[i0](@)"""),
                row("interprets escaped 'backslash' as literal", AffixDecorator(enabled = true, descriptor = """(\\@)"""), """(\[i0])"""),
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
                row("succeeds for default state", AffixDecorator(), null),
                row("succeeds for empty descriptor", AffixDecorator(enabled = true, descriptor = ""), null),
                row("succeeds for complex descriptor", AffixDecorator(enabled = true, descriptor = """\\@\@@\@\\"""), null),
                row("fails descriptor has single trailing backslash", AffixDecorator(enabled = true, descriptor = """\"""), "affix.error.trailing_escape"),
                row("succeeds if descriptor has double trailing backslash", AffixDecorator(enabled = true, descriptor = """\\"""), null),
                row("fails if descriptors has triple trailing backslash", AffixDecorator(enabled = true, descriptor = """\\\"""), "affix.error.trailing_escape"),
                row("fails for invalid settings even if disabled", AffixDecorator(enabled = false, descriptor = """\"""), "affix.error.trailing_escape"),
                //@formatter:on
            )
        ) { _, scheme, validation ->
            scheme.generator = { List(it) { "[in]" } }

            scheme shouldValidateAsBundle validation
        }
    }

    test("deepCopy") {
        lateinit var scheme: AffixDecorator


        beforeEach {
            scheme = AffixDecorator()
        }


        test("equals old instance") {
            scheme.deepCopy() shouldBe scheme
        }

        test("is independent of old instance") {
            val copy = scheme.deepCopy()

            scheme.descriptor = "other"

            copy.descriptor shouldNotBe scheme.descriptor
        }

        test("retains uuid if chosen") {
            scheme.deepCopy(true).uuid shouldBe scheme.uuid
        }

        test("replaces uuid if chosen") {
            scheme.deepCopy(false).uuid shouldNotBe scheme.uuid
        }
    }
})
