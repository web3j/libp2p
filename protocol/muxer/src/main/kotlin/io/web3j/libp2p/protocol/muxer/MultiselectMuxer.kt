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
package io.web3j.libp2p.protocol.muxer

import io.web3j.streammux.MuxedStream

/**
 * Implements a multiplexer for multiselect clients based on protocol tags. <br />
 * Implementors can register handlers for various protocols using this facility.
 *
 * [@see https://github.com/multiformats/go-multistream/blob/master/multistream.go]
 */
interface MultiselectMuxer {

    /**
     * Attaches a handler for the given protocol.
     * @param protocol the protocol name to add a handler for.
     * @param protocolHandler the protocol handler.
     */
    fun addHandler(protocol: String, protocolHandler: ProtocolHandler): Unit

    /**
     * Performs protocol negotiation and selection.
     * @param stream the stream to perform negotation over.
     * @return a pair containing the selected protocol and handler; null if one was not successfully negotiated.
     */
    fun negotiate(stream: MuxedStream): Pair<String, ProtocolHandler>?
}
