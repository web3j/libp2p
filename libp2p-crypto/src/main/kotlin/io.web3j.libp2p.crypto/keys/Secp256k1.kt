package io.web3j.libp2p.crypto.keys

import crypto.pb.Crypto
import io.web3j.libp2p.crypto.Libp2pException
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey
import io.web3j.libp2p.crypto.SECP_256K1_ALGORITHM
import org.bouncycastle.asn1.*
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.generators.ECKeyPairGenerator
import org.bouncycastle.crypto.params.*
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import org.bouncycastle.math.ec.FixedPointUtil
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.SecureRandom

// The parameters of the secp256k1 curve that Bitcoin uses.
private val CURVE_PARAMS = CustomNamedCurves.getByName(SECP_256K1_ALGORITHM)

private val CURVE: ECDomainParameters = CURVE_PARAMS.let {
    FixedPointUtil.precompute(CURVE_PARAMS.g)
    ECDomainParameters(CURVE_PARAMS.curve, CURVE_PARAMS.g, CURVE_PARAMS.n, CURVE_PARAMS.h)
}


// Secp256k1PrivateKey is an secp256k1 private key
class Secp256k1PrivateKey(private val privateKey: ECPrivateKeyParameters) : PrivKey() {

    private val priv = privateKey.d

    override val keyType = Crypto.KeyType.Secp256k1

    override fun raw(): ByteArray = priv.toByteArray()

    override fun sign(data: ByteArray): ByteArray {
        val (r, s) = with(ECDSASigner()) {
            init(true, ParametersWithRandom(privateKey, SecureRandom()))
            generateSignature(data).let {
                Pair(it[0], it[1])
            }
        }


        return with(ByteArrayOutputStream()) {
            DERSequenceGenerator(this).run {
                addObject(ASN1Integer(r))
                addObject(ASN1Integer(s))
                close()
                toByteArray()
            }
        }
    }

    override fun publicKey(): PubKey {
        val privKey = if (priv.bitLength() > CURVE.n.bitLength()) priv.mod(CURVE.n) else priv
        val publicPoint = FixedPointCombMultiplier().multiply(CURVE.g, privKey)
        return Secp256k1PublicKey(ECPublicKeyParameters(publicPoint, CURVE))
    }

    override fun hashCode(): Int = priv.hashCode()

}

// Secp256k1PublicKey is an secp256k1 public key
class Secp256k1PublicKey(private val pub: ECPublicKeyParameters) : PubKey() {

    override val keyType = Crypto.KeyType.Secp256k1

    override fun raw(): ByteArray = pub.q.getEncoded(true)

    override fun verify(data: ByteArray, signature: ByteArray): Boolean {
        val signer = ECDSASigner()
        val params = SECNamedCurves.getByName(SECP_256K1_ALGORITHM)
        val ecParams = ECDomainParameters(params.curve, params.g, params.n, params.h)
        signer.init(false, pub)

        val asn1: ASN1Primitive =
            ByteArrayInputStream(signature).use { inStream -> ASN1InputStream(inStream).use { asnInputStream -> asnInputStream.readObject() } }

        val asn1Encodables = (asn1 as ASN1Sequence).toArray()
        if (asn1Encodables.size != 2) {
            throw Libp2pException("Invalid signature: expected 2 values for 'r' and 's' but got ${asn1Encodables.size}")
        }

        val r = (asn1Encodables[0].toASN1Primitive() as ASN1Integer).value
        val s = (asn1Encodables[1].toASN1Primitive() as ASN1Integer).value
        return signer.verifySignature(data, r.abs(), s.abs())
    }

    override fun hashCode(): Int = pub.hashCode()

}

// GenerateSecp256k1Key generate a new secp256k1 private and public key pair
fun generateSecp256k1KeyPair(): Pair<PrivKey, PubKey> = with(ECKeyPairGenerator()) {
    val domain = SECNamedCurves.getByName(SECP_256K1_ALGORITHM).let {
        ECDomainParameters(it.curve, it.g, it.n, it.h)
    }
    init(ECKeyGenerationParameters(domain, SecureRandom()))
    val keypair = generateKeyPair()

    val privateKey = keypair.private as ECPrivateKeyParameters
    return Pair(Secp256k1PrivateKey(privateKey), Secp256k1PublicKey(keypair.public as ECPublicKeyParameters))
}

fun unmarshalSecp256k1PrivateKey(data: ByteArray): PrivKey =
    Secp256k1PrivateKey(ECPrivateKeyParameters(BigInteger(1, data), CURVE))

fun unmarshalSecp256k1PublicKey(data: ByteArray): PubKey =
    Secp256k1PublicKey(ECPublicKeyParameters(CURVE.curve.decodePoint(data), CURVE))
