package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.ui.EditableDatum
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.table.TableView
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * GUI tests for [StringSettingsComponent].
 */
object StringSettingsComponentTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var stringSettings: StringSettings
    lateinit var stringSettingsComponent: StringSettingsComponent
    lateinit var symbolSetTable: TableView<EditableDatum<SymbolSet>>
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    @Suppress("UNCHECKED_CAST")
    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        stringSettings = StringSettings()
            .apply {
                currentScheme.minLength = 144
                currentScheme.maxLength = 719
                currentScheme.enclosure = "\""
                currentScheme.capitalization = CapitalizationMode.RANDOM
                currentScheme.symbolSetList = listOf(SymbolSet.ALPHABET, SymbolSet.DIGITS, SymbolSet.HEXADECIMAL)
                currentScheme.activeSymbolSetList = listOf(SymbolSet.ALPHABET, SymbolSet.HEXADECIMAL)
                currentScheme.excludeLookAlikeSymbols = true
            }

        stringSettingsComponent =
            GuiActionRunner.execute<StringSettingsComponent> { StringSettingsComponent(stringSettings) }
        frame = showInFrame(stringSettingsComponent.rootPane)

        symbolSetTable = frame.table().target() as TableView<EditableDatum<SymbolSet>>
    }

    afterEachTest {
        ideaFixture.tearDown()
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
            assertThat(symbolSetTable.items.map { it.datum })
                .containsExactly(SymbolSet.ALPHABET, SymbolSet.DIGITS, SymbolSet.HEXADECIMAL)
            assertThat(symbolSetTable.items.filter { it.active }.map { it.datum })
                .containsExactly(SymbolSet.ALPHABET, SymbolSet.HEXADECIMAL)
        }

        it("loads the settings' setting for excluding look-alike symbols") {
            frame.checkBox("excludeLookAlikeSymbolsCheckBox").requireSelected()
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute {
                frame.spinner("minLength").target().value = 445
                frame.spinner("maxLength").target().value = 803
                frame.radioButton("enclosureBacktick").target().isSelected = true
                frame.radioButton("capitalizationUpper").target().isSelected = true
                frame.checkBox("excludeLookAlikeSymbolsCheckBox").target().isSelected = false

                repeat(symbolSetTable.items.size) { symbolSetTable.listTableModel.removeRow(0) }
                symbolSetTable.listTableModel.addRow(EditableDatum(false, SymbolSet.BRACKETS))
                symbolSetTable.listTableModel.addRow(EditableDatum(true, SymbolSet.MINUS))
            }

            stringSettingsComponent.saveSettings()

            assertThat(stringSettings.currentScheme.minLength).isEqualTo(445)
            assertThat(stringSettings.currentScheme.maxLength).isEqualTo(803)
            assertThat(stringSettings.currentScheme.enclosure).isEqualTo("`")
            assertThat(stringSettings.currentScheme.capitalization).isEqualTo(CapitalizationMode.UPPER)
            assertThat(stringSettings.currentScheme.symbolSetList)
                .isEqualTo(listOf(SymbolSet.BRACKETS, SymbolSet.MINUS))
            assertThat(stringSettings.currentScheme.activeSymbolSetList).isEqualTo(listOf(SymbolSet.MINUS))
            assertThat(stringSettings.currentScheme.excludeLookAlikeSymbols).isEqualTo(false)
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
                assertThat(validationInfo?.message)
                    .isEqualTo("The minimum length should be greater than or equal to 1.")
            }
        }

        describe("symbol sets") {
            it("fails if a symbol set does not have a name") {
                val symbolSet = EditableDatum(false, SymbolSet("", "abc"))
                GuiActionRunner.execute { symbolSetTable.listTableModel.addRow(symbolSet) }

                val validationInfo = stringSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.panel("symbolSetPanel").target())
                assertThat(validationInfo?.message).isEqualTo("All symbol sets must have a name.")
            }

            it("fails if two symbol sets have the same name") {
                val symbolSet1 = EditableDatum(false, SymbolSet("name1", "abc"))
                val symbolSet2 = EditableDatum(false, SymbolSet("name1", "abc"))
                GuiActionRunner.execute {
                    symbolSetTable.listTableModel.addRow(symbolSet1)
                    symbolSetTable.listTableModel.addRow(symbolSet2)
                }

                val validationInfo = stringSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.panel("symbolSetPanel").target())
                assertThat(validationInfo?.message).isEqualTo("Symbol sets must have unique names.")
            }

            it("fails if a symbol set does not have symbols") {
                val symbolSet = EditableDatum(false, SymbolSet("name", ""))
                GuiActionRunner.execute { symbolSetTable.listTableModel.addRow(symbolSet) }

                val validationInfo = stringSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.panel("symbolSetPanel").target())
                assertThat(validationInfo?.message).isEqualTo("Symbol sets must have at least one symbol each.")
            }

            it("fails if no symbol sets are selected") {
                GuiActionRunner.execute {
                    symbolSetTable.items.forEachIndexed { i, _ -> symbolSetTable.model.setValueAt(false, i, 0) }
                }

                val validationInfo = stringSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.panel("symbolSetPanel").target())
                assertThat(validationInfo?.message).isEqualTo("Activate at least one symbol set.")
            }

            it("fails if only look-alike symbols are selected and look-alike symbols are excluded") {
                val lookAlikeSymbolSet = EditableDatum(false, SymbolSet("Look-alike", "l01"))
                GuiActionRunner.execute {
                    repeat(symbolSetTable.items.count()) { symbolSetTable.listTableModel.removeRow(0) }

                    symbolSetTable.listTableModel.addRow(lookAlikeSymbolSet)
                    symbolSetTable.model.setValueAt(true, 0, 0)
                }

                val validationInfo = stringSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.panel("symbolSetPanel").target())
                assertThat(validationInfo?.message).isEqualTo("Active symbol sets must contain at least one " +
                    "non-look-alike character if look-alike characters are excluded.")
            }
        }
    }
})
