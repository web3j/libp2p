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
package io.web3j.libp2p.security.secio.util

import io.web3j.libp2p.crypto.unmarshalPublicKey
import io.web3j.libp2p.security.secio.model.ProposeMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.util.Base64

class PrototypeUtilTest {

    @Test
    @Disabled("WIP")
    fun testSecondPartOfproposeSeq() {
        /* ktlint-disable */
        val inputB64 = "AAABRgpBBB95JgzmVwiJT0Qf57JgeGdBTnTJ2MUPgY42JU9KWu4bwjJNAhArn4IZDuOijnjt09lko4nkDaj3vbQ2TZBODj8SgAJ39ZYYq+mlzfO4/06xcNpIMHPboEnF7BguYbcBZQamShFFxeIriXyKY/F7jQxjGtDVaDMgO9npFFygucWQZnwFfIOzrBqUjXQdElmmCHZqktXM5F7P65m9m3LEdlnNoU5khPYhGtcKEXcIXT+PUbmm96qghn+N47fGE1f0xQBY58NEcx6YjZ9WK1rTUd0ZyFvrgod3pNMe2aYwmJm5mbMbJ1APHzROnbpo5X3+mPcCD4StLYtS+8tkmCeESu6iIWyif7JpJ9Jr2ynEiRhH+h24/HTRXosLHxFDBJox70BmcDLq1mxHZa/cvPUpLI9aceY7OxI/iwmU+xR8WUf+vici"
        /* ktlint-enable */
        val inputBytes = Base64.getDecoder().decode(inputB64)
        val result = PrototypeUtil.parseExchangeMessage(inputBytes)
        assertNotNull(result, "Null!")
    }

    @Test
    fun loadAndParseProposal() {
        val file = File(javaClass.classLoader.getResource("secio-proposal-1.bin").file)
        val proposedFromBytes = PrototypeUtil.parseProposeMessage(file.readBytes())
        val proposedFromStream = PrototypeUtil.parseProposeMessageStream(FileInputStream(file))
        assertEquals(proposedFromBytes, proposedFromStream, "Parsing error")

        assertTrue(proposedFromBytes.supportsCipher("AES-128"), "Unsupported cipher")
        assertTrue(proposedFromBytes.supportsCipher("AES-256"), "Unsupported cipher")
        assertFalse(proposedFromBytes.supportsCipher("Foo"), "Cipher should not be supported")

        assertTrue(proposedFromBytes.supportsHash("SHA256"), "Unsupported hash")
        assertTrue(proposedFromBytes.supportsHash("SHA512"), "Unsupported hash")
        assertFalse(proposedFromBytes.supportsHash("Foo"), "Hash should not be supported")

        assertTrue(proposedFromBytes.supportsExchange("P-256"), "Unsupported exchange")
        assertTrue(proposedFromBytes.supportsExchange("P-384"), "Unsupported exchange")
        assertTrue(proposedFromBytes.supportsExchange("P-521"), "Unsupported exchange")
        assertFalse(proposedFromBytes.supportsExchange("Foo"), "Exchange should not be supported")

        assertEquals("QJLd77t1DO9ZVseiUyM9xQ==", proposedFromBytes.randomInBase64, "Incorrect random bytes")
        /* ktlint-disable */
        assertEquals(
            "CAASpgIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDLZZcGcbe4urMBVlcHgN0fpBymY+xcr14ewvamG70QZODJ1h9sljlExZ7byLiqRB3SjGbfpZ1FweznwNxWtWpjHkQjTVXeoM4EEgDSNO/Cg7KNlU0EJvgPJXeEPycAZX9qASbVJ6EECQ40VR/7+SuSqsdL1hrmG1phpIju+D64gLyWpw9WEALfzMpH5I/KvdYDW3N4g6zOD2mZNp5y1gHeXINHWzMF596O72/6cxwyiXV1eJ000k1NVnUyrPjXtqWdVLRk5IU1LFpoQoXZU5X1hKj1a2qt/lZfH5eOrF/ramHcwhrYYw1txf8JHXWO/bbNnyemTHAvutZpTNrsWATfAgMBAAE=",
            proposedFromBytes.publicKeyInBase64,
            "Incorrect public key"
        )
        /* ktlint-enable */
    }

