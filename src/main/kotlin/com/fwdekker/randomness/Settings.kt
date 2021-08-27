package com.fwdekker.randomness


/**
 * A user-configurable persistent collection.
 */
abstract class Settings : State() {
    abstract override fun deepCopy(retainUuid: Boolean): Settings
}
