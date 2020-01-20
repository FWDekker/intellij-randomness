package com.fwdekker.randomness

import com.intellij.openapi.options.ConfigurationException
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [SettingsConfigurable].
 */
object SettingsConfigurableTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var settings: DummySettings
    lateinit var settingsComponent: DummySettingsComponent
    lateinit var settingsComponentConfigurable: DummySettingsConfigurable
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        settings = DummySettings()
            .apply { currentScheme.count = 6 }

        settingsComponent = GuiActionRunner.execute<DummySettingsComponent> { DummySettingsComponent(settings) }
        settingsComponentConfigurable = DummySettingsConfigurable(settingsComponent)
        frame = Containers.showInFrame(settingsComponent.rootPane)
    }

    afterEachTest {
        ideaFixture.tearDown()
        frame.cleanUp()
    }


    describe("display name") {
        it("returns the correct display name") {
            assertThat(settingsComponentConfigurable.displayName).isEqualTo("Dummy")
        }
    }

    describe("saving modifications") {
        it("accepts correct settings") {
            GuiActionRunner.execute { frame.spinner("count").target().value = 39 }

            settingsComponentConfigurable.apply()

            assertThat(settings.currentScheme.count).isEqualTo(39)
        }

        it("rejects incorrect settings") {
            GuiActionRunner.execute { frame.spinner("count").target().value = -3 }

            Assertions.assertThatThrownBy { settingsComponentConfigurable.apply() }
                .isInstanceOf(ConfigurationException::class.java)
        }
    }

    describe("modification detection") {
        it("is initially unmodified") {
            assertThat(settingsComponentConfigurable.isModified).isFalse()
        }

        it("modifies a single detection") {
            GuiActionRunner.execute { frame.spinner("count").target().value = 124 }

            assertThat(settingsComponentConfigurable.isModified).isTrue()
        }

        it("ignores an undone modification") {
            GuiActionRunner.execute { frame.spinner("count").target().value = 17 }
            GuiActionRunner.execute { frame.spinner("count").target().value = settings.currentScheme.count }

            assertThat(settingsComponentConfigurable.isModified).isFalse()
        }

        it("ignores saved modifications") {
            GuiActionRunner.execute { frame.spinner("count").target().value = 110 }

            settingsComponentConfigurable.apply()

            assertThat(settingsComponentConfigurable.isModified).isFalse()
        }
    }

    describe("resets") {
        it("resets all fields to their initial values") {
            GuiActionRunner.execute {
                frame.spinner("count").target().value = 23

                settingsComponentConfigurable.reset()
            }

            assertThat(frame.spinner("count").target().value).isEqualTo(6)
        }

        it("is no longer marked as modified after a reset") {
            GuiActionRunner.execute {
                frame.spinner("count").target().value = 80

                settingsComponentConfigurable.reset()
            }

            assertThat(settingsComponentConfigurable.isModified).isFalse()
        }
    }
})
