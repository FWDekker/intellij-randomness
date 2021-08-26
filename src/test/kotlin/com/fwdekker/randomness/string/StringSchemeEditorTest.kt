package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.array.ArraySchemeDecorator
import com.fwdekker.randomness.ui.EditableDatum
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
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
    lateinit var ideaFixture: IdeaTestFixture
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
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        symbolSetSettings = SymbolSetSettings()
        scheme = StringScheme(symbolSetSettings)
        editor = GuiActionRunner.execute<StringSchemeEditor> { StringSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)

        symbolSetTable = frame.table().target() as TableView<EditableDatum<SymbolSet>>
    }

    afterEachTest {
        frame.cleanUp()
        ideaFixture.tearDown()
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
        it("loads the scheme's minimum length") {
            GuiActionRunner.execute { editor.loadState(StringScheme(minLength = 144, maxLength = 163)) }

            frame.spinner("minLength").requireValue(144)
        }

        it("loads the scheme's maximum length") {
            GuiActionRunner.execute { editor.loadState(StringScheme(minLength = 372, maxLength = 719)) }

            frame.spinner("maxLength").requireValue(719)
        }

        it("loads the scheme's enclosure") {
            GuiActionRunner.execute { editor.loadState(StringScheme(enclosure = "\"")) }

            frame.radioButton("enclosureNone").requireSelected(false)
            frame.radioButton("enclosureSingle").requireSelected(false)
            frame.radioButton("enclosureDouble").requireSelected(true)
            frame.radioButton("enclosureBacktick").requireSelected(false)
        }

        it("loads the scheme's capitalization") {
            GuiActionRunner.execute { editor.loadState(StringScheme(capitalization = CapitalizationMode.RANDOM)) }

            frame.radioButton("capitalizationLower").requireSelected(false)
            frame.radioButton("capitalizationUpper").requireSelected(false)
            frame.radioButton("capitalizationRandom").requireSelected(true)
        }

        it("loads the scheme's symbol sets") {
            val allSymbolSets = listOf(SymbolSet.ALPHABET, SymbolSet.DIGITS, SymbolSet.HEXADECIMAL)
            val activeSymbolSets = listOf(SymbolSet.ALPHABET, SymbolSet.HEXADECIMAL)

            GuiActionRunner.execute {
                editor.loadState(
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

        it("loads the scheme's setting for excluding look-alike symbols") {
            GuiActionRunner.execute { editor.loadState(StringScheme(excludeLookAlikeSymbols = true)) }

            frame.checkBox("excludeLookAlikeSymbolsCheckBox").requireSelected()
        }
    }

    describe("readScheme") {
        describe("defaults") {
            it("returns default enclosure if no enclosure is selected") {
                GuiActionRunner.execute { editor.loadState(StringScheme(enclosure = "unsupported")) }

                assertThat(editor.readState().enclosure).isEqualTo(StringScheme.DEFAULT_ENCLOSURE)
            }

            it("returns default brackets if no capitalization is selected") {
                GuiActionRunner.execute { editor.loadState(StringScheme(capitalization = CapitalizationMode.DUMMY)) }

                assertThat(editor.readState().capitalization).isEqualTo(StringScheme.DEFAULT_CAPITALIZATION)
            }
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
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

            val readScheme = editor.readState()
            assertThat(readScheme.minLength).isEqualTo(445)
            assertThat(readScheme.maxLength).isEqualTo(803)
            assertThat(readScheme.enclosure).isEqualTo("`")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.UPPER)
            assertThat(readScheme.activeSymbolSets).containsExactly(SymbolSet.MINUS.name)
            assertThat(readScheme.excludeLookAlikeSymbols).isFalse()
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.spinner("minLength").target().value = 445 }
            assertThat(editor.isModified()).isTrue()

            GuiActionRunner.execute { editor.loadState(editor.readState()) }
            assertThat(editor.isModified()).isFalse()

            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns a different instance from the loaded scheme") {
            val readState = editor.readState()

            assertThat(readState)
                .isEqualTo(editor.originalState)
                .isNotSameAs(editor.originalState)
            assertThat(readState.symbolSetSettings)
                .isSameAs(editor.originalState.symbolSetSettings)
            assertThat(readState.decorator)
                .isEqualTo(editor.originalState.decorator)
                .isNotSameAs(editor.originalState.decorator)
        }

        it("retains the scheme's UUID") {
            assertThat(editor.readState().uuid).isEqualTo(editor.originalState.uuid)
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("minLength").target().value = 206 }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            GuiActionRunner.execute {
                editor.loadState(StringScheme(decorator = ArraySchemeDecorator(enabled = true)))
            }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("arrayCount").target().value = 528 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
