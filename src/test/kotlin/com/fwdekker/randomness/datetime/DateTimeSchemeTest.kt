package com.fwdekker.randomness.datetime

import com.fwdekker.randomness.DataGenerationException
import com.github.sisyphsu.dateparser.DateParserUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDateTime
import java.util.regex.Pattern


/**
 * Unit tests for [DateTimeScheme].
 */
object DateTimeSchemeTest : Spek({
    lateinit var dateTimeScheme: DateTimeScheme


    beforeEachTest {
        dateTimeScheme = DateTimeScheme()
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            dateTimeScheme.pattern = "ff"

            assertThatThrownBy { dateTimeScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        it("generates a date in the given range") {
            dateTimeScheme.minDateTime = 45_582
            dateTimeScheme.maxDateTime = 1_289_568_956

            repeat(256) {
                val generatedString = dateTimeScheme.generateStrings().single()

                assertThat(DateParserUtils.parseDateTime(generatedString).toEpochMilli())
                    .isGreaterThanOrEqualTo(dateTimeScheme.minDateTime)
                    .isLessThanOrEqualTo(dateTimeScheme.maxDateTime)
            }
        }

        it("generates a date according to the given format") {
            dateTimeScheme.pattern = "YYYY.MM"

            val generatedStrings = dateTimeScheme.generateStrings(1000)

            generatedStrings.forEach { assertThat(it).matches(Pattern.compile("[0-9]{4}\\.[0-9]{2}")) }
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(DateTimeScheme().doValidate()).isNull()
        }

        it("fails if the minimum date-time is larger than the maximum date-time") {
            dateTimeScheme.minDateTime = 819_876
            dateTimeScheme.maxDateTime = 447_861

            assertThat(dateTimeScheme.doValidate())
                .isEqualTo("Minimum date-time should be less than or equal to maximum date-time.")
        }

        it("fails if the date-time pattern is invalid") {
            dateTimeScheme.pattern = "YYYY-ffff"

            assertThat(dateTimeScheme.doValidate()).isEqualTo("Unknown pattern letter: f")
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            dateTimeScheme.minDateTime = 952_549
            dateTimeScheme.arrayDecorator.count = 567

            val copy = dateTimeScheme.deepCopy()
            copy.minDateTime = 356_934
            copy.arrayDecorator.count = 30

            assertThat(dateTimeScheme.minDateTime).isEqualTo(952_549)
            assertThat(dateTimeScheme.arrayDecorator.count).isEqualTo(567)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            dateTimeScheme.minDateTime = 445
            dateTimeScheme.maxDateTime = 478
            dateTimeScheme.pattern = "mm-Y"
            dateTimeScheme.arrayDecorator.count = 904

            val newScheme = DateTimeScheme()
            newScheme.copyFrom(dateTimeScheme)

            assertThat(newScheme)
                .isEqualTo(dateTimeScheme)
                .isNotSameAs(dateTimeScheme)
            assertThat(newScheme.arrayDecorator)
                .isEqualTo(dateTimeScheme.arrayDecorator)
                .isNotSameAs(dateTimeScheme.arrayDecorator)
        }
    }


    describe("toLocalDateTime and fromLocalDateTime") {
        it("converts to a date-time and back") {
            val epoch = 8_485_011_871

            assertThat(epoch.toLocalDateTime().toEpochMilli()).isEqualTo(epoch)
        }

        it("converts to an epoch and back") {
            val instant = LocalDateTime.now().withNano(0)

            assertThat(instant.toEpochMilli().toLocalDateTime()).isEqualTo(instant)
        }
    }
})
