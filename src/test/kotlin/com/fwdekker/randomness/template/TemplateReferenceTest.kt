package com.fwdekker.randomness.template

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.integer.IntegerScheme
import icons.RandomnessIcons
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [TemplateReference].
 */
object TemplateReferenceTest : Spek({
    lateinit var templateList: TemplateList
    lateinit var reference: TemplateReference


    beforeEachTest {
        templateList = TemplateList(mutableListOf())
        reference = TemplateReference().also { it.templateList = Box({ templateList }) }
    }


    describe("parent") {
        it("fails if the parent is not in the given template list") {
            assertThatThrownBy { reference.parent }.isNotNull()
        }

        it("returns the template in the template list that contains the reference") {
            val template = Template("widow", mutableListOf(reference))
            templateList.templates = mutableListOf(Template("variety", mutableListOf(TemplateReference())), template)

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
                templateList.templates = mutableListOf(Template("oppose", mutableListOf(DummyScheme())))

                reference.templateUuid = "7a9b9822-c99e-41dc-8e6a-220ca4dec181"

                assertThat(reference.template).isNull()
            }

            it("returns the template with the given UUID if it is in the template list") {
                val template = Template("supply", mutableListOf(DummyScheme()))
                templateList.templates = mutableListOf(template)

                reference.templateUuid = template.uuid

                assertThat(reference.template).isEqualTo(template)
            }
        }

        describe("set") {
            it("sets the template UUID") {
                val template = Template("hotel", mutableListOf(DummyScheme()))
                templateList.templates = mutableListOf(template)

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
            val template = Template("milk", mutableListOf(DummyScheme()))
            templateList.templates = mutableListOf(template)

            reference.templateUuid = template.uuid

            assertThat(reference.name).isEqualTo("[milk]")
        }
    }

    describe("icons") {
        it("returns default icons if the template cannot be found") {
            reference.templateUuid = null

            assertThat(reference.icons).isEqualTo(RandomnessIcons.Data)
        }

        it("returns the template's icons") {
            val template = Template("milk", mutableListOf(IntegerScheme()))
            templateList.templates = mutableListOf(template)

            reference.templateUuid = template.uuid

            assertThat(reference.icons).isEqualTo(RandomnessIcons.Integer)
        }
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            reference.templateUuid = null

            assertThatThrownBy { reference.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        it("returns the referenced template's value") {
            templateList.templates = mutableListOf(Template(schemes = mutableListOf(DummyScheme.from("bus"))))

            reference.templateUuid = templateList.templates.single().uuid

            assertThat(reference.generateStrings(2))
                .containsExactlyElementsOf(templateList.templates.single().generateStrings(2))
        }

        it("returns an array of strings if the referenced scheme returns an array of schemes") {
            templateList.templates = mutableListOf(
                Template(
                    schemes = mutableListOf(DummyScheme.from("bus").also { it.decorator.enabled = true })
                )
            )

            reference.templateUuid = templateList.templates.single().uuid

            assertThat(reference.generateStrings(2))
                .containsExactlyElementsOf(templateList.templates.single().generateStrings(2))
        }
    }

    describe("setSettingsState") {
        it("overwrites the known list of templates") {
            val newSettings = SettingsState(templateList = TemplateList(mutableListOf()))

            reference.setSettingsState(newSettings)

            assertThat(+reference.templateList).isSameAs(newSettings.templateList)
        }
    }


    describe("doValidate") {
        it("passes for valid settings") {
            val template = Template("explain", mutableListOf(DummyScheme()))
            templateList.templates = mutableListOf(template)

            reference.templateUuid = template.uuid

            assertThat(reference.doValidate()).isNull()
        }

        //

        describe("decorator") {
            it("fails if the decorator is invalid") {
                reference.decorator.count = -36

                assertThat(reference.doValidate()).isNotNull()
            }
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            reference.templateUuid = "5d5e755f-73b9-4929-9878-17708d436f79"
            reference.decorator.count = 803

            val copy = reference.deepCopy()
            copy.templateUuid = "e5ffae74-0142-433f-a3f3-1b1bfa1aa0fa"
            copy.decorator.count = 431

            assertThat(reference.templateUuid).isEqualTo("5d5e755f-73b9-4929-9878-17708d436f79")
            assertThat(reference.decorator.count).isEqualTo(803)
        }

        it("retains the reference to the template list") {
            assertThat(+reference.deepCopy().templateList).isSameAs(+reference.templateList)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            reference.templateUuid = "1e97e778-8698-4c29-ad1a-2bd892be6292"
            reference.decorator.count = 249

            val newScheme = TemplateReference()
            newScheme.copyFrom(reference)

            assertThat(newScheme)
                .isEqualTo(reference)
                .isNotSameAs(reference)
            assertThat(+newScheme.templateList)
                .isSameAs(+reference.templateList)
            assertThat(newScheme.decorator)
                .isEqualTo(reference.decorator)
                .isNotSameAs(reference.decorator)
        }
    }
})
