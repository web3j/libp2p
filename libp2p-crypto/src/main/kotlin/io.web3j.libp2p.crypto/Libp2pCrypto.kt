package io.web3j.libp2p.crypto

/**
 * ErrRsaKeyTooSmall is returned when trying to generate or parse an RSA key
 * that's smaller than 512 bits. Keys need to be larger enough to sign a 256bit
 * hash so this is a reasonable absolute minimum.
 */
const val ErrRsaKeyTooSmall = "rsa keys must be >= 512 bits to be useful"

const val RSA_ALGORITHM = "RSA"

object Libp2pCrypto {

    val a = "SHA256withRSA"

    val provider = org.bouncycastle.jce.provider.BouncyCastleProvider()
}
