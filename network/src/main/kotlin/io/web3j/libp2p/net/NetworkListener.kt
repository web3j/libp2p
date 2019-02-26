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

/**
 * A callback interface providing access to network-related events.
 */
interface NetworkListener {

    /**
     * Called when the network commences listening on the given address.
     * @param network the network instance that the event occurred on.
     * @param multiaddr the address that listening commenced on.
     */
    fun onListeningStarted(network: Network, multiaddr: Multiaddr): Unit

    /**
     * Called when the network stops listening on the given address.
     * @param network the network instance that the event occurred on.
     * @param multiaddr the address that listening ceased for.
     */
    fun onListeningStopped(network: Network, multiaddr: Multiaddr): Unit

    /**
     * Called when a stream is opened.
     * @param network the network instance that the event occurred on.
     * @param stream the stream involved in the event.
     */
    fun onStreamOpened(network: Network, stream: NetworkStream): Unit

    /**
     * Called when a stream is closed.
     * @param network the network instance that the event occurred on.
     * @param stream the stream involved in the event.
     */
    fun onStreamClosed(network: Network, stream: NetworkStream): Unit
}
