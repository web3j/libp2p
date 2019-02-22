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
package io.web3j.libp2p.connection

import io.ipfs.multiformats.multiaddr.Multiaddr

/**
 *
 * @link {https://github.com/libp2p/interface-connection}
 *
 */
interface Transport {

    /**
     * Dials (using the connection) to the peer on the given address.
     * @param multiaddr the destination address peer to be dialed.
     * @param options the dial options.
     * @return the established connection.
     */
    fun dial(multiaddr: Multiaddr, options: TransportDialOptions? = null): Connection

    /**
     * Creates a listener on the connection instance.
     * @param options contains properties that the listener must have.
     * @param newConnectionListener a callback interface that is notified when a new connection is received.
     * @return the listener.
     */
    fun createListener(
        options: TransportListenerOptions? = null,
        newConnectionListener: TransportConnectionListener? = null
    ): TransportListener

    /**
     * Contains the options for a listener instance.
     */
    interface TransportListenerOptions

    /**
     * Contains the options to dial to a peer using the selected connection.
     */
    interface TransportDialOptions

    /**
     * Provides access to newly established connections in the connection layer.
     */
    interface TransportConnectionListener {

        /**
         * Fired when a new connection is received on the connection layer.
         * @param connection the connection that was established.
         */
        fun onNewConnection(connection: Connection): Unit
    }
}
