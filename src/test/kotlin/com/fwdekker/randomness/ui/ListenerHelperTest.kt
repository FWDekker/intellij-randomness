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


    describe("updatePreviewOnUpdateOf") {
        it("updates when an activity table is updated") {
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

                    table.data = listOf("a")
                }

                assertThat(listenerInvoked).isTrue()
            } finally {
                ideaFixture.tearDown()
            }
        }

        it("updates when a group of JRadioButtons is updated") {
            GuiActionRunner.execute {
                val group = ButtonGroup()
                JRadioButton("a").also { group.add(it) }
                JRadioButton("b").also { group.add(it) }
                JRadioButton("c").also { group.add(it) }

                addChangeListenerTo(group, listener = listener)

                group.buttons()[1].isSelected = true
            }

            assertThat(listenerInvoked).isTrue()
        }

        it("updates when a JCheckBox is updated") {
            GuiActionRunner.execute {
                val checkBox = JCheckBox()

                addChangeListenerTo(checkBox, listener = listener)

                checkBox.isSelected = true
            }

            assertThat(listenerInvoked).isTrue()
        }

        it("updates when a JSpinner is updated") {
            GuiActionRunner.execute {
                val spinner = JSpinner()

                addChangeListenerTo(spinner, listener = listener)

                spinner.value = 5
            }

            assertThat(listenerInvoked).isTrue()
        }

        it("updates when a JTextField is updated") {
            GuiActionRunner.execute {
                val textField = JTextField()

                addChangeListenerTo(textField, listener = listener)

                textField.text = "delight"
            }

            assertThat(listenerInvoked).isTrue()
        }

        it("updates when a SchemeEditor is updated") {
            GuiActionRunner.execute {
                val editor = DummySchemeEditor()

                addChangeListenerTo(editor, listener = listener)

                editor.loadState(DummyScheme.from("prove"))
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
