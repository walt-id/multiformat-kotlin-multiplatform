// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multiaddress.components

import org.erwinkok.multiformat.multiaddress.Protocol
import org.erwinkok.multiformat.multiaddress.Transcoder


class IpCidrComponent private constructor(addressByte: Byte) : Component(Protocol.IPCIDR, byteArrayOf(addressByte)) {
    override val value: String
        get() = "${addressBytes[0].toUByte()}"

    companion object : Transcoder {
        override fun bytesToComponent(protocol: Protocol, bytes: ByteArray): Result<IpCidrComponent> {
            if (bytes.size != 1) {
                return Result.failure(IllegalArgumentException("invalid length (should be == 1)"))
            }
            return Result.success(IpCidrComponent(bytes[0]))
        }

        override fun stringToComponent(protocol: Protocol, string: String): Result<IpCidrComponent> {
            return try {
                val i = string.toUInt(10)
                if (i > 255u) {
                    Result.failure(IllegalArgumentException("Invalid cpidr, must be <256"))
                } else {
                    Result.success(IpCidrComponent(i.toByte()))
                }
            } catch (e: NumberFormatException) {
                Result.failure(IllegalArgumentException("Could not parse integer: ${e.message}"))
            }
        }
    }
}
