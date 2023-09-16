package com.fwdekker.randomness.testhelpers

import com.intellij.openapi.actionSystem.impl.ActionButton
import org.assertj.swing.core.GenericTypeMatcher
import org.assertj.swing.driver.ComponentDriver
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.AbstractComponentFixture
import org.assertj.swing.fixture.FrameFixture
import java.awt.Component


/**
 * Runs [lambda] in the GUI thread.
 *
 * @param lambda the function to run in the GUI thread
 */
fun guiRun(lambda: () -> Unit) = GuiActionRunner.execute(lambda)

/**
 * Runs [lambda] in the GUI thread and returns the result.
 *
 * @param T the type of value to return
 * @param lambda the function to run in the GUI thread
 * @return the output of [lambda]
 */
fun <T> guiGet(lambda: () -> T): T = GuiActionRunner.execute(lambda)


/**
 * Creates a [GenericTypeMatcher] with a lambda.
 *
 * @param T the type of matcher to return
 * @param klass the class to be matched
 * @param requireValid `true` if and only if only valid components should be matched
 * @param matcher the matcher that returns `true` if the desired component is found
 * @return a [GenericTypeMatcher] with a lambda.
 */
fun <T : Component> matcher(klass: Class<T>, requireValid: Boolean = true, matcher: (T) -> Boolean = { true }) =
    object : GenericTypeMatcher<T>(klass) {
        override fun isMatching(component: T) = (!requireValid || component.isValid) && matcher(component)
    }

/**
 * Finds a component that matches [matcher], or throws an exception if no matches exist.
 *
 * @param T the type of component to find
 * @param matcher the matcher to compare components against
 * @return the component that matches [matcher], if any
 * @throws org.assertj.swing.exception.ComponentLookupException if no components match [matcher]
 */
fun <T : Component> FrameFixture.find(matcher: GenericTypeMatcher<T>): T =
    robot().finder().find(matcher)

/**
 * Returns the [ActionButton] that has [accessibleName].
 *
 * @param accessibleName the name of the button to return
 * @return the [ActionButton] that has [accessibleName]
 * @throws org.assertj.swing.exception.ComponentLookupException if no [ActionButton] has [accessibleName]
 */
fun FrameFixture.getActionButton(accessibleName: String): ActionButton =
    find(matcher(ActionButton::class.java) { it.accessibleContext.accessibleName == accessibleName })


/**
 * Invokes [AbstractComponentFixture.requireEnabled] if [enabled] is `true`, and invokes
 * [AbstractComponentFixture.requireDisabled] otherwise.
 */
fun <S, C : Component, D : ComponentDriver> AbstractComponentFixture<S, C, D>.requireEnabledIs(enabled: Boolean): S =
    if (enabled) this.requireEnabled()
    else this.requireDisabled()
