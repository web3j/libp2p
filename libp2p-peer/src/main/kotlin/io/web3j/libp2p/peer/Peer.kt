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
package io.web3j.libp2p.peer

import io.ipfs.multiformats.multihash.Multihash
import io.ipfs.multiformats.multihash.Type
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey
import io.web3j.libp2p.crypto.unmarshalPublicKey
import java.security.MessageDigest

/**
 * Empty peer ID exception.
 */
class EmptyPeerIdException : Exception("empty peer ID")

/**
 * Exception for peer IDs that don't embed public keys.
 */
class NoPublicKeyException : Exception("public key is not embedded in peer ID")

/**
 * ID is a libp2p peer identity.
 */
data class ID(val id: Multihash) {

    /**
     * @return a base 58-encoded string of the ID.
     */
    fun pretty(): String = idB58Encode()

    /**
     * @return a pretty peerID string in loggable JSON format.
     */
    fun loggable(): Map<String, String> {
        return mapOf("peerID" to pretty())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return this.id.raw.contentEquals((other as ID).id.raw)
    }

    /**
     * String prints out the peer.
     *
     * TODO(brian): ensure correctness at ID generation and
     * enforce this by only exposing functions that generate
     * IDs safely. Then any peer.ID type found in the
     * codebase is known to be correct.
     */
    override fun toString(): String {
        val pid = pretty()
        return if (pid.length <= 10) {
            "<peer.ID $pid>"
        } else {
            "<peer.ID ${pid.subSequence(0, 2)}*${pid.subSequence(pid.length - 6, pid.length)}"
        }
    }

    /**
     * Tests whether this ID was derived from shared-key.
     * @param sharedKey the private key.
     * @return true if it was derived.
     */
    fun matchesPrivateKey(sharedKey: PrivKey): Boolean = matchesPublicKey(sharedKey.publicKey())

    /**
     * Tests whether this ID was derived from the public key.
     * @param publicKey the public key.
     * @return true if it was derived.
     */
    fun matchesPublicKey(publicKey: PubKey): Boolean = idFromPublicKey(publicKey) == this

    /**
     * ExtractPublicKey attempts to extract the public key from an ID
     *
     * This method returns ErrNoPublicKey if the peer ID looks valid but it can't extract
     * the public key.
     */
    fun extractPublicKey(): PubKey {
        val decoded = Multihash.decode(id.raw)
        if (decoded.code != Type.ID.code) {
            throw NoPublicKeyException()
        }

        return unmarshalPublicKey(decoded.digest)
    }

    /**
     * @return base 58-encoded string.
     */
    fun idB58Encode(): String = id.toBase58String()

    /**
     * @return a hex-encoded string.
     */
    fun idHexEncode(): String = id.toHexString()

    companion object {

        /**
         * Casts a string to ID type, and validates the id to make sure it is a multihash.
         * @param value the string value.
         * @return the ID.
         */
        fun idFromString(value: String): ID = ID(Multihash.fromHexString(value))

        /**
         * Casts a byte array to ID type, and validates the id to make sure it is a multihash.
         * @param value the byte array.
         * @return the ID.
         */
        fun idFromBytes(value: ByteArray): ID = ID(Multihash.cast(value))

        /**
         * @param value the string value.
         * @return a base 58-decoded Peer.
         */
        fun idB58Decode(value: String): ID = ID(Multihash.fromBase58String(value))

        /**
         * @param value the string value.
         * @return a hex-decoded Peer
         */
        fun idHexDecode(value: String): ID = ID(Multihash.fromHexString(value))

        /**
         * @param pubKey the public key.
         * @return the Peer ID corresponding to the provided public key.
         */
        fun idFromPublicKey(pubKey: PubKey): ID {

            val sha256Bytes: ByteArray = with(MessageDigest.getInstance("SHA-256")) {
                update(pubKey.bytes())
                digest()
            }

            val hash = Multihash.encode(sha256Bytes, Type.SHA2_256.code)
            return idFromBytes(hash)
        }

        /**
         * @param privKey the private key.
         * @return the peer id corresponding to the provided private key.
         */
        fun idFromPrivateKey(privKey: PrivKey): ID = idFromPublicKey(privKey.publicKey())
    }
}

/**
 * IDSlice for sorting peers
 */
class IDSlice(val ids: MutableList<ID>) {
    fun len(): Int = ids.size

    fun swap(i: Int, j: Int) {
        val tmp = ids[i]
        ids[i] = ids[j]
        ids[j] = tmp
    }

    fun less(i: Int, j: Int): Boolean = ids[i].id.toHexString() < ids[j].id.toHexString()
}
