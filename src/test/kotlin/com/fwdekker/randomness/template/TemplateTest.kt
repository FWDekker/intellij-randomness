package com.fwdekker.randomness.template

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.shouldValidateAsBundle
import com.fwdekker.randomness.stateDeepCopyTestFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs


/**
 * Unit tests for [Template].
 */
object TemplateTest : FunSpec({
    tags(NamedTag("Scheme"))


    context("typeIcon") {
        test("returns the single scheme's icon if that scheme has an icon") {
            val scheme = IntegerScheme()
            val template = Template(schemes = mutableListOf(scheme))

            template.typeIcon shouldBe scheme.typeIcon
        }

        test("returns the default icon if the single scheme has no icon") {
            val scheme = DummyScheme().also { it.typeIcon = null }
            val template = Template(schemes = mutableListOf(scheme))

            template.typeIcon shouldBe Template.DEFAULT_ICON
        }

        test("returns the default icon if no scheme is present") {
            val template = Template()

            template.typeIcon shouldBe Template.DEFAULT_ICON
        }

        test("returns the scheme's combined icon if multiple are present") {
            val template = Template(schemes = mutableListOf(DummyScheme(), DummyScheme()))

            template.typeIcon.colors.size shouldBeGreaterThan 1
        }
    }

    context("applyContext") {
        test("applies the context to itself") {
            val newContext = Settings()
            val template = Template()

            template.applyContext(newContext)

            +template.context shouldBeSameInstanceAs newContext
        }

        test("applies the context to each scheme") {
            val newContext = Settings()
            val template = Template(schemes = mutableListOf(DummyScheme(), DummyScheme()))

            template.applyContext(newContext)

            template.schemes.forEach { +it.context shouldBeSameInstanceAs newContext }
        }
    }


    xtest("canReference") {
        TODO() // TODO: Add these tests
    }


    context("generateStrings") {
        withData(
            mapOf(
                "returns an empty string if it contains no schemes" to
                    row(Template(), ""),
                "returns the single scheme's output" to
                    row(Template(schemes = mutableListOf(DummyScheme())), "text0"),
                "returns the concatenation of the schemes' outputs" to
                    row(
                        Template(
                            schemes = mutableListOf(
                                DummyScheme(prefix = "a"),
                                DummyScheme(prefix = "b"),
                                DummyScheme(prefix = "c"),
                            ),
                        ),
                        "a0b0c0",
                    ),
            )
        ) { (scheme, output) -> scheme.generateStrings()[0] shouldBe output }

        test("throws an exception if the template is invalid") {
            val template = Template(schemes = mutableListOf(DummyScheme(valid = false)))

            shouldThrow<DataGenerationException> { template.generateStrings() }
        }
    }

    context("doValidate") {
        withData(
            mapOf(
                "succeeds for default state" to
                    row(Template(), null),
                "fails if name is blank" to
                    row(Template("  "), "template.error.no_name"),
                "fails if scheme is invalid" to
                    row(Template("Template", mutableListOf(DummyScheme(valid = false))), ""),
                "fails if array decorator is invalid" to
                    row(Template(arrayDecorator = ArrayDecorator(minCount = -24)), ""),
            )
        ) { (scheme, validation) -> scheme shouldValidateAsBundle validation }
    }

    include(stateDeepCopyTestFactory { Template() })
})
