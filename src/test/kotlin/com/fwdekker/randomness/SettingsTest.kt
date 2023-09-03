package com.fwdekker.randomness

import com.fwdekker.randomness.template.Template
import com.fwdekker.randomness.template.TemplateList
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs


/**
 * Unit tests for [Settings].
 */
object SettingsTest : FunSpec({
    test("doValidate") {
        forAll(
            //@formatter:off
            table(
                headers("description", "scheme", "validation"),
                row("succeeds for default state", Settings(), null),
                row("fails if template list is invalid", Settings(TemplateList(mutableListOf(Template("Duplicate"), Template("Duplicate")))), ""),
            )
            //@formatter:on
        ) { _, scheme, validation -> scheme shouldValidateAsBundle validation }
    }

    context("deepCopy") {
        test("deep-copies the template list") {
            val settings = Settings(TemplateList(mutableListOf(Template("old"))))

            val copy = settings.deepCopy()
            copy.templates[0].name = "new"

            settings.templates[0].name shouldBe "old"
        }
    }

    context("copyFrom") {
        test("cannot copy from another type") {
            val settings = Settings()

            shouldThrow<IllegalArgumentException> { settings.copyFrom(DummyState()) }
                .message shouldBe "Cannot copy from different type."
        }

        test("deep-copies the template list") {
            val settings = Settings(TemplateList(mutableListOf(Template("old"))))
            val other = Settings(TemplateList(mutableListOf(Template("new"))))

            settings.copyFrom(other)

            settings.templateList shouldNotBeSameInstanceAs other.templateList
            settings.templates[0].name shouldBe "new"
        }
    }
})
