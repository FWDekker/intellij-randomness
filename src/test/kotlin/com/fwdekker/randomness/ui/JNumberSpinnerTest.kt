package com.fwdekker.randomness.ui

import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.guiRun
import com.fwdekker.randomness.testhelpers.useEdtViolationDetection
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import javax.swing.JSpinner


/**
 * Unit tests for [JNumberSpinner].
 */
object JNumberSpinnerTest : FunSpec({
    useEdtViolationDetection()


    context("constructor failures") {
        test("should fail if the minimum value is greater than the maximum value") {
            shouldThrow<IllegalArgumentException> { JIntSpinner(value = -725, minValue = -602, maxValue = -929) }
                .message shouldBe "(minimum <= value <= maximum) is false"
        }

        test("should fail if the value is not in between the minimum and maximum value") {
            shouldThrow<IllegalArgumentException> { JIntSpinner(value = 1136, minValue = 552, maxValue = 944) }
                .message shouldBe "(minimum <= value <= maximum) is false"
        }
    }

    context("input handling") {
        test("should get and set the value") {
            val spinner = guiGet { JIntSpinner() }

            guiRun { spinner.value = 179 }

            spinner.value shouldBe 179
        }

        test("should return an int even if a long is set") {
            val spinner = guiGet { JIntSpinner() }

            guiRun { (spinner as JSpinner).value = 638L }

            spinner.value shouldBe 638
        }
    }
})

/**
 * Unit tests for [bindSpinners].
 */
object BindSpinnersTest : FunSpec({
    useEdtViolationDetection()


    context("binding") {
        test("throws an exception if the range is negative") {
            val min = guiGet { JSpinner() }
            val max = guiGet { JSpinner() }

            shouldThrow<IllegalArgumentException> { bindSpinners(min, max, -37.20) }
                .message shouldBe "maxRange must be a positive number."
        }
    }

    context("updating") {
        test("updates the minimum spinner if the maximum goes below its value") {
            val min = guiGet { JSpinner() }
            val max = guiGet { JSpinner() }
            bindSpinners(min, max)

            guiRun { max.value = -284.85 }

            min.value shouldBe -284.85
        }

        test("updates the maximum spinner if the minimum goes above its value") {
            val min = guiGet { JSpinner() }
            val max = guiGet { JSpinner() }
            bindSpinners(min, max)

            guiRun { min.value = 684.41 }

            max.value shouldBe 684.41
        }
    }
})

/**
 * Unit tests for [JDoubleSpinner].
 */
object JDoubleSpinnerTest : FunSpec({
    useEdtViolationDetection()


    context("input handling") {
        test("should return a double even if a long is set") {
            val spinner = guiGet { JDoubleSpinner() }

            guiRun { (spinner as JSpinner).value = -750L }

            spinner.value shouldBe -750.0
        }
    }

    context("getting surrounding values") {
        context("previous value") {
            test("returns the previous value") {
                val spinner = guiGet { JDoubleSpinner(741.4) }

                spinner.previousValue shouldBe 741.3
            }

            test("returns null if the current value is already at the minimum") {
                val spinner = guiGet { JDoubleSpinner(-637.8, minValue = -637.8) }

                spinner.previousValue shouldBe null
            }
        }

        context("next value") {
            test("returns the next value") {
                val spinner = guiGet { JDoubleSpinner(629.9) }

                spinner.nextValue shouldBe 630.0
            }

            test("returns null if the current value is already at the maximum") {
                val spinner = guiGet { JDoubleSpinner(-28.3, maxValue = -28.3) }

                spinner.nextValue shouldBe null
            }
        }
    }
})

/**
 * Unit tests for [JLongSpinner].
 */
object JLongSpinnerTest : FunSpec({
    useEdtViolationDetection()


    context("input handling") {
        test("truncates when storing a double") {
            val spinner = guiGet { JLongSpinner() }

            guiRun { spinner.setValue(786.79) }

            spinner.value shouldBe 786L
        }
    }

    context("getting surrounding values") {
        context("previous value") {
            test("returns the previous value") {
                val spinner = guiGet { JLongSpinner(56L) }

                spinner.previousValue shouldBe 55L
            }

            test("returns null if the current value is already at the minimum") {
                val spinner = guiGet { JLongSpinner(203L, minValue = 203L) }

                spinner.previousValue shouldBe null
            }
        }

        context("next value") {
            test("returns the next value") {
                val spinner = guiGet { JLongSpinner(112L) }

                spinner.nextValue shouldBe 113L
            }

            test("returns null if the current value is already at the maximum") {
                val spinner = guiGet { JLongSpinner(119L, maxValue = 119L) }

                spinner.nextValue shouldBe null
            }
        }
    }
})

/**
 * Unit tests for [JIntSpinner].
 */
object JIntSpinnerTest : FunSpec({
    useEdtViolationDetection()


    context("input handling") {
        test("truncates when storing a double") {
            val spinner = guiGet { JIntSpinner() }

            guiRun { spinner.setValue(850.45) }

            spinner.value shouldBe 850
        }

        test("overflows when storing a large long") {
            val spinner = guiGet { JIntSpinner() }

            guiRun { spinner.setValue(Integer.MAX_VALUE.toLong() + 2) }

            spinner.value shouldBe Integer.MIN_VALUE + 1
        }
    }

    context("getting surrounding values") {
        context("previous value") {
            test("returns the previous value") {
                val spinner = guiGet { JIntSpinner(205) }

                spinner.previousValue shouldBe 204
            }

            test("returns null if the current value is already at the minimum") {
                val spinner = guiGet { JIntSpinner(188, minValue = 188) }

                spinner.previousValue shouldBe null
            }
        }

        context("next value") {
            test("returns the next value") {
                val spinner = guiGet { JIntSpinner(96) }

                spinner.nextValue shouldBe 97
            }

            test("returns null if the current value is already at the maximum") {
                val spinner = guiGet { JIntSpinner(182, maxValue = 182) }

                spinner.nextValue shouldBe null
            }
        }
    }
})
