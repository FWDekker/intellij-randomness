package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArraySettings


/**
 * Inserts a dummy value.
 *
 * Mostly for testing and demonstration purposes.
 */
class DummyInsertAction : DataInsertAction() {
    companion object {
        /**
         * The value that is inserted.
         */
        const val dummyValue = "17"
    }


    override val name = "Random Dummy"

    override fun generateStrings(count: Int) = List(count) { dummyValue }
}

/**
 * Inserts an array of dummy values.
 *
 * Mostly for testing and demonstration purposes.
 *
 * @param arraySettings the settings to use for generating arrays
 */
class DummyInsertArrayAction(arraySettings: ArraySettings = ArraySettings.default) :
    DataInsertArrayAction(arraySettings, DummyInsertAction()) {
    override val name = "Random Dummy Array"
}
