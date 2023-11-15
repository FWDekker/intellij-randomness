package com.fwdekker.randomness

import com.fwdekker.randomness.testhelpers.Tags
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ActionButtonFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.EditorFixture
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.fixtures.JMenuBarFixture
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.hasAnyComponent
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import java.awt.event.KeyEvent
import java.io.File
import java.time.Duration
import java.time.Duration.ofMinutes
import javax.imageio.ImageIO
import kotlin.io.path.Path


class AutoDoc : FunSpec({
    tags(Tags.INTELLIJ_ROBOT)


    lateinit var remoteRobot: RemoteRobot
    lateinit var projectDir: File


    beforeContainer {
        Path("build/autodoc/").toFile().mkdirs()

        projectDir = tempdir("randomness-autodoc").toPath().toFile()
        Path("src/test/resources/autodoc/").toFile().copyRecursively(projectDir, overwrite=true)

        remoteRobot = RemoteRobot("http://localhost:8580")

        // Set up project
        remoteRobot.find<JButtonFixture>(byXpath("//div[@defaulticon='open.svg']")).click()
        remoteRobot.find<ActionButtonFixture>(byXpath("//div[@myicon='refresh.svg']"), timeout = Duration.ofSeconds(5)).click()
        remoteRobot.find<JTextFieldFixture>(byXpath("//div[@class='BorderlessTextField']"))
            .apply { text = ""}
            .click()
        remoteRobot.keyboard { enterText(projectDir.toString()) }
        Thread.sleep(1000L) // Wait until dir is highlighted
        remoteRobot.find<JButtonFixture>(byXpath("//div[@text='OK']")).click()

        // Wait for indexing
        waitFor(ofMinutes(5)) {
            remoteRobot.find<ComponentFixture>(byXpath("//div[@class='IdeFrameImpl']")).callJs<Boolean>(
                """
                    const frameHelper = com.intellij.openapi.wm.impl.ProjectFrameHelper.getFrameHelper(component)
                    if (frameHelper) {
                        const project = frameHelper.getProject()
                        project ? com.intellij.openapi.project.DumbService.isDumb(project) : true
                    } else {
                        true
                    }
                    """,
                true
            ).not()
        }

        // Open file in editor
        if (!remoteRobot.hasAnyComponent(byXpath("//div[@accessiblename='Editor for FooTest.java']"))) {
            if (!remoteRobot.hasAnyComponent(byXpath("//div[@class='ProjectViewTree']")))
                remoteRobot.find<JButtonFixture>(byXpath("//div[@text='Project']")).click()

            remoteRobot.find<JTreeFixture>(byXpath("//div[@class='ProjectViewTree']"))
                .apply {
                    if (!hasText("src")) findText { "autodoc" in it.text }.doubleClick()
                    if (!hasText("test")) findText("src").doubleClick()
                    if (!hasText("FooTest")) findText("test").doubleClick()
                    findText("FooTest").doubleClick()
                }
        }

        // Fix appearance
        remoteRobot.findAll<ActionButtonFixture>(byXpath("//div[@tooltiptext='Hide']")).onEach { it.click() }
        // TODO: Set font size
        // TODO: Remove inlay hints


        // TODO: Recreate array-insertion-sample.gif
        // TODO: Recreate configuration-sample.gif
        // TODO: Recreate fast-insertion.png
        // TODO: Recreate insertion-sample.gif
        // TODO: Recreate shortcuts.png

    }

    afterContainer {
        remoteRobot.find<JMenuBarFixture>(byXpath("//div[@text='File']")).click()
        remoteRobot.find<JMenuBarFixture>(byXpath("//div[@text='File']//div[@text='Close Project']")).click()
        projectDir.deleteRecursively()
        remoteRobot.find<JTreeFixture>(byXpath("//div[contains(@visible_text, 'randomness-autodoc')]")).clickRow(0)
        remoteRobot.find<JButtonFixture>(byXpath("//div[@text='Remove From List']")).click()
    }


    context("make screenshots") {
        test("fast-insertion.png") {
            remoteRobot.find<EditorFixture>(byXpath("//div[@accessiblename='Editor for FooTest.java']")).click()
            remoteRobot.keyboard { hotKey(KeyEvent.ALT_DOWN_MASK, KeyEvent.VK_R) }
            ImageIO.write(remoteRobot.getScreenshot(), "png", Path("build/autodoc/fast-insertion.png").toFile())
            remoteRobot.keyboard { escape() }
        }

        test("shortcuts.png") {
            remoteRobot.keyboard { hotKey(KeyEvent.CTRL_DOWN_MASK, KeyEvent.ALT_DOWN_MASK, KeyEvent.VK_S) }
            remoteRobot.find<JTreeFixture>(byXpath("//div[@class='MyTree']")).clickRowWithText("Keymap")
            remoteRobot.find<JTextFieldFixture>(byXpath("//div[@accessiblename='Message text filter']")).text = "randomness"
            remoteRobot.find<JTreeFixture>(byXpath("//div[@class='MyTree']"))
        }
    }
})
