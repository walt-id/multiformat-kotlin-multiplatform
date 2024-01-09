// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multiaddress.components


import org.erwinkok.multiformat.multiaddress.Protocol
import org.erwinkok.multiformat.multiaddress.Transcoder
import org.erwinkok.multiformat.multibase.bases.Base32

class OnionComponent private constructor(protocol: Protocol, private val hostBytes: ByteArray, private val port: Int) :
    Component(protocol, hostBytes + byteArrayOf((port shr 8).toByte(), port.toByte())) {
    override val value: String
        get() {
            val addressString = Base32.encodeStdLowerNoPad(hostBytes)
            return "$addressString:$port"
        }

    companion object : Transcoder {
        override fun bytesToComponent(protocol: Protocol, bytes: ByteArray): Result<OnionComponent> {
            val hostBytes = bytes.copyOfRange(0, bytes.size - 2)
            val port = ((bytes[bytes.size - 2].toInt() shl 8) or (bytes[bytes.size - 1].toInt() and 255)) and 0xffff
            return Result.success(OnionComponent(protocol, hostBytes, port))
        }

        override fun stringToComponent(protocol: Protocol, string: String): Result<OnionComponent> {
            val len = if (protocol == Protocol.ONION) {
                16
            } else {
                56
            }
            val dlen = if (protocol == Protocol.ONION) {
                10
            } else {
                35
            }
            val split = string.split(":")
            if (split.size != 2) {
                return Result.failure(IllegalArgumentException("failed to parse onion address: $string does not contain a port number"))
            }
            // onion address without the ".onion" substring
            if (split[0].length != len) {
                return Result.failure(IllegalArgumentException("failed to parse onion address: $string not a Tor onion address."))
            }
            // onion addresses do not include the multibase prefix, add it before decoding
            val onionHostBytes = Base32.decodeStdNoPad(split[0])
                .getOrElse { return Result.failure(IllegalArgumentException("Invalid onion address host: ${split[0]}")) }
            if (onionHostBytes.size != dlen) {
                return Result.failure(IllegalArgumentException("Invalid onion address host: ${split[0]}"))
            }
            val port = split[1].toInt()
            if (port < 1 || port > 65535) {
                return Result.failure(IllegalArgumentException("Port number is not in range(1, 65536): $port"))
            }
            return Result.success(OnionComponent(protocol, onionHostBytes, port))
        }
    }
}
