package com.fwdekker.randomness.ui

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.matcher
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.InplaceButton
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
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var panel: PreviewPanel
    lateinit var frame: FrameFixture

    var scheme: DummyScheme? = null
    val placeholder = ResourceBundle.getBundle("randomness").getString("settings.placeholder")


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        panel = GuiActionRunner.execute<PreviewPanel> { PreviewPanel { DummyScheme().also { scheme = it } } }
        frame = Containers.showInFrame(panel.rootComponent)

        assertThat(panel.previewText).isEqualTo(placeholder)
    }

    afterEachTest {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("updatePreview") {
        it("updates the label's contents") {
            GuiActionRunner.execute { panel.updatePreview() }

            assertThat(panel.previewText).isEqualTo(DummyScheme.DEFAULT_OUTPUT)
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
                frame.robot().finder().find(matcher(InplaceButton::class.java) { it.isValid }).doClick()
            }

            GuiActionRunner.execute { panel.updatePreview() }
            val newRandom = scheme?.random

            assertThat(newRandom?.nextInt()).isNotEqualTo(oldRandom?.nextInt())
        }
    }
})
