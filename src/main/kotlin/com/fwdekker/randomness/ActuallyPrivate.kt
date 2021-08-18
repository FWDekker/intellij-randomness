package com.fwdekker.randomness


/**
 * Indicates that the target is to be considered marked as private and should not be used externally, even though it is
 * not marked as such.
 *
 * @property reason Explains why the target is not marked as private.
 */
@Retention(AnnotationRetention.SOURCE)
annotation class ActuallyPrivate(val reason: String)
