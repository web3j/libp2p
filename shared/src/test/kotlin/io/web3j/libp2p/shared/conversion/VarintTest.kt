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
package io.web3j.libp2p.shared.conversion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.xml.bind.DatatypeConverter

class VarintTest {

    /**
     * Tests the conversion of a ULong into a varint.
     */
    @Test
    fun toVarintTest1() {
        assertEquals("AC02", createVarintHex(300))
        assertEquals("978406", createVarintHex(98839))
        assertEquals("EBB3B7C403", createVarintHex(948820459))
        assertEquals("ED8CB5C396A29C897D", createVarintHex(9012390123902158445))
    }

    /**
     * Tests the conversion of a varint back into a ULong.
     */
    @Test
    fun fromVarintTest1() {
        assertEquals(300.toULong(), createULongFromHex("AC02"))
        assertEquals(98839.toULong(), createULongFromHex("978406"))
        assertEquals(948820459.toULong(), createULongFromHex("EBB3B7C403"))
        assertEquals(9012390123902158445.toULong(), createULongFromHex("ED8CB5C396A29C897D"))
        assertEquals(300.toULong(), createULongFromHex("AC02978406"))
    }

    /**
     * Helper function to create the varint as a hex string.
     * @param value the long value to be tested.
     * @return the hex value of the varint.
     */
    private fun createVarintHex(value: Long): String {
        return DatatypeConverter.printHexBinary(Varint.toVarint(value.toULong()))
    }


    /**
     * Helper function to create the unsigned int from a hex string.
     * @param hex the hex value of the varint.
     * @return the ulong value.
     */
    private fun createULongFromHex(hex: String): ULong {
        return Varint.fromVarint(DatatypeConverter.parseHexBinary(hex)!!)
    }
}