package com.fwdekker.randomness.template

import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.clickActionButton
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.string.SymbolSet
import com.fwdekker.randomness.string.SymbolSetSettings
import com.fwdekker.randomness.ui.EditableDatum
import com.fwdekker.randomness.word.Dictionary
import com.fwdekker.randomness.word.DictionarySettings
import com.fwdekker.randomness.word.UserDictionary
import com.fwdekker.randomness.word.WordScheme
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.table.TableView
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.data.TableCell.row
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.regex.Pattern


/**
 * GUI tests for [TemplateListEditor].
 *
 * The majority of tests relies on the list of templates defined in the first `beforeEachTest`, so try to learn that
 * list by heart!
 *
 * @see TemplateTreeTest
 */
@Suppress("LargeClass") // Acceptable for tests
object TemplateListEditorTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var settingsState: SettingsState
    lateinit var editor: TemplateListEditor

    @Suppress("UNCHECKED_CAST") // Use with care
    val symbolSetTable = { (frame.table().target() as TableView<EditableDatum<SymbolSet>>).listTableModel }

    @Suppress("UNCHECKED_CAST") // Use with care
    val dictionaryTable = { (frame.table().target() as TableView<EditableDatum<Dictionary>>).listTableModel }


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        val templateList = TemplateList(
            listOf(
                Template("Further", listOf(IntegerScheme(), IntegerScheme(minValue = 7))),
                Template("Enclose", listOf(LiteralScheme("else"), WordScheme())),
                Template("Student", listOf(StringScheme(), LiteralScheme("dog"), IntegerScheme()))
            )
        )

        settingsState = SettingsState(
            templateList = templateList,
            symbolSetSettings = SymbolSetSettings(),
            dictionarySettings = DictionarySettings()
        )
        templateList.applySettingsState(settingsState)

        editor = GuiActionRunner.execute<TemplateListEditor> { TemplateListEditor(settingsState) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("buttons") {
        xdescribe("add") {
            // TODO: Find a way to test the addition popup
        }

        describe("remove") {
            it("does nothing if no node is selected") {
                GuiActionRunner.execute {
                    frame.tree().target().clearSelection()
                    frame.clickActionButton("Remove")
                }

                assertThat(editor.readState().templateList.templates.map { it.name })
                    .containsExactly("Further", "Enclose", "Student")
            }

            it("removes the selected template") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(3)
                    frame.clickActionButton("Remove")
                }

                assertThat(editor.readState().templateList.templates.map { it.name })
                    .containsExactly("Further", "Student")
            }

            it("removes the selected scheme") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(2)
                    frame.clickActionButton("Remove")
                }

                assertThat(editor.readState().templateList.templates[0].schemes).containsExactly(IntegerScheme())
            }
        }

        describe("copy") {
            it("does nothing if no node is selected") {
                GuiActionRunner.execute {
                    frame.tree().target().clearSelection()
                    frame.clickActionButton("Copy")
                }

                assertThat(editor.readState().templateList.templates.map { it.name })
                    .containsExactly("Further", "Enclose", "Student")
            }

            it("places a copy of the template underneath the selected template") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(3)
                    frame.clickActionButton("Copy")

                    // Update new template's name
                    frame.tree().target().setSelectionRow(6)
                    frame.textBox("templateName").target().text = "Ring"

                    // Adjust new template's scheme
                    frame.tree().target().setSelectionRow(7)
                    frame.textBox("literal").target().text = "speech"
                }

                val readList = editor.readState()
                assertThat(readList.templateList.templates.map { it.name })
                    .containsExactly("Further", "Enclose", "Ring", "Student")
                assertThat(readList.templateList.templates.map { it.schemes }[1][0])
                    .isEqualTo(LiteralScheme("else"))
                assertThat(readList.templateList.templates.map { it.schemes }[2][0])
                    .isEqualTo(LiteralScheme("speech"))
            }

            it("places a copy of the scheme underneath the selected scheme") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(4)
                    frame.clickActionButton("Copy")

                    frame.tree().target().setSelectionRow(5)
                    frame.textBox("literal").target().text = "what"
                }

                val readList = editor.readState()
                assertThat(readList.templateList.templates.map { it.schemes }[1][0])
                    .isEqualTo(LiteralScheme("else"))
                assertThat(readList.templateList.templates.map { it.schemes }[1][1])
                    .isEqualTo(LiteralScheme("what"))
            }
        }

        describe("move up/down") {
            it("does nothing if no node is selected") {
                GuiActionRunner.execute {
                    frame.tree().target().clearSelection()
                    frame.clickActionButton("Up")
                    frame.clickActionButton("Down")
                }

                assertThat(editor.readState().templateList.templates.map { it.name })
                    .containsExactly("Further", "Enclose", "Student")
            }

            it("marks the editor as modified if two templates are reordered") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(0)
                    frame.clickActionButton("Down")
                }

                assertThat(editor.isModified()).isTrue()
            }

            it("marks the editor as modified if two schemes are reordered") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(2)
                    frame.clickActionButton("Up")
                }

                assertThat(editor.isModified()).isTrue()
            }
        }
    }

    describe("selection after reload") {
        it("selects the desired queued selection if it exists") {
            GuiActionRunner.execute {
                editor.queueSelection = editor.originalState.templateList.templates[2].uuid
                editor.reset()
            }

            frame.tree().requireSelection(6)
        }

        it("selects the first scheme if the queued selection is invalid") {
            GuiActionRunner.execute {
                editor.queueSelection = IntegerScheme().uuid
                editor.reset()
            }

            frame.tree().requireSelection(1)
        }

        it("does not override the default selection if the desired queue selection is null") {
            GuiActionRunner.execute {
                editor.queueSelection = null
                editor.reset()
            }

            frame.tree().requireSelection(1)
        }
    }


    describe("loadState") {
        it("loads the list's templates") {
            val templates = SettingsState(TemplateList(listOf(Template(name = "Limb"), Template(name = "Pot"))))

            GuiActionRunner.execute { editor.loadState(templates) }

            assertThat(editor.readState().templateList.templates)
                .containsExactlyElementsOf(templates.templateList.templates)
        }

        it("loads the list's templates' schemes") {
            val schemes = listOf(
                listOf(IntegerScheme()),
                emptyList(),
                listOf(LiteralScheme(), DecimalScheme())
            )
            val templates = TemplateList(
                listOf(
                    Template("Prevent", schemes[0]),
                    Template("Being", schemes[1]),
                    Template("Coward", schemes[2])
                )
            )

            GuiActionRunner.execute { editor.loadState(SettingsState(templates)) }

            assertThat(editor.readState().templateList.templates.map { it.schemes }).containsExactlyElementsOf(schemes)
        }

        it("loads an empty tree if no templates are loaded") {
            GuiActionRunner.execute { editor.loadState(SettingsState(TemplateList(emptyList()))) }

            assertThat(editor.readState().templateList.templates).isEmpty()
        }
    }

    describe("readState") {
        describe("no changes made") {
            it("returns the original state if no editor changes are made") {
                assertThat(editor.readState()).isEqualTo(editor.originalState)
            }

            it("returns a different instance from the loaded scheme") {
                assertThat(editor.readState())
                    .isEqualTo(editor.originalState)
                    .isNotSameAs(editor.originalState)
            }
        }

        describe("manipulating a scheme") {
            it("unloads the scheme editor if the tree selection is cleared") {
                GuiActionRunner.execute { frame.tree().target().setSelectionRow(1) }
                frame.textBox("previewLabel").requireText(Pattern.compile(".+"))

                GuiActionRunner.execute { frame.tree().target().clearSelection() }
                frame.textBox("previewLabel").requireEmpty()
            }

            it("does not change the original symbol sets when a string scheme is changed") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(7)
                    symbolSetTable().addRow(EditableDatum(active = true, SymbolSet("ancient", "Fq9ohzV8")))
                }

                assertThat(editor.originalState.symbolSetSettings.symbolSets).doesNotContainKey("ancient")
            }

            it("does not change the original dictionaries when a word scheme is changed") {
                val dictionary = UserDictionary("nest")

                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(5)
                    dictionaryTable().addRow(EditableDatum(active = true, dictionary))
                }

                assertThat(editor.originalState.dictionarySettings.dictionaries).doesNotContain(dictionary)
            }

            it("returns a scheme with the changes in the current editor") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("minValue").target().value = 182
                }

                assertThat(editor.readState().templateList.templates[0].schemes[0])
                    .isEqualTo(IntegerScheme(minValue = 182))
            }

            it("returns a scheme with the changes from a previous editor of a different type") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("minValue").target().value = 593
                    frame.tree().target().setSelectionRow(4)
                }

                frame.textBox("literal").requireText("else") // Require that new editor has opened

                assertThat(editor.readState().templateList.templates[0].schemes[0])
                    .isEqualTo(IntegerScheme(minValue = 593))
            }

            it("returns a scheme with the changes from a previous editor of the same type") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("minValue").target().value = 746
                    frame.tree().target().setSelectionRow(2)
                }

                frame.spinner("minValue").requireValue(7L) // Require that new editor has opened

                assertThat(editor.readState().templateList.templates[0].schemes[0])
                    .isEqualTo(IntegerScheme(minValue = 746))
            }

            it("returns schemes with changes from previous editors") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("minValue").target().value = 867

                    frame.tree().target().setSelectionRow(2)
                    frame.spinner("minValue").target().value = 471
                }

                assertThat(editor.readState().templateList.templates[0].schemes)
                    .containsExactly(IntegerScheme(minValue = 867), IntegerScheme(minValue = 471))
            }

            it("returns a scheme with changes from the previous and the current editor") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("minValue").target().value = 494

                    frame.tree().target().setSelectionRow(2)

                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("maxValue").target().value = 502
                }

                assertThat(editor.readState().templateList.templates[0].schemes)
                    .contains(IntegerScheme(minValue = 494, maxValue = 502))
            }
        }

        describe("settingsState") {
            it("retains changes to symbol sets between different string schemes") {
                GuiActionRunner.execute {
                    editor.loadState(
                        SettingsState(
                            templateList = TemplateList.from(StringScheme(), StringScheme()),
                            symbolSetSettings = SymbolSetSettings(mapOf("pocket" to "0MLnYk5"))
                        )
                    )
                }

                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    symbolSetTable().addRow(EditableDatum(active = true, SymbolSet("finger", "Xg24tQ")))

                    frame.tree().target().setSelectionRow(2)
                }

                frame.table().requireCellValue(row(1).column(1), "finger")
            }

            it("retains changes to dictionaries between different word schemes") {
                GuiActionRunner.execute {
                    editor.loadState(
                        SettingsState(
                            templateList = TemplateList.from(WordScheme(), WordScheme()),
                            dictionarySettings = DictionarySettings()
                        )
                    )
                }

                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    dictionaryTable().addRow(EditableDatum(active = true, UserDictionary("rank")))

                    frame.tree().target().setSelectionRow(2)
                }

                frame.table().requireCellValue(row(1).column(2), "rank")
            }
        }
    }

    describe("applyState") {
        it("applies changes to symbol sets") {
            GuiActionRunner.execute {
                frame.tree().target().setSelectionRow(7)
                symbolSetTable().addRow(EditableDatum(active = true, SymbolSet("thumb", "4Tch7x7")))
            }

            GuiActionRunner.execute { editor.applyState() }

            assertThat(editor.originalState.symbolSetSettings.symbolSets).containsKey("thumb")
        }

        it("does not apply changes to symbol sets after a reset") {
            GuiActionRunner.execute {
                frame.tree().target().setSelectionRow(7)
                symbolSetTable().addRow(EditableDatum(active = true, SymbolSet("tell", "9Zchc3qs")))
            }

            GuiActionRunner.execute { editor.reset() }

            assertThat(editor.originalState.symbolSetSettings.symbolSets).doesNotContainKey("tell")
        }

        it("applies changes to dictionaries") {
            val dictionary = UserDictionary("basis")

            GuiActionRunner.execute {
                frame.tree().target().setSelectionRow(5)
                dictionaryTable().addRow(EditableDatum(active = true, dictionary))
            }

            GuiActionRunner.execute { editor.applyState() }

            assertThat(editor.originalState.dictionarySettings.dictionaries).contains(dictionary)
        }

        it("does not apply changes to dictionaries after a reset") {
            val dictionary = UserDictionary("guide")

            GuiActionRunner.execute {
                frame.tree().target().setSelectionRow(5)
                dictionaryTable().addRow(EditableDatum(active = true, dictionary))
            }

            GuiActionRunner.execute { editor.reset() }

            assertThat(editor.originalState.dictionarySettings.dictionaries).doesNotContain(dictionary)
        }
    }


    describe("doValidate") {
        it("returns null if all schemes and templates are valid") {
            assertThat(editor.doValidate()).isNull()
        }

        it("is invalid if the scheme in the current editor is invalid") {
            GuiActionRunner.execute {
                editor.loadState(
                    SettingsState(
                        templateList = TemplateList.from(WordScheme(), name = "Faith"),
                        dictionarySettings = DictionarySettings()
                    )
                )

                frame.spinner("minLength").target().value = 299
            }

            assertThat(editor.doValidate()).startsWith("Faith > Word > The longest word in the selected")
        }

        it("is invalid if a scheme not in the current editor is invalid") {
            GuiActionRunner.execute {
                editor.loadState(
                    SettingsState(
                        templateList = TemplateList.from(WordScheme(), IntegerScheme(), name = "Avoid"),
                        dictionarySettings = DictionarySettings()
                    )
                )

                frame.spinner("minLength").target().value = 299
                frame.tree().target().setSelectionRow(2)
            }

            assertThat(editor.doValidate()).startsWith("Avoid > Word > The longest word in the selected")
        }

        it("validates string schemes based on the unsaved symbol sets") {
            GuiActionRunner.execute {
                editor.loadState(
                    SettingsState(
                        templateList = TemplateList.from(StringScheme(), name = "Yes"),
                        symbolSetSettings = SymbolSetSettings(mapOf("fish" to "Z5a0"))
                    )
                )
            }

            GuiActionRunner.execute {
                frame.tree().target().setSelectionRow(1)
                symbolSetTable().removeRow(0)
            }

            assertThat(editor.doValidate()).isEqualTo("Yes > String > Add at least one symbol set.")
        }

        it("validates word schemes based on the unsaved dictionaries") {
            GuiActionRunner.execute {
                editor.loadState(
                    SettingsState(
                        templateList = TemplateList.from(WordScheme(), name = "War"),
                        dictionarySettings = DictionarySettings()
                    )
                )
            }

            GuiActionRunner.execute {
                frame.tree().target().setSelectionRow(1)
                dictionaryTable().removeRow(0)
                dictionaryTable().addRow(EditableDatum(active = true, UserDictionary("pearl.dic")))
            }

            assertThat(editor.doValidate()).isEqualTo("War > Word > Dictionary 'pearl.dic' is invalid: File not found.")
        }
    }


    describe("addChangeListener") {
        var listenerInvoked = false


        beforeEachTest {
            listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }
        }


        describe("manipulating the list of templates") {
            beforeEachTest {
                GuiActionRunner.execute { frame.tree().target().setSelectionRow(0) }
                listenerInvoked = false
            }


            it("invokes the listener if a template is added") {
                GuiActionRunner.execute { editor.addScheme(Template(name = "Fatten")) }

                assertThat(listenerInvoked).isTrue()
            }

            it("invokes the listener if a template is removed") {
                GuiActionRunner.execute { frame.clickActionButton("Remove") }

                assertThat(listenerInvoked).isTrue()
            }

            it("invokes the listener if a template is copied") {
                GuiActionRunner.execute { frame.clickActionButton("Copy") }

                assertThat(listenerInvoked).isTrue()
            }

            it("invokes the listener if templates are reordered") {
                GuiActionRunner.execute { frame.clickActionButton("Down") }

                assertThat(listenerInvoked).isTrue()
            }

            it("does not invoke the listener if a reorder button is pressed but the template cannot be moved") {
                GuiActionRunner.execute { frame.clickActionButton("Up") }

                assertThat(listenerInvoked).isFalse()
            }
        }

        describe("manipulating a list of schemes") {
            beforeEachTest {
                GuiActionRunner.execute { frame.tree().target().setSelectionRow(1) }
                listenerInvoked = false
            }


            it("invokes the listener if a template's scheme is added") {
                GuiActionRunner.execute { editor.addScheme(LiteralScheme()) }

                assertThat(listenerInvoked).isTrue()
            }

            it("invokes the listener if a template's scheme is removed") {
                GuiActionRunner.execute { frame.clickActionButton("Remove") }

                assertThat(listenerInvoked).isTrue()
            }

            it("invokes the listener if a template's schemes is copied") {
                GuiActionRunner.execute { frame.clickActionButton("Copy") }

                assertThat(listenerInvoked).isTrue()
            }

            it("invokes the listener if a template's schemes are reordered") {
                GuiActionRunner.execute { frame.clickActionButton("Down") }

                assertThat(listenerInvoked).isTrue()
            }

            it("does not invoke the listener if a reorder button is pressed but the template cannot be moved") {
                GuiActionRunner.execute { frame.clickActionButton("Up") }

                assertThat(listenerInvoked).isFalse()
            }
        }

        describe("manipulating a scheme") {
            beforeEachTest {
                GuiActionRunner.execute { frame.tree().target().setSelectionRow(1) }
                listenerInvoked = false
            }


            it("invokes the listener if a scheme is adjusted") {
                GuiActionRunner.execute { frame.spinner("minValue").target().value = 159 }

                assertThat(listenerInvoked).isTrue()
            }

            it("invokes the listener if a new editor is opened") {
                GuiActionRunner.execute { frame.tree().target().setSelectionRow(2) }

                assertThat(listenerInvoked).isTrue()
            }

            it("invokes the listener if a scheme's array decorator is toggled") {
                GuiActionRunner.execute { frame.checkBox("arrayEnabled").target().isSelected = true }

                assertThat(listenerInvoked).isTrue()
            }

            it("invokes the listener if a scheme's array decorator is adjusted") {
                GuiActionRunner.execute { frame.checkBox("arrayEnabled").target().isSelected = true }
                listenerInvoked = false

                GuiActionRunner.execute { frame.spinner("arrayCount").target().value = 14 }

                assertThat(listenerInvoked).isTrue()
            }
        }
    }
})
