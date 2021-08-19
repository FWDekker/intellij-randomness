package com.fwdekker.randomness.ui

import com.fwdekker.randomness.DummyScheme
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.ResourceBundle


/**
 * GUI tests for [PreviewPanel].
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
