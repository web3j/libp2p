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
package io.web3j.libp2p.peer

import io.ipfs.multiformats.multihash.Multihash
import io.ipfs.multiformats.multihash.Type
import java.lang.Exception

import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey
import io.web3j.libp2p.crypto.unmarshalPublicKey

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
     * Pretty returns a b58-encoded string of the ID
     */
    fun pretty(): String {
        return idB58Encode()
    }

    /**
     * Loggable returns a pretty peerID string in loggable JSON format
     */
    fun loggable(): Map<String, String> {
        return mapOf("peerID" to pretty())
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
     * MatchesPrivateKey tests whether this ID was derived from shared-key
     */
    fun matchesPrivateKey(sharedKey: PrivKey): Boolean {
        return matchesPublicKey(sharedKey.publicKey())
    }

    /**
     * MatchesPublicKey tests whether this ID was derived from pk
     */
    fun matchesPublicKey(publicKey: PubKey): Boolean {
        val otherId = idFromPublicKey(publicKey)
        // TODO: Check no error
        return otherId == this
    }

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
     * IDB58Encode returns b58-encoded string
     */
    fun idB58Encode(): String {
        return id.toBase58String()
    }

    /**
     * IDHexEncode returns hex-encoded string
     */
    fun idHexEncode(): String {
        return id.toHexString()
    }

    companion object {

        /**
         * IDFromString cast a string to ID type, and validate
         * the id to make sure it is a multihash.
         */
        fun idFromString(value: String): ID {
            val multihash = Multihash.fromHexString(value)
            return ID(multihash)
        }

        /**
         * IDFromBytes cast a string to ID type, and validate
         * the id to make sure it is a multihash.
         */
        fun idFromBytes(value: ByteArray): ID {
            val multihash = Multihash.cast(value)
            return ID(multihash)
        }

        /**
         * IDB58Decode returns a b58-decoded Peer.
         */
        fun idB58Decode(value: String): ID {
            val multihash = Multihash.fromBase58String(value)
            return ID(multihash)
        }

        /**
         * IDHexDecode returns a hex-decoded Peer
         */
        fun idHexDecode(value: String): ID {
            val multihash = Multihash.fromHexString(value)
            return ID(multihash)
        }

        /**
         * IDFromPublicKey returns the Peer ID corresponding to the provided public key.
         */
        fun idFromPublicKey(pubKey: PubKey): ID {
            val hash = Multihash.encode(pubKey.bytes(), Type.SHA2_256.code)
            return idFromBytes(hash)
        }

        /**
         * Return peer id corresponding to the provided private key.
         */
        fun idFromPrivateKey(privKey: PrivKey): ID {
            return idFromPublicKey(privKey.publicKey())
        }
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
