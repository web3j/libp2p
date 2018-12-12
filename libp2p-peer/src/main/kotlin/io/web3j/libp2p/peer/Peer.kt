package io.web3j.libp2p.peer

import java.lang.Exception

/**
 * Empty peer ID exception.
 */
class EmptyPeerIdException : Exception("empty peer ID")

/**
 * Exception for peer IDs that don't embed public keys.
 */
class NoPublicKeyException : Exception("public key is not embedded in peer ID")


/**
 * ID is a libp2p peer identity.
 */
data class ID(val id: String) {

    /**
     * Pretty returns a b58-encoded string of the ID
     */
    fun pretty(): String {
        return IDB58Encode(id)
    }

    /**
     * Loggable returns a pretty peerID string in loggable JSON format
     */
    fun loggable(): Map<String, String> {
        return mapOf("peerID" to pretty())
    }

    /**
     * String prints out the peer.
     *
     * TODO(brian): ensure correctness at ID generation and
     * enforce this by only exposing functions that generate
     * IDs safely. Then any peer.ID type found in the
     * codebase is known to be correct.
     */
    @Override
    fun toString(): String {
        val pid = pretty()
        if (pid.length <= 10) {
            return "<peer.ID $pid>"
        } else {
            return "<peer.ID ${pid.subSequence(0, 2)}*${pid.subSequence(pid.length-6, pid.length)}"
        }
    }

    /**
     * MatchesPrivateKey tests whether this ID was derived from shared-key
     */
    fun matchesPrivateKey(sharedKey: PrivKey): Boolean {
        return matchesPrivateKey(sharedKey.publicKey)
    }

    /**
     * MatchesPublicKey tests whether this ID was derived from pk
     */
    fun matchesPublickKey(publicKey: PubKey): Boolean {
        val otherId = idFromPublickKey(publicKey)
        // TODO: Check no error
        return otherId == this
    }

    /**
     * ExtractPublicKey attempts to extract the public key from an ID
     *
     * This method returns ErrNoPublicKey if the peer ID looks valid but it can't extract
     * the public key.
     */
    fun extractPublicKey() {

    }

    fun validate() {
        if (id.isNullOrEmpty()) {
            throw EmptyPeerIdException()
        }
    }

    fun IDB58Encode(): String {
        Base58
    }
}



func (id ID) ExtractPublicKey() (ic.PubKey, error) {
    decoded, err := mh.Decode([]byte(id))
    if err != nil {
        return nil, err
    }
    if decoded.Code != mh.ID {
        return nil, ErrNoPublicKey
    }
    pk, err := ic.UnmarshalPublicKey(decoded.Digest)
    if err != nil {
        return nil, err
    }
    return pk, nil
}


// IDFromString cast a string to ID type, and validate
// the id to make sure it is a multihash.
func IDFromString(s string) (ID, error) {
    if _, err := mh.Cast([]byte(s)); err != nil {
        return ID(""), err
    }
    return ID(s), nil
}

// IDFromBytes cast a string to ID type, and validate
// the id to make sure it is a multihash.
func IDFromBytes(b []byte) (ID, error) {
    if _, err := mh.Cast(b); err != nil {
        return ID(""), err
    }
    return ID(b), nil
}

// IDB58Decode returns a b58-decoded Peer
func IDB58Decode(s string) (ID, error) {
    m, err := mh.FromB58String(s)
    if err != nil {
        return "", err
    }
    return ID(m), err
}

// IDB58Encode returns b58-encoded string
func IDB58Encode(id ID) string {
    return b58.Encode([]byte(id))
}

// IDHexDecode returns a hex-decoded Peer
func IDHexDecode(s string) (ID, error) {
    m, err := mh.FromHexString(s)
    if err != nil {
        return "", err
    }
    return ID(m), err
}

// IDHexEncode returns hex-encoded string
func IDHexEncode(id ID) string {
    return hex.EncodeToString([]byte(id))
}

// IDFromPublicKey returns the Peer ID corresponding to pk
func IDFromPublicKey(pk ic.PubKey) (ID, error) {
    b, err := pk.Bytes()
    if err != nil {
        return "", err
    }
    hash, _ := mh.Sum(b, mh.SHA2_256, -1)
    return ID(hash), nil
}

// IDFromPrivateKey returns the Peer ID corresponding to sk
func IDFromPrivateKey(sk ic.PrivKey) (ID, error) {
    return IDFromPublicKey(sk.GetPublic())
}

/**
 * IDSlice for sorting peers
 */
class IDSlice(val ids: MutableList<ID>) {
    fun len(): Int = ids.size

    fun swap(i: Int, j: Int) {
        val tmp = ids[i]
        ids[i] = ids[j]
        ids[j] = tmp
    }

    fun less(i: Int, j: Int): Boolean = ids[i].id < ids[j].id
}