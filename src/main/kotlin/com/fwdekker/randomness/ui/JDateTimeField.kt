package com.fwdekker.randomness.ui

import com.fwdekker.randomness.Bundle
import com.github.sisyphsu.dateparser.DateParserUtils
import java.text.ParseException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import javax.swing.JFormattedTextField


/**
 * A [JFormattedTextField] for [LocalDateTime]s, supporting virtually any date-time format as its input.
 *
 * @param default the default [LocalDateTime] that is returned in case no [value] has ever been set
 */
class JDateTimeField(
    private val default: LocalDateTime = LocalDateTime.now(),
) : JFormattedTextField(DateTimeFormatter()) {
    /**
     * The current [value] represented as a [Long] of the millisecond epoch.
     */
    var longValue: Long
        get() = value.toEpochMilli()
        set(value) {
            this.value = value.toLocalDateTime()
        }

    /**
     * Returns the [LocalDateTime] contained in this field, or [default] otherwise.
     */
    override fun getValue() = super.getValue() as? LocalDateTime ?: default

    /**
     * Sets the [LocalDateTime] that is contained in this field.
     */
    override fun setValue(value: Any) {
        require(value is LocalDateTime) { Bundle("datetime_field.error.invalid_type") }

        super.setValue(value)
    }


    /**
     * Formats a string to a [LocalDateTime] and vice versa.
     */
    class DateTimeFormatter : AbstractFormatter() {
        /**
         * Attempts to parse [text] to a [LocalDateTime] using a best guess to detect the date format used.
         *
         * @throws ParseException if [text] could not be converted to a [LocalDateTime]
         */
        override fun stringToValue(text: String?): LocalDateTime =
            if (text.isNullOrBlank())
                throw ParseException(Bundle("datetime_field.error.empty_string"), 0)
            else
                try {
                    DateParserUtils.parseDateTime(text)
                } catch (exception: DateTimeParseException) {
                    throw ParseException(exception.message, exception.errorIndex)
                }

        /**
         * Returns the ISO-8601-ish representation of [value], which must be a [LocalDateTime].
         */
        override fun valueToString(value: Any?): String =
            if (value !is LocalDateTime) throw ParseException(Bundle("datetime_field.error.invalid_type"), 0)
            else java.time.format.DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).format(value)
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The format used to represent [value] as a string.
         */
        const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
    }
}


/**
 * Converts an epoch millisecond timestamp to a [LocalDateTime] object.
 */
fun Long.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)

/**
 * Converts this [LocalDateTime] to an epoch millisecond timestamp.
 */
fun LocalDateTime.toEpochMilli() = toInstant(ZoneOffset.UTC).toEpochMilli()


/**
 * Binds two [JDateTimeField]s together, analogous to how [bindSpinners] works.
 */
fun bindDateTimes(minField: JDateTimeField, maxField: JDateTimeField) {
    addChangeListenerTo(minField) {
        val minEpoch = minField.longValue
        val maxEpoch = maxField.longValue

        if (minEpoch > maxEpoch) maxField.value = minField.value
    }
    addChangeListenerTo(maxField) {
        val minEpoch = minField.longValue
        val maxEpoch = maxField.longValue

        if (maxEpoch < minEpoch) minField.value = maxField.value
    }
}
