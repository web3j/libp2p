package io.web3j.libp2p.crypto

enum class KEY_TYPE {
    /**
     * RSA is an enum for the supported RSA key type
     */
    RSA,

    /**
     * Ed25519 is an enum for the supported Ed25519 key type
     */
    ED25519,

    /**
     * Secp256k1 is an enum for the supported Secp256k1 key type
     */
    SECP256K1,

    /**
     * ECDSA is an enum for the supported ECDSA key type
     */
    ECDSA
}

interface Key {

    /**
     * Bytes returns a serialized, storeable representation of this key.
     */
    @Deprecated("Use marshal/unmarshal functions instead")
    fun bytes(): ByteArray

    /**
     * Equals checks whether two PubKeys are the same.
     */
    fun equals(other: Key): Boolean

    fun raw(): ByteArray

    fun type(): crypto.pb.Crypto.KeyType
}

/**
 * PrivKey represents a private key that can be used to generate a public key,
 * sign data, and decrypt data that was encrypted with a public key.
 */
interface PrivKey : Key {

    /**
     * Cryptographically sign the given bytes.
     */
    fun sign(data: ByteArray) : ByteArray

    /**
     * Return a public key paired with this private key.
     */
    fun publicKey(): PubKey
}

/**
 * PubKey is a public key.
 */
interface PubKey : Key {

    /**
     * Verify that 'sig' is the signed hash of 'data'.
     */
    fun verify(data: ByteArray, signature: ByteArray) : Boolean
}

/**
 * Creates a PubKey from a given byte array.
 */
interface PublicKeyMarshaller {
    fun unmarshall(data: ByteArray): PubKey
}

/**
 * Creates a private key from a given byte array.
 */
interface PrivateKeyMarshaller {
    fun unmarshall(data: ByteArray): PrivKey
}

/**
 * Generates shared key from a given private key.
 */
interface GenSharedKey : (ByteArray) -> ByteArray

class BadKeyTypeException : Exception("Invalid or unsupported key type")

/**
 * Generate a new key pair of the provided type.
 */
fun generateKeyPair(type: KEY_TYPE, bits: Int): Pair<PrivKey, PubKey> {

    return when (type) {
        KEY_TYPE.RSA -> generateRsaKeyPair(bits)
        KEY_TYPE.ED25519 -> generateEd25519KeyPair()
        KEY_TYPE.SECP256K1 -> generateSecp256k1KeyPair()
        KEY_TYPE.ECDSA -> generateEcdsaKeyPair()
    }
    /*
    	switch typ {
	case RSA:
		return GenerateRSAKeyPair(bits, src)
	case Ed25519:
		return GenerateEd25519Key(src)
	case Secp256k1:
		return GenerateSecp256k1Key(src)
	case ECDSA:
		return GenerateECDSAKeyPair(src)
	default:
		return nil, nil, ErrBadKeyType
	}
     */
}

/**
 * UnmarshalPublicKey converts a protobuf serialized public key into its
 * representative object
 */
fun unmarshalPublicKey(data: ByteArray): PubKey = TODO()

/**
 * MarshalPublicKey converts a public key object into a protobuf serialized
 * public key
 */
fun marshalPublicKey(pubKey: PubKey): ByteArray = TODO()

/**
 * UnmarshalPrivateKey converts a protobuf serialized private key into its
 * representative object
 */
fun unmarshalPrivateKey(data: ByteArray): PrivKey = TODO()

/**
 * MarshalPrivateKey converts a public key object into a protobuf serialized
 * private key
 */
fun marshalPrivateKey(privKey: PrivKey): ByteArray = TODO()


fun generateSecp256k1KeyPair(): Pair<PrivKey, PubKey> = TODO()
fun generateEcdsaKeyPair(): Pair<PrivKey, PubKey> = TODO()
