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
import io.web3j.libp2p.shared.ext.toBase64String
import org.bouncycastle.crypto.digests.SHA256Digest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class KeyUtilTest {

    @Test
    fun testKeyStretcher() {
        val digest = SHA256Digest()
        val secretKey = "2BRumHjn7bXjSbPseMw+3bnwvyqVhBIoAwsDKIW3b6M=".fromBase64()

        val hmac = HashFactory.createHmac(secretKey, digest)
        val (sk1, sk2) = KeyUtil.doKeyStretching(16, digest.digestSize, hmac)
        val (k1Iv, k1MacKey, k1CipherKey) = sk1
        val (k2Iv, k2MacKey, k2CipherKey) = sk2

        // assert k1
        Assertions.assertEquals("bMt8nKQCPZmgypwj2A4Yzth6PRAUfPgqQbzPvQcLSMA=", k1CipherKey.toBase64String())
        Assertions.assertEquals("JcBXx//oxNzxgv89g8tM2A==", k1Iv.toBase64String())
        Assertions.assertEquals("Jj8eMqMJ5HS2qXeLRVQ12jybMD0=", k1MacKey.toBase64String())

        // assert k2
        Assertions.assertEquals("sHixGyshaP74+INk/scEyheI7aK4bIMMuoGDPzHHZwE=", k2CipherKey.toBase64String())
        Assertions.assertEquals("AF5gz30nCfe2a7/El95Wgw==", k2Iv.toBase64String())
        Assertions.assertEquals("nI0S2hW1fqAp60WUKD4CkuBJigc=", k2MacKey.toBase64String())
    }
}
