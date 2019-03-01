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

/**
 * Provides a simple meta-data store for any information about a peer.
 */
interface PeerMetadata {

    /**
     * Gets the meta-data about a peer at the given key.
     * @param peerID the peer whose meta-data is to be queried.
     * @param key the key for the meta-data sought.
     * @return the value at the key for the peer.
     */
    fun getMetadata(peerID: PeerID, key: String): Any?

    /**
     * Puts the value of meta-data item for the peer at the given key.
     * @param peerID the peer ID that we are adding meta-data for.
     * @param key the key for the meta-data to be added.
     * @param value the value of the meta-data at the key.
     */
    fun setMetadata(peerID: PeerID, key: String, value: Any?): Unit
}
