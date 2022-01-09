package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.OverlayedIcon
import com.fwdekker.randomness.RandomnessIcons
import com.fwdekker.randomness.TypeIcon
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.awt.Color


/**
 * Unit tests for [TemplateGroupAction].
 */
class TemplateGroupActionTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()
    }

    afterEachTest {
        ideaFixture.tearDown()
    }


    describe("init") {
        describe("icon") {
            it("uses the template's icon as a base") {
                val icon = TypeIcon(RandomnessIcons.SCHEME, "t2Y", listOf(Color.BLUE))
                val template = Template("match", listOf(DummyScheme().also { it.typeIcon = icon }))
                val action = TemplateGroupAction(template)

                assertThat(action.templatePresentation.icon).isEqualTo(template.icon)
            }
        }

        describe("text") {
            it("uses the template's text") {
                val template = Template(name = "story")
                val action = TemplateGroupAction(template)

                assertThat(action.templatePresentation.text).isEqualTo(template.name)
            }
        }
    }
})


/**
 * Unit tests for [TemplateInsertAction].
 */
object TemplateInsertActionTest : Spek({
    describe("name") {
        it("returns the template's name") {
            val template = Template(name = "Head")
            val action = TemplateInsertAction(template)

            assertThat(action.text).isEqualTo("Head")
        }

        it("returns the template's name with 'repeat' before it") {
            val template = Template(name = "Sting")
            val action = TemplateInsertAction(template, repeat = true)

            assertThat(action.text).isEqualTo("Repeat Sting")
        }

        it("returns the template's name with 'array' after it") {
            val template = Template(name = "Invent")
            val action = TemplateInsertAction(template, array = true)

            assertThat(action.text).isEqualTo("Invent Array")
        }

        it("returns the template's name with 'repeat' before it and 'array' after it") {
            val template = Template(name = "Dust")
            val action = TemplateInsertAction(template, array = true, repeat = true)

            assertThat(action.text).isEqualTo("Repeat Dust Array")
        }
    }


    describe("generateStrings") {
        it("returns the generated strings") {
            val template = Template(schemes = listOf(DummyScheme.from("grass", "extent")))

            val strings = TemplateInsertAction(template).generateStrings(2)

            assertThat(strings).containsExactly("grass", "extent")
        }

        it("repeats the generated strings") {
            val template = Template(schemes = listOf(DummyScheme.from("refresh", "loss")))

            val strings = TemplateInsertAction(template, repeat = true).generateStrings(2)

            assertThat(strings).containsExactly("refresh", "refresh")
        }
    }
})

/**
 * Unit tests for [TemplateSettingsAction].
 */
object TemplateSettingsActionTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()
    }

    afterEachTest {
        ideaFixture.tearDown()
    }


    describe("init") {
        describe("text") {
            it("uses a default text if the template is null") {
                val action = TemplateSettingsAction(template = null)

                assertThat(action.templatePresentation.text).isEqualTo("Settings")
            }

            it("uses the template's name if the template is not null") {
                val action = TemplateSettingsAction(Template(name = "Broad"))

                assertThat(action.templatePresentation.text).isEqualTo("Broad Settings")
            }
        }

        describe("icon") {
            it("uses a default icon if the template is null") {
                val action = TemplateSettingsAction(template = null)

                assertThat(action.templatePresentation.icon).isEqualTo(RandomnessIcons.SETTINGS)
            }

            it("uses the template's icon if the template is not null") {
                val icon = TypeIcon(RandomnessIcons.SCHEME, "war", listOf(Color.GREEN))
                val template = Template("subject", listOf(DummyScheme().also { it.typeIcon = icon }))
                val action = TemplateSettingsAction(template)

                assertThat((action.templatePresentation.icon as OverlayedIcon).base).isEqualTo(icon)
            }
        }
    }
})
