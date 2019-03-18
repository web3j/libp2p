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
package io.web3j.libp2p.peer.impl

import io.ipfs.multiformats.multiaddr.Multiaddr
import io.web3j.libp2p.peer.util.PeerTestUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

/**
 * A suite of tests for the [MemoryAddressBook] implementation.
 */
class MemoryAddressBookTest {

    /**
     * Tests addresses for different peers.
     */
    @Test
    fun simplePeerWithAddressesTest() {
        val addrBook = MemoryAddressBook()
        val peer1 = PeerTestUtil.createPeer()

        // Nothing initially.
        var peersWithAddrs = addrBook.getPeersWithAddresses()
        assertTrue(peersWithAddrs.isEmpty(), "No peers should be set")

        // Add an address for Peer1.
        val peer1Addr1: Multiaddr = PeerTestUtil.createMultiaddr(2000)
        addrBook.addAddr(peer1, peer1Addr1, null)

        peersWithAddrs = addrBook.getPeersWithAddresses()
        assertEquals(1, peersWithAddrs.size, "Expected 1 PeerID")
        assertTrue(peersWithAddrs.contains(peer1), "Peer#1 not found")

        // Add an address for Peer2.
        val peer2 = PeerTestUtil.createPeer()
        val peer2Addr1: Multiaddr = PeerTestUtil.createMultiaddr(3000)
        addrBook.addAddr(peer2, peer2Addr1, null)

        peersWithAddrs = addrBook.getPeersWithAddresses()
        assertEquals(2, peersWithAddrs.size, "Expected 2 PeerIDs")
        assertTrue(peersWithAddrs.contains(peer1), "Peer#1 not found")
        assertTrue(peersWithAddrs.contains(peer2), "Peer#2 not found")
    }

    /**
     * Tests peer address expiry.
     */
    @Test
    fun addressExpiryTest() {
        val addrBook = MemoryAddressBook()
        val peer1 = PeerTestUtil.createPeer()

        val peer1Addrs = Array(10) { i -> PeerTestUtil.createMultiaddr(2000 + i) }
        addrBook.addAddrs(peer1, null, *peer1Addrs)

        // Peer 1 should be in our address book.
        var peersWithAddrs = addrBook.getPeersWithAddresses()
        assertEquals(1, peersWithAddrs.size, "Expected 1 PeerID")
        assertTrue(peersWithAddrs.contains(peer1), "Peer#1 not found")

        // Assert Peer1's non-expired addresses.
        var allPeer1Addrs = addrBook.addrsOf(peer1, true)
        assertEquals(10, allPeer1Addrs.size, "Expected 10 unexpired addresses for Peer#1")
        assertEquals(10, addrBook.addrsOf(peer1, false).size, "No addresses ought to have expired for Peer#1")

        // Now add an address that will expire in the next nanosecond.
        // We should now have 11 addresses, 10 previous + 1 expired.
        addrBook.addAddr(peer1, PeerTestUtil.createMultiaddr(100), Duration.ofNanos(1))
        allPeer1Addrs = addrBook.addrsOf(peer1, true)
        assertEquals(11, allPeer1Addrs.size, "Expected 10 unexpired addresses for Peer#1")
        assertEquals(10, addrBook.addrsOf(peer1, false).size, "Expected 1 expired address for Peer#1")

        // Remove all peer#1 addresses.
        addrBook.clearAddrs(peer1)
        allPeer1Addrs = addrBook.addrsOf(peer1, true)
        assertEquals(0, allPeer1Addrs.size, "Expected no addresses Peer#1")
    }

    /**
     * Tests updating the TTL of an address to a finite value.
     */
    @Test
    fun updateAddrTtlTest() {
        val addrBook = MemoryAddressBook()
        val peer1 = PeerTestUtil.createPeer()

        // Add an address with a TTL of 10 minutes.
        val peer1Addr1: Multiaddr = PeerTestUtil.createMultiaddr(2000)
        addrBook.addAddr(peer1, peer1Addr1, Duration.ofMinutes(10))

        // We should have that address expiring in 10 minutes.
        var allPeer1Addrs = addrBook.addrsOf(peer1, true)
        assertEquals(1, allPeer1Addrs.size, "Expected 1 address for Peer#1")

        // Now add another address that expires very soon (in 1 nanosecond).
        // We should now have the original address + expired address.
        val peer1Addr2: Multiaddr = PeerTestUtil.createMultiaddr(3000)
        addrBook.addAddr(peer1, peer1Addr2, Duration.ofNanos(1))

        allPeer1Addrs = addrBook.addrsOf(peer1, true)
        assertEquals(2, allPeer1Addrs.size, "Expected 2 addresses for Peer#1")
        assertEquals(1, addrBook.addrsOf(peer1, false).size, "Expected 1 unexpired address for Peer#1")

        // Now insert (again) address2 with a bigger TTL.
        addrBook.addAddr(peer1, peer1Addr2, Duration.ofMinutes(1))
        assertEquals(2, addrBook.addrsOf(peer1, false).size, "Expected 2 unexpired addresses for Peer#1")
    }

