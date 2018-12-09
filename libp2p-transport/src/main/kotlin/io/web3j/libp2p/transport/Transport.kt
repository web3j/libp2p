package io.web3j.libp2p.transport

import io.ipfs.multiformats.multiaddr.Multiaddr
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/**
 * The maximum duration a Dial is allowed to take.
 * This includes the time between dialing the raw network connection,
 * protocol selection as well the handshake, if applicable.
 */
val DIAL_TIMEOUT = TimeUnit.SECONDS.toMillis(60)

/**
 * The maximum duration an Accept is allowed to take.
 * This includes the time between accepting the raw network connection,
 * protocol selection as well as the handshake, if applicable.
 */
val ACCEPT_TIMEOUT = TimeUnit.SECONDS.toMillis(60)

/**
 * Conn is a generic stream-oriented network connection.
 */
interface Conn {

    /*
    TODO:
    smux.Conn
    inet.ConnSecurity
    inet.ConnMultiaddrs
     */

    /**
     * Transport returns the transport to which this connection belongs.
     */
    fun transport(): Transport
}

/**
 * Transport represents any device by which you can connect to and accept
 * connections from other peers. The built-in transports provided are TCP and UTP
 * but many more can be implemented, sctp, audio signals, sneakernet, UDT, a
 * network of drones carrying usb flash drives, and so on.
 */
interface Transport {

    /**
     * Dial dials a remote peer. It should try to reuse local listener
     * addresses if possible but it may choose not to.
     */
    @Throws(IOException::class)
    fun dial(context: CoroutineContext, remoteMultiaddr: Multiaddr, peer: PeerId): Conn

    /**
     * canDial returns true if this transport knows how to dial the given
     * multiaddr.
     *
     * Returning true does not guarantee that dialing this multiaddr will
     * succeed. This function should *only* be used to preemptively filter
     * out addresses that we can't dial.
     */
    fun canDial(multiaddr: Multiaddr): Boolean

    /**
     * Listen listens on the passed multiaddr.
     */
    @Throws(IOException::class)
    fun listen(listenAddr: Multiaddr): Listener

    /**
     * Protocol returns the set of protocols handled by this transport.
     *
     * See the Network interface for an explanation of how this is used.
     */
    fun protocols(): List<Int>

    /**
     * Proxy returns true if this is a proxy transport.
     *
     * See the Network interface for an explanation of how this is used.
     * TODO: Make this a part of the go-multiaddr protocol instead?
     */
    fun proxy(): Boolean
}

/**
 * A Listener is a generic network listener for stream-oriented
 * protocols with Multiaddr support.
 *
 */
interface Listener {

    @Throws(IOException::class)
    fun accept(): Conn

    @Throws(IOException::class)
    fun close()

    fun addr(): URL

    fun multiaddr(): Multiaddr
}

/**
 * Network is an interface with methods for managing transports.
 */
interface Network // TODO:  : inet.Network {

    /**
     * AddTransport adds a transport to this Network.
     *
     * When dialing, this Network will iterate over the protocols in the
     * remote multiaddr and pick the first protocol registered with a proxy
     * transport, if any. Otherwise, it'll pick the transport registered to
     * handle the last protocol in the multiaddr.
     *
     * When listening, this Network will iterate over the protocols in the
     * local multiaddr and pick the *last* protocol registered with a proxy
     * transport, if any. Otherwise, it'll pick the transport registered to
     * handle the last protocol in the multiaddr.
     */
    fun addTransport(transport: Transport)
}
