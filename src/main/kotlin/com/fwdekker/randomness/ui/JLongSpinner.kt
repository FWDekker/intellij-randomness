package com.fwdekker.randomness.ui

import com.fwdekker.randomness.ui.JLongSpinner.Companion.DEFAULT_MAX_VALUE
import com.fwdekker.randomness.ui.JLongSpinner.Companion.DEFAULT_MIN_VALUE
import com.intellij.openapi.ui.ValidationInfo
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel


/**
 * A [JSpinner] for longs.
 *
 * A `JLongSpinner` can only represent value from [DEFAULT_MIN_VALUE] (inclusive) until [DEFAULT_MAX_VALUE]
 * (inclusive), because not all numbers outside this range can be represented as a [Double].
 *
 * @param value    the default value represented by this `JDoubleSpinner`
 * @param minValue the smallest number that may be represented by this `JDoubleSpinner`
 * @param maxValue the largest number that may be represented by this `JDoubleSpinner`
 */
class JLongSpinner(
    value: Long = DEFAULT_VALUE,
    var minValue: Long = DEFAULT_MIN_VALUE,
    var maxValue: Long = DEFAULT_MAX_VALUE
) : JSpinner(SpinnerNumberModel(value, minValue, maxValue, DEFAULT_STEP_SIZE)) {
    companion object {
        /**
         * The default value represented by a `JDoubleSpinner`.
         */
        private const val DEFAULT_VALUE = 0L
        /**
         * The smallest number that can be represented by a `JDoubleSpinner`.
         */
        private const val DEFAULT_MIN_VALUE = java.lang.Long.MIN_VALUE
        /**
         * The largest number that can be represented by a `JDoubleSpinner`.
         */
        private const val DEFAULT_MAX_VALUE = java.lang.Long.MAX_VALUE
        /**
         * The default step size when decrementing or incrementing the value.
         */
        private const val DEFAULT_STEP_SIZE = 1L
        /**
         * The minimal and preferred width of this component.
         */
        private const val SPINNER_WIDTH = 52
    }


    init {
        val editor = NumberEditor(this)
        editor.format.decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
        setEditor(editor)

        val minimumSize = minimumSize
        minimumSize.width = SPINNER_WIDTH
        setMinimumSize(minimumSize)

        val preferredSize = preferredSize
        preferredSize.width = SPINNER_WIDTH
        setPreferredSize(preferredSize)
    }


    override fun getValue() = (super.getValue() as Number).toLong()

    /**
     * Validates the current value.
     *
     * @return `null` if the current value is valid, or a [ValidationInfo] object explaining why the current value is
     * invalid
     */
    fun validateValue() =
        when {
            value < minValue -> ValidationInfo("Please enter a value greater than or equal to $minValue.", this)
            value > maxValue -> ValidationInfo("Please enter a value less than or equal to $maxValue.", this)
            else -> null
        }
}
