package com.fwdekker.randomness.template

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.string.SymbolSetSettings
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [TemplateList].
 */
object TemplateListTest : Spek({
    lateinit var templateList: TemplateList


    beforeEachTest {
        templateList = TemplateList(mutableListOf())
    }


    describe("applySettingsState") {
        it("returns itself") {
            assertThat(templateList.applySettingsState(SettingsState())).isSameAs(templateList)
        }

        it("overwrites the settings of contained schemes") {
            val newSettings = SymbolSetSettings()
            val stringScheme = StringScheme()
            templateList.templates = mutableListOf(Template(schemes = mutableListOf(stringScheme)))

            templateList.applySettingsState(SettingsState(symbolSetSettings = newSettings))

            assertThat(+stringScheme.symbolSetSettings).isSameAs(newSettings)
        }
    }

    describe("findRecursionFrom") {
        it("returns null if the reference refers to `null`") {
            val reference = TemplateReference().also { it.templateList = Box({ TemplateList(mutableListOf()) }) }

            assertThat(TemplateList(mutableListOf()).findRecursionFrom(reference)).isNull()
        }

        it("returns null if the reference refers to a template not in this list") {
            val otherList = TemplateList.from(DummyScheme())
            val reference = TemplateReference(otherList.templates[0].uuid).also { it.templateList = Box({ otherList }) }

            assertThat(TemplateList(mutableListOf()).findRecursionFrom(reference)).isNull()
        }

        it("returns null if the recursion occurs only in another part of the list") {
            val otherTemplate = Template(name = "roast")

            val goodReference = TemplateReference(otherTemplate.uuid).also { it.templateList = Box({ templateList }) }
            val goodTemplate = Template("act", mutableListOf(goodReference))

            val badReference = TemplateReference().also { it.templateList = Box({ templateList }) }
            val badTemplate = Template("sour", mutableListOf(badReference))
            badReference.templateUuid = badTemplate.uuid

            templateList.templates = mutableListOf(otherTemplate, goodTemplate, badTemplate)

            assertThat(templateList.findRecursionFrom(goodReference)).isNull()
        }

        it("returns a short path if the reference refers to its parent") {
            val reference = TemplateReference()
            val list = TemplateList.from(reference)
            reference.templateUuid = list.templates[0].uuid
            reference.templateList = Box({ list })

            assertThat(list.findRecursionFrom(reference)).containsExactly(list.templates[0], list.templates[0])
        }

        it("returns a longer path if the reference consists of a chain") {
            val reference1 = TemplateReference().also { it.templateList = Box({ templateList }) }
            val template1 = Template("drop", mutableListOf(reference1))

            val reference2 = TemplateReference(template1.uuid).also { it.templateList = Box({ templateList }) }
            val template2 = Template("salesman", mutableListOf(reference2))

            val reference3 = TemplateReference(template2.uuid).also { it.templateList = Box({ templateList }) }
            val template3 = Template("almost", mutableListOf(reference3))

            reference1.templateUuid = template3.uuid

            templateList.templates = mutableListOf(template1, template2, template3)

            assertThat(templateList.findRecursionFrom(reference2))
                .containsExactly(template2, template1, template3, template2)
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            val ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
            ideaFixture.setUp()

            try {
                assertThat(TemplateList().doValidate()).isNull()
            } finally {
                ideaFixture.tearDown()
            }
        }

        it("passes for a template list without templates") {
            assertThat(TemplateList(templates = mutableListOf()).doValidate()).isNull()
        }

        it("fails if the single template is invalid") {
            templateList.templates =
                mutableListOf(Template("Gold", mutableListOf(DummyScheme.from(DummyScheme.INVALID_OUTPUT))))

            assertThat(templateList.doValidate()).startsWith("Gold > Dummy > ")
        }

        it("fails if multiple templates have the same name") {
            templateList.templates =
                mutableListOf(Template(name = "Solution"), Template(name = "Leg"), Template(name = "Solution"))

            assertThat(templateList.doValidate()).isEqualTo("There are multiple templates with the name 'Solution'.")
        }

        it("fails if one of multiple templates is invalid") {
            templateList.templates = mutableListOf(
                Template(name = "Moment"),
                Template(name = "View", schemes = mutableListOf(DummyScheme.from(DummyScheme.INVALID_OUTPUT))),
                Template(name = "Ahead"),
                Template(name = "Honesty")
            )

            assertThat(templateList.doValidate()).startsWith("View > Dummy > ")
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            templateList.templates = mutableListOf(Template(schemes = mutableListOf(LiteralScheme("refuse"))))

            val copy = templateList.deepCopy()
            (copy.templates.first().schemes.first() as LiteralScheme).literal = "cheer"

            assertThat((templateList.templates.first().schemes.first() as LiteralScheme).literal).isEqualTo("refuse")
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            templateList.templates = mutableListOf(Template(name = "Desert"), Template(name = "Care"))

            val newTemplateList = TemplateList(mutableListOf())
            newTemplateList.copyFrom(templateList)

            assertThat(newTemplateList)
                .isEqualTo(templateList)
                .isNotSameAs(templateList)
            assertThat(newTemplateList.templates.single { it.name == "Desert" })
                .isEqualTo(templateList.templates.single { it.name == "Desert" })
                .isNotSameAs(templateList.templates.single { it.name == "Desert" })
        }
    }
})
