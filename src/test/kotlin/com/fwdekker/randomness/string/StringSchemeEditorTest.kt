package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.ui.EditableDatum
import com.intellij.ui.table.TableView
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [StringSchemeEditor].
 */
object StringSchemeEditorTest : Spek({
    lateinit var frame: FrameFixture

    lateinit var symbolSetSettings: SymbolSetSettings
    lateinit var scheme: StringScheme
    lateinit var editor: StringSchemeEditor
    lateinit var symbolSetTable: TableView<EditableDatum<SymbolSet>>


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    @Suppress("UNCHECKED_CAST") // I checked it myself!
    beforeEachTest {
        symbolSetSettings = SymbolSetSettings()
        scheme = StringScheme(symbolSetSettings)
        editor = GuiActionRunner.execute<StringSchemeEditor> { StringSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)

        symbolSetTable = frame.table().target() as TableView<EditableDatum<SymbolSet>>
    }

    afterEachTest {
        frame.cleanUp()
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


    describe("loadScheme") {
        it("loads the settings' minimum length") {
            GuiActionRunner.execute { editor.loadScheme(StringScheme(minLength = 144, maxLength = 163)) }

            frame.spinner("minLength").requireValue(144)
        }

        it("loads the settings' maximum length") {
            GuiActionRunner.execute { editor.loadScheme(StringScheme(minLength = 372, maxLength = 719)) }

            frame.spinner("maxLength").requireValue(719)
        }

        it("loads the settings' enclosure") {
            GuiActionRunner.execute { editor.loadScheme(StringScheme(enclosure = "\"")) }

            frame.radioButton("enclosureNone").requireSelected(false)
            frame.radioButton("enclosureSingle").requireSelected(false)
            frame.radioButton("enclosureDouble").requireSelected(true)
            frame.radioButton("enclosureBacktick").requireSelected(false)
        }

        it("loads the settings' capitalization") {
            GuiActionRunner.execute { editor.loadScheme(StringScheme(capitalization = CapitalizationMode.RANDOM)) }

            frame.radioButton("capitalizationLower").requireSelected(false)
            frame.radioButton("capitalizationUpper").requireSelected(false)
            frame.radioButton("capitalizationRandom").requireSelected(true)
        }

        it("loads the settings' symbol sets") {
            val allSymbolSets = listOf(SymbolSet.ALPHABET, SymbolSet.DIGITS, SymbolSet.HEXADECIMAL)
            val activeSymbolSets = listOf(SymbolSet.ALPHABET, SymbolSet.HEXADECIMAL)

            GuiActionRunner.execute {
                editor.loadScheme(
                    StringScheme(
                        SymbolSetSettings().also { it.symbolSetList = allSymbolSets },
                        activeSymbolSets = activeSymbolSets.map { it.name }.toSet()
                    )
                )
            }

            assertThat(symbolSetTable.items.map { it.datum })
                .containsExactlyElementsOf(allSymbolSets)
            assertThat(symbolSetTable.items.filter { it.active }.map { it.datum })
                .containsExactlyElementsOf(activeSymbolSets)
        }

        it("loads the settings' setting for excluding look-alike symbols") {
            GuiActionRunner.execute { editor.loadScheme(StringScheme(excludeLookAlikeSymbols = true)) }

            frame.checkBox("excludeLookAlikeSymbolsCheckBox").requireSelected()
        }
    }

    describe("readScheme") {
        it("returns the original state if no editor changes are made") {
            assertThat(editor.readScheme()).isEqualTo(editor.originalScheme)
        }

        it("returns the editor's state") {
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

            val readScheme = editor.readScheme()
            assertThat(readScheme.minLength).isEqualTo(445)
            assertThat(readScheme.maxLength).isEqualTo(803)
            assertThat(readScheme.enclosure).isEqualTo("`")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.UPPER)
            assertThat(readScheme.activeSymbolSets).isEqualTo(listOf(SymbolSet.BRACKETS.name, SymbolSet.MINUS.name))
            assertThat(readScheme.excludeLookAlikeSymbols).isEqualTo(false)
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.spinner("minLength").target().value = 445 }
            assertThat(editor.isModified()).isTrue()

            editor.loadScheme(editor.readScheme())
            assertThat(editor.isModified()).isFalse()

            assertThat(editor.readScheme()).isEqualTo(editor.originalScheme)
        }
    }
})
