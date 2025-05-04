package com.fwdekker.randomness.ui

import com.fwdekker.randomness.Timestamp
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.guiRun
import com.fwdekker.randomness.testhelpers.matchBundle
import com.fwdekker.randomness.testhelpers.useEdtViolationDetection
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe


/**
 * Unit tests for [JDateTimeField].
 */
object JDateTimeFieldTest : FunSpec({
    lateinit var field: JDateTimeField


    useEdtViolationDetection()

    beforeNonContainer {
        field = guiGet { JDateTimeField() }
    }


    context("value") {
        context("get") {
            test("returns the default if no value has been set") {
                val default = Timestamp("2034")
                val myField = guiGet { JDateTimeField(default) }

                myField.value shouldBe default
            }
        }

        context("set") {
            test("throws an error if a non-date-time is set") {
                shouldThrow<IllegalArgumentException> { guiRun { field.setValue("invalid") } }
                    .message should matchBundle("datetime_field.error.invalid_type")
            }
        }

        test("returns the value that is set to it") {
            val dateTime = Timestamp("0006-08-17 23:50:51.974")

            guiRun { field.value = dateTime }

            field.value shouldBe dateTime
        }
    }


    context("formatter") {
        test("stores valid timestamps") {
            guiRun {
                field.text = "9017-07-12 05:22:05.767"
                field.commitEdit()
            }

            field.value.value shouldBe "9017-07-12 05:22:05.767"
        }

        test("stores liberally interpreted timestamps") {
            guiRun {
                field.text = "4494-09"
                field.commitEdit()
            }

            field.value.value shouldBe "4494-09-01 00:00:00.000"
        }

        test("stores values that cannot be interpreted as timestamps") {
            guiRun {
                field.text = "How are you?"
                field.commitEdit()
            }

            field.value.value shouldBe "How are you?"
        }

        test("interprets `null` as an empty string") {
            guiRun {
                field.text = null
                field.commitEdit()
            }

            field.value.value shouldBe ""
        }
    }
})
