package com.fwdekker.randomness.testhelpers

import kotlin.reflect.KCallable
import kotlin.reflect.KParameter


/**
 * Maps each element to its name, or returns an empty list of `this` is `null`.
 */
fun Collection<KCallable<*>>?.callableNames(): Set<String> = (this ?: emptyList()).map { it.name }.toSet()

/**
 * Maps each element to its name, or returns an empty list of `this` is `null`.
 */
fun Collection<KParameter>?.parameterNames(): Set<String> = (this ?: emptyList()).map { it.name!! }.toSet()
