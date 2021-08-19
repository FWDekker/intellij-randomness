package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.literal.LiteralScheme
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
        templateList = TemplateList(emptyList())
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
            assertThat(TemplateList(templates = emptyList()).doValidate()).isNull()
        }

        it("fails if the single template is invalid") {
            templateList.templates =
                listOf(Template(name = "Gold", schemes = listOf(DummyScheme.from(DummyScheme.INVALID_OUTPUT))))

            assertThat(templateList.doValidate()).isEqualTo("Gold > Dummy > Invalid input!")
        }

        it("fails if multiple templates have the same name") {
            templateList.templates =
                listOf(Template(name = "Solution"), Template(name = "Leg"), Template(name = "Solution"))

            assertThat(templateList.doValidate()).isEqualTo("There are multiple templates with the name 'Solution'.")
        }

        it("fails if one of multiple templates is invalid") {
            templateList.templates = listOf(
                Template(name = "Moment"),
                Template(name = "View", schemes = listOf(DummyScheme.from(DummyScheme.INVALID_OUTPUT))),
                Template(name = "Ahead"),
                Template(name = "Honesty")
            )

            assertThat(templateList.doValidate()).isEqualTo("View > Dummy > Invalid input!")
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            templateList.templates = listOf(Template(schemes = listOf(LiteralScheme("refuse"))))

            val copy = templateList.deepCopy()
            (copy.templates.first().schemes.first() as LiteralScheme).literal = "cheer"

            assertThat((templateList.templates.first().schemes.first() as LiteralScheme).literal).isEqualTo("refuse")
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            templateList.templates = listOf(Template(name = "Desert"), Template(name = "Care"))

            val newTemplateList = TemplateList(emptyList())
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
