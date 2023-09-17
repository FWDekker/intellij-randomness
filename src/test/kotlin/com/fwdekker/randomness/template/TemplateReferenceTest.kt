package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.OverlayIcon
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.setAll
import com.fwdekker.randomness.stateDeepCopyTestFactory
import com.fwdekker.randomness.testhelpers.DummyScheme
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.shouldValidateAsBundle
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot


/**
 * Unit tests for [TemplateReference].
 */
object TemplateReferenceTest : FunSpec({
    tags(NamedTag("Scheme"))


    lateinit var list: TemplateList
    lateinit var referencingTemplate: Template
    lateinit var reference: TemplateReference
    lateinit var referencedTemplate: Template


    beforeNonContainer {
        referencedTemplate = Template("referenced", mutableListOf(DummyScheme()))
        reference = TemplateReference(referencedTemplate.uuid)
        referencingTemplate = Template("referencing", mutableListOf(reference))

        list = TemplateList(mutableListOf(referencedTemplate, referencingTemplate))
        list.applyContext(Settings(list))
    }


    context("name") {
        test("returns an alternative name if no template is set") {
            reference.template = null

            reference.name shouldBe Bundle("reference.title")
        }

        test("returns the referenced template's name with brackets") {
            reference.name shouldBe "[referenced]"
        }
    }

    context("typeIcon") {
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

    context("icon") {
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

    context("parent") {
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

    context("template") {
        context("get") {
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

        context("set") {
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


    context("applyContext") {
        test("changes the list of templates into which this reference refers") {
            reference.template shouldNot beNull()

            reference.applyContext(Settings(TemplateList(mutableListOf())))

            reference.template should beNull()
        }
    }

    context("canReference") {
        test("returns true if the reference would be outside its own context") {
            reference.canReference(Template()) shouldBe true
        }

        test("returns true if the reference would not create a cycle") {
            reference.canReference(referencedTemplate) shouldBe true
        }

        test("returns true if the reference would break the current cycle") {
            val safeTemplate = Template()
            list.templates += safeTemplate
            referencedTemplate.schemes += TemplateReference(referencingTemplate.uuid)

            list.applyContext(Settings(list))

            reference.canReference(safeTemplate) shouldBe true
        }

        test("returns false if the reference would refer to itself") {
            reference.canReference(referencingTemplate) shouldBe false
        }

        test("returns false if the reference would create a cycle") {
            referencedTemplate.schemes += TemplateReference(referencingTemplate.uuid)

            list.applyContext(Settings(list))

            reference.canReference(referencedTemplate) shouldBe false
        }

        test("returns false if the reference would refer to a cycle") {
            val otherReferencedTemplate = Template(schemes = mutableListOf(TemplateReference(referencedTemplate.uuid)))
            list.templates += otherReferencedTemplate
            referencedTemplate.schemes += TemplateReference(otherReferencedTemplate.uuid)

            list.applyContext(Settings(list))

            reference.canReference(referencedTemplate) shouldBe false
        }

        test("returns true if the reference would not create a cycle but a cycle exists elsewhere") {
            val cycleTemplate1 = Template()
            list.templates += cycleTemplate1
            val cycleTemplate2 = Template(schemes = mutableListOf(TemplateReference(cycleTemplate1.uuid)))
            list.templates += cycleTemplate2
            cycleTemplate1.schemes += TemplateReference(cycleTemplate2.uuid)

            list.applyContext(Settings(list))

            reference.canReference(referencedTemplate) shouldBe true
        }
    }


    context("generateStrings") {
        withData(
            mapOf(
                "returns the referenced template's value" to
                    row({ reference }, "text0"),
                "capitalizes output" to
                    row({ reference.also { it.capitalization = CapitalizationMode.UPPER } }, "TEXT0"),
                "applies decorators in order affix, array" to
                    row(
                        { reference.also { it.affixDecorator.enabled = true; it.arrayDecorator.enabled = true } },
                        "[text0, text1, text2]",
                    ),
            )
        ) { (scheme, output) -> scheme().generateStrings()[0] shouldBe output }


        test("returns the referenced template's raw value") {
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

    context("doValidate") {
        withData(
            mapOf(
                "succeeds for default state (given appropriate context)" to
                    row({ reference }, null),
                "fails if no template is referred to" to
                    row({ reference.also { it.template = null } }, "reference.error.no_selection"),
                "fails if the referred template cannot be found" to
                    row({ reference.also { it.applyContext(Settings()) } }, "reference.error.not_found"),
                "fails if the reference is recursive" to
                    row({ reference.also { it.template = referencingTemplate } }, "reference.error.recursion"),
                "fails if affix decorator is invalid" to
                    row({ reference.also { it.affixDecorator.descriptor = """\""" } }, ""),
                "fails if array decorator is invalid" to
                    row({ reference.also { it.arrayDecorator.minCount = -24 } }, ""),
            )
        ) { (scheme, validation) -> scheme() shouldValidateAsBundle validation }
    }

    include(stateDeepCopyTestFactory { TemplateReference() })
})
