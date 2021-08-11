package com.fwdekker.randomness.ui

import com.fwdekker.randomness.DummyScheme
import com.intellij.util.ui.CollectionItemEditor
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.ResourceBundle
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JRadioButton
import javax.swing.JSpinner


/**
 * Unit tests for [PreviewPanel].
 */
object PreviewPanelTest : Spek({
    var scheme: DummyScheme? = null
    val placeholder = ResourceBundle.getBundle("randomness").getString("settings.placeholder")

    lateinit var panel: PreviewPanel
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        panel = GuiActionRunner.execute<PreviewPanel> { PreviewPanel { DummyScheme().also { scheme = it } } }
        frame = Containers.showInFrame(panel.rootComponent)

        assertThat(frame.textBox("previewLabel").text()).isEqualTo(placeholder)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("updatePreview") {
        it("updates the label's contents") {
            GuiActionRunner.execute { panel.updatePreview() }

            assertThat(frame.textBox("previewLabel").text()).isEqualTo(DummyScheme.DEFAULT_OUTPUT)
        }
    }

    describe("updatePreviewOnUpdateOf") {
        it("updates when a JSpinner is updated") {
            GuiActionRunner.execute {
                val spinner = JSpinner()
                addChangeListenerTo(spinner) { panel.updatePreview() }
                spinner.value = 5
            }

            assertThat(frame.textBox("previewLabel").text()).isEqualTo(DummyScheme.DEFAULT_OUTPUT)
        }

        it("updates when a JCheckBox is updated") {
            GuiActionRunner.execute {
                val checkBox = JCheckBox()
                addChangeListenerTo(checkBox) { panel.updatePreview() }
                checkBox.isSelected = true
            }

            assertThat(frame.textBox("previewLabel").text()).isEqualTo(DummyScheme.DEFAULT_OUTPUT)
        }

        // Requires dependency on IntelliJ classes
        xit("updates when an activity table is updated") {
            GuiActionRunner.execute {
                val itemEditor = object : CollectionItemEditor<EditableDatum<String>> {
                    override fun getItemClass() = EditableDatum(false, "")::class.java

                    override fun clone(item: EditableDatum<String>, forInPlaceEditing: Boolean) =
                        EditableDatum(item.active, item.datum)
                }
                val table = object : ActivityTableModelEditor<String>(arrayOf(), itemEditor, "", "") {}

                addChangeListenerTo(table) { panel.updatePreview() }
                table.data = listOf("a")
            }

            assertThat(frame.textBox("previewLabel").text()).isEqualTo(DummyScheme.DEFAULT_OUTPUT)
        }

        it("updates when a group of JRadioButtons is updated") {
            GuiActionRunner.execute {
                val group = ButtonGroup()
                JRadioButton("a").also { group.add(it) }
                JRadioButton("b").also { group.add(it) }
                JRadioButton("c").also { group.add(it) }

                addChangeListenerTo(group) { panel.updatePreview() }
                group.buttons()[1].isSelected = true
            }
        }
    }

    describe("seed") {
        it("reuses the old seed if the button is not pressed") {
            GuiActionRunner.execute { panel.updatePreview() }
            val oldRandom = scheme?.random

            GuiActionRunner.execute { panel.updatePreview() }
            val newRandom = scheme?.random

            assertThat(newRandom?.nextInt()).isEqualTo(oldRandom?.nextInt())
        }

        it("uses a new seed when the button is pressed") {
            GuiActionRunner.execute { panel.updatePreview() }
            val oldRandom = scheme?.random

            GuiActionRunner.execute {
                frame.button("refreshButton").target().mouseListeners.forEach { it.mouseClicked(null) }
            }

            GuiActionRunner.execute { panel.updatePreview() }
            val newRandom = scheme?.random

            assertThat(newRandom?.nextInt()).isNotEqualTo(oldRandom?.nextInt())
        }
    }
})
