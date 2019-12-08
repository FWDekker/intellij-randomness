package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * GUI tests for [UuidSettingsComponent].
 */
object UuidSettingsComponentTest : Spek({
    lateinit var uuidSettings: UuidSettings
    lateinit var uuidSettingsComponent: UuidSettingsComponent
    lateinit var uuidSettingsComponentConfigurable: UuidSettingsConfigurable
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        uuidSettings = UuidSettings()
        uuidSettings.version = 4
        uuidSettings.enclosure = "'"
        uuidSettings.capitalization = CapitalizationMode.UPPER
        uuidSettings.addDashes = false

        uuidSettingsComponent = GuiActionRunner.execute<UuidSettingsComponent> { UuidSettingsComponent(uuidSettings) }
        uuidSettingsComponentConfigurable = UuidSettingsConfigurable(uuidSettingsComponent)
        frame = showInFrame(uuidSettingsComponent.rootPane)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the settings' version") {
            frame.radioButton("version1").requireSelected(false)
            frame.radioButton("version4").requireSelected(true)
        }

        it("loads the settings' enclosure") {
            frame.radioButton("enclosureNone").requireSelected(false)
            frame.radioButton("enclosureSingle").requireSelected(true)
            frame.radioButton("enclosureDouble").requireSelected(false)
            frame.radioButton("enclosureBacktick").requireSelected(false)
        }

        it("loads the settings' capitalization mode") {
            frame.radioButton("capitalizationLower").requireSelected(false)
            frame.radioButton("capitalizationUpper").requireSelected(true)
        }

        it("loads the settings' add dashes option") {
            frame.checkBox("addDashesCheckBox").requireSelected(false)
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { uuidSettingsComponent.loadSettings(UuidSettings()) }

            assertThat(uuidSettingsComponent.doValidate()).isNull()
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute { frame.radioButton("version1").target().isSelected = true }
            GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }
            GuiActionRunner.execute { frame.radioButton("capitalizationUpper").target().isSelected = true }
            GuiActionRunner.execute { frame.checkBox("addDashesCheckBox").target().isSelected = true }

            uuidSettingsComponent.saveSettings()

            assertThat(uuidSettings.version).isEqualTo(1)
            assertThat(uuidSettings.enclosure).isEqualTo("`")
            assertThat(uuidSettings.capitalization).isEqualTo(CapitalizationMode.UPPER)
            assertThat(uuidSettings.addDashes).isEqualTo(true)
        }
    }

    describe("configurable") {
        it("returns the correct display name") {
            assertThat(uuidSettingsComponentConfigurable.displayName).isEqualTo("UUIDs")
        }

        describe("saving modifications") {
            it("accepts correct settings") {
                GuiActionRunner.execute { frame.radioButton("version1").target().isSelected = true }
                GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }
                GuiActionRunner.execute { frame.radioButton("capitalizationLower").target().isSelected = true }
                GuiActionRunner.execute { frame.checkBox("addDashesCheckBox").target().isSelected = false }

                uuidSettingsComponentConfigurable.apply()

                assertThat(uuidSettings.version).isEqualTo(1)
                assertThat(uuidSettings.enclosure).isEqualTo("`")
                assertThat(uuidSettings.capitalization).isEqualTo(CapitalizationMode.LOWER)
                assertThat(uuidSettings.addDashes).isEqualTo(false)
            }
        }

        describe("modification detection") {
            it("is initially unmodified") {
                assertThat(uuidSettingsComponentConfigurable.isModified).isFalse()
            }

            it("modifies a single detection") {
                GuiActionRunner.execute { frame.radioButton("enclosureDouble").target().isSelected = true }

                assertThat(uuidSettingsComponentConfigurable.isModified).isTrue()
            }

            it("ignores an undone modification") {
                GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }
                GuiActionRunner.execute { frame.radioButton("enclosureSingle").target().isSelected = true }

                assertThat(uuidSettingsComponentConfigurable.isModified).isFalse()
            }

            it("ignores saved modifications") {
                GuiActionRunner.execute { frame.radioButton("enclosureNone").target().isSelected = true }

                uuidSettingsComponentConfigurable.apply()

                assertThat(uuidSettingsComponentConfigurable.isModified).isFalse()
            }
        }

        describe("resets") {
            it("resets all fields properly") {
                GuiActionRunner.execute {
                    GuiActionRunner.execute { frame.radioButton("version1").target().isSelected = true }
                    GuiActionRunner.execute { frame.radioButton("enclosureNone").target().isSelected = true }
                    GuiActionRunner.execute { frame.radioButton("capitalizationLower").target().isSelected = true }
                    GuiActionRunner.execute { frame.checkBox("addDashesCheckBox").target().isSelected = true }

                    uuidSettingsComponentConfigurable.reset()
                }

                assertThat(uuidSettingsComponentConfigurable.isModified).isFalse()
            }
        }
    }
})
