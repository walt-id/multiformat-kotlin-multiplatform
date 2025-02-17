// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.cid

import org.erwinkok.multiformat.multibase.Multibase
import org.erwinkok.multiformat.multicodec.Multicodec
import org.erwinkok.multiformat.multihash.Multihash
import org.erwinkok.multiformat.util.CustomStream
import org.erwinkok.multiformat.util.writeUnsignedVarInt


class CidV1(multihash: Multihash, override val multicodec: Multicodec, override val multibase: Multibase = Multibase.BASE32) :
    Cid(multihash) {
    override val version: Int
        get() = 1

    override fun bytes(): ByteArray {
        val stream = CustomStream()
        stream.writeUnsignedVarInt(version)
        stream.writeUnsignedVarInt(multicodec.code)
        multihash.serialize(stream)

        return stream.toByteArray()
    }

    override fun prefix(): Prefix {
        return Prefix(version, multicodec, multihash.type, multihash.length)
    }

    override fun toV0(): Result<Cid> {
        if (multicodec != Multicodec.DAG_PB) {
            return Result.failure(IllegalArgumentException("Cannot convert a non dag-pb CID to CIDv0"))
        }
        if (multihash.type != Multicodec.SHA2_256) {
            return Result.failure(IllegalArgumentException("Cannot convert non sha2-256 multihash CID to CIDv0"))
        }
        if (multihash.length != 32) {
            return Result.failure(IllegalArgumentException("Cannot convert non 32 byte multihash CID to CIDv0"))
        }
        return Result.success(CidV0(multihash))
    }

    override fun toV1(): Result<Cid> {
        return Result.success(CidV1(multihash, multicodec, multibase))
    }

    override fun copy(): Cid {
        return CidV1(multihash, multicodec, multibase)
    }

    override fun toString(base: Multibase): Result<String> {
        return Result.success(base.encode(bytes()))
    }

    override fun toString(): String {
        return multibase.encode(bytes())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is CidV1) {
            return false
        }
        return multicodec == other.multicodec &&
                multibase == other.multibase &&
                multihash == other.multihash
    }

    override fun hashCode(): Int {
        var result = multihash.hashCode()
        result = 32 * result + multibase.hashCode()
        result = 31 * result + multicodec.hashCode()
        return result
    }
}
