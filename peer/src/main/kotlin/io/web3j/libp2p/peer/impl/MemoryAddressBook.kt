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
import io.web3j.libp2p.peer.AddrBook
import io.web3j.libp2p.peer.PeerID
import io.web3j.libp2p.shared.env.Libp2pException
import org.slf4j.LoggerFactory
import java.time.Duration

/**
 * A thread-safe implementation of [AddrBook] that stores all values in memory.
 */
class MemoryAddressBook : AddrBook {

    /**
     * The map from peer ID to managed address data.
     */
    private val addressMap = HashMap<PeerID, PeerAddressInfo>()

    /**
     * Adds an address for a given peer with a set validity period.
     * @param peerID the peer ID.
     * @param addr the address of the peer.
     * @param ttl an optional TTL for the address that starts from the point in time of addition.
     */
    override fun addAddr(peerID: PeerID, addr: Multiaddr, ttl: Duration?) {
        synchronized(addressMap) {
            val timedAddress = TimedAddress.create(addr, ttl)
            val addressList: PeerAddressInfo = addressMap.getOrPut(peerID) { PeerAddressInfo() }

            val existingAddress = addressList.get(addr)
            if (existingAddress != null) {
                existingAddress.updateExpiry(timedAddress)
            } else {
                addressList.add(timedAddress)
            }
        }
    }

    /**
     * Adds addresses for a given peer with the same validity period. <br />
     * If one of the peer addresses already exists and has a longer TTL, no operation should take place.<br />
     * If one of the addresses exists with a shorter TTL, extend the TTL to equal param ttl.
     * @param peerID the peer ID.
     * @param ttl an optional TTL for the addresses that starts from the point in time of addition.
     * @param addrs the addresses of the peer.
     */
    override fun addAddrs(peerID: PeerID, ttl: Duration?, vararg addrs: Multiaddr) {
        // Create all with the same expiry timestamp.
        val expiryEpochTimestamp: Long? = ttl?.let { TimedAddress.calculateEpochExpiryTime(it.toMillis()) }
        val addresses: List<TimedAddress> = addrs.map { TimedAddress.expiresAt(it, expiryEpochTimestamp) }
        synchronized(addressMap) {
            val addressList: PeerAddressInfo = addressMap.getOrPut(peerID) { PeerAddressInfo() }
            addresses.forEach {
                addressList.add(it)
            }
        }
    }

    /**
     * Removes all previously stored addresses for the peer.
     * @param peerID the peer ID of the peer whose addresses are to be removed/cleared.
     */
    override fun clearAddrs(peerID: PeerID) {
        synchronized(addressMap) {
            addressMap.remove(peerID)
        }
    }

    /**
     * Gets all the addresses for the given peer.
     * @param peerID the peer ID.
     * @param includeExpired whether to include the expired/invalid addresses.
     * @return an array of the known addresses.
     */
    override fun addrsOf(peerID: PeerID, includeExpired: Boolean): Array<Multiaddr> {
        // Don't care about dirty reads at this stage.
        val addressList = addressMap.getOrDefault(peerID, PeerAddressInfo())
        return if (includeExpired) addressList.getAddresses() else addressList.getUnexpiredAddresses()
    }

    /**
     * @return the peer IDs that have addresses in this address book.
     */
    override fun getPeersWithAddresses(): Array<PeerID> = addressMap.keys.toTypedArray()

    /**
     * Resets the TTL (ignoring the existing value) for all the addresses for the given peer.
     * @param peerID the peer ID.
     * @param ttl the optional TTL for the address.
     */
    override fun resetAddr(peerID: PeerID, ttl: Duration?): Unit {
        synchronized(addressMap) {
            addressMap[peerID]?.reset(ttl)
        }
    }

    companion object {

        /**
         * The logger instance.
         */
        val LOGGER = LoggerFactory.getLogger(AddrBook::class.java!!)

    }

    /**
     * Stores addresses for a single peer, along with TTL information on each address.
     */
    private data class PeerAddressInfo(private val addressExpiryMap: MutableMap<Multiaddr, TimedAddress> = mutableMapOf()) {

        /**
         * @return the unexpired addresses as at the current time.
         */
        fun getUnexpiredAddresses(): Array<Multiaddr> =
            addressExpiryMap.filterNot { it.value.hasExpired() }.map { it.key }.toTypedArray()

        /**
         * @return the addresses as at the current time.
         */
        fun getAddresses(): Array<Multiaddr> = addressExpiryMap.keys.toTypedArray()

        /**
         * Gets the [TimedAddress] instance mapped at the given address.
         * @param multiaddr the address.
         * @return the data mapped to the address.
         */
        fun get(multiaddr: Multiaddr): TimedAddress? {
            return addressExpiryMap.get(multiaddr)
        }

