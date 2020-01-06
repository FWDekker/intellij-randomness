package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArrayScheme
import com.fwdekker.randomness.array.ArraySettings
import icons.RandomnessIcons
import kotlin.random.Random


/**
 * Inserts a dummy value.
 *
 * Mostly for testing and demonstration purposes.
 *
 * @property dummySupplier generates dummy values to insert
 */
class DummyInsertAction(private val dummySupplier: (Random) -> String) : DataInsertAction(RandomnessIcons.Data.Base) {
    override val name = "Random Dummy"

    override fun generateStrings(count: Int) = List(count) { dummySupplier(random) }
}

/**
 * Inserts an array of dummy values.
 *
 * Mostly for testing and demonstration purposes.
 *
 * @param arrayScheme the scheme to use for generating arrays
 * @param dummySupplier generates dummy values to insert
 */
class DummyInsertArrayAction(
    arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
    dummySupplier: (Random) -> String
) : DataInsertArrayAction(arrayScheme, DummyInsertAction(dummySupplier), RandomnessIcons.Data.Array) {
    override val name = "Random Dummy Array"
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

/**
 * Inserts a repeated array of dummy values.
 *
 * Mostly for testing and demonstration purposes.
 *
 * @param arrayScheme the scheme to use for generating arrays
 * @param dummySupplier generates dummy values to insert
 */
class DummyInsertRepeatArrayAction(
    arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
    dummySupplier: (Random) -> String
) : DataInsertRepeatArrayAction(DummyInsertArrayAction(arrayScheme, dummySupplier), RandomnessIcons.Data.Repeat) {
    override val name = "Random Repeat Dummy"
}
