package io.web3j.libp2p.crypto

import crypto.pb.Crypto

// Ed25519PrivateKey is an ed25519 private key
class Ed25519PrivateKey() : PrivKey {
    // k ed25519.PrivateKey

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

// Ed25519PublicKey is an ed25519 public key
class Ed25519PublicKey : PubKey {
    // 	k ed25519.PublicKey

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

// GenerateEd25519Key generate a new ed25519 private and public key pair
fun generateEd25519KeyPair(): Pair<PrivKey, PubKey> {
    return Pair(Ed25519PrivateKey(), Ed25519PublicKey())

    /*

func GenerateEd25519Key(src io.Reader) (PrivKey, PubKey, error) {
	pub, priv, err := ed25519.GenerateKey(src)
	if err != nil {
		return nil, nil, err
	}

	return &Ed25519PrivateKey{
			k: priv,
		},
		&Ed25519PublicKey{
			k: pub,
		},
		nil
}
     */
}