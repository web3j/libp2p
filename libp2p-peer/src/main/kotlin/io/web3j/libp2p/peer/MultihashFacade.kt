package io.web3j.libp2p.peer

import io.ipfs.multiformats.multihash.Multihash
import org.kethereum.encodings.decodeBase58

class MultihashFacade {


    companion object {

        fun decodeBase58Multihash(value: String): ByteArray {
            val decodedHashedBytes: ByteArray = value.decodeBase58()
            return Multihash.decode(decodedHashedBytes).digest
        }
    }
}