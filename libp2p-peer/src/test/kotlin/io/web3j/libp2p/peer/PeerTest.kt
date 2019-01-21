package io.web3j.libp2p.peer

import io.ipfs.multiformats.multihash.Multihash
import io.ipfs.multiformats.multihash.Type
import io.web3j.libp2p.crypto.KEY_TYPE
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey
import io.web3j.libp2p.crypto.keys.generateEd25519KeyPair
import io.web3j.libp2p.crypto.generateKeyPair
import io.web3j.libp2p.crypto.unmarshalPrivateKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.kethereum.encodings.encodeToBase58String
import java.util.Base64
import java.util.concurrent.atomic.AtomicInteger

class PeerTest {

    val manualPubKeyHashBase58 = "QmRK3JgmVEGiewxWbhpXLJyjWuGuLeSTMTndA1coMHEy5o"
    val manualPrivKeyBytes = """
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

    private lateinit var gen1: Keyset //generated
    private lateinit var gen2: Keyset //generated
    private lateinit var man: Keyset //manual

    @BeforeEach
    fun init() {
        gen1 = Keyset.generate()
        gen2 = Keyset.generate()
        man = Keyset.load(manualPubKeyHashBase58, manualPrivKeyBytes)
    }

    @Test
    fun testIDMatchesPublicKey() {
        idMatchesPublicKey(gen1)
        idMatchesPublicKey(gen2)
        idMatchesPublicKey(man)
    }

    @Test
    fun testIDMatchesPrivateKey() {
        idMatchesPrivateKey(gen1)
        idMatchesPrivateKey(gen2)
        idMatchesPrivateKey(man)
    }

    // https://github.com/libp2p/go-libp2p-crypto/issues/51
    @Disabled("disabled until libp2p/go-libp2p-crypto#51 is fixed")
    @Test
    fun testPublicKeyExtraction() {
        val keyPair = generateEd25519KeyPair()
        val pubKey = keyPair.second
        val id = ID.idFromPublicKey(pubKey)
        val extractedPub = id.extractPublicKey()
        assertEquals(pubKey, extractedPub)

        // Test invalid multihash (invariant of the type of public key)
        val pk = ID(Multihash(byteArrayOf())).extractPublicKey()
        assertNull(pk)

        // Shouldn't work for, e.g. RSA keys (too large)
        val keyPair1 = generateKeyPair(KEY_TYPE.RSA, 2048)
        val pubKey1 = keyPair1.second
        val rsaId = ID.idFromPublicKey(pubKey1)
        val extractedRsaPub = rsaId.extractPublicKey()
        assertNull(extractedRsaPub)
    }

    private fun idMatchesPublicKey(ks: Keyset) {
        val p1 = ID.idB58Decode(ks.pubKeyHashBase58)

        assertEquals(ks.pubKeyHash, p1.toString())

        assertTrue(p1.matchesPublicKey(ks.pubKey))

        val p2 = ID.idFromPublicKey(ks.pubKey)

        assertEquals(p1, p2)

        assertEquals(ks.pubKeyHashBase58, p2.pretty())
    }

    private fun idMatchesPrivateKey(ks: Keyset) {
        val p1 = ID.idB58Decode(ks.pubKeyHashBase58)

        assertEquals(ks.pubKeyHash, p1.toString())

        assertTrue(p1.matchesPrivateKey(ks.privKey))

        val p2 = ID.idFromPrivateKey(ks.privKey)

        assertEquals(p1, p2)
    }

    private data class Keyset(
        val privKey: PrivKey,
        val pubKey: PubKey,
        val pubKeyHash: String,
        val pubKeyHashBase58: String
    ) {

        companion object {

            var generatedPairs = AtomicInteger(0)

            fun generate(): Keyset {
                val keyPair = generateKeyPair(KEY_TYPE.RSA, 512)

                val privKey = keyPair.first
                val pubKey = keyPair.second
                val pubKeyBytes = pubKey.bytes()
                val pubKeyHash = Multihash.encodeByName(pubKeyBytes, Type.SHA2_256.name)
                val pubKeyHashBase58 = pubKeyHash.encodeToBase58String()

                return Keyset(privKey, pubKey, pubKeyHash.toString(), pubKeyHashBase58)
            }

            fun load(manPubKeyHashBase58: String, manPrivKeyBytes: String): Keyset {
                val decodedPrivKeyBytes = Base64.getDecoder().decode(manPrivKeyBytes)
                val privKey = unmarshalPrivateKey(decodedPrivKeyBytes)
                val pubKey = privKey.publicKey()
                val pubKeyBytes = pubKey.bytes()
                val pubKeyHash = Multihash.encodeByName(pubKeyBytes, Type.SHA2_256.name)
                val pubKeyHashBase58 = pubKeyHash.encodeToBase58String()

                if (pubKeyHashBase58 != manPubKeyHashBase58) {
                    throw RuntimeException(
                        "pubKeyHashBase58 does " +
                                "not match manPubKeyHashBase58. $pubKeyHashBase58"
                    )
                }

                return Keyset(privKey, pubKey, pubKeyHash.toString(), pubKeyHashBase58)
            }
        }
    }
}