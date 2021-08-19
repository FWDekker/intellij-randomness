package com.fwdekker.randomness

import com.intellij.openapi.actionSystem.impl.ActionButton
import org.assertj.swing.core.GenericTypeMatcher
import org.assertj.swing.fixture.FrameFixture


/**
 * Clicks the action button with the given accessible name.
 *
 * @param accessibleName the name of the button to click
 */
fun FrameFixture.clickActionButton(accessibleName: String) =
    robot().finder()
        .find(object : GenericTypeMatcher<ActionButton>(ActionButton::class.java) {
            override fun isMatching(component: ActionButton) =
                component.isValid && component.accessibleContext.accessibleName == accessibleName
        })
        .click()
