package io.web3j.libp2p.crypto

import crypto.pb.Crypto
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.PrivateKey as javaPrivateKey
import java.security.PublicKey as javaPublicKey


// RsaPrivateKey is an rsa private key
class RsaPrivateKey(private val sk: javaPrivateKey, private val pk: javaPublicKey) : PrivKey {

    private val rsaPublicKey = RsaPublicKey(pk)

    override fun bytes(): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun equals(other: Key): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun raw(): ByteArray {
//        b := x509.MarshalPKCS1PrivateKey(sk.sk)
//        return b, nil
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // http://www.java2s.com/Tutorial/Java/0490__Security/BasicclassforexploringPKCS1V15Signatures.htm
//        val cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC")
//        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPublic())
//
//        val decSig = cipher.doFinal(sigBytes)
//        val aIn = ASN1InputStream(decSig)
//        val seq = aIn.readObject() as ASN1Sequence
//
//        println(ASN1Dump.dumpAsString(seq))
//
//        val hash = MessageDigest.getInstance("SHA-256", "BC")
//        hash.update(message)
//
//        val sigHash = seq.getObjectAt(1) as ASN1OctetString
//        println(MessageDigest.isEqual(hash.digest(), sigHash.octets))

    override fun type(): Crypto.KeyType {
        return Crypto.KeyType.RSA
    }

    override fun sign(data: ByteArray): ByteArray {
        val signature = Signature.getInstance("SHA256withRSA", Libp2pCrypto.provider)
        signature.initSign(sk)
        signature.update(data)
        return signature.sign()
    }

    override fun publicKey(): PubKey {
        return rsaPublicKey
    }

}


// RsaPublicKey is an rsa public key
class RsaPublicKey(private val k: javaPublicKey) : PubKey {
    override fun bytes(): ByteArray {
//        return MarshalPublicKey(pk)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun equals(other: Key): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun raw(): ByteArray {
        // return x509.MarshalPKIXPublicKey(pk.k)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun type(): Crypto.KeyType {
        return Crypto.KeyType.RSA
    }

    override fun verify(data: ByteArray, signature: ByteArray): Boolean {
        val signature1 = Signature.getInstance("SHA256withRSA", Libp2pCrypto.provider)
        signature1.initVerify(k)
        signature1.update(data)
        return signature1.verify(signature)
    }

}


/**
 * GenerateRSAKeyPair generates a new rsa private and public key.
 */
fun generateRsaKeyPair(bits: Int): Pair<PrivKey, PubKey> {

    if (bits < 512) {
        throw Libp2pException(ErrRsaKeyTooSmall)
    }


    val kp: KeyPair = with(KeyPairGenerator.getInstance(RSA_ALGORITHM, Libp2pCrypto.provider)) {
        initialize(bits)
        genKeyPair()
    }

    return Pair(RsaPrivateKey(kp.private, kp.public), RsaPublicKey(kp.public))
}