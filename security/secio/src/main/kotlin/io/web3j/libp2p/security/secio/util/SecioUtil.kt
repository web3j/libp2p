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

import io.web3j.libp2p.security.secio.model.BestParams
import io.web3j.libp2p.security.secio.model.ProposeMessage
import io.web3j.libp2p.shared.env.Libp2pException
import io.web3j.libp2p.shared.ext.compareAgainst
import io.web3j.libp2p.shared.ext.readVarintPrefixedMessage
import io.web3j.libp2p.shared.ext.toVarintPrefixedByteArray
import java.nio.charset.Charset

object SecioUtil {

    const val SECIO_STRING = "/secio/1.0.0\n"

    val SECIO_STRING_BYTES = SECIO_STRING.toByteArray(Charset.defaultCharset())

    /**
     * Selects the best parameters from the given proposals.
     */
    fun selectBest(proposal1: ProposeMessage, proposal2: ProposeMessage): BestParams {
        return selectBest(
            proposal1.publicKey.raw(),
            proposal2.publicKey.raw(), // Is this correct?

            proposal1.getNonce(),
            proposal2.getNonce(),

            proposal1.getExchanges(),
            proposal1.getCiphers(),
            proposal1.getHashes(),

            proposal2.getExchanges(),
            proposal2.getCiphers(),
            proposal2.getHashes()
        )
    }

    // support.js:81
    fun selectBest(
        localPubKeyBytes: ByteArray,
        remotePubKeyBytes: ByteArray,
        localNonce: ByteArray,
        remoteNonce: ByteArray,
        localExchanges: Array<String>,
        localCiphers: Array<String>,
        localHashes: Array<String>,
        remoteExchanges: Array<String>,
        remoteCiphers: Array<String>,
        remoteHashes: Array<String>
    ): BestParams {
        val oh1 = HashUtil.sha256(remotePubKeyBytes, localNonce)
        val oh2 = HashUtil.sha256(localPubKeyBytes, remoteNonce)
        val order = oh1.compareAgainst(oh2)
        if (order == 0) {
            // What's the likelihood of having the same digest for local & remote keys???
            throw Libp2pException("Identical checksums generated for local and remote keys, this is odd ...")
        }

        fun <T> findCommonElement(p1: Array<T>, p2: Array<T>): T {
            if (order == 0) {
                // Equivalent, so return the first item.
                return p1[0]
            }

            val (first, second) = if (order < 0) Pair(p2, p1) else Pair(p1, p2)
            return first.intersect(second.toList()).firstOrNull()
                ?: throw Libp2pException("No values in common between '$p1' and '$p2'")
        }

        // select the matches.

        val curve: String = findCommonElement(localExchanges, remoteExchanges)
        val cipher: String = findCommonElement(localCiphers, remoteCiphers)
        val hash: String = findCommonElement(localHashes, remoteHashes)
        return BestParams(curve, cipher, hash, order)
    }

    /**
     * Reads the byte array and returns the propose message if the byte array
     * is well-formed.
     * @param byteArray the byte array received.
     * @return the parsed propose message; null if the message was invalid.
     */
    fun parseSecioProposeMessage(byteArray: ByteArray): ProposeMessage? {
        if (byteArray.isEmpty()) {
            return null
        }

        // 1st byte indicates size.
        val parts = byteArray.readVarintPrefixedMessage() ?: return null

        val secioString = String(parts.first)
        if (secioString == SECIO_STRING) {
            // Found what we expect.
            return PrototypeUtil.parseProposeMessage(parts.second ?: return null)
        }
        return null
    }

    fun createMultistreamAndSecioMessage(): ByteArray {
        val msBytes = "/multistream/1.0.0\n".toVarintPrefixedByteArray()
        val siBytes = "/secio/1.0.0\n".toVarintPrefixedByteArray()
        return msBytes + siBytes
    }

    /**
     * Creates a byte array that can be sent across to the other peer.
     * @param proposal the proposal to be serialized.
     * @return the byte array containing the proposal.
     */
    fun createSecioProposeMessage(proposal: ProposeMessage): ByteArray {
        val proposalBytes = PrototypeUtil.serializeProposeMessage(proposal)
        return SECIO_STRING_BYTES.toVarintPrefixedByteArray() + proposalBytes
    }
}
