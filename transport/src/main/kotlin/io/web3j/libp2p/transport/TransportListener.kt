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

/**
 * A listener instance can emit the following events:
 * <ul>
 *      <li>listening</li>
 *      <li>close</li>
 *      <li>transport</li>
 *      <li>error</li>
 * </ul>
 */
interface TransportListener {

    /**
     * This method starts the listener and puts it in listening mode, waiting for incoming connections.
     *
     * @param multiaddr the address where the listener should bind to.
     * @return true if a transport was established.
     */
    fun listen(multiaddr: Multiaddr): Boolean

    /**
     * This method retrieves the addresses in which this listener is listening. <br />
     * Useful for when listening on sport 0 or any interface (0.0.0.0).
     *
     * @return the list of addrs.
     */
    fun getAddrs(): Multiaddr

    /**
     * Closes the listener so that no more connections can be open on this transport instance.
     * @param blocking whether to block until the listener has closed.
     * @param options optional settings for closing the listener
     */
    fun close(blocking: Boolean = false, options: TransportCloseOptions? = null): Boolean

    /**
     * Contains the options for closing a TransportListener transport.
     */
    interface TransportCloseOptions {

        /**
         * The timeout (in milliseconds) that fires and destroys all the connections on this transport if the transport
         * is not able to close graciously. (e.g { timeout: 1000 })
         */
        val timeoutMillis: Long
    }
}
