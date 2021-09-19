package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.clickActionButton
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.openapi.options.ConfigurationException
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.JBSplitter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.AbstractComponentFixture
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [TemplateListConfigurable].
 */
object TemplateListConfigurableTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var state: SettingsState
    lateinit var configurable: TemplateListConfigurable


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()

        TemplateListConfigurable.CREATE_SPLITTER =
            { vertical, proportionKey, defaultProportion -> JBSplitter(vertical, proportionKey, defaultProportion) }
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        state = SettingsState(
            TemplateList(
                listOf(
                    Template("Whip", listOf(IntegerScheme(), StringScheme())),
                    Template("Ability", listOf(DecimalScheme(), WordScheme()))
                )
            )
        )
        state.templateList.applySettingsState(state)

        configurable = GuiActionRunner.execute<TemplateListConfigurable> { TemplateListConfigurable(state) }
        frame = Containers.showInFrame(configurable.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
        GuiActionRunner.execute { configurable.disposeUIResources() }
        ideaFixture.tearDown()
    }


    describe("createComponent") {
        it("returns the same instance each time") {
            assertThat(configurable.createComponent()).isSameAs(configurable.createComponent())
        }
    }

    describe("apply") {
        it("stores correct settings") {
            GuiActionRunner.execute {
                frame.tree().target().setSelectionRow(5)
                frame.spinner("minLength").target().value = 5
            }

            configurable.apply()

            assertThat((state.templateList.templates[1].schemes[1] as WordScheme).minLength).isEqualTo(5)
        }

        it("rejects incorrect settings") {
            GuiActionRunner.execute {
                frame.tree().target().setSelectionRow(5)
                frame.spinner("minLength").target().value = 500
            }

            assertThatThrownBy { configurable.apply() }.isInstanceOf(ConfigurationException::class.java)

            assertThat((state.templateList.templates[1].schemes[1] as WordScheme).minLength).isEqualTo(3)
        }
    }


    describe("loadState") {
        it("loads the list's schemes") {
            assertThat(GuiActionRunner.execute<Int> { frame.tree().target().rowCount }).isEqualTo(6)
        }
    }

    describe("readState") {
        it("returns the original state if no editor changes are made") {
            assertThat(configurable.readState()).isEqualTo(configurable.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.tree().target().clearSelection()
                frame.clickActionButton("Add")
            }

            val readScheme = configurable.readState()
            assertThat(readScheme.templateList.templates).hasSize(3)
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute {
                frame.tree().target().clearSelection()
                frame.clickActionButton("Add")
            }
            assertThat(configurable.isModified()).isTrue()

            GuiActionRunner.execute { configurable.loadState(configurable.readState()) }
            assertThat(configurable.isModified()).isFalse()

            assertThat(configurable.readState()).isEqualTo(configurable.originalState)
        }

        it("returns different instances of the settings") {
            val readState = configurable.readState()

            assertThat(readState).isNotSameAs(state)
            assertThat(readState.templateList).isNotSameAs(state.templateList)
            assertThat(readState.symbolSetSettings).isNotSameAs(state.symbolSetSettings)
            assertThat(readState.dictionarySettings).isNotSameAs(state.dictionarySettings)
        }

        it("retains the list's UUIDs") {
            val readState = configurable.readState()

            assertThat(readState.uuid).isEqualTo(state.uuid)
            assertThat(readState.templateList.uuid).isEqualTo(state.templateList.uuid)
            assertThat(readState.templateList.templates.map { it.uuid })
                .containsExactlyElementsOf(state.templateList.templates.map { it.uuid })
        }
    }


    describe("isModified") {
        it("is not modified if the settings have not been modified and the settings are valid") {
            assertThat(configurable.readState()).isEqualTo(state)
            assertThat(configurable.isModified).isFalse()
        }

        it("is modified if the settings have been modified") {
            GuiActionRunner.execute { frame.spinner("minValue").target().value = 7 }

            assertThat(configurable.readState()).isNotEqualTo(state)
            assertThat(configurable.isModified).isTrue()
        }

        it("is modified if the settings are unmodified but invalid") {
            state.templateList.templates = listOf(
                Template(schemes = listOf(IntegerScheme(), IntegerScheme(minValue = 743, maxValue = -420)))
            )
            GuiActionRunner.execute { configurable.reset() }

            assertThat(configurable.readState()).isEqualTo(state)
            assertThat(configurable.isModified).isTrue()
        }
    }

    describe("reset") {
        it("undoes changes to the initial selection") {
            GuiActionRunner.execute { frame.spinner("minValue").target().value = 7 }

            GuiActionRunner.execute { configurable.reset() }

            assertThat(frame.spinner("minValue").target().value).isEqualTo(0L)
        }

        it("retains the selection if `queueSelection` is null") {
            configurable.queueSelection = null

            GuiActionRunner.execute { configurable.reset() }

            assertThat(frame.tree().target().selectionRows).containsExactly(1)
        }

        it("selects the indicated template after reset") {
            configurable.queueSelection = state.templateList.templates[1].uuid

            GuiActionRunner.execute { configurable.reset() }

            assertThat(frame.tree().target().selectionRows).containsExactly(3)
        }

        it("does nothing if the indicated template could not be found") {
            configurable.queueSelection = "231ee9da-8f72-4535-b770-0119fdf68f70"

            GuiActionRunner.execute { configurable.reset() }

            assertThat(frame.tree().target().selectionRows).containsExactly(1)
        }
    }

    describe("addChangeListener") {
        it("invokes the listener if the model is changed") {
            GuiActionRunner.execute { frame.tree().target().setSelectionRow(1) }
            var invoked = 0
            GuiActionRunner.execute { configurable.addChangeListener { invoked++ } }

            GuiActionRunner.execute { frame.spinner("minValue").target().value = 321 }

            assertThat(invoked).isNotZero()
        }

        it("invokes the listener if a scheme is added") {
            GuiActionRunner.execute { frame.tree().target().clearSelection() }
            var invoked = 0
            GuiActionRunner.execute { configurable.addChangeListener { invoked++ } }

            GuiActionRunner.execute { frame.clickActionButton("Add") }

            assertThat(invoked).isNotZero()
        }

        it("invokes the listener if the selection is changed") {
            GuiActionRunner.execute { frame.tree().target().clearSelection() }
            var invoked = 0
            GuiActionRunner.execute { configurable.addChangeListener { invoked++ } }

            GuiActionRunner.execute { frame.tree().target().setSelectionRow(2) }

            assertThat(invoked).isNotZero()
        }
    }


    describe("scheme editor") {
        it("loads the selected scheme's editor") {
            GuiActionRunner.execute { frame.tree().target().setSelectionRow(2) }

            frame.spinner("minLength").requireVisible()
        }

        it("retains changes made in a scheme editor") {
            GuiActionRunner.execute {
                frame.tree().target().setSelectionRow(2)
                frame.spinner("minLength").target().value = 711
            }


            GuiActionRunner.execute {
                frame.tree().target().setSelectionRow(1)
                frame.tree().target().setSelectionRow(2)
            }

            frame.spinner("minLength").requireValue(711)
        }


        describe("editor creation") {
            data class Param(
                val name: String,
                val scheme: Scheme,
                val matcher: (FrameFixture) -> AbstractComponentFixture<*, *, *>
            )

            listOf(
                Param("integer", IntegerScheme()) { it.spinner("minValue") },
                Param("decimal", DecimalScheme()) { it.spinner("minValue") },
                Param("string", StringScheme()) { it.spinner("minLength") },
                Param("UUID", UuidScheme()) { it.radioButton("version1") },
                Param("word", WordScheme()) { it.spinner("minLength") },
                Param("literal", LiteralScheme()) { it.textBox("literal") },
                Param("template reference", TemplateReference()) { it.list() }
            ).forEach { (name, scheme, matcher) ->
                it("loads an editor for ${name}s") {
                    state.templateList.templates = listOf(Template(schemes = listOf(scheme)))
                    state.templateList.applySettingsState(state)

                    GuiActionRunner.execute { configurable.reset() }

                    matcher(frame).requireVisible()
                }
            }

            it("loads an editor for templates") {
                state.templateList.templates = listOf(Template(schemes = emptyList()))
                state.templateList.applySettingsState(state)

                GuiActionRunner.execute { configurable.reset() }

                frame.textBox("templateName").requireVisible()
            }

            it("throws an error for unknown scheme types") {
                state.templateList.templates = listOf(Template(schemes = listOf(DummyScheme.from("grain"))))
                state.templateList.applySettingsState(state)

                assertThatThrownBy { GuiActionRunner.execute { configurable.reset() } }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage("Unknown scheme type 'com.fwdekker.randomness.DummyScheme'.")
            }
        }
    }
})
