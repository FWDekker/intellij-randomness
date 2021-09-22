package com.fwdekker.randomness

import com.intellij.openapi.actionSystem.impl.ActionButton
import org.assertj.swing.fixture.FrameFixture


/**
 * Returns the action button that has [accessibleName].
 *
 * @param accessibleName the name of the button to click
 */
fun FrameFixture.getActionButton(accessibleName: String) =
    robot().finder()
        .find(matcher(ActionButton::class.java) { it.isValid && it.accessibleContext.accessibleName == accessibleName })

/**
 * Clicks the action button that has [accessibleName].
 *
 * @param accessibleName the name of the button to click
 */
fun FrameFixture.clickActionButton(accessibleName: String) = getActionButton(accessibleName).click()
