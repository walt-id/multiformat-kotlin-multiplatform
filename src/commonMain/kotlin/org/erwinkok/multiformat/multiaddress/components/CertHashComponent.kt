// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multiaddress.components


import org.erwinkok.multiformat.multiaddress.Protocol
import org.erwinkok.multiformat.multiaddress.Transcoder
import org.erwinkok.multiformat.multibase.Multibase
import org.erwinkok.multiformat.multihash.Multihash
import org.erwinkok.multiformat.util.flatMap

class CertHashComponent private constructor(addressBytes: ByteArray) : Component(Protocol.CERTHASH, addressBytes) {
    override val value: String
        get() = Multibase.BASE64_URL.encode(addressBytes)

    companion object : Transcoder {
        override fun bytesToComponent(protocol: Protocol, bytes: ByteArray): Result<CertHashComponent> {
            Multihash.fromBytes(bytes)
                .onFailure { return Result.failure(it) }
            return Result.success(CertHashComponent(bytes))
        }

        override fun stringToComponent(protocol: Protocol, string: String): Result<CertHashComponent> {
            return Multibase.decode(string)
                .flatMap { bytesToComponent(protocol, it) }
        }
    }
}
