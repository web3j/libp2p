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
