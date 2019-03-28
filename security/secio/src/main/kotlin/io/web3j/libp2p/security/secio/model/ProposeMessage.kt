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

import com.google.protobuf.ByteString
import io.web3j.libp2p.crypto.PubKey
import io.web3j.libp2p.crypto.marshalPublicKey
import io.web3j.libp2p.crypto.unmarshalPublicKey
import spipe.pb.Spipe
import java.util.*

/**
 * Serves as a wrapper around the prototype [spipe.pb.Spipe.Propose] class.
 */
class ProposeMessage(val publicKey: PubKey, val random: ByteArray) {

    /**
     * The value of `rand` in base-64 encoding.
     */
    val randomInBase64: String = String(Base64.getEncoder().encode(random))

    /**
     * The value of the public key in base-64 encoding.
     */
    val publicKeyInBase64: String = String(Base64.getEncoder().encode(marshalPublicKey(publicKey)))

    /**
     * The set of supported ciphers.
     */
    private val ciphers = mutableSetOf<String>()

    /**
     * The set of supported hashes.
     */
    private val hashes = mutableSetOf<String>()

    /**
     * The set of supported exchanges.
     */
    private val exchanges = mutableSetOf<String>()

    /**
     * Adds ciphers to this message.
     * @param ciphers the ciphers to add.
     * @return this instance.
     */
    fun withCiphers(vararg ciphers: String): ProposeMessage {
        this.ciphers.addAll(ciphers)
        return this
    }

    /**
     * Adds hashes to this message.
     * @param hashes the hashes to add.
     * @return this instance.
     */
    fun withHashes(vararg hashes: String): ProposeMessage {
        this.hashes.addAll(hashes)
        return this
    }

    /**
     * Adds hashes to this message.
     * @param hashes the hashes to add.
     * @return this instance.
     */
    fun withExchanges(vararg hashes: String): ProposeMessage {
        this.exchanges.addAll(hashes)
        return this
    }

    /**
     * Checks if the given hash is included in this proposal.
     * @param hash the hash to check.
     * @return true if the hash is supported.
     */
    fun supportsHash(hash: String): Boolean = hashes.contains(hash)

    /**
     * Checks if the given cipher is included in this proposal.
     * @param cipher the cipher to check.
     * @return true if the cipher is supported.
     */
    fun supportsCipher(cipher: String): Boolean = ciphers.contains(cipher)

    /**
     * Checks if the given exchange is included in this proposal.
     * @param exchange the exchange to check.
     * @return true if the exchange is supported.
     */
    fun supportsExchange(exchange: String): Boolean = exchanges.contains(exchange)

    /**
     * Converts this instance into a `Propose`.
     * @return the propose instance.
     */
    fun asPropose(): Spipe.Propose {
        return Spipe.Propose.newBuilder()
            .setCiphers(ciphers.joinToString(","))
            .setExchanges(exchanges.joinToString(","))
            .setHashes(hashes.joinToString(","))
            .setPubkey(ByteString.copyFrom(marshalPublicKey(publicKey)))
            .setRand(ByteString.copyFrom(random))
            .build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProposeMessage

        if (publicKey != other.publicKey) return false
        if (randomInBase64 != other.randomInBase64) return false
        if (ciphers != other.ciphers) return false
        if (hashes != other.hashes) return false
        if (exchanges != other.exchanges) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + randomInBase64.hashCode()
        result = 31 * result + ciphers.hashCode()
        result = 31 * result + hashes.hashCode()
        result = 31 * result + exchanges.hashCode()
        return result
    }


    companion object {

        /**
         * Constructs a `ProposedMessage` from the prototype equivalent.
         * @param proposed the prototype message.
         * @return a wrapper around that message.
         */
        fun fromPrototype(proposed: Spipe.Propose): ProposeMessage {
            val result = ProposeMessage(
                unmarshalPublicKey(proposed.pubkey.toByteArray()),
                proposed.rand.toByteArray()
            )
            result.ciphers.addAll((proposed.ciphers ?: "").split(","))
            result.hashes.addAll((proposed.hashes ?: "").split(","))
            result.exchanges.addAll((proposed.exchanges ?: "").split(","))

            return result
        }

    }
}