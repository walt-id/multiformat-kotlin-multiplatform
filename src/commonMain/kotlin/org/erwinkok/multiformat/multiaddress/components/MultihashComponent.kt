// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multiaddress.components

import org.erwinkok.multiformat.cid.Cid
import org.erwinkok.multiformat.multiaddress.Protocol
import org.erwinkok.multiformat.multiaddress.Transcoder
import org.erwinkok.multiformat.multicodec.Multicodec
import org.erwinkok.multiformat.multihash.Multihash


class MultihashComponent private constructor(val multihash: Multihash) : Component(Protocol.P2P, multihash.bytes()) {
    override val value: String
        get() = multihash.base58()

    companion object : Transcoder {
        override fun bytesToComponent(protocol: Protocol, bytes: ByteArray): Result<MultihashComponent> {
            return Multihash.fromBytes(bytes)
                .map { MultihashComponent(it) }
        }

        override fun stringToComponent(protocol: Protocol, string: String): Result<MultihashComponent> {
            val multihash = if (string.startsWith("Qm") || string.startsWith("1")) {
                Multihash.fromBase58(string)
                    .getOrElse { return Result.failure(it) }
            } else {
                val cid = Cid.fromString(string)
                    .getOrElse { return Result.failure(IllegalArgumentException("failed to parse p2p address $string: ${it.message}")) }
                if (cid.multicodec !== Multicodec.LIBP2P_KEY) {
                    return Result.failure(IllegalArgumentException("failed to parse p2p address: $string has an invalid codec ${cid.multicodec.typeName}"))
                }
                cid.multihash
            }
            return Result.success(MultihashComponent(multihash))
        }
    }
}
