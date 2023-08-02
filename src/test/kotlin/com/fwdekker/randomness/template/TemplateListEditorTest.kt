package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.datetime.DateTimeScheme
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.getActionButton
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.JBSplitter
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.AbstractComponentFixture
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [TemplateListEditor].
 */
object TemplateListEditorTest : DescribeSpec({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var state: Settings
    lateinit var editor: TemplateListEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()

        TemplateListEditor.createSplitter =
            { vertical, proportionKey, defaultProportion -> JBSplitter(vertical, proportionKey, defaultProportion) }
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        state = Settings(
            TemplateList(
                listOf(
                    Template("Whip", listOf(IntegerScheme(), StringScheme())),
                    Template("Ability", listOf(DecimalScheme(), WordScheme()))
                )
            )
        )
        state.templateList.applyContext(state)

        editor = guiGet { TemplateListEditor(state) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        guiRun { editor.dispose() }
        ideaFixture.tearDown()
    }


    describe("loadState") {
        it("loads the list's schemes") {
            assertThat(guiRun { frame.tree().target().rowCount }).isEqualTo(6)
        }
    }

    describe("readState") {
        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            guiRun {
                frame.tree().target().selectionRows = intArrayOf(0)
                frame.getActionButton("Remove").click()
            }

            val readScheme = editor.readState()
            assertThat(readScheme.templateList.templates).hasSize(1)
        }

        it("returns the loaded state if no editor changes are made") {
            guiRun {
                frame.tree().target().selectionRows = intArrayOf(0)
                frame.getActionButton("Remove").click()
            }
            assertThat(editor.isModified()).isTrue()

            guiRun { editor.loadState(editor.readState()) }
            assertThat(editor.isModified()).isFalse()

            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns different instances of the settings") {
            val readState = editor.readState()

            assertThat(readState).isNotSameAs(state)
            assertThat(readState.templateList).isNotSameAs(state.templateList)
        }

        it("retains the list's UUIDs") {
            val readState = editor.readState()

            assertThat(readState.uuid).isEqualTo(state.uuid)
            assertThat(readState.templateList.uuid).isEqualTo(state.templateList.uuid)
            assertThat(readState.templateList.templates.map { it.uuid })
                .containsExactlyElementsOf(state.templateList.templates.map { it.uuid })
        }
    }


    describe("reset") {
        it("undoes changes to the initial selection") {
            guiRun {
                frame.tree().target().setSelectionRow(1)
                frame.spinner("minValue").target().value = 7
            }

            guiRun { editor.reset() }

            assertThat(frame.spinner("minValue").target().value).isEqualTo(0L)
        }

        it("retains the selection if `queueSelection` is null") {
            editor.queueSelection = null

            guiRun { editor.reset() }

            assertThat(frame.tree().target().selectionRows).containsExactly(0)
        }

        it("selects the indicated template after reset") {
            editor.queueSelection = state.templateList.templates[1].uuid

            guiRun { editor.reset() }

            assertThat(frame.tree().target().selectionRows).containsExactly(3)
        }

        it("does nothing if the indicated template could not be found") {
            editor.queueSelection = "231ee9da-8f72-4535-b770-0119fdf68f70"

            guiRun { editor.reset() }

            assertThat(frame.tree().target().selectionRows).containsExactly(0)
        }
    }

    describe("addChangeListener") {
        it("invokes the listener if the model is changed") {
            guiRun { frame.tree().target().setSelectionRow(1) }
            var invoked = 0
            guiRun { editor.addChangeListener { invoked++ } }

            guiRun { frame.spinner("minValue").target().value = 321 }

            assertThat(invoked).isNotZero()
        }

        it("invokes the listener if a scheme is removed") {
            guiRun { frame.tree().target().selectionRows = intArrayOf(0) }
            var invoked = 0
            guiRun { editor.addChangeListener { invoked++ } }

            guiRun { frame.getActionButton("Remove").click() }

            assertThat(invoked).isNotZero()
        }

        it("invokes the listener if the selection is changed") {
            guiRun { frame.tree().target().clearSelection() }
            var invoked = 0
            guiRun { editor.addChangeListener { invoked++ } }

            guiRun { frame.tree().target().setSelectionRow(2) }

            assertThat(invoked).isNotZero()
        }
    }


    describe("scheme editor") {
        it("loads the selected scheme's editor") {
            guiRun { frame.tree().target().setSelectionRow(2) }

            frame.textBox("pattern").requireVisible()
        }

        it("retains changes made in a scheme editor") {
            guiRun {
                frame.tree().target().setSelectionRow(2)
                frame.textBox("pattern").target().text = "strange"
            }

            guiRun {
                frame.tree().target().setSelectionRow(1)
                frame.tree().target().setSelectionRow(2)
            }

            frame.textBox("pattern").requireText("strange")
        }


        describe("editor creation") {
            it("loads the appropriate editor") {
                forAll(
                    table(
                        headers("name", "scheme", "matcher"),
                        row("integer", IntegerScheme()) { it.spinner("minValue") },
                        row("decimal", DecimalScheme()) { it.spinner("minValue") },
                        row("string", StringScheme()) { it.textBox("pattern") },
                        row("uuid", UuidScheme()) { it.radioButton("type1") },
                        row("word", WordScheme()) { it.comboBox("presets") },
                        row("date-time", DateTimeScheme()) { it.textBox("minDateTime") },
                        row("template reference", TemplateReference()) { it.comboBox("template") },
                    )
                ) { _, scheme, matcher: (FrameFixture) -> AbstractComponentFixture<*, *, *> ->
                    state.templateList.templates = listOf(Template(schemes = listOf(scheme)))
                    state.templateList.applyContext(state)

                    guiRun {
                        editor.reset()
                        frame.tree().target().setSelectionRow(1)
                    }

                    matcher(frame).requireVisible()
                }
            }

            it("loads an editor for templates") {
                state.templateList.templates = listOf(Template(schemes = emptyList()))
                state.templateList.applyContext(state)

                guiRun { editor.reset() }

                frame.textBox("templateName").requireVisible()
            }

            it("throws an error for unknown scheme types") {
                state.templateList.templates = listOf(Template(schemes = listOf(DummyScheme.from("grain"))))
                state.templateList.applyContext(state)

                assertThatThrownBy {
                    guiRun {
                        editor.reset()
                        frame.tree().target().setSelectionRow(1)
                    }
                }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage("Unknown scheme type 'com.fwdekker.randomness.DummyScheme'.")
            }
        }
    }
})
