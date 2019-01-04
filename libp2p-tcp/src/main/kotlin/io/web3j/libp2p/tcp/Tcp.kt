package io.web3j.libp2p.tcp

import io.ipfs.multiformats.multiaddr.Multiaddr
import io.ipfs.multiformats.multiaddr.Protocol
import io.web3j.libp2p.net.Conn
import io.web3j.libp2p.peer.ID
import io.web3j.libp2p.transport.Listener
import java.io.IOException
import java.time.Duration
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.coroutines.CoroutineContext

data class Tcp(
    val ConnectTimeout: Duration,
    val DisableReuseport: Boolean
) {

    var DefaultConnectTimeout = Duration.of(5, ChronoUnit.SECONDS)

    /**
     * CanDial returns true if this transport believes it can dial the given Multiaddr.
     */
    fun CanDial(addr: Multiaddr): Boolean { //TODO: Find a stable API which validates the MutliAddr
        val parts = addr.toString().substring(1)
            .split("/".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size != 4)
            return false
        if (!parts[0].startsWith("ip"))
            return false
        if (parts[2] != "tcp")
            return false
        return true
    }

    /**
     * MutliAddrDial return the Connection after dialing the given Multiaddr.
     */
    @Throws(IOException::class)
    fun MutliAddrDial(context: CoroutineContext, multiAddr: Multiaddr): Conn? {
        if (ConnectTimeout.get(ChronoUnit.SECONDS) > 0) {
            var deadline = LocalTime.now().plus(ConnectTimeout)
            // TODO: Add deadline

        }
        if (UseReuseport()) {
            //TODO: return reuse-transport impl
        }
        //TODO: depends on MutliAddrListen
        return null
    }

    /**
     * Dial dials the peer at the remote address.
     */
    fun Dial(context: CoroutineContext, multiAddr: Multiaddr, peer: ID)
            : io.web3j.libp2p.transport.Conn? {
        //TODO: depends on MutliAddrDial
        return null
    }

    /**
     * UseReuseport returns true if reuseport is enabled and available.
     */
    fun UseReuseport(): Boolean {
        return !DisableReuseport
    }

    @Throws(IOException::class)
    fun MutliAddrListen(multiAddr: Multiaddr): Listener? {
        //TODO: depends on MutliAddreNet
        return null
    }

    /**
     * Listen listens on the given multiaddr.
     */
    @Throws(IOException::class)
    fun Listen(): Listener? {
        //TODO: depends on MutliAddrListen
        return null
    }

    /**
     * Protocols returns the list of terminal protocols this transport can dial.
     */
    fun Protocols(): List<Protocol> {
        return listOf(Protocol.TCP)
    }

    /**
     * Proxy always returns false for the TCP transport.
     */
    fun Proxy(): Boolean {
        return false
    }

    override fun toString(): String {
        return "TCP"
    }
}