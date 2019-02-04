package io.web3j.libp2p.peerstore

import io.ipfs.multiformats.multiaddr.Multiaddr
import io.web3j.libp2p.shared.env.Libp2pException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import io.web3j.libp2p.peer.ID as PeerID

class PeerInfoTest {

    @Test
    fun testPeerInfoMarshal() {
        val a = Multiaddr("/ip4/1.2.3.4/tcp/4536")
        val b = Multiaddr("/ip4/1.2.3.8/udp/7777")
        val id = PeerID.idB58Decode("QmaCpDMGvV2BGHeYERUEnRQAwe3N8SzbUtfsmvsqQLuvuJ")

        val pi = PeerInfo(id, arrayOf(a, b))
        val data = pi.marshalJSON()
        val pi2 = PeerInfo.unmarshalJSON(data)

        assertEquals(pi2.peerID, pi.peerID, "IDs didn't match after marshal")
        assertEquals(pi.addrs.first(), pi2.addrs.first(), "Wrong addrs")
        assertEquals(pi.addrs[1], pi2.addrs[1], "Wrong addrs")
        assertEquals(pi2.loggable()["peerID"], id.pretty(), "loggables gave wrong peerID output")
    }

    @Test
    fun testP2pAddrParsing() {
        val id = PeerID.idB58Decode("QmaCpDMGvV2BGHeYERUEnRQAwe3N8SzbUtfsmvsqQLuvuJ")

        var addr = Multiaddr("/ip4/1.2.3.4/tcp/4536")

        val p2paddr = Multiaddr.join(addr, Multiaddr("/ipfs/${id.idB58Encode()}"))
        var pinfo = PeerInfo.infoFromP2pAddr(p2paddr)
        assertEquals(pinfo.peerID, id, "expected PeerID [$id], got [${pinfo.peerID}]")
        assertEquals(1, pinfo.addrs.size, "expected 1 addr, got ${pinfo.addrs.size}")
        assertEquals(addr, pinfo.addrs.first(), "expected addr [$addr], got [${pinfo.addrs.first()}]")

        addr = Multiaddr("/ipfs/${id.idB58Encode()}")
        pinfo = PeerInfo.infoFromP2pAddr(addr)
        assertEquals(pinfo.peerID, id, "expected PeerID [$id], got [${pinfo.peerID}]")
        assertEquals(0, pinfo.addrs.size, "expected 0 addr, got ${pinfo.addrs.size}")

        assertThrows<Libp2pException>("Expected an error") { PeerInfo.infoFromP2pAddr(Multiaddr("/ip4/1.2.3.4/tcp/4536")) }
        assertThrows<Libp2pException>("Expected an error") { PeerInfo.infoFromP2pAddr(Multiaddr("/ip4/1.2.3.4/tcp/4536/http")) }
    }

    @Test
    fun testP2pAddrConstruction() {
        val id = PeerID.idB58Decode("QmaCpDMGvV2BGHeYERUEnRQAwe3N8SzbUtfsmvsqQLuvuJ")
        val addr = Multiaddr("/ip4/1.2.3.4/tcp/4536")
        val p2paddr = Multiaddr.join(addr, Multiaddr("/ipfs/${id.idB58Encode()}"))
        var pi = PeerInfo(id, arrayOf(addr))
        var p2paddrs = pi.infoToP2pAddrs()
        assertEquals(1, p2paddrs.size, "Expected exactly 1 addr but got ${p2paddrs.size}")
        assertEquals(p2paddr, p2paddrs.first(), "Expected ${p2paddr} but got ${p2paddrs.first()}")

        pi = PeerInfo(id)
        p2paddrs = pi.infoToP2pAddrs()
        assertTrue(p2paddrs.isEmpty(), "expected 0 addrs, got ${p2paddrs.size}")
    }

}