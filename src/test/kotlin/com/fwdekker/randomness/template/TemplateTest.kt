package com.fwdekker.randomness.template

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.shouldValidateAsBundle
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.assertj.core.api.Assertions.assertThat


/**
 * Unit tests for [Template].
 */
object TemplateTest : FunSpec({
    tags(NamedTag("Scheme"))


    test("typeIcon") {
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

            assertThat(template.typeIcon.colors.size).isGreaterThan(1)
        }
    }

    test("applyContext") {
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


    test("canReference") {
        TODO()
    }


    test("generateStrings") {
        test("parameterized") {
            forAll(
                table(
                    //@formatter:off
                    headers("description", "scheme", "output"),
                    row("returns an empty string if it contains no schemes", Template(), ""),
                    row("returns the single scheme's output", Template(schemes = mutableListOf(DummyScheme())), "dummy0"),
                    row("returns the concatenation of the schemes' outputs", Template(schemes = mutableListOf(DummyScheme(prefix = "a"), DummyScheme(prefix = "b"), DummyScheme(prefix = "c"))), "a0b0c0"),
                    //@formatter:on
                )
            ) { _, scheme, output -> scheme.generateStrings()[0] shouldBe output }
        }

        test("throws an exception if the template is invalid") {
            val template = Template(schemes = mutableListOf(DummyScheme(valid = false)))

            shouldThrow<DataGenerationException> { template.generateStrings() }
        }
    }

    test("doValidate") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "validation"),
                row("succeeds for default state", Template(), null),
                row("fails if name is blank", Template("  "), "template.error.no_name"),
                row("fails if only scheme is invalid", Template("Template", mutableListOf(DummyScheme(valid = false))), "Template > DummyScheme is invalid"),
                row("fails on the first invalid scheme", Template("Template", mutableListOf(DummyScheme(), DummyScheme(valid = false), DummyScheme(valid = false))), "Template > DummyScheme is invalid"),
                row("fails if array decorator is invalid", Template(arrayDecorator = ArrayDecorator(minCount = -24)), ""),
                //@formatter:on
            )
        ) { _, scheme, validation -> scheme shouldValidateAsBundle validation }
    }

    test("deepCopy") {
        lateinit var scheme: Template


        beforeEach {
            scheme = Template()
        }


        test("equals old instance") {
            scheme.deepCopy() shouldBe scheme
        }

        test("is independent of old instance") {
            val copy = scheme.deepCopy()

            scheme.name = "other"

            copy.name shouldNotBe scheme.name
        }

        test("retains uuid if chosen") {
            scheme.deepCopy(true).uuid shouldBe scheme.uuid
        }

        test("replaces uuid if chosen") {
            scheme.deepCopy(false).uuid shouldNotBe scheme.uuid
        }
    }
})
