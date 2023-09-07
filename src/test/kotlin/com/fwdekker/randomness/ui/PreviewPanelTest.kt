package com.fwdekker.randomness.ui

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.afterNonContainer
import com.fwdekker.randomness.beforeNonContainer
import com.fwdekker.randomness.find
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.matcher
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.InplaceButton
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * Unit tests for [PreviewPanel].
 */
object PreviewPanelTest : FunSpec({
    tags(NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var panel: PreviewPanel
    lateinit var frame: FrameFixture

    var scheme: DummyScheme? = null
    val placeholder = Bundle("preview.placeholder")


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        panel = guiGet { PreviewPanel { DummyScheme().also { scheme = it } } }
        frame = Containers.showInFrame(panel.rootComponent)

        panel.previewText shouldBe placeholder
    }

    afterNonContainer {
        frame.cleanUp()
        guiRun { panel.dispose() }
        ideaFixture.tearDown()
    }


    context("updatePreview") {
        test("updates the label's contents") {
            guiRun { panel.updatePreview() }

            panel.previewText shouldBe "text0"
        }
    }

    context("seed") {
        test("reuses the old seed if the button is not pressed") {
            guiRun { panel.updatePreview() }
            val oldRandom = scheme?.random

            guiRun { panel.updatePreview() }
            val newRandom = scheme?.random

            newRandom?.nextInt() shouldBe oldRandom?.nextInt()
        }

        test("uses a new seed when the button is pressed") {
            guiRun { panel.updatePreview() }
            val oldRandom = scheme?.random

            guiRun { frame.find(matcher(InplaceButton::class.java)).doClick() }

            guiRun { panel.updatePreview() }
            val newRandom = scheme?.random

            newRandom?.nextInt() shouldNotBe oldRandom?.nextInt()
        }
    }
})
