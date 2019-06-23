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
package io.web3j.libp2p.shared.ext

import java.nio.charset.Charset
import java.util.Base64

/**
 * Converts this string to the raw bytes by decoding it using base-64 decoding.
 * @return the decoded byte array.
 */
fun String.fromBase64(): ByteArray = Base64.getDecoder().decode(this)

/**
 * Creates a serialized version of this string as a byte array that is prefixed
 * with the length as a varint.
 * @return the varint-prefixed data.
 */
fun String.toVarintPrefixedByteArray(): ByteArray = toByteArray(Charset.defaultCharset()).toVarintPrefixedByteArray()
