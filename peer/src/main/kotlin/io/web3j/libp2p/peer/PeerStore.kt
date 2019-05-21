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

import io.ipfs.multiformats.multiaddr.Protocol

/**
 * Provides a store of peer-related information, indexed by peer ID.
 * <br />The information stored for each peer includes a set of protocols, addresses and metadata.
 *
 * @see [Peerstore in Go](https://github.com/libp2p/go-libp2p-peerstore/blob/master/interface.go)
 */
interface PeerStore : PeerMetadata, AddrBook {

    /**
     * Gets the peer info for the given peer.
     * @param peerID the peer ID.
     * @return the peer info for that ID.
     */
    fun getPeerInfo(peerID: PeerID): PeerInfo?

    /**
     * Gets the protocols supported by the given peer.
     * @param peerID the peer ID.
     * @return the supported protocols.
     */
    fun getProtocols(peerID: PeerID): Array<Protocol>

    /**
     * Adds the given protocols to the list of supported protocols by the peer.
     * @param peerID the peer ID.
     * @param protocols the supported protocols for this peer.
     */
    fun addProtocols(peerID: PeerID, vararg protocols: Protocol): Unit

    /**
     * Clears the supported protocols for the peer.
     * @param peerID the peer ID.
     */
    fun clearProtocols(peerID: PeerID): Unit

    /**
     * Sets the set of supported protocols for the given peer, clearing any previously set protocols.
     * @param peerID the peer ID.
     * @param protocols the set of supported protocols for this peer.
     */
    fun setProtocols(peerID: PeerID, vararg protocols: Protocol) {
        clearProtocols(peerID)
        addProtocols(peerID, *protocols)
    }

    /**
     * @return all the peer IDs in this store.
     */
    fun getPeers(): Array<PeerID>
}
