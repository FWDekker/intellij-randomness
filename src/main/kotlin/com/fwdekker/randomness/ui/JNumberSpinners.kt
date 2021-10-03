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
 * @param minValue the smallest number that may be represented
 * @param maxValue the largest number that may be represented
 * @param stepSize the default value to increment and decrement by
 */
abstract class JNumberSpinner<T>(value: T, minValue: T, maxValue: T, stepSize: T) :
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
     * The minimal allowed value.
     */
    var minValue: T
        get() = numberToT(numberModel.minimum as Number)
        set(value) {
            numberModel.minimum = value
        }

    /**
     * The maximal allowed value.
     */
    var maxValue: T
        get() = numberToT(numberModel.maximum as Number)
        set(value) {
            numberModel.maximum = value
        }

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
     * Sets the value of the spinner.
     *
     * @param value the new value of the spinner
     */
    override fun setValue(value: Any) {
        numberModel.value = value
    }


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
 * @param value the default value
 * @param minValue the smallest number that may be represented
 * @param maxValue the largest number that may be represented
 * @param stepSize the default value to increment and decrement by
 */
class JDoubleSpinner(
    value: Double = 0.0,
    minValue: Double = -Double.MAX_VALUE,
    maxValue: Double = Double.MAX_VALUE,
    stepSize: Double = 0.1
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
    stepSize: Long = 1L
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
    stepSize: Int = 1
) : JNumberSpinner<Int>(value, minValue, maxValue, stepSize) {
    override val numberToT: (Number) -> Int
        get() = { it.toInt() }


    init {
        this.editor = NumberEditor(this).also { it.format.decimalFormatSymbols = DEFAULT_FORMAT }
    }
}
