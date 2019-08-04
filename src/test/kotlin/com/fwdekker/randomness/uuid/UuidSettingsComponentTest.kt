package com.fwdekker.randomness.uuid

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
        uuidSettings.enclosure = "'"

        uuidSettingsComponent = GuiActionRunner.execute<UuidSettingsComponent> { UuidSettingsComponent(uuidSettings) }
        uuidSettingsComponentConfigurable = UuidSettingsConfigurable(uuidSettingsComponent)
        frame = showInFrame(uuidSettingsComponent.getRootPane())
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the settings' enclosure") {
            frame.radioButton("enclosureNone").requireSelected(false)
            frame.radioButton("enclosureSingle").requireSelected(true)
            frame.radioButton("enclosureDouble").requireSelected(false)
            frame.radioButton("enclosureBacktick").requireSelected(false)
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
            GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }

            uuidSettingsComponent.saveSettings()

            assertThat(uuidSettings.enclosure).isEqualTo("`")
        }
    }

    describe("configurable") {
        it("returns the correct display name") {
            assertThat(uuidSettingsComponentConfigurable.displayName).isEqualTo("UUIDs")
        }

        describe("saving modifications") {
            it("accepts correct settings") {
                GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }

                uuidSettingsComponentConfigurable.apply()

                assertThat(uuidSettings.enclosure).isEqualTo("`")
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
                    GuiActionRunner.execute { frame.radioButton("enclosureNone").target().isSelected = true }

                    uuidSettingsComponentConfigurable.reset()
                }

                assertThat(uuidSettingsComponentConfigurable.isModified).isFalse()
            }
        }
    }
})
