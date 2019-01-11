package io.web3j.libp2p.crypto.keys

import crypto.pb.Crypto
import io.web3j.libp2p.crypto.*
import io.web3j.libp2p.crypto.Key
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.security.PrivateKey as JavaPrivateKey
import java.security.interfaces.ECPrivateKey as JavaECPrivateKey


private val CURVE: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec(P256_CURVE)

/**
 * EcdsaPrivateKey is an implementation of an ecdsa private key
 */
class EcdsaPrivateKey(private val priv: JavaPrivateKey) : PrivKey {

    val pkcs1PrivateKeyBytes: ByteArray

    init {
        // Set up private key.
        val isKeyOfFormat: Boolean = priv.format?.equals(KEY_PKCS8) ?: false
        if (!isKeyOfFormat) {
            throw Libp2pException("Private key must be of '$KEY_PKCS8' format")
        }

        val bcPrivateKeyInfo = PrivateKeyInfo.getInstance(priv.encoded)
        pkcs1PrivateKeyBytes = bcPrivateKeyInfo.parsePrivateKey().toASN1Primitive().encoded
    }

    override fun bytes(): ByteArray = marshalPrivateKey(this)

    override fun equals(other: Key): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EcdsaPrivateKey

        return bytes().contentEquals(other.bytes())
    }

    override fun raw(): ByteArray = priv.encoded

    override fun type(): Crypto.KeyType = Crypto.KeyType.ECDSA

    /**
     * Sign returns the signature of the input data.
     */
    override fun sign(data: ByteArray): ByteArray =
        with(Signature.getInstance(SHA_256_WITH_ECDSA, Libp2pCrypto.provider)) {
            // Signature is made up of r and s numbers.
            initSign(priv)
            update(data)
            sign()
        }

    override fun publicKey(): PubKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

// EcdsaPublicKey is an implementation of an ecdsa public key
class EcdsaPublicKey(private val pub: PublicKey) : PubKey {

    override fun bytes(): ByteArray = marshalPublicKey(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EcdsaPublicKey

        return bytes().contentEquals(other.bytes())
    }

    override fun hashCode(): Int = pub.hashCode()

    override fun raw(): ByteArray = pub.encoded

    override fun type(): Crypto.KeyType = Crypto.KeyType.ECDSA

    override fun verify(data: ByteArray, signature: ByteArray): Boolean =
        with(Signature.getInstance(SHA_256_WITH_ECDSA, Libp2pCrypto.provider)) {
            initVerify(pub)
            update(data)
            verify(signature)
        }

}

/**
 * GenerateECDSAKeyPairWithCurve generates a new ecdsa private and public key with a specified curve.
 */
private fun generateECDSAKeyPairWithCurve(curve: ECNamedCurveParameterSpec): Pair<PrivKey, PubKey> {
    val keypair: KeyPair = with(KeyPairGenerator.getInstance(ECDSA_ALGORITHM, Libp2pCrypto.provider)) {
        initialize(curve, SecureRandom())
        genKeyPair()
    }

    return Pair(EcdsaPrivateKey(keypair.private as JavaECPrivateKey), EcdsaPublicKey(keypair.public))
}

/**
 * GenerateEcdsaKey generate a new ecdsa private and public key pair
 */
fun generateEcdsaKeyPair(): Pair<PrivKey, PubKey> {
    // http://www.bouncycastle.org/wiki/display/JA1/Supported+Curves+%28ECDSA+and+ECGOST%29
    // and
    // http://www.bouncycastle.org/wiki/pages/viewpage.action?pageId=362269
    return generateECDSAKeyPairWithCurve(CURVE)
}

/**
 * ECDSAKeyPairFromKey generates a new ecdsa private and public key from an input private key.
 */
fun ecdsaKeyPairFromKey(priv: EcdsaPrivateKey): Pair<PrivKey, PubKey> = Pair(priv, priv.publicKey())

/**
 * UnmarshalECDSAPrivateKey returns a private key from x509 bytes.
 */
fun unmarshalEcdsaPrivateKey(data: ByteArray): PrivKey = EcdsaPrivateKey(
    KeyFactory.getInstance(ECDSA_ALGORITHM, Libp2pCrypto.provider).generatePrivate(
        PKCS8EncodedKeySpec(data)
    )
)

fun unmarshalEcdsaPublicKey(keyBytes: ByteArray): PubKey {
    return with(KeyFactory.getInstance(ECDSA_ALGORITHM, Libp2pCrypto.provider)) {
        EcdsaPublicKey(generatePublic(X509EncodedKeySpec(keyBytes)))
    }
}