package io.web3j.libp2p.crypto

/**
 * ErrRsaKeyTooSmall is returned when trying to generate or parse an RSA key
 * that's smaller than 512 bits. Keys need to be larger enough to sign a 256bit
 * hash so this is a reasonable absolute minimum.
 */
const val ErrRsaKeyTooSmall = "rsa keys must be >= 512 bits to be useful"

const val RSA_ALGORITHM = "RSA"
const val SHA_ALGORITHM = "SHA-256"

const val ECDSA_ALGORITHM = "ECDSA"

const val ED25519_ALGORITHM = "ED25519"

const val P256_CURVE = "P-256"

const val SHA_256_WITH_RSA = "SHA256withRSA"
const val SHA_256_WITH_ECDSA = "SHA256withECDSA"

const val KEY_PKCS8 = "PKCS#8"

object Libp2pCrypto {

    val provider = org.bouncycastle.jce.provider.BouncyCastleProvider()
}
