package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.OverlayedIcon
import com.fwdekker.randomness.RandomnessIcons
import com.fwdekker.randomness.TypeIcon
import com.intellij.testFramework.TestActionEvent
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

    lateinit var template: Template
    lateinit var groupAction: TemplateGroupAction


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        template = Template(name = "Former")
        groupAction = TemplateGroupAction(template)
    }

    afterEachTest {
        ideaFixture.tearDown()
    }


    describe("update") {
        describe("icon") {
            it("uses the template's icon as a base") {
                val typeIcon = TypeIcon(RandomnessIcons.SCHEME, "t2Y", listOf(Color.BLUE))
                template.schemes = listOf(DummyScheme().also { it.typeIcon = typeIcon })

                val event = TestActionEvent(groupAction)
                groupAction.update(event)

                assertThat((event.presentation.icon as OverlayedIcon).base).isEqualTo(typeIcon)
            }
        }

        describe("text") {
            it("uses the template's text") {
                val event = TestActionEvent(groupAction)
                groupAction.update(event)

                assertThat(event.presentation.text).isEqualTo("Former")
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
            assertThat(TemplateInsertAction(Template(name = "Head")).name).isEqualTo("Head")
        }

        it("returns the template's name with 'repeat' before it") {
            assertThat(TemplateInsertAction(Template(name = "Head"), repeat = true).name).isEqualTo("Repeat Head")
        }
    }
})

/**
 * Unit tests for [TemplateSettingsAction].
 */
object TemplateSettingsActionTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture

    lateinit var template: Template
    lateinit var settingsAction: TemplateSettingsAction


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        template = Template(name = "Broad")
        settingsAction = TemplateSettingsAction(template)
    }

    afterEachTest {
        ideaFixture.tearDown()
    }


    describe("update") {
        describe("text") {
            it("uses a default text if the template is null") {
                val event = TestActionEvent(settingsAction)
                TemplateSettingsAction(template = null).update(event)

                assertThat(event.presentation.text).isEqualTo("Settings")
            }

            it("uses the template's name if the template is not null") {
                val event = TestActionEvent(settingsAction)
                settingsAction.update(event)

                assertThat(event.presentation.text).isEqualTo("Broad Settings")
            }
        }

        describe("icon") {
            it("uses a default icon if the template is null") {
                val event = TestActionEvent(settingsAction)
                TemplateSettingsAction(template = null).update(event)

                assertThat(event.presentation.icon).isEqualTo(RandomnessIcons.SETTINGS)
            }

            it("uses the template's icon if the template is not null") {
                val typeIcon = TypeIcon(RandomnessIcons.SCHEME, "war", listOf(Color.GREEN))
                template.schemes = listOf(DummyScheme().also { it.typeIcon = typeIcon })

                val event = TestActionEvent(settingsAction)
                settingsAction.update(event)

                assertThat((event.presentation.icon as OverlayedIcon).base).isEqualTo(typeIcon)
            }
        }
    }
})
