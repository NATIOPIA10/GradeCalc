package com.example.gradecalc.data

import android.content.SharedPreferences
import com.example.gradecalc.domain.PinHasher

/** Keeps the PIN hash and the failed-try counter in the app's private storage. */
class PinStore(private val prefs: SharedPreferences) {
    val isSet: Boolean get() = prefs.contains(KEY_HASH) && prefs.contains(KEY_SALT)

    fun set(pin: String) {
        val salt = PinHasher.newSalt()
        prefs.edit()
            .putString(KEY_SALT, PinHasher.toHex(salt))
            .putString(KEY_HASH, PinHasher.toHex(PinHasher.hash(pin, salt)))
            .remove(KEY_FAILED)
            .remove(KEY_LAST_FAIL)
            .commit()
    }

    fun verify(pin: String): Boolean {
        val salt = prefs.getString(KEY_SALT, null)?.let(PinHasher::fromHex) ?: return false
        val expected = prefs.getString(KEY_HASH, null)?.let(PinHasher::fromHex) ?: return false
        return PinHasher.matches(pin, salt, expected)
    }

    fun clear() {
        prefs.edit().remove(KEY_SALT).remove(KEY_HASH).remove(KEY_FAILED).remove(KEY_LAST_FAIL).commit()
    }

    val failedAttempts: Int get() = prefs.getInt(KEY_FAILED, 0)
    val lastFailureAt: Long get() = prefs.getLong(KEY_LAST_FAIL, 0L)

    /** Saved at once (not in the background) so closing the app cannot reset the counter. */
    fun saveFailures(count: Int, at: Long) {
        prefs.edit().putInt(KEY_FAILED, count).putLong(KEY_LAST_FAIL, at).commit()
    }

    private companion object {
        const val KEY_SALT = "pin_salt"
        const val KEY_HASH = "pin_hash"
        const val KEY_FAILED = "pin_failed"
        const val KEY_LAST_FAIL = "pin_last_fail"
    }
}
