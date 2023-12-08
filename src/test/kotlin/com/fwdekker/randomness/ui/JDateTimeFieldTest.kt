package com.fwdekker.randomness.ui

import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.guiRun
import com.fwdekker.randomness.testhelpers.matchBundle
import com.github.sisyphsu.dateparser.DateParserUtils
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import java.text.ParseException
import java.time.LocalDateTime
import java.time.Month


/**
 * Unit tests for [JDateTimeField].
 */
object JDateTimeFieldTest : FunSpec({
    tags(Tags.SWING)


    lateinit var field: JDateTimeField


    beforeSpec {
        FailOnThreadViolationRepaintManager.install()
    }

    afterSpec {
        FailOnThreadViolationRepaintManager.uninstall()
    }

    beforeNonContainer {
        field = guiGet { JDateTimeField(LocalDateTime.MIN) }
    }


    context("value") {
        context("get") {
            test("returns the default if no value has been set") {
                val default = LocalDateTime.now()
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
            val dateTime = LocalDateTime.now()

            guiRun { field.value = dateTime }

            field.value shouldBe dateTime
        }
    }

    context("longValue") {
        test("get") {
            guiRun { field.value = LocalDateTime.of(1998, Month.SEPTEMBER, 24, 15, 5, 36) }

            field.longValue shouldBe 906_649_536_000L
        }

        test("set") {
            guiRun { field.longValue = 2_625_932_560L }

            field.value shouldBe LocalDateTime.of(1970, 1, 31, 9, 25, 32, 560_000_000)
        }
    }


    context("formatter") {
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
            guiRun {
                shouldThrow<ParseException> {
                    field.text = null
                    field.commitEdit()
                }.message should matchBundle("datetime_field.error.empty_string")
            }
        }

        test("fails if the text is empty") {
            guiRun {
                shouldThrow<ParseException> {
                    field.text = ""
                    field.commitEdit()
                }.message should matchBundle("datetime_field.error.empty_string")
            }
        }

        test("clears the old value if the set text is invalid") {
            guiRun {
                field.text = "3598-06-25"
                field.commitEdit()

                shouldThrow<ParseException> {
                    field.text = null
                    field.commitEdit()
                }

                field.text shouldBe ""
            }
        }
    }
})
