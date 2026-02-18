package seepick.localsportsclub.service

import kotlin.reflect.full.isSuperclassOf

fun <T> retry(
    maxAttempts: Int,
    suppressExceptions: List<Class<out Exception>>,
    code: () -> T,
): T =
    doRetry(maxAttempts = maxAttempts, suppressExceptions, currentAttempt = 1, code)

private fun <T> doRetry(
    maxAttempts: Int,
    suppressExceptions: List<Class<out Exception>>,
    currentAttempt: Int,
    code: () -> T,
): T =
    try {
        code()
    } catch (e: Exception) {
        if (suppressExceptions.any { it.kotlin.isSuperclassOf(e::class) } && currentAttempt < maxAttempts) {
            doRetry(maxAttempts = maxAttempts, suppressExceptions, currentAttempt = currentAttempt + 1, code)
        } else throw e
    }

private suspend fun <T> doRetrySuspend(
    maxAttempts: Int,
    suppressExceptions: List<Class<out Exception>>,
    currentAttempt: Int,
    code: suspend () -> T,
): T =
    try {
        code()
    } catch (e: Exception) {
        if (suppressExceptions.any { it.kotlin.isSuperclassOf(e::class) } && currentAttempt < maxAttempts) {
            doRetrySuspend(maxAttempts = maxAttempts, suppressExceptions, currentAttempt = currentAttempt + 1, code)
        } else throw e
    }
