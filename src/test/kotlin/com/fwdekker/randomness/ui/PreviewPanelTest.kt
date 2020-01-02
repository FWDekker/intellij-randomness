package com.fwdekker.randomness.ui

import com.fwdekker.randomness.DummyInsertAction
import com.intellij.util.ui.CollectionItemEditor
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import java.util.ResourceBundle
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JRadioButton
import javax.swing.JSpinner


/**
 * Unit tests for [PreviewPanel].
 */
object PreviewPanelTest : Spek({
    var action: DummyInsertAction? = null

    val placeholder = ResourceBundle.getBundle("randomness").getString("settings.placeholder")
    val randomText = "random_value"
    val randomTextHtml = "<html>$randomText</html>"

    lateinit var panel: PreviewPanel
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        panel = GuiActionRunner.execute<PreviewPanel> {
            PreviewPanel { DummyInsertAction { randomText }.also { action = it } }
        }
        frame = Containers.showInFrame(panel.rootPane)

        assertThat(frame.label("previewLabel").text()).isEqualTo(placeholder)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("updatePreview") {
        it("updates the label's contents") {
            GuiActionRunner.execute { panel.updatePreview() }

            assertThat(frame.label("previewLabel").text()).isEqualTo("<html>random_value</html>")
        }
    }

    describe("updatePreviewOnUpdateOf") {
        it("updates when a JSpinner is updated") {
            GuiActionRunner.execute {
                val spinner = JSpinner()
                panel.updatePreviewOnUpdateOf(spinner)
                spinner.value = 5
            }

            assertThat(frame.label("previewLabel").text()).isEqualTo(randomTextHtml)
        }

        it("updates when a JCheckBox is updated") {
            GuiActionRunner.execute {
                val spinner = JCheckBox()
                panel.updatePreviewOnUpdateOf(spinner)
                spinner.isSelected = true
            }

            assertThat(frame.label("previewLabel").text()).isEqualTo(randomTextHtml)
        }

        // Requires dependency on IntelliJ classes
        xit("updates when an activity table is updated") {
            GuiActionRunner.execute {
                val itemEditor = object : CollectionItemEditor<EditableDatum<String>> {
                    // TODO Do not instantiate instance of `EditableSymbolSet`
                    override fun getItemClass() = EditableDatum(false, "")::class.java

                    override fun clone(item: EditableDatum<String>, forInPlaceEditing: Boolean) =
                        EditableDatum(item.active, item.datum)
                }
                val table = object : ActivityTableModelEditor<String>(arrayOf(), itemEditor, "", "") {}

                panel.updatePreviewOnUpdateOf(table)
                table.data = listOf("a")
            }

            assertThat(frame.label("previewLabel").text()).isEqualTo(randomTextHtml)
        }

        it("updates when a group of JRadioButtons is updated") {
            GuiActionRunner.execute {
                val group = ButtonGroup()
                JRadioButton("a").also { group.add(it) }
                JRadioButton("b").also { group.add(it) }
                JRadioButton("c").also { group.add(it) }

                panel.updatePreviewOnUpdateOf(group)
                group.buttons()[1].isSelected = true
            }
        }

        it("throws an exception if the component type is unknown") {
            assertThatThrownBy { panel.updatePreviewOnUpdateOf("string") }
                .isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    describe("seed") {
        it("reuses the old seed if the button is not pressed") {
            GuiActionRunner.execute { panel.updatePreview() }
            val oldRandom = action?.random

            GuiActionRunner.execute { panel.updatePreview() }
            val newRandom = action?.random

            assertThat(newRandom?.nextInt()).isEqualTo(oldRandom?.nextInt())
        }

        it("uses a new seed when the button is pressed") {
            GuiActionRunner.execute { panel.updatePreview() }
            val oldRandom = action?.random

            frame.button("refreshButton").target().mouseListeners.forEach { it.mouseClicked(null) }

            GuiActionRunner.execute { panel.updatePreview() }
            val newRandom = action?.random

            assertThat(newRandom?.nextInt()).isNotEqualTo(oldRandom?.nextInt())
        }
    }
})
