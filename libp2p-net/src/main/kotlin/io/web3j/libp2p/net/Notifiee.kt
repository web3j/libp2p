/*
 * Copyright 2019 Web3Labs Ltd.
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
 * NotifyBundle implements Notifiee by calling any of the functions set on it,
 * and nop'ing if they are unset. This is the easy way to register for
 * notifications.
 */
data class NotifyBundle(
    val listenNotify: (network: Network, multiaddr: Multiaddr) -> Unit,
    val listenCloseNotify: (network: Network, multiaddr: Multiaddr) -> Unit,
    val connectedNotify: (network: Network, conn: Conn) -> Unit,
    val disconnectedNotify: (network: Network, conn: Conn) -> Unit,
    val openedStreamNotify: (network: Network, stream: Stream) -> Unit,
    val closedStreamNotify: (network: Network, stream: Stream) -> Unit
) : Notifiee {

    /**
     * Listen calls ListenF if it is not null.
     */
    override fun listen(network: Network, multiaddr: Multiaddr) {
        listenNotify(network, multiaddr)
    }

    /**
     * ListenClose calls ListenCloseF if it is not null.
     */
    override fun listenClose(network: Network, multiaddr: Multiaddr) {
        listenCloseNotify(network, multiaddr)
    }

    /**
     * Connected calls ConnectedF if it is not null.
     */
    override fun connected(network: Network, conn: Conn) {
        connectedNotify(network, conn)
    }

    /**
     * Disconnected calls DisconnectedF if it is not null.
     */
    override fun disconnected(network: Network, conn: Conn) {
        disconnectedNotify(network, conn)
    }

    /**
     * OpenedStream calls OpenedStreamF if it is not null.
     */
    override fun openedStream(network: Network, stream: Stream) {
        openedStreamNotify(network, stream)
    }

    /**
     * ClosedStream calls ClosedStreamF if it is not null.
     */
    override fun closedStream(network: Network, stream: Stream) {
        closedStreamNotify(network, stream)
    }
}
