package com.fwdekker.randomness.ui

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.testhelpers.DummyScheme
import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.find
import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.guiRun
import com.fwdekker.randomness.testhelpers.installEdtViolationDetection
import com.fwdekker.randomness.testhelpers.matcher
import com.fwdekker.randomness.testhelpers.setUpBareIdeaFixture
import com.intellij.openapi.util.Disposer
import com.intellij.ui.InplaceButton
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * Unit tests for [PreviewPanel].
 */
object PreviewPanelTest : FunSpec({
    lateinit var panel: PreviewPanel
    lateinit var frame: FrameFixture

    var scheme: DummyScheme? = null
    val placeholder = Bundle("preview.placeholder")


    installEdtViolationDetection()
    setUpBareIdeaFixture()

    beforeNonContainer {
        panel = guiGet { PreviewPanel { DummyScheme().also { scheme = it } } }
        frame = Containers.showInFrame(panel.rootComponent)

        panel.previewText shouldBe placeholder
    }

    afterNonContainer {
        frame.cleanUp()
        guiRun { Disposer.dispose(panel) }
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
