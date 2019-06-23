/*
 * Copyright 2019 BLK Technologies Limited (web3labs.com).
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
package io.web3j.libp2p.security.secio.util

import io.web3j.libp2p.shared.ext.fromBase64
import io.web3j.libp2p.shared.ext.toBase64String
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CipherUtilTest {

    @Test
    fun diffieHellmanSharedKeyGenerationTest() {
        // 32 bytes
        val ourPrivateKey = "GU3iW6Nc7h75BKM/jc2uSDQERNvwcklfPSDQmXAXlw4=".fromBase64()

        // 65 bytes
        val theirPubKey =
            "BHSu7odl2J1iteLAlMzRiANIpJ7u8jg+0Ml9xGszBVwvLdRhZpruOoMIgRYefJys7/VYaUOuewqy1rMklsB3pZw=".fromBase64()
        val expectedSecretB64 = "FjfqRQ+RK0Tt479pDZXyWrlb+5Y22iCOx0WEvfJeN5U="

        val secret = CipherUtil.computeDHSecretKey(ourPrivateKey, theirPubKey)
        Assertions.assertEquals(expectedSecretB64, secret.toBase64String(), "Incorrect secret")
    }

    @Test
    fun testSymmetricEncryptionAndDecryption() {
        // Local
        val secretKeyBytes = "yxensRGEFaD2sFyaZV7LdSIcSYxs2hPkGvkUlLJYRAY=".fromBase64()
        val iv = "AViW7MKvhUO3jhfeW0QsSA==".fromBase64()

        val expectedEncryptedB64Value = "kry/MFfp63EKeqeIP/+9gw=="
        val dataToEncryptB64 = "DgTgrG3b2dnbhKd2nLK4vQ=="

        // Now build up parameters.
        val dataToEncrypt = dataToEncryptB64.fromBase64()
        val expectedEncryptedBytes = expectedEncryptedB64Value.fromBase64()

        val encryptionCipher = CipherFactory.createAESEncryptionCipher(secretKeyBytes, iv)
        val decryptionCipher = CipherFactory.createAESDecryptionCipher(secretKeyBytes, iv)

        // Test symmetric operation.
        val encrypted = encryptionCipher.doFinal(dataToEncrypt)
        val decrypted = decryptionCipher.doFinal(encrypted)
        Assertions.assertEquals(
            dataToEncryptB64,
            decrypted.toBase64String(),
            "Symmetric encryption/decryption failed"
        )

        // Now try to decrypt the input value.
        val decrypted2 = decryptionCipher.doFinal(expectedEncryptedBytes)
        Assertions.assertEquals(dataToEncryptB64, decrypted2.toBase64String(), "Could not decrypt data")
    }
}
