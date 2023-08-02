package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.shouldValidateAsBundle
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs


/**
 * Unit tests for [TemplateList].
 */
object TemplateListTest : FunSpec({
    test("applyContext") {
        test("applies the context to itself") {
            val newContext = Settings()
            val list = TemplateList()

            list.applyContext(newContext)

            +list.context shouldBeSameInstanceAs newContext
        }

        test("applies the context to each template") {
            val newContext = Settings()
            val template = Template()
            val list = TemplateList(mutableListOf(template))

            list.applyContext(newContext)

            +template.context shouldBeSameInstanceAs newContext
        }
    }


    test("listValidReferenceTargets") {
        test("for reference") {
            test("returns an error if the given reference is not in this list") {
                //
            }

            test("returns copies") {
                //
            }
        }

        test("for template") {
            //
        }
    }


    test("getTemplateByUuid") {
        test("returns null if no such template can be found") {
            val list = TemplateList()

            list.getTemplateByUuid("46840969-0b31-4fef-92b0-42f34dbc9f8b") should beNull()
        }

        test("returns the template with the given UUID") {
            val list = TemplateList()

            val template = list.templates[0]

            list.getTemplateByUuid(template.uuid) shouldBeSameInstanceAs template
        }

        test("returns null even if the UUID corresponds to a scheme") {
            val list = TemplateList()

            val scheme = list.templates[0].schemes[0]

            list.getTemplateByUuid(scheme.uuid) should beNull()
        }
    }

    test("getSchemeByUuid") {
        test("returns null if no such scheme can be found") {
            val list = TemplateList()

            list.getSchemeByUuid("46840969-0b31-4fef-92b0-42f34dbc9f8b") should beNull()
        }

        test("returns the scheme with the given UUID") {
            val list = TemplateList()

            val scheme = list.templates[0].schemes[0]

            list.getSchemeByUuid(scheme.uuid) shouldBeSameInstanceAs scheme
        }

        test("returns the template with the given UUID") {
            val list = TemplateList()

            val template = list.templates[0]

            list.getSchemeByUuid(template.uuid) shouldBeSameInstanceAs template
        }
    }


    test("doValidate") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "validation"),
                row("succeeds for default state", TemplateList(), null),
                row("succeeds with no templates", TemplateList(mutableListOf()), null),
                row("fails if multiple templates have the same name", TemplateList(mutableListOf(Template("same"), Template("same"))), "template_list.error.duplicate_name"),
                row("fails if the single template is invalid", TemplateList(mutableListOf(Template(schemes = mutableListOf(DummyScheme(valid = false))))), ""),
                row("fails if one of multiple templates is invalid", TemplateList(mutableListOf(Template("Valid"), Template("Invalid", mutableListOf(DummyScheme(valid = false))))), ""),
                //@formatter:on
            )
        ) { _, scheme, validation -> scheme shouldValidateAsBundle validation }
    }

    test("deepCopy") {
        lateinit var list: TemplateList


        beforeEach {
            list = TemplateList()
        }


        test("equals old instance") {
            list.deepCopy() shouldBe list
        }

        test("is independent of old instance") {
            val copy = list.deepCopy()

            list.templates[0].name = "other"

            copy.templates[0].name shouldNotBe list.templates[0].name
        }

        test("retains uuid if chosen") {
            list.deepCopy(true).uuid shouldBe list.uuid
        }

        test("replaces uuid if chosen") {
            list.deepCopy(false).uuid shouldNotBe list.uuid
        }
    }
})
