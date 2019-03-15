package com.fwdekker.randomness.ui

import com.fwdekker.randomness.ValidationException
import com.fwdekker.randomness.ui.JSpinnerRange.Companion.DEFAULT_MAX_RANGE
import javax.swing.JSpinner


/**
 * A container for two [JSpinner]s that indicate a range of values.
 *
 * @param min the `JSpinner` that contains the minimum value
 * @param max the `JSpinner` that contains the maximum value
 * @param maxRange the maximum span that can may expressed by this `JSpinnerRange`. Defaults to [DEFAULT_MAX_RANGE]
 */
class JSpinnerRange(
    private val min: JSpinner,
    private val max: JSpinner,
    private val maxRange: Double = DEFAULT_MAX_RANGE
) {
    companion object {
        /**
         * The maximum span that can be expressed by a `JSpinnerRange`.
         */
        // TODO Make private. Currently only used in tests because Java cannot use default arguments.
        const val DEFAULT_MAX_RANGE = 1E53
    }


    init {
        if (maxRange < 0)
            throw IllegalArgumentException("maxRange must be a positive number.")
    }


    /**
     * Validates this range.
     *
     * @throws ValidationException if the `JSpinner`s do not form a valid range
     */
    @Throws(ValidationException::class)
    fun validate() {
        val minValue = (min.value as Number).toDouble()
        val maxValue = (max.value as Number).toDouble()

        if (minValue > maxValue)
            throw ValidationException("The maximum should be no smaller than the minimum.", max)
        if (maxValue - minValue > maxRange)
            throw ValidationException("The range should not exceed $maxRange.", max)
    }
}
