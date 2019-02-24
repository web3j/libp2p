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
 * Represents a generic transport instance across any type of transport.
 */
interface RawConnection {

    /**
     * Returns the multiaddrs for the peers in this connection.
     * @return the addresses for the peers.
     */
    fun getObservedAddrs(): List<Multiaddr>

    /**
     * @return the Transport instance that this connection belongs to.
     */
    fun getTransport(): Transport

    // TODO[Sam]: Sort this out!
    fun getPeerInfo(): Any = TODO("define return type")
}
