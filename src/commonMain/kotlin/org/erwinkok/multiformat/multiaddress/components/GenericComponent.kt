// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multiaddress.components


import org.erwinkok.multiformat.multiaddress.Protocol
import org.erwinkok.multiformat.multiaddress.Transcoder
import org.erwinkok.multiformat.multibase.bases.Base16

class GenericComponent private constructor(protocol: Protocol, addressBytes: ByteArray) : Component(protocol, addressBytes) {
    @OptIn(ExperimentalStdlibApi::class)
    override val value: String
        get() = addressBytes.toHexString()

    companion object : Transcoder {
        override fun bytesToComponent(protocol: Protocol, bytes: ByteArray): Result<GenericComponent> {
            return Result.success(GenericComponent(protocol, bytes))
        }

        override fun stringToComponent(protocol: Protocol, string: String): Result<GenericComponent> {
            val bytes = Base16.decode(string)
                .getOrElse { return Result.failure(IllegalArgumentException("invalid value $string for protocol ${protocol.codec.typeName}: ${it.message}")) }
            return Result.success(GenericComponent(protocol, bytes))
        }
    }
}
