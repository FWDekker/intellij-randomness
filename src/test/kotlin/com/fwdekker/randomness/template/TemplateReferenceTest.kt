package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.OverlayIcon
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.setAll
import com.fwdekker.randomness.shouldValidateAsBundle
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe


/**
 * Unit tests for [TemplateReference].
 */
object TemplateReferenceTest : FunSpec({
    tags(NamedTag("Scheme"))


    lateinit var list: TemplateList
    lateinit var referencingTemplate: Template
    lateinit var reference: TemplateReference
    lateinit var referencedTemplate: Template


    beforeEach {
        referencedTemplate = Template("referenced", mutableListOf(DummyScheme()))
        reference = TemplateReference(referencedTemplate.uuid)
        referencingTemplate = Template("referencing", mutableListOf(reference))
        list = TemplateList(mutableListOf(referencedTemplate, referencingTemplate))
        list.applyContext(Settings(list))
    }


    test("name") {
        test("returns an alternative name if no template is set") {
            reference.template = null

            reference.name shouldBe Bundle("reference.title")
        }

        test("returns the referenced template's name with brackets") {
            reference.name shouldBe "[referenced]"
        }
    }

    test("typeIcon") {
        test("uses the default type icon if the template is null") {
            reference.template = null

            reference.typeIcon shouldBe TemplateReference.DEFAULT_ICON
        }

        test("uses the default type icon if the template is not in the context's template list") {
            reference.template = Template("new")

            reference.typeIcon shouldBe TemplateReference.DEFAULT_ICON
        }

        test("uses the referenced template's type icon") {
            reference.typeIcon shouldBe referencedTemplate.typeIcon
        }
    }

    test("icon") {
        test("uses the default icon with a link overlay if the template is null") {
            reference.template = null

            reference.icon.base shouldBe TemplateReference.DEFAULT_ICON
            reference.icon.overlays shouldContainExactly listOf(OverlayIcon.REFERENCE)
        }

        test("uses the default icon with a link overlay if the template is not in the context's template list") {
            reference.template = Template("new")

            reference.icon.base shouldBe TemplateReference.DEFAULT_ICON
            reference.icon.overlays shouldContainExactly listOf(OverlayIcon.REFERENCE)
        }

        test("uses the referenced template's icon with a link overlay") {
            reference.icon.base shouldBe referencedTemplate.typeIcon
            reference.icon.overlays shouldContainExactly listOf(OverlayIcon.REFERENCE)
        }

        test("appends the link overlay to its decorators' overlays") {
            reference.arrayDecorator.enabled = true

            reference.icon.overlays shouldContainExactly listOf(reference.arrayDecorator.icon, OverlayIcon.REFERENCE)
        }
    }

    test("parent") {
        test("throws an exception if the reference is not in the context's template list") {
            list.templates.clear()

            shouldThrow<NoSuchElementException> { reference.parent }
        }

        test("throws an exception if the reference is twice in the context's template list") {
            list.templates.setAll(listOf(referencingTemplate, referencingTemplate))

            shouldThrow<IllegalArgumentException> { reference.parent }
        }

        test("returns the template in the context's template list that contains the reference") {
            reference.parent shouldBe referencingTemplate
        }
    }

    test("template") {
        test("get") {
            test("returns null if the UUID is null") {
                reference.templateUuid = null

                reference.template should beNull()
            }

            test("returns null if there is no template with the given UUID in the template list") {
                reference.templateUuid = "7a9b9822-c99e-41dc-8e6a-220ca4dec181"

                reference.template should beNull()
            }

            test("returns the template with the given UUID if it is in the template list") {
                reference.template shouldBe referencedTemplate
            }

            test("returns the template including changes") {
                referencedTemplate.name = "new"

                reference.template?.name shouldBe "new"
            }
        }

        test("set") {
            test("sets the template UUID") {
                val template = Template("new")
                list.templates += template

                reference.template = template

                reference.templateUuid shouldBe template.uuid
            }

            test("sets the template UUID to null if the given template is null") {
                reference.template = null

                reference.templateUuid should beNull()
            }

            test("sets the template UUID regardless of the set template's contents") {
                val new = Template("new")
                list.templates += new
                reference.template = new

                val other = Template("other").also { it.uuid = new.uuid }
                reference.template = other

                reference.template?.name shouldBe "new"
            }
        }
    }


    test("applyContext") {
        test("changes the list of templates into which this reference refers") {
            reference.template shouldNot beNull()

            reference.applyContext(Settings(TemplateList(mutableListOf())))

            reference.template should beNull()
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
                    row("returns the referenced template's value", reference, "dummy0"),
                    row("capitalizes output", reference.also { it.capitalization = CapitalizationMode.UPPER }, "DUMMY0"),
                    row("applies decorators in order affix, array", reference.also { it.affixDecorator.enabled = true; it.arrayDecorator.enabled = true }, """["dummy0", "dummy1", "dummy2"]"""),
                    //@formatter:on
                )
            ) { _, scheme, output -> scheme.generateStrings()[0] shouldBe output }
        }


        test("returns the referenced template's value") {
            reference.generateStrings(2) shouldBe referencedTemplate.generateStrings(2)
        }

        test("returns the referenced template's decorated value") {
            referencedTemplate.arrayDecorator.also {
                it.enabled = true
                it.minCount = 6
                it.maxCount = 6
            }

            reference.templateUuid = referencedTemplate.uuid

            reference.generateStrings(2) shouldBe referencedTemplate.generateStrings(2)
        }
    }

    test("doValidate") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "validation"),
                row("succeeds for default state (given appropriate context)", reference, null),
                row("fails if no template is referred to", reference.also { it.template = null }, "reference.error.no_selection"),
                row("fails if the referred template cannot be found", reference.also { it.applyContext(Settings()) }, "reference.error.not_found"),
                row("fails if the reference is recursive", run { referencingTemplate.schemes += TemplateReference(reference.uuid); referencingTemplate.applyContext(Settings(list)); reference }, "reference.error.recursion"),
                row("fails if affix decorator is invalid", reference.also { it.affixDecorator.descriptor = """\""" }, ""),
                row("fails if array decorator is invalid", reference.also { it.arrayDecorator.minCount = -24 }, ""),
                //@formatter:on
            )
        ) { _, scheme, validation -> scheme shouldValidateAsBundle validation }
    }

    test("deepCopy") {
        lateinit var scheme: TemplateReference


        beforeEach {
            scheme = TemplateReference()
        }


        test("equals old instance") {
            scheme.deepCopy() shouldBe scheme
        }

        test("is independent of old instance") {
            val copy = scheme.deepCopy()

            scheme.templateUuid = "251c0737-b1f1-40ec-9dc1-80a6c43b4dcb"

            copy.templateUuid shouldNotBe scheme.templateUuid
        }

        test("retains uuid if chosen") {
            scheme.deepCopy(true).uuid shouldBe scheme.uuid
        }

        test("replaces uuid if chosen") {
            scheme.deepCopy(false).uuid shouldNotBe scheme.uuid
        }
    }
})
