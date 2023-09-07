package com.fwdekker.randomness

import com.fwdekker.randomness.ui.withName
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import java.awt.Component
import javax.swing.JCheckBox


/**
 * Unit tests for [SchemeEditor].
 */
object SchemeEditorTest : FunSpec({
    tags(NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture
    lateinit var editor: DummySchemeEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()
    }

    afterNonContainer {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    fun registerTestEditor(createEditor: () -> DummySchemeEditor) {
        editor = guiGet(createEditor)
        frame = Containers.showInFrame(editor.rootComponent)
    }


    context("components") {
        test("returns named components in order") {
            registerTestEditor {
                DummySchemeEditor {
                    panel {
                        row("Label") {
                            textField().withName("field1")
                            textField()
                            textField().withName("field3")
                        }
                    }
                }
            }

            editor.components.map { (it as Component).name } shouldContainExactly listOf("field1", "field3")
        }

        test("returns extra components") {
            registerTestEditor { DummySchemeEditor().also { it.addExtraComponent("extra") } }

            editor.components shouldContainExactly listOf("extra")
        }
    }

    context("preferredFocusComponent") {
        test("returns the first visible named component") {
            registerTestEditor {
                DummySchemeEditor {
                    panel {
                        row("Label") {
                            textField()
                            spinner(0..10).withName("spinner").visible(false)
                            checkBox("Check").withName("checkbox")
                        }
                    }
                }
            }

            editor.preferredFocusedComponent should beInstanceOf<JCheckBox>()
        }

        test("returns null if there are no visible named components") {
            registerTestEditor {
                DummySchemeEditor {
                    panel {
                        row("Label") {
                            textField()
                            spinner(0..10).withName("spinner").visible(false)
                        }
                    }
                }
            }

            editor.preferredFocusedComponent should beNull()
        }
    }


    context("reset") {
        test("resets the editor to its original state") {
            registerTestEditor {
                val scheme = DummyScheme(prefix = "old")

                DummySchemeEditor(scheme) { panel { row { textField().bindText(scheme::prefix) } } }
            }
            frame.textBox().requireText("old")

            guiRun { frame.textBox().target().text = "new" }
            frame.textBox().requireText("new")
            guiRun { editor.reset() }

            frame.textBox().requireText("old")
        }

        test("resets decorators to their original state") {
            registerTestEditor {
                val decorator = DummyDecoratorScheme(append = "old")
                val scheme = DummyScheme(decorators = listOf(decorator))

                DummySchemeEditor(scheme) {
                    panel {
                        row {
                            DummyDecoratorSchemeEditor(decorator)
                                .also { addDecoratorEditor(it) }
                                .let { cell(it.rootComponent) }
                        }
                    }
                }
            }
            frame.textBox().requireText("old")

            guiRun { frame.textBox().target().text = "new" }
            frame.textBox().requireText("new")
            guiRun { editor.reset() }

            frame.textBox().requireText("old")
        }
    }

    context("apply") {
        test("applies changes from the editor") {
            val scheme = DummyScheme(prefix = "old")
            registerTestEditor { DummySchemeEditor(scheme) { panel { row { textField().bindText(scheme::prefix) } } } }

            guiRun { frame.textBox().target().text = "new" }
            editor.apply()

            scheme.name shouldBe "new"
        }

        test("applies changes from decorators") {
            val decorator = DummyDecoratorScheme(append = "old")
            val scheme = DummyScheme(decorators = listOf(decorator))
            registerTestEditor {
                DummySchemeEditor(scheme) {
                    panel {
                        row {
                            DummyDecoratorSchemeEditor(decorator)
                                .also { addDecoratorEditor(it) }
                                .let { cell(it.rootComponent) }
                        }
                    }
                }
            }

            guiRun { frame.textBox().target().text = "new" }
            editor.apply()

            scheme.decorators[0] shouldBeSameInstanceAs decorator
            (scheme.decorators[0] as DummyDecoratorScheme).append shouldBe "new"
        }
    }


    context("addChangeListener") {
        test("invokes the listener when a visible named component is edited") {
            registerTestEditor { DummySchemeEditor { panel { row { textField().withName("name") } } } }

            var updateCount = 0
            editor.addChangeListener { updateCount++ }
            guiRun { frame.textBox().target().text = "new" }

            updateCount shouldBe 1
        }

        test("invokes the listener when an extra component is edited") {
            registerTestEditor { DummySchemeEditor { panel { row { textField().also { addExtraComponent(it) } } } } }

            var updateCount = 0
            editor.addChangeListener { updateCount++ }
            guiRun { frame.textBox().target().text = "new" }

            updateCount shouldBe 1
        }

        test("invokes the listener when a decorator editor's component is edited") {
            registerTestEditor {
                val decorator = DummyDecoratorScheme(append = "old")
                val scheme = DummyScheme(decorators = listOf(decorator))

                DummySchemeEditor(scheme) {
                    panel {
                        row {
                            DummyDecoratorSchemeEditor(decorator)
                                .also { addDecoratorEditor(it) }
                                .let { cell(it.rootComponent) }
                        }
                    }
                }
            }

            var updateCount = 0
            editor.addChangeListener { updateCount++ }
            guiRun { frame.textBox().target().text = "new" }

            updateCount shouldBe 1
        }
    }
})
