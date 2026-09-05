package com.fwdekker.randomness.ui

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.testhelpers.DummyScheme
import com.fwdekker.randomness.testhelpers.find
import com.fwdekker.randomness.testhelpers.matcher
import com.fwdekker.randomness.testhelpers.runEdt
import com.fwdekker.randomness.testhelpers.useSharedBareIdeaFixture
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


    useSharedBareIdeaFixture()

    beforeEach {
        panel = runEdt { PreviewPanel { DummyScheme().also { scheme = it } } }
        frame = Containers.showInFrame(panel.rootComponent)

        panel.previewText shouldBe placeholder
    }

    afterEach {
        frame.cleanUp()
        runEdt { Disposer.dispose(panel) }
    }


    context("updatePreview") {
        test("updates the label's contents") {
            runEdt { panel.updatePreview() }

            panel.previewText shouldBe "text0"
        }
    }

    context("seed") {
        test("reuses the old seed if the button is not pressed") {
            runEdt { panel.updatePreview() }
            val oldRandom = scheme?.random

            runEdt { panel.updatePreview() }
            val newRandom = scheme?.random

            newRandom?.nextInt() shouldBe oldRandom?.nextInt()
        }

        test("uses a new seed when the button is pressed") {
            runEdt { panel.updatePreview() }
            val oldRandom = scheme?.random

            runEdt { frame.find(matcher(InplaceButton::class.java)).doClick() }

            runEdt { panel.updatePreview() }
            val newRandom = scheme?.random

            newRandom?.nextInt() shouldNotBe oldRandom?.nextInt()
        }
    }
})
