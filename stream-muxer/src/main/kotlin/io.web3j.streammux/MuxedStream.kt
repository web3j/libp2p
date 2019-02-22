package io.web3j.streammux

import java.time.Duration

interface MuxedStream {

    /**
     * @return the bytes from the underlying muxed connection.
     */
    fun read(): ByteArray

    /**
     * Writes the given bytes to the underlying muxed connection.
     * @param byteArray the byte array to be written out.
     * @return the number of bytes written.
     */
    fun write(byteArray: ByteArray): Long

    /**
     * Closes the underlying muxed connection.
     * @return true if the connection was successfully closed.
     */
    fun close(): Boolean

    /**
     * Reset closes both ends of the stream. Use this to tell the remote
     * side to hang up and go away.
     */
    fun reset(): Unit

    /**
     * Sets the deadline for the muxed stream.
     * @param ttl the deadline/TTL.
     */
    fun setDeadline(ttl: Duration)


}