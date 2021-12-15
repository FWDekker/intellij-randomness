package com.fwdekker.randomness.ui

import com.fwdekker.randomness.Bundle
import com.github.sisyphsu.dateparser.DateParserUtils
import java.text.ParseException
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import javax.swing.JFormattedTextField


/**
 * A [JFormattedTextField] for [LocalDateTime]s, supporting virtually any date-time format as its input.
 *
 * @property default The default [LocalDateTime] that is returned in case no [value] has ever been set.
 */
class JDateTimeField(private val default: LocalDateTime) : JFormattedTextField(DateTimeFormatter()) {
    /**
     * Returns the [LocalDateTime] contained in this field, or [default] otherwise.
     *
     * @return the [LocalDateTime] contained in this field, or [default] otherwise
     */
    override fun getValue() = super.getValue() as? LocalDateTime ?: default

    /**
     * Sets the [LocalDateTime] that is contained in this field.
     *
     * @param value the [LocalDateTime] to set
     */
    override fun setValue(value: Any) {
        require(value is LocalDateTime) { Bundle("datetimefield.error.invalid_type") }

        super.setValue(value)
    }


    /**
     * Formats a string to a [LocalDateTime] and vice versa.
     */
    class DateTimeFormatter : AbstractFormatter() {
        /**
         * Attempts to parse [text] to a [LocalDateTime] using a best guess to detect the date format used.
         *
         * @param text the text to parse to a [LocalDateTime]
         * @return the [LocalDateTime] parsed from [text]
         * @throws ParseException if [text] could not be converted to a [LocalDateTime]
         */
        override fun stringToValue(text: String?): LocalDateTime =
            if (text.isNullOrBlank())
                throw ParseException(Bundle("datetimefield.error.empty_string"), 0)
            else
                try {
                    DateParserUtils.parseDateTime(text)
                } catch (e: DateTimeParseException) {
                    throw ParseException(e.message, e.errorIndex)
                }

        /**
         * Returns the ISO-8601-ish representation of [value], which must be a [LocalDateTime].
         *
         * @param value the [LocalDateTime] to return the string representation of
         * @return the ISO-8601-ish representation of [value], which must be a [LocalDateTime]
         */
        override fun valueToString(value: Any?): String =
            if (value !is LocalDateTime) throw ParseException(Bundle("datetimefield.error.invalid_type"), 0)
            else java.time.format.DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).format(value)
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The format used to represent [value] as a string.
         */
        const val DATE_TIME_FORMAT = "YYYY-MM-dd HH:mm:ss.SSS"
    }
}
