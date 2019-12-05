package com.fwdekker.randomness.ui

import com.intellij.openapi.ui.ValidationInfo
import javax.swing.JSpinner


/**
 * A container for two [JSpinner]s that indicate a range of values.
 *
 * @param min the `JSpinner` that represents the minimum value
 * @param max the `JSpinner` that represents the maximum value
 * @param maxRange the maximum difference between `min` and `max`
 * @param name the name used in error messages
 */
class JSpinnerRange @JvmOverloads constructor(
    private val min: JSpinner,
    private val max: JSpinner,
    private val maxRange: Double = DEFAULT_MAX_RANGE,
    name: String? = null
) {
    companion object {
        /**
         * The maximum span that can be expressed.
         */
        private const val DEFAULT_MAX_RANGE = 1E53
    }

    /**
     * The `name` parameter preceded by a whitespace if it was not null, or an empty string otherwise.
     */
    val name = if (name != null) " $name" else ""
    /**
     * The current minimum value of the range.
     */
    val minValue: Double
        get() = (this.min.value as Number).toDouble()
    /**
     * The current maximum value of the range.
     */
    val maxValue: Double
        get() = (this.max.value as Number).toDouble()


    init {
        require(maxRange >= 0) { "maxRange must be a positive number." }
    }


    /**
     * Validates this range.
     *
     * @return `null` if the current value is valid, or a `ValidationInfo` object explaining why the current value is
     * invalid
     */
    fun validateValue() =
        when {
            minValue > maxValue ->
                ValidationInfo("The maximum$name should not be smaller than the minimum$name.", max)
            maxValue - minValue > maxRange ->
                ValidationInfo("The range should not exceed $maxRange.", max)
            else ->
                null
        }
}
