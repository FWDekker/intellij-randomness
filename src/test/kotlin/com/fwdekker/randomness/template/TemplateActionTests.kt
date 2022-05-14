package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.OverlayIcon
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
object TemplateGroupActionTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()
    }

    afterEachTest {
        ideaFixture.tearDown()
    }


    describe("init") {
        it("uses the template's text, description, and icon") {
            val icon = TypeIcon(RandomnessIcons.SCHEME, "t2Y", listOf(Color.BLUE))
            val template = Template("Match", listOf(DummyScheme().also { it.typeIcon = icon }))
            val action = TemplateGroupAction(template)

            assertThat(action.templatePresentation.text).isEqualTo(template.name)
            assertThat(action.templatePresentation.description).isEqualTo("Inserts a(n) Match at all carets.")
            assertThat(action.templatePresentation.icon).isEqualTo(template.icon)
        }
    }
})


/**
 * Unit tests for [TemplateInsertAction].
 */
object TemplateInsertActionTest : Spek({
    describe("init") {
        describe("text and description") {
            it("returns the template's default variant") {
                val template = Template(name = "Head")
                val action = TemplateInsertAction(template)

                assertThat(action.text).isEqualTo("Head")
                assertThat(action.templatePresentation.description).isEqualTo("Inserts a(n) Head at all carets.")
            }

            it("returns the template's array variant") {
                val template = Template(name = "Case")
                val action = TemplateInsertAction(template, array = true)

                assertThat(action.text).isEqualTo("Case Array")
                assertThat(action.templatePresentation.description).isEqualTo("Inserts an array of Case at all carets.")
            }

            it("returns the template's repeat variant") {
                val template = Template(name = "Sting")
                val action = TemplateInsertAction(template, repeat = true)

                assertThat(action.text).isEqualTo("Sting Repeat")
                assertThat(action.templatePresentation.description).isEqualTo("Inserts the same Sting at each caret.")
            }

            it("returns the template's repeat-array variant") {
                val template = Template(name = "Dust")
                val action = TemplateInsertAction(template, array = true, repeat = true)

                assertThat(action.text).isEqualTo("Dust Repeat Array")
                assertThat(action.templatePresentation.description)
                    .isEqualTo("Inserts the same array of Dust at each caret.")
            }
        }

        describe("icon") {
            it("does not add a repeat overlay for a non-repeat variant") {
                val template = Template(name = "Woolen")
                val action = TemplateInsertAction(template, repeat = false)

                assertThat((action.templatePresentation.icon as OverlayedIcon).overlays)
                    .doesNotContain(OverlayIcon.REPEAT)
            }

            it("adds a repeat overlay for a repeat variant") {
                val template = Template(name = "Chalk")
                val action = TemplateInsertAction(template, repeat = true)

                assertThat((action.templatePresentation.icon as OverlayedIcon).overlays).contains(OverlayIcon.REPEAT)
            }
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

        describe("description") {
            it("has no description of the template is null") {
                val action = TemplateSettingsAction(Template(name = "Year"))

                assertThat(action.templatePresentation.description).isEqualTo("Opens settings for Year.")
            }

            it("uses the template's name to describe the action if the template is not null") {
                val action = TemplateSettingsAction(template = null)

                assertThat(action.templatePresentation.description).isNull()
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
