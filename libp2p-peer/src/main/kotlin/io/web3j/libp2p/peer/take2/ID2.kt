package io.web3j.libp2p.peer.take2

import io.ipfs.multiformats.multihash.DecodedMultihash
import io.ipfs.multiformats.multihash.Multihash
import io.ipfs.multiformats.multihash.Type
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey
import io.web3j.libp2p.crypto.unmarshalPublicKey
import io.web3j.libp2p.peer.NoPublicKeyException
import org.apache.commons.codec.binary.Hex
import org.kethereum.encodings.decodeBase58
import org.kethereum.encodings.encodeToBase58String

// ID is a libp2p peer identity.
// type ID string
class ID2 {

    // private lateinit var _id: String
    private lateinit var _bytes: ByteArray

    constructor(b: ByteArray) {
        _bytes = b
    }

    constructor(id: String) : this(id.toByteArray())


    constructor(m: Multihash) : this(m.raw)


    // Pretty returns a b58-encoded string of the ID
    fun Pretty(): String = IDB58Encode(_bytes)

    // Validate check if ID is empty or not
    fun Validate(): Boolean = _bytes.size > 0 //.isNotEmpty()

    // IDHexEncode returns hex-encoded string
    fun IDHexEncode(): String {
        // return hex.EncodeToString([]byte(id))
        return Hex.encodeHexString(_bytes)
    }

    // MatchesPrivateKey tests whether this ID was derived from sk
    fun MatchesPrivateKey(sk: PrivKey): Boolean {
        return MatchesPublicKey(sk.publicKey())
    }

    // MatchesPublicKey tests whether this ID was derived from pk
    fun MatchesPublicKey(pk: PubKey): Boolean {
        val otherId = IDFromPublicKey(pk)
        return otherId == this
    }

    // ExtractPublicKey attempts to extract the public key from an ID
    //
    // This method returns ErrNoPublicKey if the peer ID looks valid but it can't extract
    // the public key.
    fun ExtractPublicKey(): PubKey {
        val decoded: DecodedMultihash = Multihash.decode(_bytes)

        if (decoded.code != Type.ID.code) {
            throw NoPublicKeyException()
        }

        return unmarshalPublicKey(decoded.digest)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return this._bytes.contentEquals((other as ID2)._bytes)
    }

    /**
     * String prints out the peer.
     *
     * TODO(brian): ensure correctness at ID generation and
     * enforce this by only exposing functions that generate
     * IDs safely. Then any peer.ID type found in the
     * codebase is known to be correct.
     */
    override fun toString(): String {
        val pid = Pretty()
        return if (pid.length <= 10) {
            "<peer.ID $pid>"
        } else {
            "<peer.ID ${pid.subSequence(0, 2)}*${pid.subSequence(pid.length - 6, pid.length)}"
        }
    }

    companion object {

        // IDB58Encode returns b58-encoded string
        fun IDB58Encode(id: String): String {
            // return b58.Encode([] byte (id))
            return IDB58Encode(id.toByteArray())
        }

        fun IDB58Encode(bytes: ByteArray): String {
            // return b58.Encode([] byte (id))
            return bytes.encodeToBase58String()
        }

        // IDFromString cast a string to ID type, and validate
        // the id to make sure it is a multihash.
        fun IDFromString(s: String): ID2 {
            Multihash.cast(s.toByteArray())
            return ID2(s)
        }

        // IDFromPrivateKey returns the Peer ID corresponding to sk
        fun IDFromPrivateKey(sk: PrivKey): ID2 {
            return IDFromPublicKey(sk.publicKey())
        }

        // IDFromPublicKey returns the Peer ID corresponding to pk
        fun IDFromPublicKey(pk: PubKey): ID2 {
            val b = pk.bytes()
            val hash: ByteArray = Multihash.encode(b, Type.SHA2_256.code)
            return ID2(hash)
        }

        // IDFromBytes cast a string to ID type, and validate
        // the id to make sure it is a multihash.
        fun IDFromBytes(b: ByteArray): ID2 {
            Multihash.cast(b)
            return ID2(b)
        }


        // IDB58Decode returns a b58-decoded Peer
        fun IDB58Decode(s: String): ID2 {
            return ID2(s.decodeBase58())
        }

        // IDHexDecode returns a hex-decoded Peer
        fun IDHexDecode(s: String): ID2 {
            val m: Multihash = Multihash.fromHexString(s)
            return ID2(m)
        }

    }

}