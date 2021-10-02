package com.fwdekker.randomness.ui

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.DummySchemeEditor
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.util.ui.CollectionItemEditor
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.GuiActionRunner
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JRadioButton
import javax.swing.JSpinner
import javax.swing.JTextField


/**
 * Unit tests for the extension functions in `ListenerHelperKt`.
 */
object ListenerHelperTest : Spek({
    var listenerInvoked = false
    lateinit var listener: () -> Unit


    beforeEachTest {
        listenerInvoked = false
        listener = { listenerInvoked = true }
    }


    describe("addChangeListenerTo") {
        it("invokes the listener when an ActivityTableModelEditor is updated") {
            val ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
            ideaFixture.setUp()

            try {
                GuiActionRunner.execute {
                    val itemEditor = object : CollectionItemEditor<EditableDatum<String>> {
                        override fun getItemClass() = EditableDatum(false, "")::class.java

                        override fun clone(item: EditableDatum<String>, forInPlaceEditing: Boolean) =
                            EditableDatum(item.active, item.datum)
                    }
                    val table = object : ActivityTableModelEditor<String>(arrayOf(), itemEditor, "", "") {}

                    addChangeListenerTo(table, listener = listener)
                    listenerInvoked = false

                    table.data = listOf("a")
                }

                assertThat(listenerInvoked).isTrue()
            } finally {
                ideaFixture.tearDown()
            }
        }

        it("invokes the listener when a ButtonGroup is updated") {
            GuiActionRunner.execute {
                val group = ButtonGroup()
                JRadioButton("a").also { group.add(it) }
                JRadioButton("b").also { group.add(it) }
                JRadioButton("c").also { group.add(it) }

                addChangeListenerTo(group, listener = listener)
                listenerInvoked = false

                group.buttons()[1].isSelected = true
            }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener when a JCheckBox is updated") {
            GuiActionRunner.execute {
                val checkBox = JCheckBox()

                addChangeListenerTo(checkBox, listener = listener)
                listenerInvoked = false

                checkBox.isSelected = true
            }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener when a JRadioButton is updated") {
            GuiActionRunner.execute {
                val radioButton = JRadioButton()

                addChangeListenerTo(radioButton, listener = listener)
                listenerInvoked = false

                radioButton.isSelected = true
            }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener when a JSpinner is updated") {
            GuiActionRunner.execute {
                val spinner = JSpinner()

                addChangeListenerTo(spinner, listener = listener)
                listenerInvoked = false

                spinner.value = 5
            }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener when a JTextField is updated") {
            GuiActionRunner.execute {
                val textField = JTextField()

                addChangeListenerTo(textField, listener = listener)
                listenerInvoked = false

                textField.text = "delight"
            }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener when a StateEditor is updated") {
            GuiActionRunner.execute {
                val editor = DummySchemeEditor()

                addChangeListenerTo(editor, listener = listener)
                listenerInvoked = false

                editor.loadState(DummyScheme.from("prove"))
            }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener when a VariableLabelRadioButton is changed") {
            GuiActionRunner.execute {
                val button = VariableLabelRadioButton()

                addChangeListenerTo(button, listener = listener)
                listenerInvoked = false

                button.label = "supply"
            }

            assertThat(listenerInvoked).isTrue()
        }

        it("fails if the given component is not recognized") {
            assertThatThrownBy { addChangeListenerTo("rock") {} }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Unknown component type 'java.lang.String'.")
        }
    }
})