    @Test
    fun recreateProposeMessageTest() {

        lateinit var templateBytes: ByteArray

        // Load our template.
        val templateProposeMessage = with(File(javaClass.classLoader.getResource("secio-proposal-1.bin").file)) {
            templateBytes = readBytes()
            PrototypeUtil.parseProposeMessage(templateBytes)
        }

        // Create an identical Propose message.
        val randB64 = "QJLd77t1DO9ZVseiUyM9xQ=="
        /* ktlint-disable */
        val publicKeyB64 =
            "CAASpgIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDLZZcGcbe4urMBVlcHgN0fpBymY+xcr14ewvamG70QZODJ1h9sljlExZ7byLiqRB3SjGbfpZ1FweznwNxWtWpjHkQjTVXeoM4EEgDSNO/Cg7KNlU0EJvgPJXeEPycAZX9qASbVJ6EECQ40VR/7+SuSqsdL1hrmG1phpIju+D64gLyWpw9WEALfzMpH5I/KvdYDW3N4g6zOD2mZNp5y1gHeXINHWzMF596O72/6cxwyiXV1eJ000k1NVnUyrPjXtqWdVLRk5IU1LFpoQoXZU5X1hKj1a2qt/lZfH5eOrF/ramHcwhrYYw1txf8JHXWO/bbNnyemTHAvutZpTNrsWATfAgMBAAE="
        /* ktlint-enable */
        val pubKey = unmarshalPublicKey(Base64.getDecoder().decode(publicKeyB64.toByteArray()))
        val random = Base64.getDecoder().decode(randB64.toByteArray())

        val reconstructed = ProposeMessage(pubKey, random)
            .withCiphers("AES-256", "AES-128")
            .withHashes("SHA256", "SHA512")
            .withExchanges("P-256", "P-384", "P-521")

        assertEquals(
            templateProposeMessage, reconstructed,
            "Unable to create an identical 'Propose' message to the template"
        )

        val reconstructedBytes = toByteArray(reconstructed)
        val reconstructedBytesBackToPropose = PrototypeUtil.parseProposeMessage(reconstructedBytes)

        assertEquals(reconstructed, reconstructedBytesBackToPropose, "Operation is not reflexive")
        assertTrue(
            templateBytes.contentEquals(reconstructedBytes),
            "Byte array versions differ between template and reconstructed messages"
        )
    }

    /**
     * Creates a serialized version of the given message that can be sent to a peer.
     * @param proposeMessage the message to be serialized.
     * @return the byte array equivalent.
     */
    private fun toByteArray(proposeMessage: ProposeMessage): ByteArray {
        val bytes = proposeMessage.asPropose().toByteArray()
        val byteCount = bytes.size

        val byteCountAsBytes = ByteBuffer.allocate(java.lang.Integer.BYTES).putInt(byteCount).array()
        val newArray = ByteArray(byteCount + 4)
        System.arraycopy(byteCountAsBytes, 0, newArray, 0, 4)
        System.arraycopy(bytes, 0, newArray, 4, byteCount)

        return newArray
    }

    @Test
    @Disabled("WIP")
    fun loadAndParseExchange() {
        // TODO: add support for exchange messages.
        val file = File(javaClass.classLoader.getResource("secio-exchange-1.bin").file)
        val exchangeFromBytes = PrototypeUtil.parseExchangeMessage(file.readBytes())
        val exchangeFromStream = PrototypeUtil.parseExchangeMessageStream(FileInputStream(file))
        assertEquals(exchangeFromBytes, exchangeFromStream, "Parsing error")

//        assertTrue(proposedFromBytes.supportsCipher("AES-128"), "Unsupported cipher")
//        assertTrue(proposedFromBytes.supportsCipher("AES-256"), "Unsupported cipher")
//        assertFalse(proposedFromBytes.supportsCipher("Foo"), "Cipher should not be supported")
//
//        assertTrue(proposedFromBytes.supportsHash("SHA256"), "Unsupported hash")
//        assertTrue(proposedFromBytes.supportsHash("SHA512"), "Unsupported hash")
//        assertFalse(proposedFromBytes.supportsHash("Foo"), "Hash should not be supported")
//
//        assertTrue(proposedFromBytes.supportsExchange("P-256"), "Unsupported exchange")
//        assertTrue(proposedFromBytes.supportsExchange("P-384"), "Unsupported exchange")
//        assertTrue(proposedFromBytes.supportsExchange("P-521"), "Unsupported exchange")
//        assertFalse(proposedFromBytes.supportsExchange("Foo"), "Exchange should not be supported")
//
//        assertEquals("QJLd77t1DO9ZVseiUyM9xQ==", proposedFromBytes.randomInBase64, "Incorrect random bytes")
//        assertEquals(
//            "CAASpgIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDLZZcGcbe4urMBVlcHgN0fpBymY+xcr14ewvamG70QZODJ1h9sljlExZ7byLiqRB3SjGbfpZ1FweznwNxWtWpjHkQjTVXeoM4EEgDSNO/Cg7KNlU0EJvgPJXeEPycAZX9qASbVJ6EECQ40VR/7+SuSqsdL1hrmG1phpIju+D64gLyWpw9WEALfzMpH5I/KvdYDW3N4g6zOD2mZNp5y1gHeXINHWzMF596O72/6cxwyiXV1eJ000k1NVnUyrPjXtqWdVLRk5IU1LFpoQoXZU5X1hKj1a2qt/lZfH5eOrF/ramHcwhrYYw1txf8JHXWO/bbNnyemTHAvutZpTNrsWATfAgMBAAE=",
//            proposedFromBytes.publicKeyInBase64,
//            "Incorrect public key"
//        )
    }
}
