package io.web3j.libp2p.peerstore

import io.ipfs.multiformats.multiaddr.Multiaddr
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import io.web3j.libp2p.peer.ID as PeerID

class PeerInfoTest {

    private fun mustAddr(s: String): Multiaddr = Multiaddr(s)

    @Test
    fun testPeerInfoMarshal() {
        val a = mustAddr("/ip4/1.2.3.4/tcp/4536")
        val b = mustAddr("/ip4/1.2.3.8/udp/7777")
        val id = PeerID.idB58Decode("QmaCpDMGvV2BGHeYERUEnRQAwe3N8SzbUtfsmvsqQLuvuJ")

        val pi = PeerInfo(id, arrayOf(a, b))
        val data = pi.marshalJSON()

        val pi2 = PeerInfo.unmarshalJSON(data)
        if (pi2.peerID != pi.peerID) {
            fail("ids didnt match after marshal")
        }

        if (!pi.addrs.first().equals(pi2.addrs.first())) {
            fail("wrong addrs")
        }

        if (!pi.addrs[1].equals(pi2.addrs[1])) {
            fail("wrong addrs")
        }

        val lgbl = pi2.loggable()
        if (lgbl["peerID"] != id.pretty()) {
            fail("loggables gave wrong peerID output")
        }
    }

    /*





func TestPeerInfoMarshalWithPointer(t *testing.T) {
	a := mustAddr(t, "/ip4/1.2.3.4/tcp/4536")
	b := mustAddr(t, "/ip4/1.2.3.8/udp/7777")
	id, err := peer.IDB58Decode("QmaCpDMGvV2BGHeYERUEnRQAwe3N8SzbUtfsmvsqQLuvuJ")
	if err != nil {
		t.Fatal(err)
	}

	pi := PeerInfo{
		ID:    id,
		Addrs: []ma.Multiaddr{a, b},
	}

	data, err := json.Marshal(&pi)
	if err != nil {
		t.Fatal(err)
	}

	pi2 := new(PeerInfo)
	if err := json.Unmarshal(data, pi2); err != nil {
		t.Fatal(err)
	}

	if pi2.ID != pi.ID {
		t.Fatal("ids didnt match after marshal")
	}

	if !pi.Addrs[0].Equal(pi2.Addrs[0]) {
		t.Fatal("wrong addrs")
	}

	if !pi.Addrs[1].Equal(pi2.Addrs[1]) {
		t.Fatal("wrong addrs")
	}

	lgbl := pi2.Loggable()
	if lgbl["peerID"] != id.Pretty() {
		t.Fatal("loggables gave wrong peerID output")
	}
}

func TestPeerInfoMarshalWithValue(t *testing.T) {
	a := mustAddr(t, "/ip4/1.2.3.4/tcp/4536")
	b := mustAddr(t, "/ip4/1.2.3.8/udp/7777")
	id, err := peer.IDB58Decode("QmaCpDMGvV2BGHeYERUEnRQAwe3N8SzbUtfsmvsqQLuvuJ")
	if err != nil {
		t.Fatal(err)
	}

	pi := PeerInfo{
		ID:    id,
		Addrs: []ma.Multiaddr{a, b},
	}

	data, err := json.Marshal(pi)
	if err != nil {
		t.Fatal(err)
	}

	pi2 := new(PeerInfo)
	if err := json.Unmarshal(data, pi2); err != nil {
		t.Fatal(err)
	}

	if pi2.ID != pi.ID {
		t.Fatal("ids didnt match after marshal")
	}

	if !pi.Addrs[0].Equal(pi2.Addrs[0]) {
		t.Fatal("wrong addrs")
	}

	if !pi.Addrs[1].Equal(pi2.Addrs[1]) {
		t.Fatal("wrong addrs")
	}

	lgbl := pi2.Loggable()
	if lgbl["peerID"] != id.Pretty() {
		t.Fatal("loggables gave wrong peerID output")
	}
}

func TestP2pAddrParsing(t *testing.T) {
	id, err := peer.IDB58Decode("QmaCpDMGvV2BGHeYERUEnRQAwe3N8SzbUtfsmvsqQLuvuJ")
	if err != nil {
		t.Error(err)
	}
	addr := ma.StringCast("/ip4/1.2.3.4/tcp/4536")
	p2paddr := ma.Join(addr, ma.StringCast("/ipfs/"+peer.IDB58Encode(id)))

	pinfo, err := InfoFromP2pAddr(p2paddr)
	if err != nil {
		t.Error(err)
	}

	if pinfo.ID != id {
		t.Fatalf("expected PeerID [%s], got [%s]", id, pinfo.ID)
	}

	if len(pinfo.Addrs) != 1 {
		t.Fatalf("expected 1 addr, got %d", len(pinfo.Addrs))
	}

	if !addr.Equal(pinfo.Addrs[0]) {
		t.Fatalf("expected addr [%s], got [%s]", addr, pinfo.Addrs[0])
	}

	addr = ma.StringCast("/ipfs/" + peer.IDB58Encode(id))
	pinfo, err = InfoFromP2pAddr(addr)
	if err != nil {
		t.Error(err)
	}

	if pinfo.ID != id {
		t.Fatalf("expected PeerID [%s], got [%s]", id, pinfo.ID)
	}

	if len(pinfo.Addrs) > 0 {
		t.Fatalf("expected 0 addrs, got %d", len(pinfo.Addrs))
	}

	addr = ma.StringCast("/ip4/1.2.3.4/tcp/4536")
	pinfo, err = InfoFromP2pAddr(addr)
	if err == nil {
		t.Fatalf("expected error, got none")
	}

	addr = ma.StringCast("/ip4/1.2.3.4/tcp/4536/http")
	pinfo, err = InfoFromP2pAddr(addr)
	if err == nil {
		t.Fatalf("expected error, got none")
	}
}

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