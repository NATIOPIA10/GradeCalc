package com.example.gradecalc.domain

/** Result of checking a PIN the user typed. */
sealed interface PinResult {
    data object Ok : PinResult
    data class Wrong(val attemptsLeft: Int) : PinResult
    data class LockedOut(val remainingMs: Long) : PinResult
}

/** The rules of the PIN lock. Pure Kotlin so they can be unit tested. */
object PinPolicy {
    const val MIN_LENGTH = 4
    const val MAX_LENGTH = 6

    /** After this many wrong tries in a row the PIN pad is locked for [LOCKOUT_MS]. */
    const val MAX_ATTEMPTS = 5
    const val LOCKOUT_MS = 30_000L

    /** The app locks again when it was in the background for longer than this. */
    const val AUTO_LOCK_MS = 30_000L

    fun isValid(pin: String): Boolean = pin.length in MIN_LENGTH..MAX_LENGTH && pin.all { it in '0'..'9' }

    /** Milliseconds left before another try is allowed, or 0 when a try is allowed now. */
    fun lockoutRemainingMs(failedAttempts: Int, lastFailureAt: Long, now: Long): Long {
        if (failedAttempts < MAX_ATTEMPTS) return 0L
        return (lastFailureAt + LOCKOUT_MS - now).coerceIn(0L, LOCKOUT_MS)
    }

    /** True when the app was away long enough that the PIN must be asked for again. */
    fun shouldAutoLock(pinEnabled: Boolean, backgroundedAt: Long, now: Long): Boolean =
        pinEnabled && backgroundedAt > 0L && now - backgroundedAt >= AUTO_LOCK_MS
}
