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
package io.web3j.libp2p.net

import io.ipfs.multiformats.multiaddr.Multiaddr
import io.ipfs.multiformats.multiaddr.Protocol
import io.web3j.libp2p.peer.PeerID
import io.web3j.streammux.MuxedConnection

/**
 * The IPFS Network package handles all of the peer-to-peer networking. It connects to other hosts, it encrypts communications, it muxes messages between the network's client services and target hosts. It has multiple subcomponents:
 */
interface Network {

    /**
     * Attempts to create a connection to the given peer.
     * @param peerId the peer to be dialed.
     * @return the connection that was established
     */
    fun dial(peerId: PeerID): MuxedConnection

    /**
     * @return the (local) peer ID of this host.
     */
    fun getPeerID(): PeerID

    /**
     * Starts a listener on the given multiaddrs.
     * @param addresses the addresses to listen on.
     * @return true if there was at least one successful listener started on any given address.
     */
    fun listen(vararg addresses: Multiaddr): Boolean

    /**
     * Creates a new stream to the given peer with any of the given protocols. <br />
     * If there is no connection already established this function will create one.
     * @param peerId the peer to connect to.
     * @param protocols the set of protocols to be used.
     * @return the newly established network stream.
     */
    fun newStream(peerId: PeerID, vararg protocols: Protocol): NetworkStream

    /**
     * Sets the handler for new streams opened by a remote entity/peer.
     * @param protocol the stream's protocol that the handler is interested in.
     * @param streamHandler the handler instance.
     */
    fun setStreamHandler(protocol: Protocol, streamHandler: StreamHandler): Unit

    /**
     * Registers a listener to receive network events.
     * @param networkListener the network listener.
     */
    fun setNetworkListener(networkListener: NetworkListener): Unit
}
