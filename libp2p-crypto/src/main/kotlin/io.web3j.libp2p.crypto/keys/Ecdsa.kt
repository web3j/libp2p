package io.web3j.libp2p.crypto.keys

import crypto.pb.Crypto
import io.web3j.libp2p.crypto.Key
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey

// EcdsaPrivateKey is an implementation of an ecdsa private key
class EcdsaPrivateKey() : PrivKey {
    // k ecdsa.PrivateKey

    override fun bytes(): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun equals(other: Key): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun raw(): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun type(): Crypto.KeyType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sign(data: ByteArray): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun publicKey(): PubKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

// EcdsaPublicKey is an implementation of an ecdsa public key
class EcdsaPublicKey : PubKey {
    // 	k ecdsa.PublicKey

    override fun bytes(): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun equals(other: Key): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun raw(): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun type(): Crypto.KeyType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun verify(data: ByteArray, signature: ByteArray): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

// GenerateEcdsaKey generate a new ecdsa private and public key pair
fun generateEcdsaKeyPair(): Pair<PrivKey, PubKey> {
    return Pair(EcdsaPrivateKey(), EcdsaPublicKey())

    /*

priv, err := ecdsa.GenerateKey(curve, src)
	if err != nil {
		return nil, nil, err
	}

	return &ECDSAPrivateKey{priv}, &ECDSAPublicKey{&priv.PublicKey}, nil
     */
}

fun unmarshalEcdsaPrivateKey(data: ByteArray): PrivKey = TODO()

fun unmarshalEcdsaPublicKey(data: ByteArray): PubKey = TODO()