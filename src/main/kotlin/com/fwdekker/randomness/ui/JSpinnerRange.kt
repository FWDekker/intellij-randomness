package com.fwdekker.randomness.ui

import com.fwdekker.randomness.ui.JSpinnerRange.Companion.DEFAULT_MAX_RANGE
import com.intellij.openapi.ui.ValidationInfo
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

    val minValue: Double
        get() = (this.min.value as Number).toDouble()
    val maxValue: Double
        get() = (this.max.value as Number).toDouble()


    init {
        if (maxRange < 0)
            throw IllegalArgumentException("maxRange must be a positive number.")
    }


    /**
     * Validates this range.
     *
     * @return `null` if the current value is valid, or a [ValidationInfo] object explaining why the current value is
     * invalid
     */
    fun validateValue() =
        when {
            minValue > maxValue -> ValidationInfo("The maximum should be no smaller than the minimum.", max)
            maxValue - minValue > maxRange -> ValidationInfo("The range should not exceed $maxRange.", max)
            else -> null
        }
}
