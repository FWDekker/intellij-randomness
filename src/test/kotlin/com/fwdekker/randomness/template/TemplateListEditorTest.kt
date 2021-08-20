package com.fwdekker.randomness.template

import com.fwdekker.randomness.clickActionButton
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.DictionarySettings
import com.fwdekker.randomness.word.WordScheme
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
 */
object TemplateListEditorTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var templateList: TemplateList
    lateinit var editor: TemplateListEditor


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        templateList = TemplateList(
            listOf(
                Template("Further", listOf(IntegerScheme(), IntegerScheme(minValue = 7))),
                Template("Enclose", listOf(LiteralScheme("else"))),
                Template("Student", listOf(StringScheme(), LiteralScheme("envelope"), IntegerScheme())),
            )
        )
        editor = GuiActionRunner.execute<TemplateListEditor> { TemplateListEditor(templateList) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("event handling") {
        describe("buttons") {
            describe("add template") {
                it("selects the added template") {
                    GuiActionRunner.execute {
                        frame.tree().target().setSelectionRow(0)
                        editor.addScheme(Template(name = "Child"))
                    }

                    frame.tree().requireSelection(3)
                    frame.textBox("templateName").requireText("Child")
                }

                it("marks the editor as modified if a template is added") {
                    GuiActionRunner.execute {
                        frame.tree().target().setSelectionRow(0)
                        editor.addScheme(Template(name = "Whip"))
                    }

                    assertThat(editor.isModified()).isTrue()
                }

                it("adds the template to the bottom if no node is selected") {
                    GuiActionRunner.execute {
                        frame.tree().target().clearSelection()
                        editor.addScheme(Template(name = "Elephant"))
                    }

                    assertThat(editor.readState().templates.map { it.name })
                        .containsExactly("Further", "Enclose", "Student", "Elephant")
                }

                it("adds the template underneath the currently selected template") {
                    GuiActionRunner.execute {
                        frame.tree().target().setSelectionRow(3)
                        editor.addScheme(Template(name = "Flower"))
                    }

                    assertThat(editor.readState().templates.map { it.name })
                        .containsExactly("Further", "Enclose", "Flower", "Student")
                }

                it("adds the template underneath the parent of the currently selected scheme") {
                    GuiActionRunner.execute {
                        frame.tree().target().setSelectionRow(4)
                        editor.addScheme(Template(name = "Shoe"))
                    }

                    assertThat(editor.readState().templates.map { it.name })
                        .containsExactly("Further", "Enclose", "Shoe", "Student")
                }
            }

            describe("add scheme") {
                it("selects the added scheme") {
                    GuiActionRunner.execute {
                        frame.tree().target().setSelectionRow(0)
                        editor.addScheme(LiteralScheme("beside"))
                    }

                    frame.tree().requireSelection(3)
                    frame.textBox("literal").requireText("beside")
                }

                it("marks the editor as modified if a scheme is added") {
                    GuiActionRunner.execute {
                        frame.tree().target().setSelectionRow(1)
                        editor.addScheme(LiteralScheme("extent"))
                    }

                    assertThat(editor.isModified()).isTrue()
                }

                it("fails if no node is selected") {
                    GuiActionRunner.execute {
                        frame.tree().target().clearSelection()

                        assertThatThrownBy { editor.addScheme(IntegerScheme()) }
                            .isInstanceOf(IllegalStateException::class.java)
                            .hasMessage("Cannot add non-template to root.")
                    }
                }

                it("adds the scheme at the bottom of the selected template") {
                    GuiActionRunner.execute {
                        frame.tree().target().setSelectionRow(3)
                        editor.addScheme(UuidScheme())
                    }

                    assertThat(editor.readState().templates[1].schemes)
                        .containsExactly(LiteralScheme("else"), UuidScheme())
                }

                it("adds the scheme underneath the currently selected scheme") {
                    GuiActionRunner.execute {
                        frame.tree().target().setSelectionRow(1)
                        editor.addScheme(DecimalScheme())
                    }

                    assertThat(editor.readState().templates[0].schemes)
                        .containsExactly(IntegerScheme(), DecimalScheme(), IntegerScheme(minValue = 7))
                }
            }

            describe("remove") {
                it("does nothing if no node is selected") {
                    GuiActionRunner.execute {
                        frame.tree().target().clearSelection()
                        frame.clickActionButton("Remove")
                    }
                }

                describe("remove template") {
                    it("marks the editor as modified if a single template is removed") {
                        GuiActionRunner.execute {
                            frame.tree().target().setSelectionRow(0)
                            frame.clickActionButton("Remove")
                        }

                        assertThat(editor.isModified()).isTrue()
                    }

                    it("marks the editor as modified if the last template is removed") {
                        GuiActionRunner.execute {
                            editor.loadState(TemplateList(listOf(Template(schemes = listOf(LiteralScheme())))))
                        }
                        require(!editor.isModified()) { "Editor incorrectly marked as modified." }

                        GuiActionRunner.execute {
                            frame.tree().target().setSelectionRow(0)
                            frame.clickActionButton("Remove")
                        }

                        assertThat(editor.isModified()).isTrue()
                    }

                    it("selects nothing if the last template is removed") {
                        GuiActionRunner.execute {
                            repeat(3) {
                                frame.tree().target().setSelectionRow(0)
                                frame.clickActionButton("Remove")
                            }
                        }

                        frame.tree().requireNoSelection()
                    }

                    it("selects the next template if a non-bottom template is removed") {
                        GuiActionRunner.execute {
                            frame.tree().target().setSelectionRow(3)
                            frame.clickActionButton("Remove")
                        }

                        frame.tree().requireSelection(3)
                        frame.textBox("templateName").requireText("Student")
                    }

                    it("selects the new bottom template if the bottom template is removed") {
                        GuiActionRunner.execute {
                            frame.tree().target().setSelectionRow(5)
                            frame.clickActionButton("Remove")
                        }

                        frame.tree().requireSelection(3)
                        frame.textBox("templateName").requireText("Enclose")
                    }
                }

                describe("remove scheme") {
                    it("selects the template if its last scheme is removed") {
                        GuiActionRunner.execute {
                            frame.tree().target().setSelectionRow(4)
                            frame.clickActionButton("Remove")
                        }

                        frame.tree().requireSelection(3)
                        frame.textBox("templateName").requireText("Enclose")
                    }

                    it("marks the editor as modified if a scheme is removed") {
                        GuiActionRunner.execute {
                            frame.tree().target().setSelectionRow(4)
                            frame.clickActionButton("Remove")
                        }

                        assertThat(editor.isModified()).isTrue()
                    }

                    it("selects the next scheme if a non-bottom scheme is removed") {
                        GuiActionRunner.execute {
                            frame.tree().target().setSelectionRow(6)
                            frame.clickActionButton("Remove")
                        }

                        frame.tree().requireSelection(6)
                        frame.textBox("literal").requireText("envelope")
                    }

                    it("selects the new bottom scheme if the bottom scheme is removed") {
                        GuiActionRunner.execute {
                            frame.tree().target().setSelectionRow(8)
                            frame.clickActionButton("Remove")
                        }

                        frame.tree().requireSelection(7)
                        frame.textBox("literal").requireText("envelope")
                    }
                }
            }

            describe("copy") {
                it("places a copy of the template underneath the selected template") {
                    GuiActionRunner.execute {
                        frame.tree().target().setSelectionRow(3)
                        frame.clickActionButton("Copy")

                        // Update new template's name
                        frame.tree().target().setSelectionRow(5)
                        frame.textBox("templateName").target().text = "Ring"

                        // Adjust new template's scheme
                        frame.tree().target().setSelectionRow(6)
                        frame.textBox("literal").target().text = "speech"
                    }

                    val readList = editor.readState()
                    assertThat(readList.templates.map { it.name })
                        .containsExactly("Further", "Enclose", "Ring", "Student")
                    assertThat(readList.templates.map { it.schemes }[1][0]).isEqualTo(LiteralScheme("else"))
                    assertThat(readList.templates.map { it.schemes }[2][0]).isEqualTo(LiteralScheme("speech"))
                }

                it("places a copy of the scheme underneath the selected scheme") {
                    GuiActionRunner.execute {
                        frame.tree().target().setSelectionRow(4)
                        frame.clickActionButton("Copy")

                        frame.tree().target().setSelectionRow(5)
                        frame.textBox("literal").target().text = "what"
                    }

                    val readList = editor.readState()
                    assertThat(readList.templates.map { it.schemes }[1][0]).isEqualTo(LiteralScheme("else"))
                    assertThat(readList.templates.map { it.schemes }[1][1]).isEqualTo(LiteralScheme("what"))
                }
            }

            describe("move up/down") {
                it("marks the editor as modified if two templates are reordered") {
                    GuiActionRunner.execute {
                        frame.tree().target().setSelectionRow(0)
                        frame.clickActionButton("Down")
                    }

                    assertThat(editor.isModified()).isTrue()
                }

                it("marks the editor as modified if two schemes are reordered") {
                    GuiActionRunner.execute {
                        frame.tree().target().setSelectionRow(1)
                        frame.clickActionButton("Down")
                    }

                    assertThat(editor.isModified()).isTrue()
                }
            }
        }

        describe("scheme editor") {
            it("unloads the scheme editor if the tree selection is cleared") {
                GuiActionRunner.execute { frame.tree().target().setSelectionRow(1) }
                frame.textBox("previewLabel").requireText(Pattern.compile(".+"))

                GuiActionRunner.execute { frame.tree().target().clearSelection() }
                frame.textBox("previewLabel").requireEmpty()
            }
        }
    }

    describe("selection after reload") {
        it("selects the first scheme after reload") {
            frame.tree().requireSelection(1)
        }

        it("selects the first template if it does not have any schemes") {
            GuiActionRunner.execute {
                editor.loadState(
                    TemplateList(listOf(Template("Flame", emptyList()), Template("Pen", listOf(IntegerScheme()))))
                )
            }

            frame.tree().requireSelection(0)
        }

        it("does nothing if no templates or schemes are loaded") {
            GuiActionRunner.execute { editor.loadState(TemplateList(emptyList())) }

            frame.tree().requireNoSelection()
        }


        describe("queueSelection") {
            it("selects the desired queued selection if it exists") {
                GuiActionRunner.execute {
                    editor.queueSelection = templateList.templates[2].uuid
                    editor.reset()
                }

                frame.tree().requireSelection(5)
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
    }


    describe("loadScheme") {
        it("loads the list's templates") {
            val templates = TemplateList(listOf(Template(name = "Limb"), Template(name = "Pot")))

            GuiActionRunner.execute { editor.loadState(templates) }

            assertThat(editor.readState().templates).containsExactlyElementsOf(templates.templates)
        }

        it("loads the list's templates' schemes") {
            val schemes = listOf(
                listOf(IntegerScheme()),
                emptyList(),
                listOf(LiteralScheme(), StringScheme())
            )
            val templates = TemplateList(
                listOf(
                    Template("Prevent", schemes[0]),
                    Template("Being", schemes[1]),
                    Template("Coward", schemes[2])
                )
            )

            GuiActionRunner.execute { editor.loadState(templates) }

            assertThat(editor.readState().templates.map { it.schemes }).containsExactlyElementsOf(schemes)
        }

        it("loads an empty tree if no templates are loaded") {
            GuiActionRunner.execute { editor.loadState(TemplateList(emptyList())) }

            assertThat(editor.readState().templates).isEmpty()
        }
    }

    describe("readScheme") {
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

        describe("manipulating the list of templates") {
            it("returns the list with a template added") {
                GuiActionRunner.execute { editor.addScheme(Template(name = "Prize")) }

                assertThat(editor.readState().templates).contains(Template(name = "Prize"))
            }

            it("returns the list with a template and all its children added") {
                val template = Template("Danger", listOf(LiteralScheme("Ill"), IntegerScheme(), StringScheme()))

                GuiActionRunner.execute { editor.addScheme(template) }

                assertThat(editor.readState().templates).contains(template)
            }

            it("returns the list with a template removed") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(0)
                    frame.clickActionButton("Remove")
                }

                assertThat(editor.readState().templates.map { it.name }).doesNotContain("Further")
            }

            it("returns the list with a template copied") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(3)
                    frame.clickActionButton("Copy")
                    frame.textBox("templateName").target().text = "Enjoy"
                }

                assertThat(editor.readState().templates.map { it.name }).contains("Enclose", "Enjoy")
            }

            it("returns the list with a template moved up") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(3)
                    frame.clickActionButton("Up")
                }

                assertThat(editor.readState().templates.map { it.name })
                    .containsExactly("Enclose", "Further", "Student")
            }

            it("returns the list with a template moved down") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(3)
                    frame.clickActionButton("Down")
                }

                assertThat(editor.readState().templates.map { it.name })
                    .containsExactly("Further", "Student", "Enclose")
            }

            it("returns an empty list if all templates are removed") {
                GuiActionRunner.execute {
                    repeat(3) {
                        frame.tree().target().setSelectionRow(0)
                        frame.clickActionButton("Remove")
                    }
                }

                assertThat(editor.readState().templates).isEmpty()
            }

            it("returns a list of the single template if a first template is added") {
                GuiActionRunner.execute { editor.loadState(TemplateList(emptyList())) }

                GuiActionRunner.execute { editor.addScheme(Template(name = "Seem")) }

                assertThat(editor.readState().templates).contains(Template(name = "Seem"))
            }
        }

        describe("manipulating a list of schemes") {
            it("returns the list with a scheme added") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(2)
                    editor.addScheme(IntegerScheme())
                }

                assertThat(editor.readState().templates[0].schemes).hasSize(3)
            }

            it("returns the list with a scheme removed") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    frame.clickActionButton("Remove")
                }

                assertThat(editor.readState().templates[0].schemes).containsExactly(IntegerScheme(minValue = 7))
            }

            it("returns the list with a scheme copied") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(7)
                    frame.clickActionButton("Copy")
                }

                assertThat(editor.readState().templates.last().schemes).hasSize(4)
            }

            it("returns the list with a scheme moved up") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(7)
                    frame.clickActionButton("Up")
                }

                assertThat(editor.readState().templates.last().schemes)
                    .containsExactly(LiteralScheme("envelope"), StringScheme(), IntegerScheme())
            }

            it("returns the list with a scheme moved down") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(6)
                    frame.clickActionButton("Down")
                }

                assertThat(editor.readState().templates.last().schemes)
                    .containsExactly(LiteralScheme("envelope"), StringScheme(), IntegerScheme())
            }

            it("returns an empty list if all schemes are removed from a template") {
                GuiActionRunner.execute {
                    repeat(3) {
                        frame.tree().target().setSelectionRow(0)
                        frame.clickActionButton("Remove")
                    }
                }

                assertThat(editor.readState().templates).isEmpty()
            }

            it("returns a list of the single scheme if a first scheme is added to a template") {
                GuiActionRunner.execute { editor.loadState(TemplateList(listOf(Template(schemes = emptyList())))) }

                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(0)
                    editor.addScheme(LiteralScheme("behavior"))
                }

                assertThat(editor.readState().templates[0].schemes).containsExactly(LiteralScheme("behavior"))
            }
        }

        describe("manipulating a scheme") {
            it("returns a scheme with the changes in the current editor") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("minValue").target().value = 182
                }

                assertThat(editor.readState().templates[0].schemes[0])
                    .isEqualTo(IntegerScheme(minValue = 182))
            }

            it("returns a scheme with the changes from a previous editor of a different type") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("minValue").target().value = 593
                    frame.tree().target().setSelectionRow(4)
                }

                frame.textBox("literal").requireText("else") // Require that new editor has opened

                assertThat(editor.readState().templates[0].schemes[0])
                    .isEqualTo(IntegerScheme(minValue = 593))
            }

            it("returns a scheme with the changes from a previous editor of the same type") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("minValue").target().value = 746
                    frame.tree().target().setSelectionRow(2)
                }

                frame.spinner("minValue").requireValue(7L) // Require that new editor has opened

                assertThat(editor.readState().templates[0].schemes[0])
                    .isEqualTo(IntegerScheme(minValue = 746))
            }

            it("returns schemes with changes from previous editors") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("minValue").target().value = 867

                    frame.tree().target().setSelectionRow(2)
                    frame.spinner("minValue").target().value = 471
                }

                assertThat(editor.readState().templates[0].schemes)
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

                assertThat(editor.readState().templates[0].schemes)
                    .contains(IntegerScheme(minValue = 494, maxValue = 502))
            }
        }
    }


    describe("doValidate") {
        it("returns null if all schemes and templates are valid") {
            assertThat(editor.doValidate()).isNull()
        }

        it("is invalid if the scheme in the current editor is invalid") {
            GuiActionRunner.execute {
                val template = Template("Faith", listOf(WordScheme(DictionarySettings())))
                editor.loadState(TemplateList(listOf(template)))

                frame.spinner("minLength").target().value = 299
            }

            assertThat(editor.doValidate()).startsWith("Faith > Word > The longest word in the selected")
        }

        it("is invalid if a scheme not in the current editor is invalid") {

            GuiActionRunner.execute {
                val template = Template("Avoid", listOf(WordScheme(DictionarySettings()), IntegerScheme()))
                editor.loadState(TemplateList(listOf(template)))

                frame.spinner("minLength").target().value = 299
                frame.tree().target().setSelectionRow(2)
            }

            assertThat(editor.doValidate()).startsWith("Avoid > Word > The longest word in the selected")
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
