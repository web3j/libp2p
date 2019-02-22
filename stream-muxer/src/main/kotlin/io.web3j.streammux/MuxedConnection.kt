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
package io.web3j.streammux

import io.ipfs.multiformats.multiaddr.Multiaddr
import io.ipfs.multiformats.multiaddr.Protocol

/**
 * Represents a stream-multiplexing connection to a remote peer.
 */
interface MuxedConnection {

    /**
     * Negotiates and opens a new stream with the other endpoint.
     * @param protocol the protocol to be used in the stream
     * @param streamId the stream ID.
     * @param peerId the ID of the peer that the stream is to connect to.
     * @param multiaddr the address that the stream is to connect to.
     */
    fun openStream(protocol: Protocol, streamId: Long, peerId: Any, multiaddr: Multiaddr): Stream

    /**
     * Accepts a muxed stream opened by the other side.
     */
    fun acceptStream(): Stream

    /**
     * @return whether a connection is fully closed, so it can be garbage collected.
     */
    fun isClosed(): Boolean

    /**
     * Closes the connection and all associated streams.
     * @return true if the connection was fully closed.
     */
    fun close(): Boolean
}
