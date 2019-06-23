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
package io.web3j.libp2p.security.secio.model

import io.web3j.libp2p.crypto.Libp2pCrypto
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECPublicKeySpec
import spipe.pb.Spipe
import java.security.KeyFactory
import java.util.Base64

/**
 * Serves as a wrapper around the prototype [spipe.pb.Spipe.Exchange] class.
 */
class ExchangeMessage(val publicKey: BCECPublicKey, val signature: ByteArray) {

    /**
     * The value of `signature` in base-64 encoding.
     */
    val signatureInBase64: String = String(Base64.getEncoder().encode(signature))

    /**
     * The value of the public key in base-64 encoding.
     */
    val publicKeyInBase64: String = String(Base64.getEncoder().encode(publicKey.encoded))

    companion object {

        /**
         * Constructs a `ExchangeMessage` from the prototype equivalent.
         * @param exchange the prototype message.
         * @return a wrapper around that message.
         */
        fun fromPrototype(exchange: Spipe.Exchange): ExchangeMessage {
            val params = ECNamedCurveTable.getParameterSpec("prime256v1")
            val pubKey = ECPublicKeySpec(params.curve.decodePoint(exchange.epubkey.toByteArray()), params)
            val kf = KeyFactory.getInstance("ECDH", Libp2pCrypto.provider)
            val bcPubKey: BCECPublicKey = kf.generatePublic(pubKey) as BCECPublicKey
            return ExchangeMessage(bcPubKey, exchange.signature.toByteArray())
        }
    }
}
