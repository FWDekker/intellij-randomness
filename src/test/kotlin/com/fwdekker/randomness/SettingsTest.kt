package com.fwdekker.randomness

import com.fwdekker.randomness.template.Template
import com.fwdekker.randomness.template.TemplateList
import com.fwdekker.randomness.testhelpers.DummyState
import com.fwdekker.randomness.testhelpers.shouldValidateAsBundle
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs


/**
 * Unit tests for [Settings].
 */
object SettingsTest : FunSpec({
    context("doValidate") {
        withData(
            mapOf(
                "succeeds for default state" to
                    row(Settings(), null),
                "fails if template list is invalid" to
                    row(
                        Settings(
                            templateList = TemplateList(mutableListOf(Template("Duplicate"), Template("Duplicate"))),
                        ),
                        "",
                    ),
            )
        ) { (scheme, validation) ->
            scheme shouldValidateAsBundle validation
        }
    }

    context("deepCopy") {
        test("deep-copies the template list") {
            val settings = Settings(templateList = TemplateList(mutableListOf(Template("old"))))

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
            val settings = Settings(templateList = TemplateList(mutableListOf(Template("old"))))
            val other = Settings(templateList = TemplateList(mutableListOf(Template("new"))))

            settings.copyFrom(other)

            settings.templateList shouldNotBeSameInstanceAs other.templateList
            settings.templates[0].name shouldBe "new"
        }
    }
})
