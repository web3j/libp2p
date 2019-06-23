/*
 * Copyright 2019 BLK Technologies Limited (web3labs.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.web3j.libp2p.transport.tcp.provider.netty

import com.google.protobuf.ByteString
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.web3j.libp2p.crypto.Libp2pCrypto
import io.web3j.libp2p.crypto.keys.RsaPrivateKey
import io.web3j.libp2p.crypto.unmarshalPrivateKey
import io.web3j.libp2p.crypto.unmarshalPublicKey
import io.web3j.libp2p.security.secio.model.ExchangeMessage
import io.web3j.libp2p.security.secio.model.ProposeMessage
import io.web3j.libp2p.security.secio.util.SecioUtil
import io.web3j.libp2p.security.secio.util.CipherFactory
import io.web3j.libp2p.security.secio.util.CipherUtil
import io.web3j.libp2p.security.secio.util.HashFactory
import io.web3j.libp2p.security.secio.util.HashUtil
import io.web3j.libp2p.security.secio.util.KeyUtil
import io.web3j.libp2p.security.secio.util.PrototypeUtil
import io.web3j.libp2p.shared.env.Libp2pException
import io.web3j.libp2p.shared.ext.readVarintPrefixedMessage
import io.web3j.libp2p.shared.ext.toVarintPrefixedByteArray
import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.slf4j.LoggerFactory
import spipe.pb.Spipe
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class AccumulatorInboundHandler : ChannelInboundHandlerAdapter() {

    val ALL_CIPHERS = arrayOf("AES-256", "AES-128")
    val ALL_HASHES = arrayOf("SHA256", "SHA512")
    val ALL_EXCHANGES = arrayOf("P-256", "P-384", "P-521")

    val nonceSize = 16

    var receiveStep = 1

    val AES_CTR_CIPHER = "AES/CTR/NoPadding"

    val ourPrivateKeyB64 =
        "CAASpwkwggSjAgEAAoIBAQCaNSDOjPz6T8HZsf7LDpxiQRiN2OjeyIHUS05p8QWOr3EFUCFsC31R4moihE5HN+FxNalUyyFZU//yjf1pdn" +
                "lMJqrVByJSMa+y2y4x2FucpoCAO97Tx+iWzwlZ2UXEUXM1Y81mhPbeWXy+wP2xElTgIER0Tsn/thoA0SD2u9wJuVvM7dB7cBc" +
                "HYmqV6JH+KWCedRTum6O1BssqP/4Lbm2+rkrbZ4+oVRoU2DRLoFhKqwqLtylrbuj4XOI3XykMXV5+uQXz1JzubNOB9lsc6K+e" +
                "RC+w8hhhDuFMgzkZ4qomCnx3uhO67KaICd8yqqBa6PJ/+fBM5Xk4hjyR40bwcf41AgMBAAECggEAZnrCJ6IYiLyyRdr9SbKXC" +
                "NDb4YByGYPEi/HT1aHgIJfFE1PSMjxcdytxfyjP4JJpVtPjiT9JFVU2ddoYu5qJN6tGwjVwgJEWg1UXmPaAw1T/drjS94kVsA" +
                "s82qICtFmwp52Apg3dBZ0Qwq/8qE1XbG7lLyohIbfCBiL0tiPYMfkcsN9gnFT/kFCX0LVs2pa9fHCRMY9rqCc4/rWJa1w8sMu" +
                "Q23y4lDaxKF9OZVvOHFQkbBDrkquWHE4r55fchCz/rJklkPJUNENuncBRu0/2X+p4IKFD1DnttXNwb8j4LPiSlLro1T0hiUr5" +
                "gO2QmdYwXFF63Q3mjQy0+5I4eNbjjQKBgQDZvZy3gUKS/nQNkYfq9za80uLbIj/cWbO+ZZjXCsj0fNIcQFJcKMBoA7DjJvu2" +
                "S/lf86/41YHkPdmrLAEQAkJ+5BBNOycjYK9minTEjIMMmZDTXXugZ62wnU6F46uLkgEChTqEP57Y6xwwV+JaEDFEsW5N1eE9" +
                "lEVX9nGIr4phMwKBgQC1TazLuEt1WBx/iUT83ita7obXqoKNzwsS/MWfY2innzYZKDOqeSYZzLtt9uTtp4X4uLyPbYs0qFYh" +
                "XLsUYMoGHNN8+NdjoyxCjQRJRBkMtaNR0lc5lVDWl3bTuJovjFCgAr9uqJrmI5OHcCIk/cDpdWb3nWaMihVlePmiTcTy9wKBg" +
                "QCU0u7c1jKkudqks4XM6a+2HAYGdUBk4cLjLhnrUWnNAcuyl5wzdX8dGPi8KZb+IKuQE8WBNJ2VXVj7kBYh1QmSJVunDflQSvNY" +
                "COaKuOeRoxzD+y9Wkca74qkbBmPn/6FFEb7PSZTO+tPHjyodGNgz9XpJJRjQuBk1aDJtlF3m1QKBgE5SAr5ym65SZOU3UGUIOKRs" +
                "fDW4Q/OsqDUImvpywCgBICaX9lHDShFFHwau7FA52ScL7vDquoMB4UtCOtLfyQYA9995w9oYCCurrVlVIJkb8jSLcADBHw3EmqF" +
                "1kq" +
                "3NqJqm9TmBfoDCh52vdCCUufxgKh33kfBOSlXuf7B8dgMbAoGAZ3r0/m" +
                "BQX6S+s5+xCETMTSNv7TQzxgtURIpVs+ZVr2cMhWhiv+n" +
                "0Omab9X9Z50se8cWl5lkvx8vn3D/XHHIPrMF6qk7RAXtvReb+PeitNvm0odqjFv0J2qki6fDs0HKwq4kojAXI1Md8Th0eobNjsy" +
                "21fEEJT7uKMJdovI/SErI="
    val ourPublicKeyB64 =
        "CAASpgIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCaNSDOjPz6T8HZsf7LDpxiQRiN2OjeyIHUS05p8QWOr3EFUCFsC31R4mo" +
                "ihE5HN+FxNalUyyFZU//yjf1pdnlMJqrVByJSMa+y2y4x2FucpoCAO97Tx+iWzwlZ2UXEUXM1Y81mhPbeWXy+wP2xElTgIER0Ts" +
                "n/thoA0SD2u9wJuVvM7dB7cBcHYmqV6JH+KWCedRTum6O1BssqP/4Lbm2+rkrbZ4+oVRoU2DRLoFhKqwqLtylrbuj4XOI3XykMX" +
                "V5+uQXz1JzubNOB9lsc6K+eRC+w8hhhDuFMgzkZ4qomCnx3uhO67KaICd8yqqBa6PJ/+fBM5Xk4hjyR40bwcf41AgMBAAE="

    val ourPrivBytes = Base64.getDecoder().decode(ourPrivateKeyB64.toByteArray())
    val ourPrivkey = unmarshalPrivateKey(ourPrivBytes)
    val privateKeySigner = Signature.getInstance("SHA256withRSA", Libp2pCrypto.provider)
        .also { it.initSign((ourPrivkey as RsaPrivateKey).sk) }

    val signFunction: (ByteArray) -> ByteArray = {
        with(privateKeySigner) {
            this.update(it)
            this.sign()
        }
    }

    val ourPubBytes = Base64.getDecoder().decode(ourPublicKeyB64.toByteArray())
    val ourPubkey = unmarshalPublicKey(ourPubBytes)

    val ourRandom = Base64.getDecoder().decode("QJLd77t1DO9ZVseiUyM9xQ==".toByteArray())
    val ourProposal: ProposeMessage = createOurProposal()
    lateinit var ourEcPrivateKey: BCECPrivateKey //ECPrivateKeyParameters
    lateinit var ourEcPublicKey: BCECPublicKey // ECPublicKeyParameters

    lateinit var sharedSecretBytes: ByteArray
    lateinit var sharedSecret: SecretKey

    lateinit var bestSelectedCurveT: String // curveT
    lateinit var bestSelectedCipherT: String // cipherT
    lateinit var bestSelectedHashT: Digest // hashT
    var order: Int = 0// used with key stretching

    lateinit var localKeysIv: ByteArray
    lateinit var localKeysCipherKey: ByteArray
    lateinit var localKeysMacKey: ByteArray

    lateinit var remoteKeysIv: ByteArray
    lateinit var remoteKeysCipherKey: ByteArray
    lateinit var remoteKeysMacKey: ByteArray

    lateinit var localMacFunc: (ByteArray) -> ByteArray
    lateinit var localCipherFunc: (ByteArray) -> ByteArray
    lateinit var remoteMacFunc: (ByteArray) -> ByteArray
    lateinit var remoteCipherFunc: (ByteArray) -> ByteArray

    // Other side.
    lateinit var otherExchange: ExchangeMessage
    lateinit var otherProposal: ProposeMessage

    // Field/flags.
    var receivedMultistream = false
    var receivedSecio = false
    var sentMultistream = false
    var sentSecio = false
    var sentExchange = false
    var receivedSecioPropose = false
    var receivedSecioExchange = false
    var receivedEncryptedNonce = false
    var receivedEncryptedNonce2 = false

    var sentSecioPropose = false

    // 1st: /multistream/1.0.0
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        msg as ByteArray

        /*
        // runHandshake performs initial communication over insecure channel to share
        // keys, IDs, and initiate communication, assigning all necessary params.
        // requires the duplex channel to be a msgio.ReadWriter (for framed messaging)
        */

        if (!receivedMultistream) {
            processMultistreamAndMaybeSecio(msg, ctx)
        } else if (!receivedSecioPropose) {
            processSecio(msg, ctx)
        } else {
            processProtocolMesssage(msg, ctx)
        }
        receiveStep++
    }

    private fun processMultistreamAndMaybeSecio(msg: ByteArray, ctx: ChannelHandlerContext) {
        LOGGER.info("RECEIVE STEP#$receiveStep: multistream & secio")
        val content = msg.readVarintPrefixedMessage()
        if (content == null) {
            LOGGER.error("Invalid message received, expected a multistream")
            return
        }

        // Parse input.
        val maybeMultistream = String(content.first)
        if (maybeMultistream == "/multistream/1.0.0\n") {
            receivedMultistream = true
        }
        if (content.second != null) {
            val parts = content.second!!.readVarintPrefixedMessage()
            if (parts != null) {
                val maybeSecio = String(parts.first)
                if (maybeSecio == "/secio/1.0.0\n") {
                    receivedSecio = true
                }
            }
        }

        if (receivedMultistream && !sentMultistream) {
            sentMultistream = true
            sentSecio = true
            sendAsync("multistream+secio", SecioUtil.createMultistreamAndSecioMessage(), ctx)
        } else {
            LOGGER.error("Unprocessed byte stream, abort connection")
        }
    }

    private fun processSecio(msg: ByteArray, ctx: ChannelHandlerContext) {
        LOGGER.info("RECEIVE STEP#$receiveStep: secio")

        // step 1. Propose -- propose cipher suite + send pubkeys + nonce
        val proposeMessage = SecioUtil.parseSecioProposeMessage(msg) ?: throw Libp2pException("Unexpected data")
        this.otherProposal = proposeMessage
        this.receivedSecioPropose = true

        // 1. write proposal & read proposal
        // 2. select protocols
        selectProtocols() // Technically we should wait until we send our proposal.

        if (!sentSecioPropose) {
            sentSecioPropose = true
            sendAsync("secio-propose", SecioUtil.createSecioProposeMessage(ourProposal), ctx)
        }
    }

    private fun processProtocolMesssage(msg: ByteArray, ctx: ChannelHandlerContext) {
        // Do protocol negotiation.
        if (!receivedSecioExchange) {
            otherExchange = PrototypeUtil.parseExchangeMessage(msg)
            generateKeys()
            sendOurExchange(ctx)
            receivedSecioExchange = true
        } else if (!receivedEncryptedNonce) {
//            val rawData = msg.readIntPrefixedMessage()!!
//            val l = rawData.size
//            val macSize = 32; // TODO: use hmac.size()
//
//            if (l < macSize) {
//                throw Libp2pException("buffer (${l}) shorter than MAC size (${macSize})")
//            }
//
//            val mark = l - macSize
//            val data: ByteArray = rawData.sliceArray(IntRange(0, mark - 1))
//            val macd: ByteArray = rawData.sliceArray(IntRange(mark, l - 1))
//            val expectedDigest = this.remoteMacFunc(data)
//
//            if (!macd.contentEquals(expectedDigest)) {
//                throw Libp2pException("MAC Invalid")
//            }
//
//            val decrypted = remoteCipherFunc(data)
//            if (!ourProposal.random.contentEquals(decrypted)) {
//                throw Libp2pException("Incorrect nonce returned by other side")
//            }
//
//            // Now, we need to send the other side's nonce
//            val nonceToWriteOut = otherProposal.random
//            val e1 = this.localCipherFunc(nonceToWriteOut)
//            val e2 = this.localMacFunc(e1)
//            val result = (e1 + e2).toVarintPrefixedByteArray()
//            sendAsync("encryptedNonce", result, ctx)
            receivedEncryptedNonce = true
        } else if (!receivedEncryptedNonce2) { // TODO: rename this!
            val rawDataParts = msg.readVarintPrefixedMessage()!!
//            val l = rawData.size // 52, 48
//            val macSize = 32; // TODO: use hmac.size()
//
//            if (l < macSize) {
//                throw Libp2pException("buffer (${l}) shorter than MAC size (${macSize})")
//            }
//
//            val mark = l - macSize // 20, 16
//            val data: ByteArray = rawData.sliceArray(IntRange(0, mark - 1)) // 20, 16
//            val macd: ByteArray = rawData.sliceArray(IntRange(mark, l - 1)) // 32, 32
//            val expectedDigest = this.remoteMacFunc(data)
//
//            if (!macd.contentEquals(expectedDigest)) {
//                throw Libp2pException("MAC Invalid")
//            }
//
//
//            val tempCipherFunc = { input: ByteArray ->
//                val aCipherInstance = Cipher.getInstance(AES_CTR_CIPHER, Libp2pCrypto.provider)
//                aCipherInstance.init(
//                    Cipher.DECRYPT_MODE,
//                    SecretKeySpec(this.localKeysCipherKey, "AES"),
//                    IvParameterSpec(localKeysIv)
//                )
//                aCipherInstance.doFinal(input)
//            }
//
//            val d1 = tempCipherFunc(data)
//            val decrypted = remoteCipherFunc(data)
//            if (!ourProposal.random.contentEquals(decrypted)) {
//                throw Libp2pException("Incorrect nonce returned by other side")
//            }
//
//            // Now, we need to send the other side's nonce
//            val nonceToWriteOut = otherProposal.random
//            val e1 = this.localCipherFunc(nonceToWriteOut)
//            val e2 = this.localMacFunc(e1)
//            val result = (e1 + e2).toVarintPrefixedByteArray()
//            sendAsync("encryptedNonce", result, ctx)
            receivedEncryptedNonce2 = true
        } else {
            LOGGER.error("UNHANDLED PROTOCOL MESSAGE") // 56 bytes
        }
    }

    private fun sendOurExchange(ctx: ChannelHandlerContext) {
        if (!sentExchange) {
            // Create the exchange.
            val exchangeOut = Spipe.Exchange.newBuilder()
                .setEpubkey(ByteString.copyFrom(ourEcPublicKey.q.getEncoded(false)))
                .setSignature(
                    createExchangeSignature(
                        ourProposal.asPropose(),
                        otherProposal!!.asPropose(),
                        ourEcPublicKey.q.getEncoded(false)
                    )
                )
                .build()

            sendAsync("exchange", exchangeOut.toByteArray().toVarintPrefixedByteArray(), ctx)
            sentExchange = true
        }
    }

    private fun createExchangeSignature(
        pmOut: Spipe.Propose,
        pmIn: Spipe.Propose,
        sLocalEphemeralKey: ByteArray
    ): ByteString {
        val selectionOutBytes = KeyUtil.gatherCorpusToSign(pmOut, pmIn, sLocalEphemeralKey)
        val signature = signFunction(selectionOutBytes)
        // exchangeOut.Signature, err = s.localKey.Sign(selectionOutBytes)
        return ByteString.copyFrom(signature)
    }

    private fun selectProtocols() {
        LOGGER.info("1.2 protocol selection")
        val bestParams = SecioUtil.selectBest(ourProposal, otherProposal)

        this.bestSelectedHashT = SHA256Digest()
        if (bestParams.cipher == "SHA-512") {
            this.bestSelectedHashT = SHA512Digest()
        }

        this.bestSelectedCurveT = bestParams.curve // curveT
        this.bestSelectedCipherT = bestParams.cipher // cipherT
        this.order = order
    }

    private fun sendAsync(step: String, message: Any, ctx: ChannelHandlerContext) {
        val cf = ctx.channel().write(message)
        cf.addListener { future1 ->
            if (future1.isSuccess) {
                LOGGER.debug("[$step]: message written")
            } else {
                LOGGER.error("[$step]: message write failed")
                throw future1.cause()
            }
        }
        ctx.flush()
    }

    private fun createOurProposal(): ProposeMessage {
        return ProposeMessage(ourPubkey, ourRandom)
            .withCiphers(*ALL_CIPHERS)
            .withHashes(*ALL_HASHES)
            .withExchanges(*ALL_EXCHANGES)
    }

    private fun generateKeys() {
        LOGGER.info("2.2 - generateKeys")
        // Generate ephemeral EC keys first.
        val kpgen = KeyPairGenerator.getInstance("ECDH", Libp2pCrypto.provider)
        kpgen.initialize(ECGenParameterSpec("prime256v1"), SecureRandom())
        val pair = kpgen.generateKeyPair()
        this.ourEcPrivateKey = pair.private as BCECPrivateKey
        this.ourEcPublicKey = pair.public as BCECPublicKey

        // Second, generate the shared key
        this.sharedSecretBytes = CipherUtil.computeDHSecretKey(ourEcPrivateKey, otherExchange!!.publicKey)
        this.sharedSecret = SecretKeySpec(this.sharedSecretBytes, "AES")

        // Do key stretching - crypto.js:167
        val hmac = HashFactory.createHmac(this.sharedSecretBytes, this.bestSelectedHashT)

        // Assume AES-256
        val keys = KeyUtil.doKeyStretching(16, this.bestSelectedHashT.digestSize, hmac)

        if (this.order > 0) {
            val localKeys = keys.first
            this.localKeysIv = localKeys.iv
            this.localKeysCipherKey = localKeys.cipherKey
            this.localKeysMacKey = localKeys.macKey

            val remoteKeys = keys.second
            this.remoteKeysIv = remoteKeys.iv
            this.remoteKeysCipherKey = remoteKeys.cipherKey
            this.remoteKeysMacKey = remoteKeys.macKey
        } else {
            // swap
            val localKeys = keys.second
            this.localKeysIv = localKeys.iv
            this.localKeysCipherKey = localKeys.cipherKey
            this.localKeysMacKey = localKeys.macKey

            val remoteKeys = keys.first
            this.remoteKeysIv = remoteKeys.iv
            this.remoteKeysCipherKey = remoteKeys.cipherKey
            this.remoteKeysMacKey = remoteKeys.macKey
        }

        LOGGER.info("2.3 - MAC and cipher")
        // Now build the function pointers to do the encryption and decryption, like:
        val localHmac = HashFactory.createHmac(localKeysMacKey, bestSelectedHashT)
        this.localMacFunc = { input: ByteArray -> HashUtil.hash(localHmac, input) }

        val encryptionCipher = CipherFactory.createAESEncryptionCipher(this.localKeysCipherKey, localKeysIv)
        this.localCipherFunc = { input: ByteArray -> encryptionCipher.doFinal(input) }

        val remoteHmac = HashFactory.createHmac(remoteKeysMacKey, bestSelectedHashT)
        this.remoteMacFunc = { input: ByteArray ->
            HashUtil.hash(remoteHmac, input)
        }

        val decryptionCipher = CipherFactory.createAESDecryptionCipher(this.remoteKeysCipherKey, remoteKeysIv)
        this.remoteCipherFunc = { input: ByteArray -> decryptionCipher.doFinal(input) }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable) {
        LOGGER.warn("----- exceptionCaught: ${cause.message}", cause)
        super.exceptionCaught(ctx, cause)
    }

    companion object {
        val LOGGER = LoggerFactory.getLogger(AccumulatorInboundHandler::class.java.name)!!
    }
}
