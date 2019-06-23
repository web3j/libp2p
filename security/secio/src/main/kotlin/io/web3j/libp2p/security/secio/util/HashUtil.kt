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

import org.bouncycastle.crypto.macs.HMac
import java.security.MessageDigest

object HashUtil {

    private val sha256 = MessageDigest.getInstance("SHA-256")

    /**
     * Computes the SHA-256 hash of the contents of the given arrays.
     * @param arrays the arrays.
     * @return the SHA-256 hash of all the args combined.
     */
    fun sha256(vararg arrays: ByteArray): ByteArray {
        return sha256.digest(arrays.fold(ByteArray(0), { a, b -> a + b }))
    }

    /**
     * Runs the hash over the given data array.
     * @param hmac the hash method to be applied.
     * @param data the data to be hashed.
     * @return the hash of the given data.
     */
    fun hash(hmac: HMac, data: ByteArray): ByteArray {
        // This exists mainly to aid testing whilst we sort out hash/encryption differences
        // between BC and JS.
        return with(ByteArray(hmac.macSize)) {
            hmac.update(data, 0, data.size)
            hmac.doFinal(this, 0)
            this
        }
    }
}
