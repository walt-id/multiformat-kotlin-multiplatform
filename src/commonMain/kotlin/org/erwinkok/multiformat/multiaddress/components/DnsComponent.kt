// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multiaddress.components

import io.ktor.utils.io.core.*
import org.erwinkok.multiformat.multiaddress.Protocol
import org.erwinkok.multiformat.multiaddress.Transcoder

class DnsComponent private constructor(protocol: Protocol, val address: String) : Component(protocol, address.toByteArray()) {
    override val value: String
        get() = address

    companion object : Transcoder {
        override fun bytesToComponent(protocol: Protocol, bytes: ByteArray): Result<DnsComponent> {
            return Result.success(DnsComponent(protocol, bytes.decodeToString()))
        }

        override fun stringToComponent(protocol: Protocol, string: String): Result<DnsComponent> {
            return Result.success(DnsComponent(protocol, string))
        }
    }
}
