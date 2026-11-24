package com.example.gradecalc

import com.example.gradecalc.domain.PinPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinPolicyTest {
    @Test fun validPins_are4To6Digits() {
        assertTrue(PinPolicy.isValid("1234"))
        assertTrue(PinPolicy.isValid("123456"))
    }

    @Test fun invalidPins_areRejected() {
        assertFalse(PinPolicy.isValid("123"))
        assertFalse(PinPolicy.isValid("1234567"))
        assertFalse(PinPolicy.isValid("12a4"))
        assertFalse(PinPolicy.isValid(""))
        assertFalse(PinPolicy.isValid("12 4"))
    }

    @Test fun noLockout_beforeMaxAttempts() =
        assertEquals(0L, PinPolicy.lockoutRemainingMs(4, 1_000L, 1_001L))

    @Test fun lockout_afterMaxAttempts_countsDown() {
        assertEquals(30_000L, PinPolicy.lockoutRemainingMs(5, 1_000L, 1_000L))
        assertEquals(20_000L, PinPolicy.lockoutRemainingMs(5, 1_000L, 11_000L))
    }

    @Test fun lockout_endsAfterThirtySeconds() =
        assertEquals(0L, PinPolicy.lockoutRemainingMs(5, 1_000L, 31_000L))

    @Test fun lockout_clockMovedBack_neverExceedsLimit() =
        assertEquals(30_000L, PinPolicy.lockoutRemainingMs(5, 100_000L, 0L))

    @Test fun autoLock_afterThirtySecondsAway() =
        assertTrue(PinPolicy.shouldAutoLock(true, 1_000L, 31_000L))

    @Test fun autoLock_notWhenBackQuickly() =
        assertFalse(PinPolicy.shouldAutoLock(true, 1_000L, 20_000L))

    @Test fun autoLock_notWithoutPin() =
        assertFalse(PinPolicy.shouldAutoLock(false, 1_000L, 99_000L))

    @Test fun autoLock_notWhenNeverBackgrounded() =
        assertFalse(PinPolicy.shouldAutoLock(true, 0L, 99_000L))
}
