package com.fwdekker.randomness

import com.fwdekker.randomness.DummyScheme.Companion.DEFAULT_COUNT
import com.fwdekker.randomness.Scheme.Companion.DEFAULT_NAME
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * Unit tests for [SettingsComponent].
 */
object SettingsComponentTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var settings: DummySettings
    lateinit var settingsComponent: DummySettingsComponent
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        settings = DummySettings()
            .apply {
                schemes = mutableListOf(
                    DummyScheme(count = 1),
                    DummyScheme(myName = "Scheme1", count = 3),
                    DummyScheme(myName = "Scheme2", count = 5)
                )
                currentSchemeName = "Scheme2"
            }

        settingsComponent = GuiActionRunner.execute<DummySettingsComponent> { DummySettingsComponent(settings) }
        frame = Containers.showInFrame(settingsComponent.rootPane)
    }

    afterEachTest {
        ideaFixture.tearDown()
        frame.cleanUp()
    }


    describe("schemes") {
        lateinit var schemesPanel: DummySettingsComponent.DummySchemesPanel
        lateinit var savedSettings: Settings<*, DummyScheme>
        lateinit var unsavedSettings: Settings<*, DummyScheme>


        beforeEachTest {
            schemesPanel = settingsComponent.schemesPanel
            savedSettings = settings
            unsavedSettings = schemesPanel.settings
        }


        it("loads the currently-selected scheme") {
            assertThat(frame.spinner("count").target().value).isEqualTo(5)

            assertThat(unsavedSettings.currentSchemeName).isEqualTo("Scheme2")
        }

        describe("modifying") {
            it("does not modify the scheme before saving") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 4 }

                assertThat(savedSettings.currentScheme.count).isEqualTo(5)
            }

            it("modifies the currently-selected scheme after saving") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 9 }

                settingsComponent.saveSettings()

                assertThat(savedSettings.currentScheme.count).isEqualTo(9)
            }
        }

        describe("switching") {
            it("does nothing if the switched-to scheme does not exist") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 96 }

                GuiActionRunner.execute { schemesPanel.actions.onSchemeChanged(null) }

                assertThat(unsavedSettings.currentSchemeName).isEqualTo("Scheme2")
                assertThat(frame.spinner("count").target().value).isEqualTo(96)
            }

            it("loads the scheme after switching") {
                GuiActionRunner.execute { schemesPanel.actions.onSchemeChanged(schemesPanel.getScheme("Scheme1")) }

                assertThat(unsavedSettings.currentSchemeName).isEqualTo("Scheme1")
                assertThat(frame.spinner("count").target().value).isEqualTo(3)
            }

            it("retains unsaved changes after switching schemes back and forth") {
                GuiActionRunner.execute {
                    frame.spinner("count").target().value = 29
                    schemesPanel.actions.onSchemeChanged(schemesPanel.getScheme("Scheme1"))
                    schemesPanel.actions.onSchemeChanged(schemesPanel.getScheme("Scheme2"))
                }

                assertThat(unsavedSettings.currentSchemeName).isEqualTo("Scheme2")
                assertThat(frame.spinner("count").target().value).isEqualTo(29)
            }
        }

        describe("renaming") {
            it("cannot rename the default scheme") {
                GuiActionRunner.execute { schemesPanel.actions.onSchemeChanged(schemesPanel.getScheme(DEFAULT_NAME)) }

                assertThatThrownBy { schemesPanel.actions.renameScheme(schemesPanel.getScheme(DEFAULT_NAME), "New") }
                    .isInstanceOf(IllegalArgumentException::class.java)
            }

            it("renames a scheme and thereby removes the old scheme") {
                GuiActionRunner.execute {
                    schemesPanel.actions.onSchemeChanged(schemesPanel.getScheme("Scheme1"))
                    schemesPanel.actions.renameScheme(schemesPanel.getScheme("Scheme1"), "Scheme1 New")
                }

                assertThat(unsavedSettings.currentSchemeName).isEqualTo("Scheme1 New")
                assertThat(unsavedSettings.schemes.none { it.name == "Scheme1" }).isTrue()
            }
        }

        describe("removing") {
            it("cannot remove the default scheme") {
                GuiActionRunner.execute { schemesPanel.actions.onSchemeChanged(schemesPanel.getScheme(DEFAULT_NAME)) }

                assertThatThrownBy { schemesPanel.removeScheme(schemesPanel.getScheme(DEFAULT_NAME)) }
                    .isInstanceOf(IllegalArgumentException::class.java)
            }

            it("removes the current scheme and switches to the default scheme") {
                GuiActionRunner.execute {
                    schemesPanel.actions.onSchemeChanged(schemesPanel.getScheme("Scheme1"))
                    schemesPanel.removeScheme(schemesPanel.getScheme("Scheme1"))
                }

                assertThat(unsavedSettings.currentSchemeName).isEqualTo("Default")
            }
        }

        describe("duplicating") {
            it("creates a new scheme with the same values") {
                GuiActionRunner.execute {
                    schemesPanel.actions.onSchemeChanged(schemesPanel.getScheme("Scheme1"))
                    schemesPanel.actions.duplicateScheme(schemesPanel.getScheme("Scheme1"), "Scheme1 Copy")
                }

                assertThat(unsavedSettings.currentSchemeName).isEqualTo("Scheme1 Copy")
                assertThat(frame.spinner("count").target().value).isEqualTo(3)
            }

            it("duplicates a scheme including its unsaved changes") {
                GuiActionRunner.execute {
                    schemesPanel.actions.onSchemeChanged(schemesPanel.getScheme("Scheme1"))
                    frame.spinner("count").target().value = 23
                }

                GuiActionRunner.execute {
                    schemesPanel.actions.duplicateScheme(schemesPanel.getScheme("Scheme1"), "Scheme1 Copy")
                    schemesPanel.actions.onSchemeChanged(schemesPanel.getScheme("Scheme1 Copy"))
                }

                assertThat(unsavedSettings.currentSchemeName).isEqualTo("Scheme1 Copy")
                assertThat(frame.spinner("count").target().value).isEqualTo(23)
            }
        }

        describe("resetting") {
            it("cannot reset the non-default scheme") {
                GuiActionRunner.execute { schemesPanel.actions.onSchemeChanged(schemesPanel.getScheme("Scheme1")) }

                assertThatThrownBy { schemesPanel.actions.resetScheme(schemesPanel.getScheme("Scheme1")) }
                    .isInstanceOf(IllegalArgumentException::class.java)
            }

            it("resets the default scheme") {
                GuiActionRunner.execute { schemesPanel.actions.resetScheme(schemesPanel.getScheme(DEFAULT_NAME)) }
                settingsComponent.saveSettings()

                assertThat(frame.spinner("count").target().value).isEqualTo(DEFAULT_COUNT)
            }
        }
    }
})
