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
package io.web3j.libp2p.security.secio

import spipe.pb.Spipe

class Soemthing {

    /*
// SessionGenerator constructs secure communication sessions for a peer.
type Transport struct {
	LocalID    peer.ID
	PrivateKey ci.PrivKey
}

func New(sk ci.PrivKey) (*Transport, error) {
	id, err := peer.IDFromPrivateKey(sk)
	if err != nil {
		return nil, err
	}
	return &Transport{
		LocalID:    id,
		PrivateKey: sk,
	}, nil
}

func (sg *Transport) SecureInbound(ctx context.Context, insecure net.Conn) (cs.Conn, error) {
	return newSecureSession(ctx, sg.LocalID, sg.PrivateKey, insecure, "")
}
func (sg *Transport) SecureOutbound(ctx context.Context, insecure net.Conn, p peer.ID) (cs.Conn, error) {
	return newSecureSession(ctx, sg.LocalID, sg.PrivateKey, insecure, p)
}


     */

    fun abc() {
//        Spipe.Exchange

        val proposeBytes = Spipe.Propose.newBuilder().setCiphers("ABC")
            .setExchanges("DEF")
            .build()
            .toByteArray()

//        proposeIn := new(pb.Propose)
//        if err = proto.Unmarshal(proposeInBytes, proposeIn); err != nil {
//            return err
//        }

        /*
            PbPrivateKey.newBuilder()
        .setType(privKey.keyType)
        .setData(ByteString.copyFrom(privKey.raw()))
        .build()
        .toByteArray()
         */
    }

    companion object {

        /**
         * The protocol ID to be used when negotiating with multistream.
         */
        const val ID = "/secio/1.0.0"
    }
}
