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

import io.web3j.libp2p.security.secio.model.StretchedKeys
import org.bouncycastle.crypto.macs.HMac
import spipe.pb.Spipe
import java.nio.charset.Charset

object KeyUtil {

    const val IV_SIZE_AES_128 = 16
    const val KEY_SIZE_AES_128 = 16

    const val IV_SIZE_AES_256 = 16
    const val KEY_SIZE_AES_256 = 32

    private val seed = "key expansion".toByteArray(Charset.defaultCharset())

    /**
     * Runs the key stretching algorithm.
     * @param ivSize the initial vector size. TODO: cipher.ivSize, e.g. IV_SIZE_AES_256 = 16
     * @param cipherKeySize the size of the digest. TODO: digest.digestSize
     * @param hmac the hash method to be applied.
     * @return the stretched keys for both local and remote values.
     */
    fun doKeyStretching(ivSize: Int, cipherKeySize: Int, hmac: HMac): Pair<StretchedKeys, StretchedKeys> {
        val hmacKeySize = 20
        val resultLength = 2 * (ivSize + cipherKeySize + hmacKeySize)

        var a: ByteArray = HashUtil.hash(hmac, seed)

        var result = mutableListOf<ByteArray>()
        var j = 0
        while (j < resultLength) {
            // stretch
            val b: ByteArray = HashUtil.hash(hmac, a + seed)
            var todo = b.size

            if (j + todo > resultLength) {
                todo = resultLength - j
            }

            result.add(b)

            j += todo
            a = HashUtil.hash(hmac, a)
        }

        // Now call "finish"
        val half = resultLength / 2
        val resultBuffer: ByteArray = result.fold(ByteArray(0), { acc, curr -> acc + curr })

        val r1: ByteArray = resultBuffer.slice(IntRange(0, half - 1)).toByteArray()
        val r2: ByteArray = resultBuffer.slice(IntRange(half, resultLength - 1)).toByteArray()
        return Pair(extractStretchedKey(r1, ivSize, cipherKeySize), extractStretchedKey(r2, ivSize, cipherKeySize))
    }

    fun gatherCorpusToSign(pmOut: Spipe.Propose, pmIn: Spipe.Propose, sLocalEphemeralKey: ByteArray): ByteArray {
        val selectionOut =
            ByteArray(pmOut.serializedSize + pmIn.serializedSize + sLocalEphemeralKey.size)

        var i = 0
        with(pmOut.toByteArray()) {
            var counter = 0
            while (counter < size) {
                selectionOut[i++] = get(counter++)
            }
        }

        with(pmIn.toByteArray()) {
            var counter = 0
            while (counter < size) {
                selectionOut[i++] = get(counter++)
            }
        }

        with(sLocalEphemeralKey) {
            var counter = 0
            while (counter < size) {
                selectionOut[i++] = get(counter++)
            }
        }

        return selectionOut
    }

    private fun extractStretchedKey(input: ByteArray, ivSize: Int, cipherKeySize: Int): StretchedKeys {
        val iv = input.slice(IntRange(0, ivSize - 1)).toByteArray()
        val cipherKey = input.slice(IntRange(ivSize, ivSize + cipherKeySize - 1)).toByteArray()
        val macKey = input.slice(IntRange(ivSize + cipherKeySize, input.size - 1)).toByteArray()
        return StretchedKeys(iv, macKey, cipherKey)
    }
}
