package com.fwdekker.randomness

import com.intellij.openapi.actionSystem.impl.ActionButton
import org.assertj.swing.fixture.FrameFixture


/**
 * Returns the `ActionButton` that has [accessibleName].
 *
 * @param accessibleName the name of the button to return
 */
fun FrameFixture.getActionButton(accessibleName: String): ActionButton =
    robot().finder()
        .find(matcher(ActionButton::class.java) { it.isValid && it.accessibleContext.accessibleName == accessibleName })

/**
 * Clicks the `ActionButton` that has [accessibleName].
 *
 * @param accessibleName the name of the button to click
 */
fun FrameFixture.clickActionButton(accessibleName: String) = getActionButton(accessibleName).click()
