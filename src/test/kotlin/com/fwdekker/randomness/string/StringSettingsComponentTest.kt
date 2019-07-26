package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.ui.JCheckBoxList
import com.intellij.openapi.options.ConfigurationException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.core.GenericTypeMatcher
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.finder.WindowFinder
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xdescribe
import java.awt.Dialog
import javax.swing.JButton


/**
 * GUI tests for [StringSettingsComponent].
 */
object StringSettingsComponentTest : Spek({
    lateinit var stringSettings: StringSettings
    lateinit var stringSettingsComponent: StringSettingsComponent
    lateinit var stringSettingsComponentConfigurable: StringSettingsConfigurable
    lateinit var componentSymbolSets: JCheckBoxList<SymbolSet>
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    @Suppress("UNCHECKED_CAST")
    beforeEachTest {
        stringSettings = StringSettings()
        stringSettings.minLength = 144
        stringSettings.maxLength = 719
        stringSettings.enclosure = "\""
        stringSettings.capitalization = CapitalizationMode.RANDOM
        stringSettings.symbolSetList = listOf(SymbolSet.ALPHABET, SymbolSet.DIGITS, SymbolSet.HEXADECIMAL)
        stringSettings.activeSymbolSetList = listOf(SymbolSet.ALPHABET, SymbolSet.HEXADECIMAL)

        stringSettingsComponent =
            GuiActionRunner.execute<StringSettingsComponent> { StringSettingsComponent(stringSettings) }
        stringSettingsComponentConfigurable = StringSettingsConfigurable(stringSettingsComponent)
        frame = showInFrame(stringSettingsComponent.getRootPane())

        componentSymbolSets = frame.table("symbolSets").target() as JCheckBoxList<SymbolSet>
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the settings' minimum value") {
            frame.spinner("minLength").requireValue(144)
        }

        it("loads the settings' maximum value") {
            frame.spinner("maxLength").requireValue(719)
        }

        it("loads the settings' enclosure") {
            frame.radioButton("enclosureNone").requireSelected(false)
            frame.radioButton("enclosureSingle").requireSelected(false)
            frame.radioButton("enclosureDouble").requireSelected(true)
            frame.radioButton("enclosureBacktick").requireSelected(false)
        }

        it("loads the settings' capitalization") {
            frame.radioButton("capitalizationLower").requireSelected(false)
            frame.radioButton("capitalizationUpper").requireSelected(false)
            frame.radioButton("capitalizationRandom").requireSelected(true)
        }

        it("loads the settings' symbol sets") {
            assertThat(componentSymbolSets.entries)
                .containsExactly(SymbolSet.ALPHABET, SymbolSet.DIGITS, SymbolSet.HEXADECIMAL)
            assertThat(componentSymbolSets.activeEntries)
                .containsExactly(SymbolSet.ALPHABET, SymbolSet.HEXADECIMAL)
        }
    }

    describe("input handling") {
        describe("length range") {
            it("truncates decimals in the minimum length") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 553.92f }

                frame.spinner("minLength").requireValue(553)
            }

            it("truncates decimals in the maximum length") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 796.01f }

                frame.spinner("maxLength").requireValue(796)
            }
        }

        describe("symbol set manipulation") {
            val subDialogMatcher = object : GenericTypeMatcher<Dialog>(Dialog::class.java) {
                override fun isMatching(component: Dialog?) = component?.title?.startsWith("Randomness - ") ?: false
            }
            val okButtonMatcher = object : GenericTypeMatcher<JButton>(JButton::class.java) {
                override fun isMatching(component: JButton?) = component?.text == "OK"
            }

            xdescribe("adding symbol sets") {
                it("adds a new symbol set") {
                    frame.button("symbolSetAdd").click()
                    val fixture = WindowFinder.findDialog(subDialogMatcher).using(frame.robot())
                    fixture.textBox("name").setText("glad")
                    fixture.textBox("symbols").setText("sorry")
                    fixture.button(okButtonMatcher).click()

                    assertThat(componentSymbolSets.entries).contains(SymbolSet("glad", "sorry"))
                }

                it("does not add a new symbol set with a name that is already in use") {
                    val duplicateName = componentSymbolSets.entries.first().name

                    frame.button("symbolSetAdd").click()
                    val fixture = WindowFinder.findDialog(subDialogMatcher).using(frame.robot())
                    fixture.textBox("name").setText(duplicateName)
                    fixture.textBox("symbols").setText("clear")
                    fixture.button(okButtonMatcher).click()

                    assertThat(componentSymbolSets.entries).doesNotContain(SymbolSet(duplicateName, "clear"))
                }
            }

            xdescribe("editing symbol sets") {
                it("changes the name of a symbol set") {
                    componentSymbolSets.entries.first().apply {
                        name = "old name"
                        symbols = "old symbols"
                    }

                    GuiActionRunner.execute { frame.table("symbolSets").target().addRowSelectionInterval(0, 0)}
                    frame.button("symbolSetEdit").click()

                    val fixture = WindowFinder.findDialog(subDialogMatcher).using(frame.robot())
                    fixture.textBox("name").setText("new name")
                    fixture.button(okButtonMatcher).click()

                    assertThat(componentSymbolSets.entries.first().name).isEqualTo("new name")
                    assertThat(componentSymbolSets.entries.first().symbols).isEqualTo("old symbols")
                }

                it("does not change the name if the name is already in use") {
                    val duplicateName = componentSymbolSets.entries.first().name
                    val initialEntries = componentSymbolSets.entries
                    GuiActionRunner.execute { frame.table("symbolSets").target().addRowSelectionInterval(1, 1) }

                    frame.button("symbolSetEdit").click()
                    val fixture = WindowFinder.findDialog(subDialogMatcher).using(frame.robot())
                    fixture.textBox("name").setText(duplicateName)
                    fixture.button(okButtonMatcher).click()

                    assertThat(componentSymbolSets.entries).isEqualTo(initialEntries)
                }

                it("changes the symbols of a symbol set") {
                    componentSymbolSets.entries.first().apply {
                        name = "old name"
                        symbols = "old symbols"
                    }
                    GuiActionRunner.execute { frame.table("symbolSets").target().addRowSelectionInterval(0, 0) }

                    frame.button("symbolSetEdit").click()
                    val fixture = WindowFinder.findDialog(subDialogMatcher).using(frame.robot())
                    fixture.textBox("symbols").setText("new symbols")
                    fixture.button(okButtonMatcher).click()

                    assertThat(componentSymbolSets.entries.first().name).isEqualTo("old name")
                    assertThat(componentSymbolSets.entries.first().symbols).isEqualTo("new symbols")
                }

                it("changes nothing when no symbol sets are highlighted") {
                    val initialEntries = componentSymbolSets.entries

                    GuiActionRunner.execute {
                        frame.table("symbolSets").target().clearSelection()
                        frame.button("symbolSetEdit").target().doClick()
                    }

                    assertThat(componentSymbolSets.entries).isEqualTo(initialEntries)
                }
            }

            describe("removing symbol sets") {
                it("removes the highlighted symbol set") {
                    val initialEntries = componentSymbolSets.entries

                    GuiActionRunner.execute {
                        frame.table("symbolSets").target().addRowSelectionInterval(2, 2)
                        frame.button("symbolSetRemove").target().doClick()
                    }

                    assertThat(componentSymbolSets.entries).isEqualTo(initialEntries.minus(initialEntries.last()))
                }

                it("removes nothing when no symbol sets are highlighted") {
                    val initialEntries = componentSymbolSets.entries

                    GuiActionRunner.execute {
                        frame.table("symbolSets").target().clearSelection()
                        frame.button("symbolSetRemove").target().doClick()
                    }

                    assertThat(componentSymbolSets.entries).isEqualTo(initialEntries)
                }
            }

            it("disables the edit and remove buttons when no symbol sets are highlighted") {
                GuiActionRunner.execute { frame.table("symbolSets").target().clearSelection() }

                frame.button("symbolSetEdit").requireDisabled()
                frame.button("symbolSetRemove").requireDisabled()
            }
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { stringSettingsComponent.loadSettings(StringSettings()) }

            assertThat(stringSettingsComponent.doValidate()).isNull()
        }

        describe("length range") {
            it("fails if the minimum length is negative") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = -161 }

                val validationInfo = stringSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
                assertThat(validationInfo?.message).isEqualTo("Enter a value greater than or equal to 1.")
            }

            it("fails if the minimum length is greater than the maximum length") {
                GuiActionRunner.execute {
                    frame.spinner("minLength").target().value = 234
                    frame.spinner("maxLength").target().value = 233
                }

                val validationInfo = stringSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
                assertThat(validationInfo?.message)
                    .isEqualTo("The maximum length should not be smaller than the minimum length.")
            }
        }

        describe("symbol sets") {
            it("fails if no symbol sets are selected") {
                GuiActionRunner.execute { componentSymbolSets.setActiveEntries(emptyList()) }

                val validationInfo = stringSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.table("symbolSets").target())
                assertThat(validationInfo?.message).isEqualTo("Select at least one symbol set.")
            }
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute {
                frame.spinner("minLength").target().value = 445
                frame.spinner("maxLength").target().value = 803
                frame.radioButton("enclosureBacktick").target().isSelected = true
                frame.radioButton("capitalizationUpper").target().isSelected = true
                componentSymbolSets.setEntries(listOf(SymbolSet.BRACKETS, SymbolSet.MINUS))
                componentSymbolSets.setActiveEntries(listOf(SymbolSet.MINUS))
            }

            stringSettingsComponent.saveSettings()

            assertThat(stringSettings.minLength).isEqualTo(445)
            assertThat(stringSettings.maxLength).isEqualTo(803)
            assertThat(stringSettings.enclosure).isEqualTo("`")
            assertThat(stringSettings.capitalization).isEqualTo(CapitalizationMode.UPPER)
            assertThat(stringSettings.symbolSetList).isEqualTo(listOf(SymbolSet.BRACKETS, SymbolSet.MINUS))
            assertThat(stringSettings.activeSymbolSetList).isEqualTo(listOf(SymbolSet.MINUS))
        }
    }

    describe("configurable") {
        it("returns the correct display name") {
            assertThat(stringSettingsComponentConfigurable.displayName).isEqualTo("Strings")
        }

        describe("saving modifications") {
            it("accepts correct settings") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 19 }
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 55 }

                stringSettingsComponentConfigurable.apply()

                assertThat(stringSettings.maxLength).isEqualTo(55)
            }

            it("rejects incorrect settings") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = -45 }

                Assertions.assertThatThrownBy { stringSettingsComponentConfigurable.apply() }
                    .isInstanceOf(ConfigurationException::class.java)
            }
        }

        describe("modification detection") {
            it("is initially unmodified") {
                assertThat(stringSettingsComponentConfigurable.isModified).isFalse()
            }

            it("modifies a single detection") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 91 }

                assertThat(stringSettingsComponentConfigurable.isModified).isTrue()
            }

            it("ignores an undone modification") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 84 }
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = stringSettings.maxLength }

                assertThat(stringSettingsComponentConfigurable.isModified).isFalse()
            }

            it("ignores saved modifications") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 204 }

                stringSettingsComponentConfigurable.apply()

                assertThat(stringSettingsComponentConfigurable.isModified).isFalse()
            }
        }

        describe("resets") {
            it("resets all fields properly") {
                val newSymbolSets = setOf(SymbolSet.ALPHABET, SymbolSet.SPECIAL)

                GuiActionRunner.execute {
                    frame.spinner("minLength").target().value = 75
                    frame.spinner("maxLength").target().value = 102
                    frame.radioButton("enclosureSingle").target().isSelected = true
                    frame.radioButton("capitalizationLower").target().isSelected = true
                    componentSymbolSets.setActiveEntries(newSymbolSets)

                    stringSettingsComponentConfigurable.reset()
                }

                assertThat(stringSettingsComponentConfigurable.isModified).isFalse()
            }
        }
    }
})
