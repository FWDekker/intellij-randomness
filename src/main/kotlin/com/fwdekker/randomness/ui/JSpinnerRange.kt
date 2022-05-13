package com.fwdekker.randomness.ui

import com.fwdekker.randomness.ValidationInfo
import javax.swing.JSpinner


/**
 * A container for two [JSpinner]s that indicate a range of values.
 *
 * @property min the `JSpinner` that represents the minimum value
 * @property max the `JSpinner` that represents the maximum value
 * @property maxRange the maximum difference between `min` and `max`
 * @param name the name to use in error messages
 */
class JSpinnerRange(
    private val min: JSpinner,
    private val max: JSpinner,
    private val maxRange: Double? = null,
    name: String? = null
) {
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
        if (maxRange != null)
            require(maxRange >= 0) { "maxRange must be a positive number." }

        min.addChangeListener { if (minValue > maxValue) max.value = minValue }
        max.addChangeListener { if (maxValue < minValue) min.value = maxValue }
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
            maxRange != null && maxValue - minValue > maxRange ->
                ValidationInfo("The$name range should not exceed $maxRange.", max)
            else ->
                null
        }
}
