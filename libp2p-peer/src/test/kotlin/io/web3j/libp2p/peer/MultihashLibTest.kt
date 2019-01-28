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
package io.web3j.libp2p.peer

import io.ipfs.multiformats.multihash.Type
import io.web3j.libp2p.crypto.unmarshalPrivateKey
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import java.util.Base64
import io.ipfs.multiformats.multihash.Multihash as KMultihash
import io.ipfs.multihash.Multihash as JMultihash

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
class MultihashLibTest {

    val pubKeyHashBase58 = "QmRK3JgmVEGiewxWbhpXLJyjWuGuLeSTMTndA1coMHEy5o"

    val privateKeyBase64 = """
    CAAS4AQwggJcAgEAAoGBAL7w+Wc4VhZhCdM/+Hccg5Nrf4q9NXWwJylbSrXz/unFS24wyk6pEk0zi3W
    7li+vSNVO+NtJQw9qGNAMtQKjVTP+3Vt/jfQRnQM3s6awojtjueEWuLYVt62z7mofOhCtj+VwIdZNBo
    /EkLZ0ETfcvN5LVtLYa8JkXybnOPsLvK+PAgMBAAECgYBdk09HDM7zzL657uHfzfOVrdslrTCj6p5mo
    DzvCxLkkjIzYGnlPuqfNyGjozkpSWgSUc+X+EGLLl3WqEOVdWJtbM61fewEHlRTM5JzScvwrJ39t7o6
    CCAjKA0cBWBd6UWgbN/t53RoWvh9HrA2AW5YrT0ZiAgKe9y7EMUaENVJ8QJBAPhpdmb4ZL4Fkm4OKia
    NEcjzn6mGTlZtef7K/0oRC9+2JkQnCuf6HBpaRhJoCJYg7DW8ZY+AV6xClKrgjBOfERMCQQDExhnzu2
    dsQ9k8QChBlpHO0TRbZBiQfC70oU31kM1AeLseZRmrxv9Yxzdl8D693NNWS2JbKOXl0kMHHcuGQLMVA
    kBZ7WvkmPV3aPL6jnwp2pXepntdVnaTiSxJ1dkXShZ/VSSDNZMYKY306EtHrIu3NZHtXhdyHKcggDXr
    qkBrdgErAkAlpGPojUwemOggr4FD8sLX1ot2hDJyyV7OK2FXfajWEYJyMRL1Gm9Uk1+Un53RAkJneqp
    JGAzKpyttXBTIDO51AkEA98KTiROMnnU8Y6Mgcvr68/SMIsvCYMt9/mtwSBGgl80VaTQ5Hpaktl6Xbh
    VUt5Wv0tRxlXZiViCGCD1EtrrwTw==
    """.trimIndent().replace("\n", "")

    @Test
    fun loadUsingJavaLibrary() {
        val reconstructedPkMultihash: JMultihash = with(Base64.getDecoder().decode(privateKeyBase64)) {
            val privKey = unmarshalPrivateKey(this)
            val pubKey = privKey.publicKey()
            val pubKeyBytes = pubKey.bytes()

            // Try with java-multihash.
            with(MessageDigest.getInstance("SHA-256")) {
                update(pubKeyBytes)
                JMultihash(JMultihash.Type.sha2_256, digest())
            }
        }

        val inputPkMultihash: JMultihash = JMultihash.fromBase58(pubKeyHashBase58)
        Assertions.assertEquals(
            inputPkMultihash.toBase58(),
            reconstructedPkMultihash.toBase58(),
            "Java-library multi-hash values differ for the public key"
        )
    }

    @Disabled("Awaiting a resolution to: https://github.com/changjiashuai/kotlin-multihash/issues/3")
    @Test
    fun loadUsingKotlinLibrary() {
        val reconstructedPkMultihash: KMultihash = with(Base64.getDecoder().decode(privateKeyBase64)) {
            val privKey = unmarshalPrivateKey(this)
            val pubKey = privKey.publicKey()
            val pubKeyBytes = pubKey.bytes()
            val pubKeyHash: ByteArray = KMultihash.encodeByName(pubKeyBytes, Type.SHA2_256.named) // hash(bpk)
            KMultihash.cast(pubKeyHash)
        }

        val inputPkMultihash: KMultihash = KMultihash.fromBase58String(pubKeyHashBase58)
        Assertions.assertEquals(
            inputPkMultihash.toBase58String(),
            reconstructedPkMultihash.toBase58String(),
            "Kotlin-library multi-hash values differ for the public key"
        )
    }
}
