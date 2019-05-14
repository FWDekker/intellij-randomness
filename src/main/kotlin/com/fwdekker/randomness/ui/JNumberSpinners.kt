package com.fwdekker.randomness.ui

import com.intellij.openapi.ui.ValidationInfo
import java.awt.Dimension
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
 * @property numberToT converts a [Number] to a [T]
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


/**
 * A [JNumberSpinner] for doubles.
 *
 * @param value the default value
 * @param minValue the smallest number that may be represented
 * @param maxValue the largest number that may be represented
 * @param stepSize the default value to increment and decrement by
 * @param name the name of this component
 */
class JDoubleSpinner @JvmOverloads constructor(
    value: Double = 0.0,
    minValue: Double = -Double.MAX_VALUE,
    maxValue: Double = Double.MAX_VALUE,
    stepSize: Double = 0.1,
    name: String? = null
) : JNumberSpinner<Double>(value, minValue, maxValue, stepSize) {
    override val numberToT: (Number) -> Double
        get() = { it.toDouble() }


    init {
        this.name = name
        this.editor = NumberEditor(this, "#")
        this.minimumSize = Dimension(52, minimumSize.height)
        this.preferredSize = Dimension(52, preferredSize.height)
    }
}


/**
 * A [JNumberSpinner] for longs.
 *
 * @param value the default value
 * @param minValue the smallest number that may be represented
 * @param maxValue the largest number that may be represented
 * @param stepSize the default value to increment and decrement by
 * @param name the name of this component
 */
class JLongSpinner @JvmOverloads constructor(
    value: Long = 0L,
    minValue: Long = Long.MIN_VALUE,
    maxValue: Long = Long.MAX_VALUE,
    stepSize: Long = 1L,
    name: String? = null
) : JNumberSpinner<Long>(value, minValue, maxValue, stepSize) {
    override val numberToT: (Number) -> Long
        get() = { it.toLong() }


    init {
        this.name = name
        this.editor = NumberEditor(this, "#")
        this.minimumSize = Dimension(52, minimumSize.height)
        this.preferredSize = Dimension(52, preferredSize.height)
    }
}


/**
 * A [JNumberSpinner] for integers.
 *
 * @param value the default value
 * @param minValue the smallest number that may be represented
 * @param maxValue the largest number that may be represented
 * @param stepSize the default value to increment and decrement by
 * @param name the name of this component
 */
class JIntSpinner @JvmOverloads constructor(
    value: Int = 0,
    minValue: Int = Int.MIN_VALUE,
    maxValue: Int = Int.MAX_VALUE,
    stepSize: Int = 1,
    name: String? = null
) : JNumberSpinner<Int>(value, minValue, maxValue, stepSize) {
    override val numberToT: (Number) -> Int
        get() = { it.toInt() }


    init {
        this.name = name
        this.editor = NumberEditor(this, "#")
        this.minimumSize = Dimension(52, minimumSize.height)
        this.preferredSize = Dimension(52, preferredSize.height)
    }
}
