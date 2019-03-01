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
import java.time.Duration

/**
 * A parallel to a conventional address book that supports peer addresses.
 *
 * @see [addr_book in Go](https://github.com/libp2p/go-libp2p-peerstore/blob/master/pstoremem/addr_book.go)
 */
interface AddrBook {

    /**
     * Adds an address for a given peer with a set validity period.
     * @param peerID the peer ID.
     * @param addr the address of the peer.
     * @param ttl an optional TTL for the address that starts from the point in time of addition.
     */
    fun addAddr(peerID: PeerID, addr: Multiaddr, ttl: Duration? = null): Unit

    /**
     * Adds addresses for a given peer with the same validity period. <br />
     * If one of the peer addresses already exists and has a longer TTL, no operation should take place.<br />
     * If one of the addresses exists with a shorter TTL, extend the TTL to equal param ttl.
     * @param peerID the peer ID.
     * @param ttl an optional TTL for the addresses that starts from the point in time of addition.
     * @param addrs the addresses of the peer.
     */
    fun addAddrs(peerID: PeerID, ttl: Duration? = null, vararg addrs: Multiaddr): Unit

    /**
     * Removes all previously stored addresses for the peer.
     * @param peerID the peer ID of the peer whose addresses are to be removed/cleared.
     */
    fun clearAddrs(peerID: PeerID): Unit

    /**
     * Sets the addresses for the given peer, all with the same validity period, removing any other addresses that were
     * previously registered.
     * @param peerID
     * @param ttl an optional TTL for the addresses that starts from the point in time of insertion.
     * @param addrs the addresses of the peer.
     */
    fun setAddrs(peerID: PeerID, ttl: Duration? = null, vararg addrs: Multiaddr) {
        clearAddrs(peerID)
        addAddrs(peerID, ttl, *addrs)
    }

    /**
     * Gets all the addresses for the given peer.
     * @param peerID the peer ID.
     * @param includeExpired whether to include the expired/invalid addresses.
     * @return an array of the known addresses.
     */
    fun addrsOf(peerID: PeerID, includeExpired: Boolean = false): Array<Multiaddr>

    /**
     * @return the peer IDs that have addresses in this address book.
     */
    fun getPeersWithAddresses(): Array<PeerID>
}
