package com.fwdekker.randomness.datetime

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.Icons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.ui.toLocalDateTime
import com.intellij.ui.JBColor
import com.intellij.util.xmlb.annotations.OptionTag
import java.awt.Color
import java.time.Instant
import java.time.format.DateTimeFormatter


/**
 * Contains settings for generating random date-times.
 *
 * @property minDateTime The minimum date-time as a millisecond epoch to be generated, inclusive.
 * @property maxDateTime The maximum date-time as a millisecond epoch to be generated, inclusive.
 * @property pattern The pattern in which the generated date-time is formatted.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class DateTimeScheme(
    var minDateTime: Long = DEFAULT_MIN_DATE_TIME,
    var maxDateTime: Long = DEFAULT_MAX_DATE_TIME,
    var pattern: String = DEFAULT_PATTERN,
    @OptionTag val arrayDecorator: ArrayDecorator = ArrayDecorator(),
) : Scheme() {
    override val name = Bundle("datetime.title")
    override val typeIcon get() = BASE_ICON
    override val decorators get() = listOf(arrayDecorator)


    override fun generateUndecoratedStrings(count: Int): List<String> {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return List(count) { formatter.format(random.nextLong(minDateTime, maxDateTime + 1).toLocalDateTime()) }
    }


    override fun doValidate(): String? {
        val formatIsValid =
            try {
                DateTimeFormatter.ofPattern(pattern)
                null
            } catch (exception: IllegalArgumentException) {
                exception.message
            }

        return if (minDateTime > maxDateTime) Bundle("datetime.error.min_datetime_above_max")
        else formatIsValid
    }

    override fun deepCopy(retainUuid: Boolean) =
        copy(arrayDecorator = arrayDecorator.deepCopy(retainUuid)).deepCopyTransient(retainUuid)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The base icon for date-times.
         */
        val BASE_ICON
            get() = TypeIcon(Icons.SCHEME, "2:3", listOf(JBColor(Color(249, 139, 158, 154), Color(249, 139, 158, 154))))

        /**
         * The default value of the [minDateTime] field.
         */
        val DEFAULT_MIN_DATE_TIME: Long = Instant.EPOCH.toEpochMilli()

        /**
         * The default value of the [maxDateTime] field.
         */
        val DEFAULT_MAX_DATE_TIME: Long = Instant.parse("2030-12-31T23:59:59.00Z").toEpochMilli()

        /**
         * The default value of the [pattern] field.
         */
        const val DEFAULT_PATTERN: String = "yyyy-MM-dd HH:mm:ss"
    }
}
