package com.fwdekker.randomness

import icons.RandomnessIcons
import kotlin.random.Random


/**
 * Inserts a dummy value.
 *
 * Mostly for testing and demonstration purposes.
 *
 * @param dummySupplier generates dummy values to insert
 */
class DummyInsertAction(private val dummySupplier: (Random) -> String) : DataInsertAction(RandomnessIcons.Data.Base) {
    override val name = "Random Dummy"

    override fun generateStrings(count: Int) = List(count) { dummySupplier(random) }
}

/**
 * Inserts a repeated array of dummy values.
 *
 * Mostly for testing and demonstration purposes.
 *
 * @param dummySupplier generates dummy values to insert
 */
class DummyInsertRepeatAction(dummySupplier: (Random) -> String) :
    DataInsertRepeatAction(DummyInsertAction(dummySupplier), RandomnessIcons.Data.Repeat) {
    override val name = "Random Repeat Dummy"
}
