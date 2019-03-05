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
package io.web3j.libp2p.peer.util

import io.ipfs.multiformats.multiaddr.Multiaddr
import io.ipfs.multiformats.multiaddr.Protocol
import io.ipfs.multiformats.multihash.Multihash
import io.ipfs.multiformats.multihash.Type
import io.web3j.libp2p.crypto.KEY_TYPE
import io.web3j.libp2p.crypto.generateKeyPair
import io.web3j.libp2p.peer.PeerID
import java.security.MessageDigest

/**
 * Contains utilities for testing [PeerID] instances.
 */
object PeerTestUtil {

    /**
     * Creates a [Multiaddr] for localhost on the given port.
     * @param the port.
     * @return the mulitaddr.
     */
    fun createMultiaddr(port: Int) = Multiaddr("/${Protocol.IP4.named}/127.0.0.1/tcp/$port")

    /**
     * @return a random [PeerID] instance.
     */
    fun createPeer(): PeerID {
        val pubKeyBytes = generateKeyPair(KEY_TYPE.RSA, 512).second.bytes()
        val sha256Bytes: ByteArray = with(MessageDigest.getInstance("SHA-256")) {
            update(pubKeyBytes)
            digest()
        }

        val kBytes: ByteArray = Multihash.encodeByName(sha256Bytes, Type.SHA2_256.named)
        val mh = Multihash.cast(kBytes)
        return PeerID(mh)
    }
}
