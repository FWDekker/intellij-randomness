package com.fwdekker.randomness.ui

import com.intellij.openapi.ui.ValidationInfo
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel


/**
 * A [JSpinner] for doubles.
 *
 * A `JDoubleSpinner` can only represent value from `-1E53` (inclusive) until `1E53` (inclusive) because not all numbers
 * outside this range can be represented as a [Double].
 *
 * @param minValue the smallest number that may be represented
 * @param maxValue the largest number that may be represented
 */
class JDoubleSpinner(
    private val minValue: Double = DEFAULT_MIN_VALUE,
    private val maxValue: Double = DEFAULT_MAX_VALUE
) : JSpinner(SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, DEFAULT_STEP_SIZE)) {
    companion object {
        /**
         * The default step size when decrementing or incrementing the value.
         */
        private const val DEFAULT_STEP_SIZE = 0.1
        /**
         * The smallest number that can be represented.
         */
        private const val DEFAULT_MIN_VALUE = -1E53
        /**
         * The largest number that can be represented.
         */
        private const val DEFAULT_MAX_VALUE = 1E53
    }


    init {
        if (minValue < DEFAULT_MIN_VALUE)
            throw IllegalArgumentException("minValue should not be smaller than $DEFAULT_MIN_VALUE.")
        if (maxValue > DEFAULT_MAX_VALUE)
            throw IllegalArgumentException("maxValue should not be greater than $DEFAULT_MAX_VALUE.")
        if (minValue > maxValue)
            throw IllegalArgumentException("minValue should be greater than maxValue.")

        editor = NumberEditor(this).also { it.format.decimalFormatSymbols = DecimalFormatSymbols(Locale.US) }
    }


    /**
     * Returns the current value of the model.
     *
     * @return the current value of the model
     */
    override fun getValue() = (super.getValue() as Number).toDouble()

    /**
     * Validates the current value.
     *
     * @return `null` if the current value is valid, or a `ValidationInfo` object explaining why the current value is
     * invalid
     */
    fun validateValue() =
        when {
            value < minValue -> ValidationInfo("Please enter a value greater than or equal to $minValue.", this)
            value > maxValue -> ValidationInfo("Please enter a value less than or equal to $maxValue.", this)
            else -> null
        }
}
