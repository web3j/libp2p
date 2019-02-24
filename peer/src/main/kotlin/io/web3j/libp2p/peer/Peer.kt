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
 * Empty peer PeerID exception.
 */
class EmptyPeerIdException : Exception("empty peer PeerID")

/**
 * Exception for peer IDs that don't embed public keys.
 */
class NoPublicKeyException : Exception("public key is not embedded in peer PeerID")

/**
 * PeerID is a libp2p peer identity.
 */
data class PeerID(val id: Multihash) {

    /**
     * @return a base 58-encoded string of the PeerID.
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
        return this.id.raw.contentEquals((other as PeerID).id.raw)
    }

    /**
     * String prints out the peer.
     *
     * TODO(brian): ensure correctness at PeerID generation and
     * enforce this by only exposing functions that generate
     * IDs safely. Then any peer.PeerID type found in the
     * codebase is known to be correct.
     */
    override fun toString(): String {
        val pid = pretty()
        return if (pid.length <= 10) {
            "<peer.PeerID $pid>"
        } else {
            "<peer.PeerID ${pid.subSequence(0, 2)}*${pid.subSequence(pid.length - 6, pid.length)}"
        }
    }

    /**
     * Tests whether this PeerID was derived from shared-key.
     * @param sharedKey the private key.
     * @return true if it was derived.
     */
    fun matchesPrivateKey(sharedKey: PrivKey): Boolean = matchesPublicKey(sharedKey.publicKey())

    /**
     * Tests whether this PeerID was derived from the public key.
     * @param publicKey the public key.
     * @return true if it was derived.
     */
    fun matchesPublicKey(publicKey: PubKey): Boolean = idFromPublicKey(publicKey) == this

    /**
     * ExtractPublicKey attempts to extract the public key from an PeerID
     *
     * This method returns ErrNoPublicKey if the peer PeerID looks valid but it can't extract
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
         * Casts a string to PeerID type, and validates the id to make sure it is a multihash.
         * @param value the string value.
         * @return the PeerID.
         */
        fun idFromString(value: String): PeerID = PeerID(Multihash.fromHexString(value))

        /**
         * Casts a byte array to PeerID type, and validates the id to make sure it is a multihash.
         * @param value the byte array.
         * @return the PeerID.
         */
        fun idFromBytes(value: ByteArray): PeerID = PeerID(Multihash.cast(value))

        /**
         * @param value the string value.
         * @return a base 58-decoded Peer.
         */
        fun idB58Decode(value: String): PeerID = PeerID(Multihash.fromBase58String(value))

        /**
         * @param value the string value.
         * @return a hex-decoded Peer
         */
        fun idHexDecode(value: String): PeerID = PeerID(Multihash.fromHexString(value))

        /**
         * @param pubKey the public key.
         * @return the Peer PeerID corresponding to the provided public key.
         */
        fun idFromPublicKey(pubKey: PubKey): PeerID = with(MessageDigest.getInstance("SHA-256")) {
            update(pubKey.bytes())
            idFromBytes(Multihash.encode(digest(), Type.SHA2_256.code))
        }

        /**
         * @param privKey the private key.
         * @return the peer id corresponding to the provided private key.
         */
        fun idFromPrivateKey(privKey: PrivKey): PeerID = idFromPublicKey(privKey.publicKey())
    }
}

/**
 * IDSlice for sorting peers
 */
class IDSlice(val peerIds: MutableList<PeerID>) {
    fun len(): Int = peerIds.size

    fun swap(i: Int, j: Int) {
        val tmp = peerIds[i]
        peerIds[i] = peerIds[j]
        peerIds[j] = tmp
    }

    fun less(i: Int, j: Int): Boolean = peerIds[i].id.toHexString() < peerIds[j].id.toHexString()
}
