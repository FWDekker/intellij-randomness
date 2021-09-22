package com.fwdekker.randomness

import org.assertj.swing.core.GenericTypeMatcher
import java.awt.Component


/**
 * Creates a [GenericTypeMatcher] with a lambda.
 *
 * @param T the type of matcher to return
 * @param klass the class to be matched
 * @param matcher the matcher that returns `true` if the desired component is found
 * @return a [GenericTypeMatcher] with a lambda.
 */
fun <T : Component> matcher(klass: Class<T>, matcher: (T) -> Boolean) =
    object : GenericTypeMatcher<T>(klass) {
        override fun isMatching(component: T) = matcher(component)
    }

/**
 * Returns a [GenericTypeMatcher] for returning the first component that is named [name].
 *
 * @param T the type of matcher to return
 * @param name the name to return a matcher for
 * @return a [GenericTypeMatcher] for returning the first component that is named [name]
 */
fun <T : Component> nameMatcher(klass: Class<T>, name: String) = matcher(klass) { it.name == name }