    /**
     * Tests updating the TTL of an address to various finite and infinite values.
     */
    @Test
    fun updateAddrTtlTest2() {
        val addrBook = MemoryAddressBook()
        val peer1 = PeerTestUtil.createPeer()

        // Add an address with a TTL of 1 minute.
        val peer1Addr1: Multiaddr = PeerTestUtil.createMultiaddr(2000)
        addrBook.addAddr(peer1, peer1Addr1, Duration.ofMinutes(1))
        assertEquals(1, addrBook.addrsOf(peer1, false).size, "Expected 1 unexpired address for Peer#1")

        // Now try to update the TTL to be 1 hour.
        addrBook.addAddr(peer1, peer1Addr1, Duration.ofHours(1))
        assertEquals(1, addrBook.addrsOf(peer1, false).size, "Expected 1 unexpired address for Peer#1")

        // Now try to reduce the TTL to 5 minutes.
        addrBook.addAddr(peer1, peer1Addr1, Duration.ofMinutes(5))
        var allPeer1Addrs = addrBook.addrsOf(peer1, false)
        assertEquals(1, allPeer1Addrs.size, "Expected 1 address for Peer#1")

        // Next, set the values to something small - this should be ignored.
        addrBook.addAddr(peer1, peer1Addr1, Duration.ofNanos(1))
        allPeer1Addrs = addrBook.addrsOf(peer1, false)
        assertEquals(1, allPeer1Addrs.size, "Expected 1 address for Peer#1")

        // Now add a new peer with an address that is going to expire soon.
        val peer2 = PeerTestUtil.createPeer()
        val peer2Addr1: Multiaddr = PeerTestUtil.createMultiaddr(2000)
        addrBook.addAddr(peer2, peer2Addr1, Duration.ofNanos(1))
        assertEquals(0, addrBook.addrsOf(peer2, false).size, "Expected no unexpired address for Peer#2")

        // Add the record again with no TTL.
        addrBook.addAddr(peer2, peer2Addr1, null)
        assertEquals(1, addrBook.addrsOf(peer2, false).size, "Expected 1 unexpired address for Peer#2")
    }

    /**
     * Tests resetting the TTLs on all configured addresses for a peer.
     */
    @Test
    fun resetAddrTest() {
        val addrBook = MemoryAddressBook()
        val peer1 = PeerTestUtil.createPeer()

        // Now add another address that expires very soon (in 1 nanosecond).
        // We should now have the original address + expired address.

        // Add 2 address, one with a TTL of 1 nanosecond and the other with 1 minute.
        val peer1Addr1: Multiaddr = PeerTestUtil.createMultiaddr(2000)
        val peer1Addr2: Multiaddr = PeerTestUtil.createMultiaddr(3000)
        val peer1Addr3: Multiaddr = PeerTestUtil.createMultiaddr(4000)

        // Only 1 address should be live.
        addrBook.addAddr(peer1, peer1Addr1, Duration.ofNanos(1))
        addrBook.addAddr(peer1, peer1Addr2, Duration.ofMinutes(10))
        assertEquals(1, addrBook.addrsOf(peer1, false).size, "Expected 1 unexpired address for Peer#1")

        // Now reset all entries to expire in 10 minutes.
        addrBook.resetAddr(peer1, Duration.ofMinutes(10))
        assertEquals(2, addrBook.addrsOf(peer1, false).size, "Expected 2 unexpired addresses for Peer#1")

        // Add another address that expires in 1 nanosecond, then reset it not to exprie.
        addrBook.addAddr(peer1, peer1Addr3, Duration.ofNanos(1))
        assertEquals(2, addrBook.addrsOf(peer1, false).size, "Expected 2 unexpired addresses for Peer#1")
        // reset.
        addrBook.resetAddr(peer1, null)
        assertEquals(3, addrBook.addrsOf(peer1, false).size, "Expected 3 unexpired addresses for Peer#1")

        // Reset a non-existent peer.
        addrBook.resetAddr(PeerTestUtil.createPeer(), Duration.ofMinutes(10))
        assertEquals(1, addrBook.getPeersWithAddresses().size, "Expected 1 peer with an address")
    }
}
