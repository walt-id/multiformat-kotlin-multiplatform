// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.cid

import org.erwinkok.multiformat.multibase.Multibase
import org.erwinkok.multiformat.multicodec.Multicodec
import org.erwinkok.multiformat.multihash.Multihash
import org.erwinkok.multiformat.util.flatMap
import org.erwinkok.multiformat.util.readUnsignedVarInt
import org.erwinkok.multiformat.util.toCustomStream

abstract class Cid protected constructor(val multihash: Multihash) {
    abstract val version: Int
    abstract val multicodec: Multicodec
    abstract val multibase: Multibase

    abstract fun prefix(): Prefix
    abstract fun bytes(): ByteArray
    abstract fun toV0(): Result<Cid>
    abstract fun toV1(): Result<Cid>
    abstract fun copy(): Cid
    abstract fun toString(base: Multibase): Result<String>

    companion object {
        fun fromString(str: String): Result<Cid> {
            val vstr =
                if (str.startsWith("/ipfs/")) {
                    str.substring(6)
                } else {
                    str
                }
            if (vstr.length < 2) {
                return Result.failure(IllegalArgumentException("cid too short"))
            }
            if (vstr.length == 46 && vstr.startsWith("Qm")) {
                // version is a base58btc string multihash, so v0
                return Multihash.fromBase58(str)
                    .map { CidV0(it) }
            }
            // version is a CID String encoded with multibase, so v1
            val encoding = Multibase.encoding(vstr)
                .getOrElse { return Result.failure(it) }
            return Multibase.decode(vstr)
                .flatMap { fromBytes(it, encoding) }
        }

        fun fromBytes(data: ByteArray, encoding: Multibase = Multibase.BASE32): Result<Cid> {
            if (data.size > 2 && data[0].toInt() == Multicodec.SHA2_256.code && data[1].toInt() == 32) {
                if (data.size < 34) {
                    return Result.failure(IllegalArgumentException("not enough bytes for cid v0"))
                }
                return Multihash.fromBytes(data.copyOfRange(0, 34))
                    .map { CidV0(it) }
            }
            val stream = data.toCustomStream()
            val version = stream.readUnsignedVarInt()
                .getOrElse { return Result.failure(it) }
            if (version.toInt() != 1) {
                return Result.failure(IllegalArgumentException("expected 1 as the cid version number, got: $version"))
            }
            val codec = stream.readUnsignedVarInt()
                .flatMap { Multicodec.codeToType(it.toInt()) }
                .getOrElse { return Result.failure(it) }
            val multihash = Multihash.fromStream(stream)
                .getOrElse { return Result.failure(it) }
            return Result.success(CidV1(multihash, codec, encoding))
        }

        @Deprecated("Have one consistent interface", ReplaceWith("fromBytes(data)", "org.erwinkok.libp2p.multiformat.cid.fromBytes"))
        fun cast(data: ByteArray): Result<Cid> {
            return fromBytes(data)
        }

        @Deprecated("Have one consistent interface", ReplaceWith("fromString(string)", "org.erwinkok.libp2p.multiformat.cid.fromString"))
        fun decode(string: String): Result<Cid> {
            return fromString(string)
        }

        fun builder(): CidBuilder {
            return CidBuilder()
        }
    }
}
