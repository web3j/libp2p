/*
 * Copyright 2019 BLK Technologies Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.web3j.libp2p.crypto.keys

import io.web3j.libp2p.crypto.ErrRsaKeyTooSmall
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.unmarshalPrivateKey
import io.web3j.libp2p.crypto.unmarshalPublicKey
import io.web3j.libp2p.shared.env.Libp2pException
import org.junit.jupiter.api.Assertions
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
        val (priv, pub) = generateRsaKeyPair(512)
        val data = ByteArray(0)
        val sig = priv.sign(data)
        assertTrue(pub.verify(data, sig), "signature didn't match")
    }

    @Test
    fun testRSABasicSignAndVerify() {
        val (priv, pub) = generateRsaKeyPair(512)
        val data = "hello! and welcome to some awesome crypto primitives".toByteArray()
        val sig = priv.sign(data)

        assertTrue(pub.verify(data, sig), "signature didn't match")

        // change data : data[0] = ^ data [0]
        data[0] = data[0].xor(data[0])

        assertFalse(pub.verify(data, sig), "should have produced a verification error")
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
    fun testRSAMarshalLoop() {
        val (priv, pub) = generateRsaKeyPair(512)
        val privB = priv.bytes()
        val privNew: PrivKey = unmarshalPrivateKey(privB)
        assertTrue(priv.equals(privNew) || privNew.equals(priv), "keys are not equal")

        val pubB = pub.bytes()
        val pubNew = unmarshalPublicKey(pubB)
        assertTrue(pub.equals(pubNew) || pubNew.equals(pub), "keys are not equal")
    }

    @Test
    fun testKeyCycle() {
        val (priv, pub) = generateRsaKeyPair(512)
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