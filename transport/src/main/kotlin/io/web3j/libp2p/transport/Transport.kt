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
package io.web3j.libp2p.transport

import io.ipfs.multiformats.multiaddr.Multiaddr
import io.ipfs.multiformats.multiaddr.Protocol

/**
 * The Transport interface can be viewed as a factory that creates connections.
 *
 * @link {https://github.com/libp2p/interface-transport}
 *
 */
interface Transport {

    /**
     * Dials (using the transport) to the peer on the given address.
     * @param multiaddr the destination address peer to be dialed.
     * @param options the dial options.
     * @return the established transport.
     */
    fun dial(multiaddr: Multiaddr, options: TransportDialOptions? = null): RawConnection

    /**
     * Checks whether the given address can be dialed with this transport implementation. <br />
     * @param multiaddr the address to be checked.
     * @return true if the address can be dialed; a result of <code>true</code> does not guarantee that the
     * <code>dial</code> call will succeed.
     */
    fun canDial(multiaddr: Multiaddr): Boolean

    /**
     * Creates a listener on the transport instance.
     * @param options contains properties that the listener must have.
     * @param newConnectionListener a callback interface that is notified when a new transport is received.
     * @return the listener.
     */
    fun createListener(
        options: TransportListenerOptions? = null,
        newConnectionListener: TransportConnectionListener? = null
    ): TransportListener

    /**
     * @return the set of protocols handled by this transport instance.
     */
    fun getProtocols(): Array<Protocol>

    /**
     * Contains the options for a listener instance.
     */
    interface TransportListenerOptions

    /**
     * Contains the options to dial to a peer using the selected transport.
     */
    interface TransportDialOptions

    /**
     * Provides access to newly established connections in the transport layer.
     */
    interface TransportConnectionListener {

        /**
         * Fired when a new transport is received on the transport layer.
         * @param connection the transport that was established.
         */
        fun onNewConnection(connection: RawConnection): Unit
    }
}
