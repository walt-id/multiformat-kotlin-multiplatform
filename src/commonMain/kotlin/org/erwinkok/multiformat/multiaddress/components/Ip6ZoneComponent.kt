// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multiaddress.components

import org.erwinkok.multiformat.multiaddress.Protocol
import org.erwinkok.multiformat.multiaddress.Transcoder

class Ip6ZoneComponent private constructor(val address: String) : Component(Protocol.IP6ZONE, address.encodeToByteArray()) {
    override val value: String
        get() = address

    companion object : Transcoder {
        @OptIn(ExperimentalStdlibApi::class)
        override fun bytesToComponent(protocol: Protocol, bytes: ByteArray): Result<Ip6ZoneComponent> {
            if (bytes.isEmpty()) {
                return Result.failure(IllegalArgumentException("invalid length (should be > 0)"))
            }
            if (bytes.indexOf('/'.code.toByte()) >= 0) {
                return Result.failure(IllegalArgumentException("IPv6 zone ID contains '/': " + bytes.toHexString()))
            }
            return Result.success(Ip6ZoneComponent(bytes.decodeToString()))
        }

        override fun stringToComponent(protocol: Protocol, string: String): Result<Ip6ZoneComponent> {
            if (string.isEmpty()) {
                return Result.failure(IllegalArgumentException("Empty IPv6Zone"))
            }
            if (string.contains("/")) {
                return Result.failure(IllegalArgumentException("IPv6Zone ID contains '/': $string"))
            }
            return Result.success(Ip6ZoneComponent(string))
        }
    }
}
