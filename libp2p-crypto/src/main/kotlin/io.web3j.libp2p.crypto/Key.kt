
enum KEY_TYPE {
    /**
     * RSA is an enum for the supported RSA key type
     */
    RSA,

    /**
     * Ed25519 is an enum for the supported Ed25519 key type
     */
    Ed25519,

    /**
     * Secp256k1 is an enum for the supported Secp256k1 key type
     */
    Secp256k1,

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
    fun bytes(): Byte[]

    /**
     * Equals checks whether two PubKeys are the same.
     */
    fun equals(other: Key): Boolean

    fun raw(): Byte[]

    fun type()
}