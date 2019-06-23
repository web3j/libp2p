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

import io.web3j.libp2p.crypto.Libp2pCrypto
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 *
 */
object CipherFactory {

    /**
     * The cipher name for the aes-ctr spec.
     */
    private val AES_CTR = "AES/CTR/NoPadding"

    /**
     * Creates an AES-CTR cipher to be used for encryption with the default Sun security provider.
     * @param secretKeyBytes the secret key byte array.
     * @param iv the initial vector.
     * @return the cipher spec.
     */
    fun createDefaultAESEncryptionCipher(secretKeyBytes: ByteArray, iv: ByteArray): Cipher =
        with(Cipher.getInstance(AES_CTR)) {
            this.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secretKeyBytes, "AES"), IvParameterSpec(iv))
            this
        }

    /**
     * Creates an AES-CTR cipher to be used for encryption.
     * @param secretKeyBytes the secret key byte array.
     * @param iv the initial vector.
     * @return the cipher spec.
     */
    fun createAESEncryptionCipher(secretKeyBytes: ByteArray, iv: ByteArray): Cipher =
        with(Cipher.getInstance(AES_CTR, Libp2pCrypto.provider)) {
            this.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secretKeyBytes, "AES"), IvParameterSpec(iv))
            this
        }

    /**
     * Creates an AES-CTR cipher to be used for decryption with the default Sun security provider.
     * @param secretKeyBytes the secret key byte array.
     * @param iv the initial vector.
     * @return the cipher spec.
     */
    fun createDefaultAESDecryptionCipher(secretKeyBytes: ByteArray, iv: ByteArray): Cipher =
        with(Cipher.getInstance(AES_CTR)) {
            this.init(Cipher.DECRYPT_MODE, SecretKeySpec(secretKeyBytes, "AES"), IvParameterSpec(iv))
            this
        }

    /**
     * Creates an AES-CTR cipher to be used for decryption.
     * @param secretKeyBytes the secret key byte array.
     * @param iv the initial vector.
     * @return the cipher spec.
     */
    fun createAESDecryptionCipher(secretKeyBytes: ByteArray, iv: ByteArray): Cipher =
        with(Cipher.getInstance(AES_CTR, Libp2pCrypto.provider)) {
            this.init(Cipher.DECRYPT_MODE, SecretKeySpec(secretKeyBytes, "AES"), IvParameterSpec(iv))
            this
        }
}
