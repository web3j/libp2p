package io.web3j.libp2p.crypto.keys

import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.unmarshalPrivateKey
import io.web3j.libp2p.crypto.unmarshalPublicKey
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.experimental.xor

class EcdsaTest {

    @Test
    fun testECDSABasicSignAndVerify() {
        val (priv, pub) = generateEcdsaKeyPair()
        val data = "hello! and welcome to some awesome crypto primitives".toByteArray()
        val sig = priv.sign(data)

        Assertions.assertTrue(pub.verify(data, sig), "signature didn't match")

        // change data : data[0] = ^ data [0]
        data[0] = data[0].xor(data[0])

        Assertions.assertFalse(pub.verify(data, sig), "should have produced a verification error")
    }

    @Test
    fun testECDSABasicSignAndVerifyWithRegeneratedPublicKey() {
        val (priv, _) = generateEcdsaKeyPair()
        val data = "hello! and welcome to some awesome crypto primitives".toByteArray()
        val sig = priv.sign(data)

        val pub = priv.publicKey()
        Assertions.assertTrue(pub.verify(data, sig), "signature didn't match")

        // change data : data[0] = ^ data [0]
        data[0] = data[0].xor(data[0])

        Assertions.assertFalse(pub.verify(data, sig), "should have produced a verification error")
    }

    @Test
    fun testECDSASignZero() {
        val (priv, pub) = generateEcdsaKeyPair()
        val data = ByteArray(0)
        val sig = priv.sign(data)
        Assertions.assertTrue(pub.verify(data, sig), "signature didn't match")
    }

    @Test
    fun testECDSAMarshalLoop() {
        val (priv, pub) = generateEcdsaKeyPair()
        val privB = priv.bytes()
        val privNew: PrivKey = unmarshalPrivateKey(privB)
        Assertions.assertTrue(priv.equals(privNew) || privNew.equals(priv), "keys are not equal")

        val pubB = pub.bytes()
        val pubNew = unmarshalPublicKey(pubB)
        Assertions.assertTrue(pub.equals(pubNew) || pubNew.equals(pub), "keys are not equal")
    }

    @Test
    fun testKeyCycle() {
        val (priv, pub) = generateEcdsaKeyPair()
        val privBytes = priv.bytes()
        val reconstructedPrivateKey = unmarshalPrivateKey(privBytes)
        Assertions.assertEquals(priv, reconstructedPrivateKey, "Incorrect private key marshalling/unmarshalling")

        val pubBytes = pub.bytes()
        val reconstructedPublicKey = unmarshalPublicKey(pubBytes)
        Assertions.assertEquals(pub, reconstructedPublicKey, "Incorrect public key marshalling/unmarshalling")

        val pubRegenerated = priv.publicKey()
        Assertions.assertTrue(pub.equals(pubRegenerated) || pubRegenerated.equals(pub), "keys are not equal")
    }
}