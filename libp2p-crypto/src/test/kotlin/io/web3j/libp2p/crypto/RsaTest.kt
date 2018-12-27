package io.web3j.libp2p.crypto

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.experimental.xor

class RsaTest {

    @Test
    fun testRSASmallKey() {
        val thrown = Assertions.assertThrows(Libp2pException::class.java) { generateRsaKeyPair(384) }
        Assertions.assertEquals(ErrRsaKeyTooSmall, thrown.message, "should have refused to create small RSA key")
    }

    @Test
    fun testRSASignZero() {
        val keys = generateRsaKeyPair(512)
        val data = ByteArray(0)
        val sig = keys.first.sign(data)
        Assertions.assertTrue(keys.second.verify(data, sig), "signature didn't match")
    }

    @Test
    fun testRSABasicSignAndVerify() {
        val pair = generateRsaKeyPair(512)
        val data = "hello! and welcome to some awesome crypto primitives".toByteArray()
        val sig = pair.first.sign(data)

        Assertions.assertTrue(pair.second.verify(data, sig), "signature didn't match")

        // change data : data[0] = ^ data [0]
        data[0] = data[0].xor(data[0])

        Assertions.assertFalse(pair.second.verify(data, sig), "should have produced a verification error")
    }

    @Test
    fun testRSAMarshalLoop() {
        val keys = generateRsaKeyPair(512)

        val privB = keys.first.bytes();
        val privNew = unmarshalPrivateKey(privB)
        Assertions.assertTrue(!keys.first.equals(privNew) || !privNew.equals(keys.first), "keys are not equal")

        val pubB = keys.second.bytes()
        val pubNew = unmarshalPublicKey(pubB)
        Assertions.assertTrue(!keys.second.equals(pubNew) || !pubNew.equals(keys.second), "keys are not equal")
    }

}