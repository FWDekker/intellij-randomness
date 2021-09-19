package com.fwdekker.randomness

import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


/**
 * Runs the given function and returns its return value, or throws an exception if it takes longer than
 * [GENERATOR_TIMEOUT] milliseconds.
 *
 * @param T the return type of [generator]
 * @param generator the function to call
 * @return the return value of [generator]
 * @throws DataGenerationException if the generator timed out or if data could not be generated
 */
@Throws(DataGenerationException::class)
fun <T> generateTimely(generator: () -> T): T {
    val executor = Executors.newSingleThreadExecutor()
    try {
        return executor.submit<T> { generator() }.get(GENERATOR_TIMEOUT, TimeUnit.MILLISECONDS)
    } catch (e: TimeoutException) {
        throw DataGenerationException(Bundle("helpers.error.timed_out"), e)
    } catch (e: ExecutionException) {
        throw DataGenerationException(e.cause?.message ?: e.message, e)
    } finally {
        executor.shutdown()
    }
}


/**
 * The timeout in milliseconds before the generator should be interrupted when generating a value.
 */
const val GENERATOR_TIMEOUT = 5000L
