package com.fwdekker.randomness

import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


/**
 * Functions relating to time-limited behavior.
 */
object Timely {
    /**
     * The timeout in milliseconds before the generator should be interrupted when generating a value.
     */
    const val GENERATOR_TIMEOUT = 5000L


    /**
     * Runs the [generator] and returns its return value, or throws an exception if it takes longer than
     * [GENERATOR_TIMEOUT] milliseconds.
     *
     * @throws DataGenerationException if the generator timed out or if data could not be generated
     */
    @Throws(DataGenerationException::class)
    fun <T> generateTimely(generator: () -> T): T {
        val executor = Executors.newSingleThreadExecutor()
        try {
            return executor.submit<T> { generator() }.get(GENERATOR_TIMEOUT, TimeUnit.MILLISECONDS)
        } catch (exception: TimeoutException) {
            throw DataGenerationException(Bundle("misc.timed_out"), exception)
        } catch (exception: ExecutionException) {
            throw DataGenerationException(exception.cause?.message ?: exception.message, exception)
        } finally {
            executor.shutdown()
        }
    }
}
