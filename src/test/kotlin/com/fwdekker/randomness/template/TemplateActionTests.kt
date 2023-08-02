package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Icons
import com.fwdekker.randomness.OverlayIcon
import com.fwdekker.randomness.OverlayedIcon
import com.fwdekker.randomness.TypeIcon
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import java.awt.Color


/**
 * Unit tests for [TemplateGroupAction].
 */
object TemplateGroupActionTest : FunSpec({
    tags(NamedTag("IdeaFixture"))


    lateinit var ideaFixture: IdeaTestFixture


    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()
    }

    afterEach {
        ideaFixture.tearDown()
    }


    test("init") {
        test("uses the template's text, description, and icon") {
            val icon = TypeIcon(Icons.SCHEME, "txt", listOf(Color.BLUE))
            val template = Template("Name", mutableListOf(DummyScheme().also { it.typeIcon = icon }))

            val action = TemplateGroupAction(template)

            action.templatePresentation.text shouldBe template.name
            action.templatePresentation.description shouldBe "Inserts a(n) Name at all carets."
            action.templatePresentation.icon shouldBe template.icon
        }
    }
})

/**
 * Unit tests for [TemplateInsertAction].
 */
object TemplateInsertActionTest : FunSpec({
    test("init") {
        test("text and description") {
            val template = Template("Name")

            forAll(
                table(
                    headers("action", "name", "description"),
                    row(
                        TemplateInsertAction(template),
                        "Name",
                        "Inserts a(n) Name at all carets.",
                    ),
                    row(
                        TemplateInsertAction(template, array = true),
                        "Name Array",
                        "Inserts an array of Name at all carets.",
                    ),
                    row(
                        TemplateInsertAction(template, repeat = true),
                        "Name Repeat",
                        "Inserts the same Name at each caret.",
                    ),
                    row(
                        TemplateInsertAction(template, array = true, repeat = true),
                        "Name Repeat Array",
                        "Inserts the same array of Name at each caret.",
                    ),
                )
            ) { action, name, description ->
                action.text shouldBe name
                action.templatePresentation.description shouldBe description
            }
        }

        test("icon") {
            test("does not add a repeat overlay for a non-repeat variant") {
                val template = Template()
                val action = TemplateInsertAction(template, repeat = false)

                (action.templatePresentation.icon as OverlayedIcon).overlays shouldNotContain OverlayIcon.REPEAT
            }

            test("adds a repeat overlay for a repeat variant") {
                val template = Template()
                val action = TemplateInsertAction(template, repeat = true)

                (action.templatePresentation.icon as OverlayedIcon).overlays shouldContain OverlayIcon.REPEAT
            }
        }
    }


    test("generateStrings") {
        test("returns the generated strings") {
            val template = Template(schemes = mutableListOf(DummyScheme()))

            val strings = TemplateInsertAction(template).generateStrings(2)

            strings shouldContainExactly listOf("dummy0", "dummy1")
        }

        test("repeats the generated strings") {
            val template = Template(schemes = mutableListOf(DummyScheme()))

            val strings = TemplateInsertAction(template, repeat = true).generateStrings(2)

            strings shouldContainExactly listOf("dummy0", "dummy0")
        }
    }
})

/**
 * Unit tests for [TemplateSettingsAction].
 */
object TemplateSettingsActionTest : FunSpec({
    tags(NamedTag("IdeaFixture"))


    lateinit var ideaFixture: IdeaTestFixture


    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()
    }

    afterEach {
        ideaFixture.tearDown()
    }


    test("init") {
        test("text") {
            test("uses a default text if the template is null") {
                val action = TemplateSettingsAction(template = null)

                action.templatePresentation.text shouldBe Bundle("template.name.settings")
            }

            test("uses the template's name if the template is not null") {
                val action = TemplateSettingsAction(Template("Name"))

                action.templatePresentation.text shouldBe "Name Settings"
            }
        }

        test("description") {
            test("has no description of the template is null") {
                val action = TemplateSettingsAction(template = null)

                action.templatePresentation.description should beNull()
            }

            test("uses the template's name to describe the action if the template is not null") {
                val action = TemplateSettingsAction(Template("Name"))

                action.templatePresentation.description shouldBe "Opens settings for Name."
            }
        }

        test("icon") {
            test("uses a default icon if the template is null") {
                val action = TemplateSettingsAction(template = null)

                action.templatePresentation.icon shouldBe Icons.SETTINGS
            }

            test("uses the template's icon if the template is not null") {
                val icon = TypeIcon(Icons.SCHEME, "war", listOf(Color.GREEN))
                val template = Template("subject", mutableListOf(DummyScheme().also { it.typeIcon = icon }))
                val action = TemplateSettingsAction(template)

                (action.templatePresentation.icon as OverlayedIcon).base shouldBe icon
            }
        }
    }
})
