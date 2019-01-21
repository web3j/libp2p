package io.web3j.libp2p.crypto.keys

import crypto.pb.Crypto
import io.web3j.libp2p.crypto.ErrRsaKeyTooSmall
import io.web3j.libp2p.crypto.KEY_PKCS8
import io.web3j.libp2p.crypto.Libp2pCrypto
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey
import io.web3j.libp2p.crypto.RSA_ALGORITHM
import io.web3j.libp2p.crypto.SHA_256_WITH_RSA
import io.web3j.libp2p.shared.env.Libp2pException
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.pkcs.RSAPrivateKey
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.security.PrivateKey as JavaPrivateKey
import java.security.PublicKey as JavaPublicKey

class RsaPrivateKey(private val sk: JavaPrivateKey, private val pk: JavaPublicKey) : PrivKey(Crypto.KeyType.RSA) {

    /*
     * PKCS#8 and PKCS#1 are standards that define how private keys are to be stored; java stores its RSA keys using
     * the PKCS#8 format and  older libraries use PKCS1.
     * It is important to note that PKCS#8 includes the data payload in PKCS1 format alongside some additional values.
     */

    private val rsaPublicKey = RsaPublicKey(pk)
    private val pkcs1PrivateKeyBytes: ByteArray

    init {
        // Set up private key.
        val isKeyOfFormat: Boolean = sk.format?.equals(KEY_PKCS8) ?: false
        if (!isKeyOfFormat) {
            throw Libp2pException("Private key must be of '$KEY_PKCS8' format")
        }

        val bcPrivateKeyInfo = PrivateKeyInfo.getInstance(sk.encoded)
        pkcs1PrivateKeyBytes = bcPrivateKeyInfo.parsePrivateKey().toASN1Primitive().encoded
    }

    override fun raw(): ByteArray = pkcs1PrivateKeyBytes

    override fun sign(data: ByteArray): ByteArray =
        with(Signature.getInstance(SHA_256_WITH_RSA, Libp2pCrypto.provider)) {
            initSign(sk)
            update(data)
            sign()
        }

    override fun publicKey(): PubKey = rsaPublicKey

    override fun hashCode(): Int = pk.hashCode()
}

// RsaPublicKey is an rsa public key
class RsaPublicKey(private val k: JavaPublicKey) : PubKey(Crypto.KeyType.RSA) {

    override fun raw(): ByteArray = k.encoded

    override fun verify(data: ByteArray, signature: ByteArray): Boolean =
        with(Signature.getInstance(SHA_256_WITH_RSA, Libp2pCrypto.provider)) {
            initVerify(k)
            update(data)
            verify(signature)
        }

    override fun hashCode(): Int = k.hashCode()
}

/**
 * GenerateRSAKeyPair generates a new rsa private and public key.
 */
fun generateRsaKeyPair(bits: Int): Pair<PrivKey, PubKey> {
    if (bits < 512) {
        throw Libp2pException(ErrRsaKeyTooSmall)
    }

    val kp: KeyPair = with(
        KeyPairGenerator.getInstance(
            RSA_ALGORITHM,
            Libp2pCrypto.provider
        )
    ) {
        initialize(bits)
        genKeyPair()
    }

    return Pair(
        RsaPrivateKey(kp.private, kp.public),
        RsaPublicKey(kp.public)
    )
}

/**
 * UnmarshalRsaPublicKey returns a public key from the input x509 bytes
 */
fun unmarshalRsaPublicKey(data: ByteArray): PubKey =
    RsaPublicKey(KeyFactory.getInstance(RSA_ALGORITHM, Libp2pCrypto.provider).generatePublic(X509EncodedKeySpec(data)))

/**
 * Converts the given private key (in PKCS1 format) to a PKCS8 key.
 */
fun unmarshalRsaPrivateKey(data: ByteArray): PrivKey {
    // Input is ASN1 DER encoded PKCS1 private key bytes.
    val rsaPrivateKey = RSAPrivateKey.getInstance(ASN1Primitive.fromByteArray(data))
    val privateKeyParameters = RSAPrivateCrtKeyParameters(
        rsaPrivateKey.modulus,
        rsaPrivateKey.publicExponent,
        rsaPrivateKey.privateExponent,
        rsaPrivateKey.prime1,
        rsaPrivateKey.prime2,
        rsaPrivateKey.exponent1,
        rsaPrivateKey.exponent2,
        rsaPrivateKey.coefficient
    )

    // Now convert to a PKSC#8 key.
    val privateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(privateKeyParameters)
    val algorithmId = privateKeyInfo.privateKeyAlgorithm.algorithm.id
    val spec = PKCS8EncodedKeySpec(privateKeyInfo.encoded)
    val sk = KeyFactory.getInstance(algorithmId, Libp2pCrypto.provider).generatePrivate(spec)

    // We can extract the public key from the modulus and exponent of the private key. Woot!
    val publicKeySpec = RSAPublicKeySpec(privateKeyParameters.modulus, privateKeyParameters.publicExponent)
    val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
    val pk = keyFactory.generatePublic(publicKeySpec)

    return RsaPrivateKey(sk, pk)
}