        /**
         * Adds the given address into the list.
         * @param timedAddress the address entry to be added.
         */
        fun add(timedAddress: TimedAddress) {
            addressExpiryMap[timedAddress.address] = timedAddress
        }

        /**
         * Resets the TTL (ignoring the existing value) for all the addresses.
         * @param ttl the optional TTL for the address.
         */
        fun reset(ttl: Duration?): Unit {
            this.addressExpiryMap.values.forEach {
                it.reset(ttl)
            }
        }
    }

    /**
     * Represents an address that has a TTL.
     * @param address the address.
     * @param epochExpiryTime the expiry time; [Long.MAX_VALUE] if the address does not expire.
     */
    private class TimedAddress(val address: Multiaddr, var epochExpiryTime: Long) {

        /**
         * @return true if the address does not have a TTL / expiry.
         */
        fun doesNotExpire(): Boolean = epochExpiryTime == Long.MAX_VALUE

        /**
         * @return true if the address has a TTL.
         */
        fun doesExpire(): Boolean = !doesNotExpire()

        /**
         * Updates the TTL on this address if the provided address has a greater expiry TTL.
         * @param timedAddress the other address whose TTL is to be compared with..
         */
        fun updateExpiry(timedAddress: MemoryAddressBook.TimedAddress) {
            if (this.address != timedAddress.address) {
                throw Libp2pException("You cannot update the TTL with a different address record")
            }

            var updated = false
            if (timedAddress.doesNotExpire() && doesExpire()) {
                this.epochExpiryTime = Long.MAX_VALUE
                updated = true
            } else if (timedAddress.doesExpire() && doesExpire()) {
                // Both expire, so choose the later of the two.
                if (this.epochExpiryTime < timedAddress.epochExpiryTime) {
                    this.epochExpiryTime = timedAddress.epochExpiryTime
                    updated = true
                }
            }

            if (updated) {
                LOGGER.debug("Updated address: $timedAddress")
            }
        }

        /**
         * @return true if this address has reached its expiry timestamp.
         */
        fun hasExpired(): Boolean = this.epochExpiryTime <= System.currentTimeMillis()

        /**
         * Resets the TTL (ignoring the existing value) for the address.
         * @param ttl the optional TTL for the address.
         */
        fun reset(ttl: Duration?): Unit {
            if (ttl == null) {
                epochExpiryTime = Long.MAX_VALUE
            } else {
                epochExpiryTime = ttl.toMillis() + System.currentTimeMillis()
            }

        }

        /**
         * @return a string representation of this instance.
         */
        override fun toString(): String {
            return "TimedAddress[addr={${this.address}, expiry=${this.epochExpiryTime}]"
        }


        companion object {

            /**
             * Calculates the expiry time for the given TTL.
             * @param ttl the TTL in milliseconds.
             * @return the epoch expiry time.
             */
            fun calculateEpochExpiryTime(ttl: Long) = ttl + System.currentTimeMillis()

            /**
             * Creates a [TimedAddress] instance that does not have an expiry time.
             * @param addr the address that does not expire.
             * @return the TimedAddress wrapper.
             */
            fun unexpirable(addr: Multiaddr): TimedAddress = TimedAddress(addr, Long.MAX_VALUE)

            /**
             * Creates a [TimedAddress] instance that has an expiry time.
             * @param addr the address that has a TTL.
             * @param ttl the TTL of the address from the current system time.
             * @return the TimedAddress wrapper.
             */
            fun expirable(addr: Multiaddr, ttl: Long): TimedAddress =
                TimedAddress(addr, calculateEpochExpiryTime(ttl))

            /**
             * Creates a [TimedAddress] instance that has an expiry time.
             * @param addr the address that has a TTL.
             * @param expiryTimestamp the exact epoch expiry timestamp of the address.
             * @return the TimedAddress wrapper.
             */
            fun expiresAt(addr: Multiaddr, expiryTimestamp: Long?): TimedAddress {
                return if (expiryTimestamp == null) unexpirable(addr) else TimedAddress(addr, expiryTimestamp)
            }

            /**
             * Determines what type [TimedAddress] instance is to be created based on the presence
             * of a TTL attribute.
             * @param addr the address that has a TTL.
             * @param ttl the TTL of the address from the current system time.
             * @return the TimedAddress wrapper.
             */
            fun create(addr: Multiaddr, ttl: Duration?): TimedAddress {
                return if (ttl == null) unexpirable(addr) else expirable(addr, ttl.toMillis())
            }
        }
    }


}