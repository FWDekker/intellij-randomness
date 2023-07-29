package com.fwdekker.randomness.template

import com.fwdekker.randomness.MutableBox
import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.OverlayIcon
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.integer.IntegerScheme
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy


/**
 * Unit tests for [TemplateReference].
 */
object TemplateReferenceTest : DescribeSpec({
    lateinit var templateList: TemplateList
    lateinit var reference: TemplateReference
    lateinit var companion: Template


    beforeEach {
        reference = TemplateReference()
        companion = Template("station", listOf(DummyScheme.from("bus")))

        templateList = TemplateList(listOf(companion, Template("reference", listOf(reference))))
        reference.templateList = MutableBox({ templateList })
        reference.templateUuid = companion.uuid
    }


    describe("parent") {
        it("fails if the parent is not in the given template list") {
            templateList.templates = emptyList()

            assertThatThrownBy { reference.parent }.isNotNull()
        }

        it("returns the template in the template list that contains the reference") {
            val template = Template("widow", listOf(reference))
            templateList.templates = listOf(Template("variety", listOf(TemplateReference())), template)

            assertThat(reference.parent).isEqualTo(template)
        }
    }

    describe("template") {
        describe("get") {
            it("returns null if the UUID is null") {
                reference.templateUuid = null

                assertThat(reference.template).isNull()
            }

            it("returns null if there is no template with the given UUID in the template list") {
                templateList.templates = listOf(Template("oppose", listOf(DummyScheme())))

                reference.templateUuid = "7a9b9822-c99e-41dc-8e6a-220ca4dec181"

                assertThat(reference.template).isNull()
            }

            it("returns the template with the given UUID if it is in the template list") {
                val template = Template("supply", listOf(DummyScheme()))
                templateList.templates = listOf(template)

                reference.templateUuid = template.uuid

                assertThat(reference.template).isEqualTo(template)
            }
        }

        describe("set") {
            it("sets the template UUID") {
                val template = Template("hotel", listOf(DummyScheme()))
                templateList.templates = listOf(template)

                reference.template = template

                assertThat(reference.templateUuid).isEqualTo(template.uuid)
            }

            it("sets the template UUID to null if the given template is null") {
                reference.template = null

                assertThat(reference.templateUuid).isNull()
            }
        }
    }

    describe("name") {
        it("returns a simple string if the template cannot be found") {
            reference.templateUuid = null

            assertThat(reference.name).isEqualTo("Reference")
        }

        it("surrounds the target template's name with square brackets") {
            val template = Template("milk", listOf(DummyScheme()))
            templateList.templates = listOf(template)

            reference.templateUuid = template.uuid

            assertThat(reference.name).isEqualTo("[milk]")
        }
    }

    describe("icon") {
        it("uses the default type icon if the template cannot be found") {
            reference.templateUuid = null

            assertThat(reference.typeIcon).isEqualTo(TemplateReference.DEFAULT_ICON)
        }

        it("uses the template's type icon") {
            val template = Template("Modern", listOf(IntegerScheme()))
            templateList.templates = listOf(template)

            reference.templateUuid = template.uuid

            assertThat(reference.typeIcon).isEqualTo(template.typeIcon)
        }

        it("uses the default icon with a link overlay if the template cannot be found") {
            reference.templateUuid = null

            assertThat(reference.icon.base).isEqualTo(TemplateReference.DEFAULT_ICON)
            assertThat(reference.icon.overlays).containsExactly(OverlayIcon.REFERENCE)
        }

        it("uses the template's icon with a link overlay") {
            val template = Template("Milk", listOf(IntegerScheme()))
            templateList.templates = listOf(template)

            reference.templateUuid = template.uuid

            assertThat(reference.icon.base).isEqualTo(template.typeIcon)
            assertThat(reference.icon.overlays).containsExactly(OverlayIcon.REFERENCE)
        }

        it("appends the link overlay to its decorators' overlays") {
            val template = Template("Tour", listOf(IntegerScheme()))
            templateList.templates = listOf(template)

            reference.templateUuid = template.uuid
            reference.arrayDecorator.enabled = true

            assertThat(reference.icon.overlays)
                .containsExactly(reference.arrayDecorator.icon, OverlayIcon.REFERENCE)
        }
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            reference.templateUuid = null

            assertThatThrownBy { reference.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        it("returns the referenced template's value") {
            assertThat(reference.generateStrings(2)).isEqualTo(companion.generateStrings(2))
        }

        it("returns an array of strings if the referenced scheme returns an array of schemes") {
            companion.arrayDecorator.also {
                it.enabled = true
                it.minCount = 6
                it.maxCount = 6
            }

            reference.templateUuid = companion.uuid

            assertThat(reference.generateStrings(2)).isEqualTo(companion.generateStrings(2))
        }

        describe("quotation") {
            it("adds no quotations if the quotations are an empty string") {
                reference.quotation = ""

                assertThat(reference.generateStrings().single()).isEqualTo("bus")
            }

            it("repeats the first character of the quotations on both ends") {
                reference.quotation = "L"

                assertThat(reference.generateStrings().single()).isEqualTo("LbusL")
            }

            it("surrounds the output with the respective characters of the quotation string") {
                reference.quotation = "pn"

                assertThat(reference.generateStrings().single()).isEqualTo("pbusn")
            }
        }

        describe("capitalization") {
            it("applies the given capitalization") {
                reference.quotation = ""
                reference.capitalization = CapitalizationMode.UPPER

                assertThat(reference.generateStrings().single()).isEqualTo("BUS")
            }
        }
    }

    describe("setSettingsState") {
        it("overwrites the known list of templates") {
            val newSettings = Settings(templateList = TemplateList(emptyList()))

            reference.applyContext(newSettings)

            assertThat(+reference.templateList).isSameAs(newSettings.templateList)
        }
    }


    describe("doValidate") {
        it("passes for the default settings, after setting the template to reference") {
            assertThat(reference.doValidate()).isNull()
        }

        it("fails if the template UUID is null") {
            reference.templateUuid = null

            assertThat(reference.doValidate()).isEqualTo("Select a template to reference.")
        }

        it("fails if the template cannot be found in the list") {
            reference.templateUuid = "ca27e68d-11be-4679-af92-4f5f13c69772"

            assertThat(reference.doValidate()).isEqualTo("Cannot find referenced template.")
        }

        it("fails if the reference is recursive") {
            val otherReference = TemplateReference().also { it.templateList += templateList }

            val otherTemplate = Template("Ray", listOf(otherReference))
            val template = Template("Various", listOf(reference))
            otherReference.template = template
            reference.template = otherTemplate

            templateList.templates = listOf(template, otherTemplate)

            assertThat(reference.doValidate()).isEqualTo("Found recursion: (Various → Ray → Various)")
        }

        it("fails if the quotation has more than two characters") {
            reference.quotation = "week"

            assertThat(reference.doValidate()).isEqualTo(Bundle("reference.error.quotation_length"))
        }

        it("fails if the decorator is invalid") {
            reference.arrayDecorator.minCount = -36

            assertThat(reference.doValidate()).isNotNull()
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            reference.templateUuid = "5d5e755f-73b9-4929-9878-17708d436f79"
            reference.arrayDecorator.minCount = 803

            val copy = reference.deepCopy()
            copy.templateUuid = "e5ffae74-0142-433f-a3f3-1b1bfa1aa0fa"
            copy.arrayDecorator.minCount = 431

            assertThat(reference.templateUuid).isEqualTo("5d5e755f-73b9-4929-9878-17708d436f79")
            assertThat(reference.arrayDecorator.minCount).isEqualTo(803)
        }

        it("creates an independent copy of the template list box") {
            val copy = reference.deepCopy(retainUuid = true)
            copy.templateList += TemplateList()

            assertThat(+copy.templateList).isNotSameAs(+reference.templateList)
        }

        it("creates a shallow copy of the template list") {
            (+reference.templateList).templates = listOf(Template(schemes = listOf(reference)))

            val copy = reference.deepCopy()
            (+copy.templateList).templates = listOf(Template(schemes = listOf(copy, DummyScheme())))

            assertThat((+reference.templateList).templates[0].schemes).hasSize(2)
        }
    }

    describe("copyFrom") {
        it("cannot copy from a different type") {
            assertThatThrownBy { reference.copyFrom(DummyScheme()) }.isNotNull()
        }

        it("copies state from another instance") {
            reference.templateUuid = "1e97e778-8698-4c29-ad1a-2bd892be6292"
            reference.quotation = "fe"
            reference.capitalization = CapitalizationMode.RANDOM
            reference.arrayDecorator.minCount = 249

            val newScheme = TemplateReference()
            newScheme.copyFrom(reference)

            assertThat(newScheme)
                .isEqualTo(reference)
                .isNotSameAs(reference)
            assertThat(+newScheme.templateList)
                .isSameAs(+reference.templateList)
            assertThat(newScheme.arrayDecorator)
                .isEqualTo(reference.arrayDecorator)
                .isNotSameAs(reference.arrayDecorator)
        }

        it("updates the target's reference to the template list") {
            reference.copyFrom(TemplateReference().also { it.templateList += TemplateList(emptyList()) })

            assertThat(+reference.templateList).isNotSameAs(templateList)
        }

        it("writes a shallow copy of the given scheme's template list into the target") {
            val otherList = TemplateList.from(DummyScheme())
            val otherScheme = TemplateReference()
            otherScheme.templateList += otherList

            reference.copyFrom(otherScheme)
            (+otherScheme.templateList).templates = listOf(Template(name = "Realize"))

            assertThat((+reference.templateList).templates).containsExactly(Template(name = "Realize"))
        }
    }
})
