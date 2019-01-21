package io.web3j.libp2p.crypto.keys

import io.web3j.libp2p.crypto.ErrRsaKeyTooSmall
import io.web3j.libp2p.crypto.unmarshalPrivateKey
import io.web3j.libp2p.crypto.unmarshalPublicKey
import io.web3j.libp2p.shared.env.Libp2pException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.experimental.xor

class Secp256k1Test {

    @Test
    fun testSecp256k1BasicSignAndVerify() {
        val (priv, pub) = generateSecp256k1KeyPair()
        val data = "hello! and welcome to some awesome crypto primitives".toByteArray()
        val sig = priv.sign(data)
        Assertions.assertTrue(pub.verify(data, sig), "signature didn't match")

        // change data : data[0] = ^ data [0]
        data[0] = data[0].xor(data[0])

        Assertions.assertFalse(pub.verify(data, sig), "should have produced a verification error")

        val thrown = Assertions.assertThrows(Libp2pException::class.java) { generateRsaKeyPair(384) }
        Assertions.assertEquals(ErrRsaKeyTooSmall, thrown.message, "should have refused to create small RSA key")
    }

    @Test
    fun testSecp256k1SignZero() {
        val (priv, pub) = generateSecp256k1KeyPair()
        val data = ByteArray(0)
        val sig = priv.sign(data)
        Assertions.assertTrue(pub.verify(data, sig), "signature didn't match")
    }

    @Test
    fun testSecp256k1SignNonZero() {
        val (priv, pub) = generateSecp256k1KeyPair()
        val data = "Hello there!".toByteArray()
        val sig = priv.sign(data)
        Assertions.assertTrue(pub.verify(data, sig), "signature didn't match")
    }

    @Test
    fun testKeyCycle() {
        val (priv, pub) = generateSecp256k1KeyPair()
        val privBytes = priv.bytes()
        val reconstructedPrivateKey = unmarshalPrivateKey(privBytes)
        Assertions.assertEquals(priv, reconstructedPrivateKey, "Incorrect private key marshalling/unmarshalling")

        val pubRegenerated = priv.publicKey()
        Assertions.assertTrue(pub.equals(pubRegenerated) || pubRegenerated.equals(pub), "keys are not equal")

        val pubBytes = pub.bytes()
        val reconstructedPublicKey = unmarshalPublicKey(pubBytes)
        Assertions.assertEquals(pub, reconstructedPublicKey, "Incorrect public key marshalling/unmarshalling")
    }

    @Test
    fun testSecp256k1MarshalLoop() {
        val (priv, pub) = generateSecp256k1KeyPair()
        val privB = priv.bytes()
        val privNew = unmarshalPrivateKey(privB)
        Assertions.assertTrue(priv.equals(privNew) || privNew.equals(priv), "keys are not equal")

        val pubBytes = pub.bytes()
        val pubNew = unmarshalPublicKey(pubBytes)
        Assertions.assertTrue(pub.equals(pubNew) || pubNew.equals(pub), "keys are not equal")
    }
}