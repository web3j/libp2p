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
package io.web3j.libp2p.peerstore

import io.ipfs.multiformats.multiaddr.Multiaddr
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey
import io.web3j.libp2p.peer.IDSlice
import java.time.Duration
import io.web3j.libp2p.peer.ID as PeerID

// TODO: javadoc!
class Interfaces {


    /*
    var ErrNotFound = errors.New("item not found")

var (
	// AddressTTL is the expiration time of addresses.
	AddressTTL = time.Hour

	// TempAddrTTL is the ttl used for a short lived address
	TempAddrTTL = time.Second * 10

	// ProviderAddrTTL is the TTL of an address we've received from a provider.
	// This is also a temporary address, but lasts longer. After this expires,
	// the records we return will require an extra lookup.
	ProviderAddrTTL = time.Minute * 10

	// RecentlyConnectedAddrTTL is used when we recently connected to a peer.
	// It means that we are reasonably certain of the peer's address.
	RecentlyConnectedAddrTTL = time.Minute * 10

	// OwnObservedAddrTTL is used for our own external addresses observed by peers.
	OwnObservedAddrTTL = time.Minute * 10
)

// Permanent TTLs (distinct so we can distinguish between them, constant as they
// are, in fact, permanent)
const (

	// PermanentAddrTTL is the ttl for a "permanent address" (e.g. bootstrap nodes).
	PermanentAddrTTL = math.MaxInt64 - iota

	// ConnectedAddrTTL is the ttl used for the addresses of a peer to whom
	// we're connected directly. This is basically permanent, as we will
	// clear them + re-add under a TempAddrTTL after disconnecting.
	ConnectedAddrTTL
)

     */

    // Peerstore provides a threadsafe store of Peer related
// information.
    interface Peerstore {
        val addrBook: AddrBook
        val keyBook: KeyBook
        val peerMetaData: PeerMetadata
        val metrics: Metrics

        // PeerInfo returns a peer.PeerInfo struct for given peer.ID.
        // This is a small slice of the information Peerstore has on
        // that peer, useful to other services.
        fun peerInfo(peerId: PeerID): PeerInfo

        fun getProtocols(peerId: PeerID): Array<String>

        fun addProtocols(peerId: PeerID, vararg string: String)

        fun setProtocols(peerId: PeerID, vararg string: String)

        fun supportsProtocols(peerId: PeerID, vararg string: String): Array<String>

        // Peers returns all of the peer IDs stored across all inner stores.
        fun peers(): IDSlice
    }


    /**
     * PeerMetadata can handle values of any type. Serializing values is
     * up to the implementation. Dynamic type introspection may not be
     * supported, in which case explicitly enlisting types in the
     * serializer may be required.
     *
     * Refer to the docs of the underlying implementation for more
     * information.
     */
    interface PeerMetadata {

        // TODO: perhaps use operators instead???
        // TODO: what to return?

        // Get/Put is a simple registry for other peer-related key/value pairs.
        // if we find something we use often, it should become its own set of
        // methods. this is a last resort.
        fun get(eerId: PeerID, key: String): Any

//        interface {}
//
//        fun put(eerId: PeerID, key: String, val interface {})
    }


    /**
     * Holds the multiaddrs of peers.
     */
    interface AddrBook {

        // AddAddr calls AddAddrs(p, []ma.Multiaddr{addr}, ttl)
        fun addAddr(peerId: PeerID, addr: Multiaddr, ttl: Duration)

        // AddAddrs gives this AddrBook addresses to use, with a given ttl
        // (time-to-live), after which the address is no longer valid.
        // If the manager has a longer TTL, the operation is a no-op for that address
        fun addAddrs(peerId: PeerID, addrs: Array<Multiaddr>, ttl: Duration)

        // SetAddr calls mgr.SetAddrs(p, addr, ttl)
        fun setAddr(peerId: PeerID, addr: Multiaddr, ttl: Duration)

        // SetAddrs sets the ttl on addresses. This clears any TTL there previously.
        // This is used when we receive the best estimate of the validity of an address.
        fun setAddrs(peerId: PeerID, addrs: Array<Multiaddr>, ttl: Duration)

        // UpdateAddrs updates the addresses associated with the given peer that have
        // the given oldTTL to have the given newTTL.
        fun updateAddrs(peerId: PeerID, oldTTL: Duration, newTTL: Duration)

        // Addresses returns all known (and valid) addresses for a given peer
        fun addrs(peerId: PeerID): Array<Multiaddr>

//        // AddrStream returns a channel that gets all addresses for a given
//        // peer sent on it. If new addresses are added after the call is made
//        // they will be sent along through the channel as well.
//        fun addrStream(context: context.Context, peerId: PeerID) <-chan ma.Multiaddr

        // ClearAddresses removes all previously stored addresses
        fun clearAddrs(peerId: PeerID)

        // PeersWithAddrs returns all of the peer IDs stored in the AddrBook
        fun peersWithAddrs(): IDSlice
    }


    /**
     * Tracks the keys of peers.
     */
    interface KeyBook {

        // PubKey stores the public key of a peer.
        fun pubKey(peerId: PeerID): PubKey

        // AddPubKey stores the public key of a peer.
        fun addPubKey(peerId: PeerID, pubKey: PubKey)

        // PrivKey returns the private key of a peer.
        fun PrivKey(peerID: PeerID): PrivKey

        // AddPrivKey stores the private key of a peer.
        fun addPrivKey(peerId: PeerID, privateKey: PrivKey)

        // PeersWithKeys returns all the peer IDs stored in the KeyBook
        fun peersWithKeys(): IDSlice
    }

}