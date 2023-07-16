package com.fwdekker.randomness

import com.intellij.openapi.actionSystem.impl.ActionButton
import org.assertj.swing.core.GenericTypeMatcher
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.FrameFixture
import java.awt.Component


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
 * Returns the selected item of a [javax.swing.JComboBox] in [this] with name [name].
 *
 * @param T the type of item contained in the [javax.swing.JComboBox]
 * @receiver the fixture to find the [javax.swing.JComboBox] in
 * @param name the name of the [javax.swing.JComboBox] to return the value of
 * @return the selected item of the [javax.swing.JComboBox] in [this] with name [name]
 */
@Suppress("UNCHECKED_CAST") // Responsibility of caller
fun <T> FrameFixture.getComboBoxItem(name: String): T =
    GuiActionRunner.execute<T> { this.comboBox(name).target().selectedItem as T }
