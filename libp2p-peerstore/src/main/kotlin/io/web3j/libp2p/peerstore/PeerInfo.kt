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

import com.fasterxml.jackson.databind.ObjectMapper
import io.ipfs.multiformats.multiaddr.Multiaddr
import io.ipfs.multiformats.multiaddr.Protocol
import io.web3j.libp2p.shared.env.Libp2pException
import io.web3j.libp2p.peer.ID as PeerID


/**
 * PeerInfo is a small struct used to pass around a peer with
 * a set of addresses (and later, keys?). This is not meant to be
 * a complete view of the system, but rather to model updates to
 * the peerstore. It is used by things like the routing system.
 */
data class PeerInfo(val peerID: PeerID, val addrs: Array<Multiaddr> = emptyArray()) {

    // TODO: review whether "Any" is the best type for this.
    fun loggable(): Map<String, Any> = mapOf(
        "peerID" to peerID.pretty(), "addrs" to addrs
    )

    fun marshalJSON(): ByteArray = with(ObjectMapper()) {
        val out = mapOf<String, Any>(
            "ID" to peerID.pretty(),
            "Addrs" to addrs.map { it.toString() }.toTypedArray()
        )
        writeValueAsBytes(out)
    }

    fun infoToP2pAddrs(): Array<Multiaddr> {
        val tpl = "/${Protocol.IPFS.named}/"
        val p2addr = Multiaddr(tpl + peerID.idB58Encode())

        val b = this.addrs.map { it.encapsulate(p2addr) }.toTypedArray()
        return b
    }

    companion object {

        private val invalidAddrEx = Libp2pException("invalid p2p multiaddr")


        fun unmarshalJSON(b: ByteArray): PeerInfo {
            val data: Map<String, Any> = ObjectMapper().readValue(b, Map::class.java) as Map<String, Any>
            val pid = PeerID.idB58Decode(data["ID"] as String)
            val addrs = (data["Addrs"] as List<String>).map { Multiaddr(it) }.toTypedArray()
            return PeerInfo(pid, addrs)
        }


        fun infoFromP2pAddr(m: Multiaddr): PeerInfo {
            val parts: List<Multiaddr> = m.split()
            if (parts.isEmpty()) {
                throw invalidAddrEx
            }

            val partsByProtocol: Map<Boolean, List<Multiaddr>> = parts.groupBy { ma ->
                ma.getProtocols().contains(Protocol.IPFS)
            }

            val ipfsParts = partsByProtocol[true]
            val nonIpfsParts = partsByProtocol[false]

            if (ipfsParts.isNullOrEmpty()) {
                throw invalidAddrEx
            }

            val ipfspart = ipfsParts.first()

            // make sure the /ipfs value parses as a peer.ID
            val peerIdParts = ipfspart.toString().split("/")
            val peerIdStr = peerIdParts.last()
            val id = PeerID.idB58Decode(peerIdStr)

            // we might have received just an /ipfs part, which means there's no addr.
            var addrs = mutableListOf<Multiaddr>()
            if (!nonIpfsParts.isNullOrEmpty()) {
                // addrs = append(addrs, ma.Join(parts[:len( parts) - 1]...))
                addrs.add(Multiaddr.join(*nonIpfsParts.toTypedArray()))
            }

            return PeerInfo(id, addrs.toTypedArray())
        }


    }
}
