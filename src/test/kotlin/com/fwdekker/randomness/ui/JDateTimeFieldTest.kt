package com.fwdekker.randomness.ui

import com.github.sisyphsu.dateparser.DateParserUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.text.ParseException
import java.time.LocalDateTime


/**
 * GUI tests for [JDateTimeField].
 */
object JDateTimeFieldTest : Spek({
    lateinit var field: JDateTimeField


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        field = GuiActionRunner.execute<JDateTimeField> { JDateTimeField(LocalDateTime.MIN) }
    }


    describe("value") {
        describe("get") {
            it("returns the default if no value has been set") {
                val default = LocalDateTime.now()
                val myField = GuiActionRunner.execute<JDateTimeField> { JDateTimeField(default) }

                assertThat(myField.value).isEqualTo(default)
            }
        }

        describe("set") {
            it("throws an error if a non-date-time is set") {
                assertThatThrownBy { GuiActionRunner.execute { field.setValue("invalid") } }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Input value must be a 'LocalDateTime'.")
            }
        }

        it("returns the value that is set to it") {
            val dateTime = LocalDateTime.now()
            GuiActionRunner.execute { field.value = dateTime }

            assertThat(field.value).isEqualTo(dateTime)
        }
    }


    describe("formatter") {
        it("formats the value as the intended date") {
            val dateTime = LocalDateTime.now().withNano(0)
            GuiActionRunner.execute { field.value = dateTime }

            assertThat(DateParserUtils.parseDateTime(field.text)).isEqualTo(dateTime)
        }

        it("interprets input dates of other formats") {
            GuiActionRunner.execute {
                field.text = "4494-09-23"
                field.commitEdit()
            }

            assertThat(field.value).isEqualTo(DateParserUtils.parseDateTime(field.text))
        }

        it("fails if the text is null") {
            GuiActionRunner.execute {
                assertThatThrownBy {
                    field.text = null
                    field.commitEdit()
                }
                    .isInstanceOf(ParseException::class.java)
                    .hasMessage("Input string must not be empty.")
            }
        }

        it("fails if the text is empty") {
            GuiActionRunner.execute {
                assertThatThrownBy {
                    field.text = ""
                    field.commitEdit()
                }
                    .isInstanceOf(ParseException::class.java)
                    .hasMessage("Input string must not be empty.")
            }
        }

        it("retains the old value if the set text is invalid") {
            GuiActionRunner.execute {
                assertThatThrownBy {
                    field.text = "invalid date"
                    field.commitEdit()
                }
                    .isInstanceOf(ParseException::class.java)
                    .hasMessage("Text invalid date cannot parse at 0")
            }
        }
    }
})
