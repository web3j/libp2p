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
data class PeerInfo(val peerID: PeerID, val addrs: Array<Multiaddr>) {

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
        var addrs = mutableListOf<Multiaddr>()//[]ma.Multiaddr
        var tpl = "/${Protocol.IPFS.named}/"
        addrs.forEach { m ->
            val p2addr = Multiaddr(tpl + peerID.idB58Encode())
            addrs.add(m.encapsulate(p2addr))
        }

        return addrs.toTypedArray()
    }


    companion object {

        private val invalidAddrEx = Libp2pException("invalid p2p multiaddr")


        fun unmarshalJSON(b: ByteArray): PeerInfo {
            val data: Map<String, Any> = ObjectMapper().readValue(b, Map::class.java) as Map<String, Any>
            val pid = PeerID.idB58Decode(data["ID"] as String)
            val addrs = (data["Addrs"] as Array<String>).map { Multiaddr(it) }.toTypedArray()
            return PeerInfo(pid, addrs)
        }


        fun infoFromP2pAddr(m: Multiaddr): PeerInfo {
            val parts: List<Multiaddr> = m.split()
            if (parts.isEmpty()) {
                throw invalidAddrEx
            }

            // TODO(lgierth): we shouldn't assume /ipfs is the last part
            val ipfspart = parts.last()

            ipfspart.getProtocols().firstOrNull()?.let {
                if (it.code != Protocol.IPFS.code) {
                    throw invalidAddrEx
                }
            }

            // make sure the /ipfs value parses as a peer.ID
            val peerIdParts = ipfspart.toString().split("/")
            val peerIdStr = peerIdParts.last()
            val id = PeerID.idB58Decode(peerIdStr)

            // we might have received just an /ipfs part, which means there's no addr.
            var addrs = mutableListOf<Multiaddr>()
            if (parts.size > 1) {
                // TODO: check if this is correct.
                addrs.add(Multiaddr.join(parts.last())) // addrs = append(addrs, ma.Join(parts[:len( parts) - 1]...))
            }

            return PeerInfo(id, addrs.toTypedArray())
        }


    }
}
