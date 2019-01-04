package io.web3j.libp2p.crypto.keys

import crypto.pb.Crypto
import io.web3j.libp2p.crypto.Key
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey

// Secp256k1PrivateKey is an secp256k1 private key
class Secp256k1PrivateKey() : PrivKey {
    // k secp256k1.PrivateKey

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

    override fun sign(data: ByteArray) : ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun publicKey(): PubKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

// Secp256k1PublicKey is an secp256k1 public key
class Secp256k1PublicKey : PubKey {
    // 	k secp256k1.PublicKey

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

    override fun verify(data: ByteArray, signature: ByteArray) : Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

// GenerateSecp256k1Key generate a new secp256k1 private and public key pair
fun generateSecp256k1KeyPair(): Pair<PrivKey, PubKey> {
    return Pair(Secp256k1PrivateKey(), Secp256k1PublicKey())

    /*
	privk, err := btcec.NewPrivateKey(btcec.S256())
	if err != nil {
		return nil, nil, err
	}

	k := (*Secp256k1PrivateKey)(privk)
	return k, k.GetPublic(), nil
     */
}

fun unmarshalSecp256k1PrivateKey(data: ByteArray): PrivKey = TODO()

fun unmarshalSecp256k1PublicKey(data: ByteArray): PubKey = TODO()