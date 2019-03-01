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
package io.web3j.libp2p.peer

import io.ipfs.multiformats.multiaddr.Multiaddr
import io.ipfs.multiformats.multiaddr.Protocol

/**
 * A data holder interface that provides access to information on a peer in the network.
 *
 * @see [peer-info in JS](https://github.com/libp2p/js-peer-info/blob/master/src/index.js)
 * @see [PeerInfo in Go](https://github.com/libp2p/go-libp2p-peerstore/blob/master/peerinfo.go)
 */
interface PeerData {

    /**
     * @return the multiaddrs associated with the peer.
     */
    fun getAddrs(): Array<Multiaddr>

    /**
     * @return the protocols supported by this peer.
     */
    fun getProtocols(): Protocol

    /**
     * Adds the given protocols to the set of supported protocols by this peer.
     * @param protocols the (additional) supported protocols.
     */
    fun addProtocols(vararg protocols: Protocol): Unit

    /**
     * Sets the supposed protocols of the peer to be the provided protocols, clearing any previous values.
     * @param protocols the supported protocols.
     */
    fun setProtocols(vararg protocols: Protocol) {
        clearProtocols()
        addProtocols(*protocols)
    }

    /**
     * Clears the set of protocols supported by this peer.
     */
    fun clearProtocols(): Unit

    /**
     * Adds some meta-data about the peer.
     * @param key the key for this data.
     * @param value the value of this data.
     */
    fun putMetadata(key: String, value: Any?): Unit

    /**
     * Gets the meta-data about the peer at the given key.
     * @param key the key for the data.
     */
    fun getMetadata(key: String): Any?
}
