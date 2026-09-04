package com.fwdekker.randomness.affix

import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.shouldValidateAsBundle
import com.fwdekker.randomness.testhelpers.stateDeepCopyTestFactory
import com.fwdekker.randomness.testhelpers.stateSerializationTestFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.tuple
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe


/**
 * Unit tests for [AffixDecorator].
 */
object AffixDecoratorTest : FunSpec({
    tags(Tags.PLAIN, Tags.SCHEME)


    context("generateStrings") {
        withTests(
            mapOf(
                "returns default input if disabled" to
                    tuple(AffixDecorator(enabled = false, descriptor = """<@>"""), """[i0]"""),
                "appends and prepends if no '@'" to
                    tuple(AffixDecorator(enabled = true, descriptor = """*"""), "*[i0]*"),
                "replaces 'at' with input" to
                    tuple(AffixDecorator(enabled = true, descriptor = "(@)"), "([i0])"),
                "replaces multiple 'at' with input" to
                    tuple(AffixDecorator(enabled = true, descriptor = "(@|@)"), "([i0]|[i0])"),
                "interprets escaped 'at' as literal" to
                    tuple(AffixDecorator(enabled = true, descriptor = """(\@)"""), """(@)[i0](@)"""),
                "interprets escaped 'backslash' as literal" to
                    tuple(AffixDecorator(enabled = true, descriptor = """(\\@)"""), """(\[i0])"""),
            )
        ) { (scheme, output) ->
            scheme.generator = { count -> List(count) { "[i$it]" } }

            scheme.generateStrings()[0] shouldBe output
        }
    }

    context("doValidate") {
        withTests(
            mapOf(
                "succeeds for default state" to
                    tuple(AffixDecorator(), null),
                "succeeds for empty descriptor" to
                    tuple(AffixDecorator(enabled = true, descriptor = ""), null),
                "succeeds for complex descriptor" to
                    tuple(AffixDecorator(enabled = true, descriptor = """\\@\@@\@\\"""), null),
                "fails descriptor has single trailing backslash" to
                    tuple(AffixDecorator(enabled = true, descriptor = """\"""), "affix.error.trailing_escape"),
                "succeeds if descriptor has double trailing backslash" to
                    tuple(AffixDecorator(enabled = true, descriptor = """\\"""), null),
                "fails if descriptors has triple trailing backslash" to
                    tuple(AffixDecorator(enabled = true, descriptor = """\\\"""), "affix.error.trailing_escape"),
                "ignores invalid settings if disabled" to
                    tuple(AffixDecorator(enabled = false, descriptor = """\"""), null),
            )
        ) { (scheme, validation) ->
            scheme.generator = { count -> List(count) { "[i$it]" } }

            scheme shouldValidateAsBundle validation
        }
    }

    include(stateDeepCopyTestFactory { AffixDecorator() })

    include(stateSerializationTestFactory { AffixDecorator() })
})
