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

import io.web3j.libp2p.shared.ext.fromBase64
import io.web3j.libp2p.shared.ext.readVarintPrefixedMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SecioUtilTest {

    @Test
    fun parseMultistreamAndSecioMessage() {
        val input: ByteArray = "Ey9tdWx0aXN0cmVhbS8xLjAuMAoNL3NlY2lvLzEuMC4wCg==".fromBase64()
        val messagePair = input.readVarintPrefixedMessage()
        assertNotNull(messagePair, "Unable to parse bytes")
        messagePair!!
        assertEquals("/multistream/1.0.0\n", String(messagePair.first))

        val secondArray = messagePair.second
        assertNotNull(secondArray, "No second array of data detected")
        secondArray!!
        val secondPair = secondArray.readVarintPrefixedMessage()
        assertNotNull(secondPair, "Second byte sequence could not be extracted")
        secondPair!!
        assertEquals("/secio/1.0.0\n", String(secondPair.first))
        assertNull(secondPair.second, "No second array should be returned")
    }

    @Test
    fun parseAndRereateSecioProposeMessageTest() {
        val secioBytes =
            ("DS9zZWNpby8xLjAuMAoAAAF8ChBXc+uV2a5qHMbdN/MW57MrEqsCCAASpgIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC" +
                    "zEWu+VKBPxmd4ZTlWYjh9Hww4NF6WvRqdy1zPvw8VfsFbDICUxvmw6/mjI12/lYMUXYPL1s3W7wGdFs3dVXLIxucwvejI85O" +
                    "nv+wLUmLxYbXK/9PUNKu9XXHb0TxDOEsbpMSPmWv4TCkKCsB/B4KGXG4PamUrZOCWiybcPnvmRhK9cZO9PL+o6fdeIiAVTv+" +
                    "TurY8yjrZCFNMOWv60183qom8lNdccD4rb1RAz3T5vdz/wPiyN/9mg+Wo0arxzPyr/+TsL+zjIYKiYUccRhkPCXCvMB+g/wd" +
                    "973A8t8amobnhDPtqqI9Ra2sV4URnOxMB6GDh0eWYOiudoJcZI1Z9AgMBAAEaEVAtMjU2LFAtMzg0LFAtNTIxIhhBRVMtMjU" +
                    "2LEFFUy0xMjgsQmxvd2Zpc2gqDVNIQTI1NixTSEE1MTI=").fromBase64()
        val proposeMessage = SecioUtil.parseSecioProposeMessage(secioBytes)
        assertNotNull(proposeMessage, "Message bytes could not be parsed")

        val reconstructedBytes = SecioUtil.createSecioProposeMessage(proposeMessage!!)
        assertEquals(secioBytes.size, reconstructedBytes.size, "Incorrect size")
        assertTrue(secioBytes.contentEquals(reconstructedBytes), "Reconstructed secio stream differs from original")
    }

    @Test
    fun selectBestProposalParametersTest() {
        val localPubKeyBytes =
            ("CAASpgIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCaNSDOjPz6T8HZsf7LDpxiQRiN2OjeyIHUS05p8QWOr3EFUCFsC3" +
                    "1R4moihE5HN+FxNalUyyFZU//yjf1pdnlMJqrVByJSMa+y2y4x2FucpoCAO97Tx+iWzwlZ2UXEUXM1Y81mhPbeWXy+wP2xE" +
                    "lTgIER0Tsn/thoA0SD2u9wJuVvM7dB7cBcHYmqV6JH+KWCedRTum6O1BssqP/4Lbm2+rkrbZ4+oVRoU2DRLoFhKqwqLtylr" +
                    "buj4XOI3XykMXV5+uQXz1JzubNOB9lsc6K+eRC+w8hhhDuFMgzkZ4qomCnx3uhO67KaICd8yqqBa6PJ/+fBM5Xk4hjyR40b" +
                    "wcf41AgMBAAE=").fromBase64()
        val remotePubKeyBytes =
            ("CAASpgIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDLZZcGcbe4urMBVlcHgN0fpBymY+xcr14ewvamG70QZODJ1h9sljl" +
                    "ExZ7byLiqRB3SjGbfpZ1FweznwNxWtWpjHkQjTVXeoM4EEgDSNO/Cg7KNlU0EJvgPJXeEPycAZX9qASbVJ6EECQ40VR/7+Su" +
                    "SqsdL1hrmG1phpIju+D64gLyWpw9WEALfzMpH5I/KvdYDW3N4g6zOD2mZNp5y1gHeXINHWzMF596O72/6cxwyiXV1eJ000k1" +
                    "NVnUyrPjXtqWdVLRk5IU1LFpoQoXZU5X1hKj1a2qt/lZfH5eOrF/ramHcwhrYYw1txf8JHXWO/bbNnyemTHAvutZpTNrsWAT" +
                    "fAgMBAAE=").fromBase64()
        val localNonce = "43Gsw3IROAYT0byed3w5Hw==".fromBase64()
        val remoteNonce = "H7WmedRsxKzPxzHUgpoaog==".fromBase64()

        val localExchanges = arrayOf("P-256", "P-384", "P-521")
        val localCiphers = arrayOf("AES-128", "AES-256")
        val localHashes = arrayOf("SHA-256", "SHA-512")

        val remoteExchanges = localExchanges
        val remoteCiphers = localCiphers
        val remoteHashes = localHashes

        val bestParams = SecioUtil.selectBest(
            localPubKeyBytes,
            remotePubKeyBytes,
            localNonce,
            remoteNonce,
            localExchanges,
            localCiphers,
            localHashes,
            remoteExchanges,
            remoteCiphers,
            remoteHashes
        )

        assertEquals(-1, bestParams.order)
        assertEquals("P-256", bestParams.curve)
        assertEquals("AES-128", bestParams.cipher)
        assertEquals("SHA-256", bestParams.hash)
    }
}
