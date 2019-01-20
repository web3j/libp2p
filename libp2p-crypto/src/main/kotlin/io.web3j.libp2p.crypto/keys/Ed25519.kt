package io.web3j.libp2p.crypto.keys

import crypto.pb.Crypto
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.SecureRandom


// Ed25519PrivateKey is an ed25519 private key
class Ed25519PrivateKey(private val priv: Ed25519PrivateKeyParameters) : PrivKey(Crypto.KeyType.Ed25519) {

    override fun raw(): ByteArray = priv.encoded

    override fun sign(data: ByteArray): ByteArray = with(Ed25519Signer()) {
        init(true, priv)
        update(data, 0, data.size)
        generateSignature()
    }

    override fun publicKey(): PubKey = Ed25519PublicKey(priv.generatePublicKey())

    override fun hashCode(): Int = priv.hashCode()
}

// Ed25519PublicKey is an ed25519 public key
class Ed25519PublicKey(private val pub: Ed25519PublicKeyParameters) : PubKey(Crypto.KeyType.Ed25519) {

    override fun raw(): ByteArray = pub.encoded

    override fun verify(data: ByteArray, signature: ByteArray): Boolean = with(Ed25519Signer()) {
        init(false, pub)
        update(data, 0, data.size)
        verifySignature(signature)
    }


    override fun hashCode(): Int = pub.hashCode()

}

// GenerateEd25519Key generate a new ed25519 private and public key pair
fun generateEd25519KeyPair(): Pair<PrivKey, PubKey> = with(Ed25519KeyPairGenerator()) {
    init(Ed25519KeyGenerationParameters(SecureRandom()))
    val keypair = generateKeyPair()
    val privateKey = keypair.private as Ed25519PrivateKeyParameters
    Pair(Ed25519PrivateKey(privateKey), Ed25519PublicKey(keypair.public as Ed25519PublicKeyParameters))
}

fun unmarshalEd25519PrivateKey(data: ByteArray): PrivKey = Ed25519PrivateKey(Ed25519PrivateKeyParameters(data, 0))


fun unmarshalEd25519PublicKey(data: ByteArray): PubKey = Ed25519PublicKey(Ed25519PublicKeyParameters(data, 0))
