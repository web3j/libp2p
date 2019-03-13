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
 * @link [https://github.com/libp2p/interface-transport]
 */
interface Transport {

    /**
     * Dials (using the transport) to the peer on the given address.
     * @param multiaddr the destination address peer to be dialed.
     * @return the established transport.
     */
    fun dial(multiaddr: Multiaddr): TransportConnection

    /**
     * Checks whether the given address can be dialed with this transport implementation. <br />
     * @param multiaddr the address to be checked.
     * @return true if the address can be dialed; a result of <code>true</code> does not guarantee that the
     * <code>dial</code> call will succeed.
     */
    fun canDial(multiaddr: Multiaddr): Boolean

    /**
     * Registers a listener on the transport instance at the given address.
     * @param multiaddr the multiaddr to listen on.
     * @param listener a callback interface that is notified when a new transport is received.
     * @return the multiaddr that the listener was started for.
     */
    fun listen(multiaddr: Multiaddr, listener: TransportConnectionListener): Multiaddr

    /**
     * @return the set of protocols handled by this transport instance.
     */
    fun getProtocols(): Array<Protocol>

}
