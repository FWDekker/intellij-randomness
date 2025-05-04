package com.fwdekker.randomness

import com.fwdekker.randomness.Timestamp.Companion.FORMAT
import com.github.sisyphsu.dateparser.DateParserUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.random.Random


/**
 * A textual representation of a moment in time, with additional support for handling invalid user inputs.
 *
 * A [Timestamp] is similar in function to [LocalDateTime]: Both represent the notation of time rather than the moment
 * in time itself, and neither supports timezones. The most important difference with [LocalDateTime] is that the
 * [Timestamp]'s [value] is not guaranteed to be valid. Call [doValidate] to determine if (and why) the [value] is
 * (in)valid.
 *
 * The format for date-time timestamps is defined by [FORMAT]. However, [value]s are interpreted quite
 * liberally, so a [value] that deviates from [FORMAT] may still be valid. For example, a [value] of `5819` is
 * equivalent to a [value] of `5819-01-01 00:00:00.000`. This liberal interpretation is performed by [DateParserUtils].
 * The constructor of [Timestamp] proactively reformats the [value] into the [FORMAT]. Therefore, a
 * [Timestamp] contains a valid [value] if and only if [value] matches the [FORMAT] after the constructor
 * completes.
 *
 * To represent a [Timestamp] which is guaranteed to be valid, use [LocalDateTime] instead.
 *
 * @property value The textual representation of the timestamp.
 */
data class Timestamp(var value: String = "1970-01-01 00:00:00.000") : State() {
    /**
     * If [value] is invalid, this is the error message explaining why; otherwise, if [value] is valid, this is `null`.
     */
    private var error: String? = null

    /**
     * The [LocalDateTime] representation of [value], or `null` if [value] is invalid.
     */
    var dateTime: LocalDateTime? = null
        private set

    /**
     * The epoch millisecond representation of [value], or `null` if [value] is invalid.
     */
    var epochMilli: Long? = null
        private set


    init {
        try {
            if (value.isNotBlank()) {
                DateParserUtils.parseDateTime(value)?.also {
                    value = it.format(DateTimeFormatter.ofPattern(FORMAT))
                    dateTime = it
                    epochMilli = it.toInstant(ZoneOffset.UTC).toEpochMilli()
                }
            }
        } catch (exception: DateTimeParseException) {
            error = exception.message
        }
    }


    /**
     * Returns `true` if `this` occurs before [that], and returns `false` otherwise.
     *
     * If the [value] of either `this` or [that] is not valid, then this method returns `false`.
     *
     * Standard assumptions for [Comparable] are not guaranteed to hold. For example, it is entirely possible that
     * `a == b`, `a.isBefore(b)`, `b.isBefore(a)` are all `false` at the same time; for example if `a` and `b` have
     * different invalid [value]s. For this reason, [Timestamp] does not implement [Comparable]. However, if `this` and
     * [that] both have valid [value]s, then these strange situations do not occur.
     */
    fun isBefore(that: Timestamp): Boolean {
        val thisEpoch = this.epochMilli ?: Long.MAX_VALUE
        val thatEpoch = that.epochMilli ?: Long.MIN_VALUE

        return thisEpoch < thatEpoch
    }


    /**
     * Returns `null` if [value] is valid, or returns a string describing why it is not.
     *
     * Recall that a [value] is valid if [DateParserUtils] can interpret [value] as a date-time timestamp.
     */
    override fun doValidate(): String? = error

    override fun deepCopy(retainUuid: Boolean): Timestamp = Timestamp(value).deepCopyTransient(retainUuid)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The canonical format by which [Timestamp]s are represented.
         */
        const val FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
    }
}


/**
 * Returns `null` if this [String] is a valid date-time pattern, or a string describing why it is invalid otherwise.
 *
 * @see DateTimeFormatter
 */
fun String.doValidateDateTimePattern(): String? =
    try {
        DateTimeFormatter.ofPattern(this)
        null
    } catch (exception: IllegalArgumentException) {
        exception.message
    }

/**
 * Returns a random [LocalDateTime] between [min] (inclusive) and [max] (inclusive).
 *
 * Despite the method's name, this method returns a [LocalDateTime] instead of a [Timestamp]. As per the documentation
 * of [Timestamp], the class [LocalDateTime] can be seen as a [Timestamp] of which the [Timestamp.value] is guaranteed
 * to be valid.
 */
fun Random.nextTimestampInclusive(min: Timestamp, max: Timestamp): LocalDateTime {
    val epoch = nextLong(min.epochMilli!!, max.epochMilli!! + 1)
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC)
}
