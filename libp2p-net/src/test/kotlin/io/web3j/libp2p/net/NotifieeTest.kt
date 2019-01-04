package io.web3j.libp2p.net

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.ipfs.multiformats.multiaddr.Multiaddr
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.lang.RuntimeException

class NotifieeTest {

    private lateinit var network: Network
    private lateinit var conn: Conn
    private lateinit var stream: Stream
    private lateinit var multiaddr: Multiaddr
    private lateinit var notifiee: NotifyBundle

    private var called = false

    @BeforeEach
    fun init() {
        network = Mockito.mock(Network::class.java)
        conn = Mockito.mock(Conn::class.java)
        stream = Mockito.mock(Stream::class.java)
        multiaddr = Multiaddr("/ip4/127.0.0.1/udp/1234")
        notifiee = NotifyBundle(::setCalledToTrue, ::setCalledToTrue,
                                ::setCalledToTrueConn, ::setCalledToTrueConn,
                                ::setCalledToTrueStream, ::setCalledToTrueStream
        )
    }

    @Test
    fun testListen() {
        checkIfCalledIsFalse()

        notifiee.listen(network, multiaddr)
        assertEquals(true, called)
        verify(notifiee, times(1)).listenNotify(network, multiaddr)
    }

    @Test
    fun testListenClose() {
        checkIfCalledIsFalse()

        notifiee.listenClose(network, multiaddr)
        assertEquals(true, called)
        verify(notifiee, times(1)).listenCloseNotify(network, multiaddr)
    }

    @Test
    fun testConnected() {
        checkIfCalledIsFalse()

        notifiee.connected(network, conn)
        assertEquals(true, called)
        verify(notifiee, times(1)).connectedNotify(network, conn)
    }

    @Test
    fun testDisconnected() {
        checkIfCalledIsFalse()

        notifiee.disconnected(network, conn)
        assertEquals(true, called)
        verify(notifiee, times(1)).disconnectedNotify(network, conn)
    }

    @Test
    fun testOpenedStream() {
        checkIfCalledIsFalse()

        notifiee.openedStream(network, stream)
        assertEquals(true, called)
        verify(notifiee, times(1)).openedStreamNotify(network, stream)
    }

    @Test
    fun testClosedStream() {
        checkIfCalledIsFalse()

        notifiee.closedStream(network, stream)
        assertEquals(true, called)
        verify(notifiee, times(1)).closedStreamNotify(network, stream)
    }

    private fun checkIfCalledIsFalse() {
        called = false

        if (called) {
            throw RuntimeException("Called should be false")
        }
    }

    private fun setCalledToTrue(network: Network, multiaddr: Multiaddr) {
        called = true
    }

    private fun setCalledToTrueConn(network: Network, conn: Conn) {
        called = true
    }

    private fun setCalledToTrueStream(network: Network, stream: Stream) {
        called = true
    }
}
