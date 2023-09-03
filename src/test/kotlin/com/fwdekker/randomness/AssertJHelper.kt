package com.fwdekker.randomness

import com.fwdekker.randomness.ui.JDateTimeField
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.ui.dsl.builder.MutableProperty
import org.assertj.swing.core.GenericTypeMatcher
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.FrameFixture
import org.assertj.swing.fixture.JCheckBoxFixture
import org.assertj.swing.fixture.JComboBoxFixture
import org.assertj.swing.fixture.JSpinnerFixture
import org.assertj.swing.fixture.JTextComponentFixture
import java.awt.Component
import kotlin.reflect.KMutableProperty0


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


// TODO: Document these functions!
@Suppress("UNCHECKED_CAST")
fun <T> KMutableProperty0<T>.prop(): MutableProperty<Any?> = MutableProperty({ get() }, { set(it as T) })

@Suppress("UNCHECKED_CAST")
fun <SELF, FIELD> SELF.prop(get: (SELF) -> (() -> FIELD), set: (SELF) -> ((FIELD) -> Unit)): MutableProperty<Any?> =
    MutableProperty(get(this)) { set(this)(it as FIELD) }

fun JCheckBoxFixture.isSelectedProp() = this.prop({ this.target()::isSelected }, { this.target()::setSelected })

fun JComboBoxFixture.itemProp() = this.prop({ this.target().editor::getItem }, { this.target().editor::setItem })

fun JSpinnerFixture.valueProp() = this.prop({ this.target()::getValue }, { this.target()::setValue })

fun JTextComponentFixture.dateTimeProp() = (this.target() as JDateTimeField)::longValue.prop()

fun JTextComponentFixture.textProp() = this.prop({ this.target()::getText }, { this.target()::setText })
