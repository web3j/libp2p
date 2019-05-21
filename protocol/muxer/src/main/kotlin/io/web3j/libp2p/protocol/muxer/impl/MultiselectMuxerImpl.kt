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

import io.web3j.libp2p.protocol.muxer.MultiselectMuxer
import io.web3j.libp2p.protocol.muxer.ProtocolHandler
import io.web3j.streammux.MuxedStream
import org.slf4j.LoggerFactory

/**
 * A (thread-safe) implementation of [MultiselectMuxer] that handles protocol routing.
 */
class MultiselectMuxerImpl : MultiselectMuxer {

    /**
     * Our map of protocols to handlers.
     */
    private val handlerMap = mutableMapOf<String, ProtocolHandler>()

    /**
     * Attaches a handler for the given protocol.
     * @param protocol the protocol name to add a handler for.
     * @param protocolHandler the protocol handler.
     */
    override fun addHandler(protocol: String, protocolHandler: ProtocolHandler) {
        synchronized(handlerMap) {
            handlerMap[protocol] = protocolHandler
        }
    }

    /**
     * Performs protocol negotiation and selection.
     * @param stream the stream to perform negotiation over.
     * @return a pair containing the selected protocol and handler; null if one was not successfully negotiated.
     */
    override fun negotiate(stream: MuxedStream): Pair<String, ProtocolHandler>? {
        TODO("TODO: negotiate a protocol!")
        val negotiatedProtocol = ""
        // Reference: https://github.com/filecoin-project/lua-filecoin/blob/master/libs/multiselect.lua

        LOGGER.debug("Negotiated protocol: $negotiatedProtocol")
        val handler = synchronized(handlerMap) {
            handlerMap[negotiatedProtocol]
        }

        return if (handler == null) {
            null
        } else Pair(negotiatedProtocol, handler)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MultiselectMuxerImpl.javaClass)!!
    }
}
