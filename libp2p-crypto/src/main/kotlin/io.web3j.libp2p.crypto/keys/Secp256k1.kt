package io.web3j.libp2p.crypto.keys

import crypto.pb.Crypto
import io.web3j.libp2p.crypto.Key
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey
import io.web3j.libp2p.crypto.SHA_ALGORITHM
import org.bouncycastle.util.encoders.Hex
import java.security.MessageDigest


// Secp256k1PrivateKey is an secp256k1 private key
class Secp256k1PrivateKey() : PrivKey {
    // k secp256k1.PrivateKey

    override val keyType = Crypto.KeyType.Secp256k1

    override fun raw(): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sign(data: ByteArray): ByteArray {
        val hash = with(MessageDigest.getInstance(SHA_ALGORITHM)) {
            digest(data) // hash := sha256.Sum256(data)
        }

        val sha256hex = String(Hex.encode(hash))


        /*
        	hash := sha256.Sum256(data)
	sig, err := (*btcec.PrivateKey)(k).Sign(hash[:])
	if err != nil {
		return nil, err
	}

	return sig.Serialize(), nil
         */
        return ByteArray(0)
    }

    override fun publicKey(): PubKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

// Secp256k1PublicKey is an secp256k1 public key
class Secp256k1PublicKey : PubKey {
    // 	k secp256k1.PublicKey

    override val keyType = Crypto.KeyType.Secp256k1

    override fun bytes(): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun equals(other: Key): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun raw(): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun verify(data: ByteArray, signature: ByteArray): Boolean {
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