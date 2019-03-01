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
package io.web3j.streammux

/**
 * Contains the generic and common functions across all types of streams.
 * <br />
 * This has been modelled so that other streams can be built off this building block without
 * having to copy across these common functions in their definitions.
 * <br />
 * Ideally, this would live in the common module across the entire project.
 */
interface BasicStream {

    /**
     * @return the bytes from the stream..
     */
    fun read(): ByteArray

    /**
     * Writes the given bytes to the stream.
     * @param byteArray the byte array to be written out.
     * @return the number of bytes written.
     */
    fun write(byteArray: ByteArray): Long

    /**
     * Closes the stream.
     * @return true if the stream was successfully closed.
     */
    fun close(): Boolean
}
