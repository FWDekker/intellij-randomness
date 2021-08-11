package com.fwdekker.randomness.ui

import javax.swing.JSpinner


/**
 * Binds two spinners that form a range of valid values together.
 *
 * This function adds listeners to the spinners so that if one spinner's value is adjusted, the other's value is also
 * adjusted if not doing so would make the range invalid. Specifically, this function guarantees that the minimum value
 * never becomes larger than the maximum value, and guarantees that the difference between the minimum and the maximum
 * never exceeds [maxRange].
 *
 * @param min the `JSpinner` that represents the minimum value
 * @param max the `JSpinner` that represents the maximum value
 * @param maxRange the maximum difference between `min` and `max`
 */
fun bindSpinners(min: JSpinner, max: JSpinner, maxRange: Double? = null) {
    if (maxRange != null)
        require(maxRange >= 0) { "maxRange must be a positive number." }

    min.addChangeListener {
        val minValue = (min.value as Number).toDouble()
        val maxValue = (max.value as Number).toDouble()

        if (minValue > maxValue) max.value = minValue
        if (maxRange != null && maxValue - minValue > maxRange) max.value = minValue + maxRange
    }
    max.addChangeListener {
        val minValue = (min.value as Number).toDouble()
        val maxValue = (max.value as Number).toDouble()

        if (maxValue < minValue) min.value = maxValue
        if (maxRange != null && maxValue - minValue > maxRange) min.value = maxValue - maxRange
    }
}
