package com.fwdekker.randomness.ui

import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.swing.JComponent
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel


/**
 * An abstract [JSpinner] for numbers that contains common logic for its subclasses.
 *
 * @param T the type of number
 * @param value the default value
 * @param minValue the smallest number that may be represented, or `null` if there is no limit
 * @param maxValue the largest number that may be represented, or `null` if there is no limit
 * @param stepSize the default value to increment and decrement by
 */
abstract class JNumberSpinner<T>(value: T, minValue: T?, maxValue: T?, stepSize: T) :
    JSpinner(SpinnerNumberModel(value, minValue, maxValue, stepSize)) where T : Number, T : Comparable<T> {
    /**
     * Transforms a [Number] into a [T].
     */
    abstract val numberToT: (Number) -> T

    /**
     * A helper function to return the super class's model as an instance of [SpinnerNumberModel].
     */
    private val numberModel: SpinnerNumberModel
        get() = super.getModel() as SpinnerNumberModel

    /**
     * The component that can be used to edit the spinner.
     */
    val editorComponent: JComponent?
        get() = editor.getComponent(0) as? JComponent


    /**
     * Returns the current value of the spinner.
     *
     * @return the current value of the spinner
     */
    override fun getValue(): T = numberToT(numberModel.number)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default number format used to display numbers.
         */
        val DEFAULT_FORMAT = DecimalFormatSymbols(Locale.US)
    }
}


/**
 * A [JNumberSpinner] for doubles.
 *
 * Note that setting `minValue` or `maxValue` to a very large number may cause the parent component's width to be overly
 * large.
 *
 * @param value the default value
 * @param minValue the smallest number that may be represented
 * @param maxValue the largest number that may be represented
 * @param stepSize the default value to increment and decrement by
 */
class JDoubleSpinner(
    value: Double = 0.0,
    minValue: Double? = null,
    maxValue: Double? = null,
    stepSize: Double = 0.1,
) : JNumberSpinner<Double>(value, minValue, maxValue, stepSize) {
    override val numberToT: (Number) -> Double
        get() = { it.toDouble() }


    init {
        this.editor = NumberEditor(this).also { it.format.decimalFormatSymbols = DEFAULT_FORMAT }
    }
}


/**
 * A [JNumberSpinner] for longs.
 *
 * @param value the default value
 * @param minValue the smallest number that may be represented
 * @param maxValue the largest number that may be represented
 * @param stepSize the default value to increment and decrement by
 */
class JLongSpinner(
    value: Long = 0L,
    minValue: Long = Long.MIN_VALUE,
    maxValue: Long = Long.MAX_VALUE,
    stepSize: Long = 1L,
) : JNumberSpinner<Long>(value, minValue, maxValue, stepSize) {
    override val numberToT: (Number) -> Long
        get() = { it.toLong() }


    init {
        this.editor = NumberEditor(this).also { it.format.decimalFormatSymbols = DEFAULT_FORMAT }
    }
}


/**
 * A [JNumberSpinner] for integers.
 *
 * @param value the default value
 * @param minValue the smallest number that may be represented
 * @param maxValue the largest number that may be represented
 * @param stepSize the default value to increment and decrement by
 */
class JIntSpinner(
    value: Int = 0,
    minValue: Int = Int.MIN_VALUE,
    maxValue: Int = Int.MAX_VALUE,
    stepSize: Int = 1,
) : JNumberSpinner<Int>(value, minValue, maxValue, stepSize) {
    override val numberToT: (Number) -> Int
        get() = { it.toInt() }


    init {
        this.editor = NumberEditor(this).also { it.format.decimalFormatSymbols = DEFAULT_FORMAT }
    }
}


/**
 * Binds two spinners that form a range of valid values together.
 *
 * This function adds listeners to the spinners so that if one spinner's value is adjusted, the other's value is also
 * adjusted if not doing so would make the range invalid. Specifically, this function guarantees that the minimum value
 * never becomes larger than the maximum value, and guarantees that the difference between the minimum and the maximum
 * never exceeds [maxRange].
 *
 * @param min the [JSpinner] that represents the minimum value
 * @param max the [JSpinner] that represents the maximum value
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
