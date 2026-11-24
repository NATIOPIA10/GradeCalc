package com.example.gradecalc

import com.example.gradecalc.domain.PinHasher
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {
    private val salt = ByteArray(16) { it.toByte() }

    @Test fun samePin_matches() {
        val hash = PinHasher.hash("1234", salt)
        assertTrue(PinHasher.matches("1234", salt, hash))
    }

    @Test fun wrongPin_doesNotMatch() {
        val hash = PinHasher.hash("1234", salt)
        assertFalse(PinHasher.matches("1235", salt, hash))
    }

    @Test fun differentSalt_givesDifferentHash() {
        val other = ByteArray(16) { (it + 1).toByte() }
        assertFalse(PinHasher.hash("1234", salt).contentEquals(PinHasher.hash("1234", other)))
    }

    @Test fun hash_doesNotContainThePin() =
        assertNotEquals("1234", PinHasher.toHex(PinHasher.hash("1234", salt)))

    @Test fun hex_roundTrips() {
        val bytes = byteArrayOf(0, 1, 15, 16, 127, -128, -1)
        assertArrayEquals(bytes, PinHasher.fromHex(PinHasher.toHex(bytes)))
    }

    @Test fun newSalt_isRandomAnd16Bytes() {
        val a = PinHasher.newSalt()
        val b = PinHasher.newSalt()
        assertTrue(a.size == 16 && b.size == 16)
        assertFalse(a.contentEquals(b))
    }
}
