package com.fwdekker.randomness.ui

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.github.sisyphsu.dateparser.DateParserUtils
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import java.text.ParseException
import java.time.LocalDateTime


/**
 * Unit tests for [JDateTimeField].
 */
object JDateTimeFieldTest : FunSpec({
    tags(NamedTag("Swing"))


    lateinit var field: JDateTimeField


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        field = guiGet { JDateTimeField(LocalDateTime.MIN) }
    }


    test("value") {
        test("get") {
            test("returns the default if no value has been set") {
                val default = LocalDateTime.now()
                val myField = guiGet { JDateTimeField(default) }

                myField.value shouldBe default
            }
        }

        test("set") {
            test("throws an error if a non-date-time is set") {
                shouldThrow<IllegalArgumentException> { guiRun { field.setValue("invalid") } }
                    .message shouldBe Bundle("datetime_field.error.invalid_type")
            }
        }

        test("returns the value that is set to it") {
            val dateTime = LocalDateTime.now()

            guiRun { field.value = dateTime }

            field.value shouldBe dateTime
        }
    }

    test("longValue") {
        test("get") {
            field.longValue = 2_625_932_560L

            field.value shouldBe TODO()
        }

        test("set") {
            field.value = TODO()

            field.longValue shouldBe TODO()
        }
    }


    test("formatter") {
        test("formats the value as the intended date") {
            val dateTime = LocalDateTime.now().withNano(0)

            guiRun { field.value = dateTime }

            DateParserUtils.parseDateTime(field.text) shouldBe dateTime
        }

        test("interprets input dates of other formats") {
            guiRun {
                field.text = "4494-09-23"
                field.commitEdit()
            }

            field.value shouldBe DateParserUtils.parseDateTime(field.text)
        }

        test("fails if the text is null") {
            shouldThrow<ParseException> {
                guiRun {
                    field.text = null
                    field.commitEdit()
                }
            }.message shouldBe Bundle("datetime_field.error.empty_string")
        }

        test("fails if the text is empty") {
            shouldThrow<ParseException> {
                guiRun {
                    field.text = ""
                    field.commitEdit()
                }
            }.message shouldBe Bundle("datetime_field.error.empty_string")
        }

        test("retains the old value if the set text is invalid") {
            shouldThrow<ParseException> {
                guiRun {
                    field.text = null
                    field.commitEdit()
                }
            }.message shouldStartWith "Text invalid date cannot parse at"
        }
    }
})
