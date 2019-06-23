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
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.jce.spec.ECPublicKeySpec
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.KeyAgreement

/**
 * All non-builder, non-factory based cipher utilities go here.
 */
object CipherUtil {

    /**
     * Computes the secret key using the Diffie-Hellman method.
     * @param privateKey the private key.
     * @param publicKey the public key.
     * @return the byte array of the secret.
     */
    fun computeDHSecretKey(privateKey: PrivateKey, publicKey: PublicKey): ByteArray =
        with(KeyAgreement.getInstance("ECDH", Libp2pCrypto.provider)) {
            init(privateKey)
            doPhase(publicKey, true)
            return generateSecret()
        }

    /**
     * Computes the secret key using the Diffie-Hellman method.
     * @param privateKeyBytes the private key bytes.
     * @param publicKeyBytes the public key bytes.
     * @return the byte array of the secret.
     */
    fun computeDHSecretKey(privateKeyBytes: ByteArray, publicKeyBytes: ByteArray): ByteArray =
        computeDHSecretKey(loadPrivateKey(privateKeyBytes), loadPublicKey(publicKeyBytes))

    private fun loadPrivateKey(data: ByteArray): PrivateKey {
        val params = ECNamedCurveTable.getParameterSpec("prime256v1")
        val prvkey = ECPrivateKeySpec(BigInteger(data), params)
        val kf = KeyFactory.getInstance("ECDH", Libp2pCrypto.provider)
        return kf.generatePrivate(prvkey)
    }

    private fun loadPublicKey(data: ByteArray): PublicKey {
        val params = ECNamedCurveTable.getParameterSpec("prime256v1")
        val pubKey = ECPublicKeySpec(params.curve.decodePoint(data), params)
        val kf = KeyFactory.getInstance("ECDH", Libp2pCrypto.provider)
        return kf.generatePublic(pubKey)
    }
}
