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
 * A factory for creating (muxed) streams over a single connection; represents a stream-multiplexing transport to a
 * remote peer.
 */
interface MuxedConnection {

    /**
     * Negotiates and opens a new stream with the other endpoint.
     * @param protocol the protocol to be used in the stream.
     * @param multiaddr the address that the stream is to connect to.
     * @return the muxed stream.
     */
    fun openStream(protocol: Protocol, multiaddr: Multiaddr): MuxedStream

    /**
     * TODO[Raul]: How does Peer#1 know when to call this? Is this triggered by a listener to
     * some other event?
     *
     * Accepts a stream opened by the other peer.
     * @return the stream that was opened.
     */
    fun acceptStream(): MuxedStream

    /**
     * @return whether a transport is fully closed, so it can be garbage collected.
     */
    fun isClosed(): Boolean

    /**
     * Closes the transport and all associated streams.
     * @return true if the transport was fully closed.
     */
    fun close(): Boolean
}
