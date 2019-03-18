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
package io.web3j.libp2p.protocol.muxer.impl

import io.web3j.libp2p.protocol.muxer.MultiselectClient
import io.web3j.libp2p.protocol.muxer.MultistreamErrorCodes
import io.web3j.libp2p.protocol.muxer.util.MultistreamCommunicator
import io.web3j.libp2p.protocol.muxer.util.MultistreamSelectException
import io.web3j.streammux.MuxedStream
import org.slf4j.LoggerFactory

/**
 * The default implementation of the  [MultiselectClient] interface.
 *
 * [@see https://github.com/multiformats/go-multistream/blob/master/client.go]
 */
class MultiselectClientImpl : MultiselectClient {

    /**
     * Performs the initial multistream handshake, informing the muxer of the protocol
     * that will be used to communicate on the stream.
     * @param stream the stream to agree the protocol for communication for.
     * @param protocol the protocol to select.
     * @return the selected protocol for communication over the stream; null if the muxer
     * on the other end cannot handle this protocol.
     * @throws [MultistreamSelectException] if an error was encountered.
     */
    override fun selectProtocolOrFail(stream: MuxedStream, protocol: String): String? {
        // Perform handshake to ensure multiselect protocol IDs match.
        handshake(stream)
        // Try to select the given protocol.
        return attemptProtocolSelection(stream, protocol)
    }

    /**
     * Goes through the given protocols and returns the first protocol that is agreed
     * with the other peer for use over the the stream.
     * @param stream the stream to agree the protocol for communication for.
     * @param protocols the protocols to try.
     * @return the selected protocol for communication over the stream; null if one was not agreed upon.
     * @throws [MultistreamSelectException] if an error was encountered.
     */
    override fun selectOneOf(stream: MuxedStream, vararg protocols: String): String? {
        handshake(stream)

        // For each protocol, attempt to select that protocol and return the first protocol selected.
        protocols.forEach { protocol ->
            val selectedProtocol = attemptProtocolSelection(stream, protocol)
            if (selectedProtocol != null) {
                return selectedProtocol
            }
        }

        // No protocols were found, so return no protocols supported error.
        throw MultistreamSelectException(MultistreamErrorCodes.NO_PROTOCOLS_SUPPORTED)
    }

    /**
     * Ensures that both peers are using the same (multiselect) protocol.
     * @param stream the stream to communicate over.
     * @throws [MultistreamSelectException] if the handshake was not completed successfully.
     */
    private fun handshake(stream: MuxedStream) {
        // Send the multistream protocol ID first to the other peer.
        MultistreamCommunicator.writeAndWait(stream, PROTOCOL_ID)

        // Read in the protocol ID from other party
        val handshakeContents = MultistreamCommunicator.readStreamUntilEof(stream)

        if (PROTOCOL_ID != handshakeContents) {
            LOGGER.warn("Expected $PROTOCOL_ID but received '$handshakeContents' for the protocol")
            throw MultistreamSelectException(MultistreamErrorCodes.PROTOCOL_ID_MISMATCH)
        }

        // Handshake succeeded.
    }

    /**
     * Attempts to select the given protocol for use over the stream.
     * @param stream the stream to agree the protocol for communication for.
     * @param protocol th eprotocol to try.
     */
    private fun attemptProtocolSelection(stream: MuxedStream, protocol: String): String? {
        // Tell counterparty we want to use protocol
        MultistreamCommunicator.writeAndWait(stream, protocol)
        val peerResponse = MultistreamCommunicator.readStreamUntilEof(stream)

        // Return protocol if response is equal to protocol or raise error
        return when (peerResponse) {
            protocol -> protocol
            PROTOCOL_NOT_FOUND_MSG -> {
                LOGGER.info("Protocol ($protocol) not supported by peer")
                null
            }
            else -> throw MultistreamSelectException(MultistreamErrorCodes.UNEXPECTED_RESPONSE)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MultiselectClientImpl.javaClass)!!

        // Identifies the multistream protocol itself.
        private const val PROTOCOL_ID = "/multistream/1.0.0"

        // Used to indicate that the requested protocol was not found.
        private const val PROTOCOL_NOT_FOUND_MSG = "na"
    }
}
