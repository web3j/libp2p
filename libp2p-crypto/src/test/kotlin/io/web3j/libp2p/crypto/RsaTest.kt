package io.web3j.libp2p.crypto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.experimental.xor

class RsaTest {

    @Test
    fun testRSASmallKey() {
        val thrown = assertThrows(Libp2pException::class.java) { generateRsaKeyPair(384) }
        assertEquals(ErrRsaKeyTooSmall, thrown.message, "should have refused to create small RSA key")
    }

    @Test
    fun testRSASignZero() {
        val keys = generateRsaKeyPair(512)
        val data = ByteArray(0)
        val sig = keys.first.sign(data)
        assertTrue(keys.second.verify(data, sig), "signature didn't match")
    }

    @Test
    fun testRSABasicSignAndVerify() {
        val pair = generateRsaKeyPair(512)
        val data = "hello! and welcome to some awesome crypto primitives".toByteArray()
        val sig = pair.first.sign(data)

        assertTrue(pair.second.verify(data, sig), "signature didn't match")

        // change data : data[0] = ^ data [0]
        data[0] = data[0].xor(data[0])

        assertFalse(pair.second.verify(data, sig), "should have produced a verification error")
    }

    @Test
    fun testRSAMarshalLoop() {
        val keys = generateRsaKeyPair(512)

        val privB = keys.first.bytes();
        val privNew = unmarshalPrivateKey(privB)
        assertTrue(!keys.first.equals(privNew) || !privNew.equals(keys.first), "keys are not equal")

        val pubB = keys.second.bytes()
        val pubNew = unmarshalPublicKey(pubB)
        assertTrue(!keys.second.equals(pubNew) || !pubNew.equals(keys.second), "keys are not equal")
    }

}