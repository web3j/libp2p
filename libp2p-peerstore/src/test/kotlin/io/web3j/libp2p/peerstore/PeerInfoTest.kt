package io.web3j.libp2p.peerstore

import io.ipfs.multiformats.multiaddr.Multiaddr
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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

        // addr := ma.StringCast("/ip4/1.2.3.4/tcp/4536")
        var addr = Multiaddr("/ip4/1.2.3.4/tcp/4536")

        // p2paddr := ma.Join(addr, ma.StringCast("/ipfs/"+peer.IDB58Encode(id)))
        val p2paddr = Multiaddr.join(addr, Multiaddr("/ipfs/${id.idB58Encode()}"))

        // pinfo, err := InfoFromP2pAddr(p2paddr)
        var pinfo = PeerInfo.infoFromP2pAddr(p2paddr)

        // pinfo.ID == id
        assertEquals(pinfo.peerID, id, "expected PeerID [$id], got [${pinfo.peerID}]")

        // len(pinfo.Addrs) == 1
        assertEquals(1, pinfo.addrs.size, "expected 1 addr, got ${pinfo.addrs.size}")

        // addr.Equal(pinfo.Addrs[0])
        assertEquals(addr, pinfo.addrs.first(), "expected addr [$addr], got [${pinfo.addrs.first()}]")

        addr = Multiaddr("/ipfs/${id.idB58Encode()}")
        pinfo = PeerInfo.infoFromP2pAddr(addr)

        assertEquals(pinfo.peerID, id, "expected PeerID [$id], got [${pinfo.peerID}]")
        assertEquals(0, pinfo.addrs.size, "expected 0 addr, got ${pinfo.addrs.size}")

        addr = Multiaddr("/ip4/1.2.3.4/tcp/4536")
        pinfo = PeerInfo.infoFromP2pAddr(addr)

        addr = Multiaddr("/ip4/1.2.3.4/tcp/4536/http")
        pinfo = PeerInfo.infoFromP2pAddr(addr)
    }

    /*
    func TestP2pAddrConstruction(t *testing.T) {
        id, err := peer.IDB58Decode("QmaCpDMGvV2BGHeYERUEnRQAwe3N8SzbUtfsmvsqQLuvuJ")
        if err != nil {
            t.Error(err)
        }
        addr := ma.StringCast("/ip4/1.2.3.4/tcp/4536")
        p2paddr := ma.Join(addr, ma.StringCast("/ipfs/"+peer.IDB58Encode(id)))

        pi := &PeerInfo{ID: id, Addrs: []ma.Multiaddr{addr}}
        p2paddrs, err := InfoToP2pAddrs(pi)
        if err != nil {
            t.Error(err)
        }

        if len(p2paddrs) != 1 {
            t.Fatalf("expected 1 addr, got %d", len(p2paddrs))
        }

        if !p2paddr.Equal(p2paddrs[0]) {
            t.Fatalf("expected [%s], got [%s]", p2paddr, p2paddrs[0])
        }

        pi = &PeerInfo{ID: id}
        p2paddrs, err = InfoToP2pAddrs(pi)
        if err != nil {
            t.Error(err)
        }

        if len(p2paddrs) > 0 {
            t.Fatalf("expected 0 addrs, got %d", len(p2paddrs))
        }

        pi = &PeerInfo{Addrs: []ma.Multiaddr{ma.StringCast("/ip4/1.2.3.4/tcp/4536")}}
        _, err = InfoToP2pAddrs(pi)
        if err == nil {
            t.Fatalf("expected error, got none")
        }
    }
    */

}