/*
 * Copyright 2019 Web3Labs Ltd.
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

import crypto.pb.Crypto
import io.web3j.libp2p.crypto.ECDSA_ALGORITHM
import io.web3j.libp2p.crypto.KEY_PKCS8
import io.web3j.libp2p.crypto.Libp2pCrypto
import io.web3j.libp2p.crypto.P256_CURVE
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey
import io.web3j.libp2p.crypto.SHA_256_WITH_ECDSA
import io.web3j.libp2p.shared.env.Libp2pException
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import org.bouncycastle.jce.spec.ECPublicKeySpec
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.security.PrivateKey as JavaPrivateKey
import java.security.interfaces.ECPrivateKey as JavaECPrivateKey

private val CURVE: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec(P256_CURVE)

/**
 * EcdsaPrivateKey is an implementation of an ecdsa private key
 */
class EcdsaPrivateKey(private val priv: JavaPrivateKey) : PrivKey(Crypto.KeyType.ECDSA) {

    init {
        // Set up private key.
        if (priv.format != KEY_PKCS8) {
            throw Libp2pException("Private key must be of '$KEY_PKCS8' format")
        }
    }

    override fun raw(): ByteArray = priv.encoded

    /**
     * Sign returns the signature of the input data.
     */
    override fun sign(data: ByteArray): ByteArray =
        with(Signature.getInstance(SHA_256_WITH_ECDSA, Libp2pCrypto.provider)) {
            // Signature is made up of r and s numbers.
            initSign(priv)
            update(data)
            sign()
        }

    override fun publicKey(): PubKey {
        val pubSpec: ECPublicKeySpec = (priv as BCECPrivateKey).run {
            val q = parameters.g.multiply((this as org.bouncycastle.jce.interfaces.ECPrivateKey).d)
            ECPublicKeySpec(q, parameters)
        }

        return with(KeyFactory.getInstance(ECDSA_ALGORITHM, Libp2pCrypto.provider)) {
            EcdsaPublicKey(generatePublic(pubSpec))
        }
    }

    override fun hashCode(): Int = priv.hashCode()
}

// EcdsaPublicKey is an implementation of an ecdsa public key
class EcdsaPublicKey(private val pub: PublicKey) : PubKey(Crypto.KeyType.ECDSA) {

    override fun raw(): ByteArray = pub.encoded

    override fun verify(data: ByteArray, signature: ByteArray): Boolean =
        with(Signature.getInstance(SHA_256_WITH_ECDSA, Libp2pCrypto.provider)) {
            initVerify(pub)
            update(data)
            verify(signature)
        }

    override fun hashCode(): Int = pub.hashCode()
}

/**
 * GenerateECDSAKeyPairWithCurve generates a new ecdsa private and public key with a specified curve.
 */
private fun generateECDSAKeyPairWithCurve(curve: ECNamedCurveParameterSpec): Pair<PrivKey, PubKey> {
    val keypair: KeyPair = with(KeyPairGenerator.getInstance(ECDSA_ALGORITHM, Libp2pCrypto.provider)) {
        initialize(curve, SecureRandom())
        genKeyPair()
    }

    return Pair(EcdsaPrivateKey(keypair.private as JavaECPrivateKey), EcdsaPublicKey(keypair.public))
}

/**
 * GenerateEcdsaKey generate a new ecdsa private and public key pair
 */
fun generateEcdsaKeyPair(): Pair<PrivKey, PubKey> {
    // http://www.bouncycastle.org/wiki/display/JA1/Supported+Curves+%28ECDSA+and+ECGOST%29
    // and
    // http://www.bouncycastle.org/wiki/pages/viewpage.action?pageId=362269
    return generateECDSAKeyPairWithCurve(CURVE)
}

/**
 * ECDSAKeyPairFromKey generates a new ecdsa private and public key from an input private key.
 */
fun ecdsaKeyPairFromKey(priv: EcdsaPrivateKey): Pair<PrivKey, PubKey> = Pair(priv, priv.publicKey())

/**
 * UnmarshalECDSAPrivateKey returns a private key from x509 bytes.
 */
fun unmarshalEcdsaPrivateKey(data: ByteArray): PrivKey = EcdsaPrivateKey(
    KeyFactory.getInstance(ECDSA_ALGORITHM, Libp2pCrypto.provider).generatePrivate(
        PKCS8EncodedKeySpec(data)
    )
)

fun unmarshalEcdsaPublicKey(keyBytes: ByteArray): PubKey =
    with(KeyFactory.getInstance(ECDSA_ALGORITHM, Libp2pCrypto.provider)) {
        EcdsaPublicKey(generatePublic(X509EncodedKeySpec(keyBytes)))
    }