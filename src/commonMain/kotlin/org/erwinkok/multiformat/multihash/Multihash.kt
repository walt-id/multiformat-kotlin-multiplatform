// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multihash


import org.erwinkok.multiformat.multibase.bases.Base16
import org.erwinkok.multiformat.multibase.bases.Base58
import org.erwinkok.multiformat.multicodec.Multicodec
import org.erwinkok.multiformat.util.*

class Multihash private constructor(val type: Multicodec, val digest: ByteArray) {
    val name: String
        get() = type.typeName

    val code: Int
        get() = type.code

    val length: Int
        get() = digest.size

    @OptIn(ExperimentalStdlibApi::class)
    fun hex(): String {
        return bytes().toHexString()
    }

    fun base58(): String {
        return Base58.encodeToStringBtc(bytes())
    }

    fun bytes(): ByteArray {
        val stream = CustomStream()
        stream.writeUnsignedVarInt(code)
        stream.writeUnsignedVarInt(digest.size)
        stream.writeBytes(digest)
        return stream.toByteArray()
    }

    fun serialize(s: CustomStream) {
        s.writeBytes(bytes())
    }

    override fun toString(): String {
        return base58()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is Multihash) {
            return super.equals(other)
        }
        return type == other.type &&
                digest.contentEquals(other.digest)
    }

    override fun hashCode(): Int {
        return digest.contentHashCode() xor type.hashCode()
    }

    companion object {
        private val ErrTooShort = IllegalArgumentException("multihash too short. must be >= 2 bytes")
        private val ErrInvalidMultihash = IllegalArgumentException("input isn't valid multihash")

        private const val MAX_IDENTITY_HASH_LENGTH = 1024 * 1024

        fun fromHexString(s: String): Result<Multihash> {
            return Base16.decode(s)
                .flatMap { fromBytes(it) }
        }

        fun fromBase58(s: String): Result<Multihash> {
            return Base58.decodeStringBtc(s)
                .fold({ fromBytes(it) }, { Result.failure(ErrInvalidMultihash) })
        }

        fun fromTypeAndDigest(type: Multicodec, digest: ByteArray): Result<Multihash> {
            if (digest.size > Int.MAX_VALUE) {
                return Result.failure(IllegalArgumentException("digest too long, supporting only <= 2^31-1"))
            }
            return Result.success(Multihash(type, digest))
        }

        fun fromBytes(bytes: ByteArray): Result<Multihash> {
            return deserialize(bytes)
        }

        fun fromStream(stream: CustomStream): Result<Multihash> {
            return deserialize(stream)
        }

        fun sum(type: Multicodec, data: ByteArray, length: Int = -1): Result<Multihash> {
            val hasher = MultihashRegistry.getHasher(type, length)
                .getOrElse { return Result.failure(it) }
            hasher.write(data)
            var sum = hasher.sum()
            val vlength = if (length < 0) {
                hasher.size()
            } else {
                length
            }
            if (sum.size < vlength) {
                return Result.failure(IllegalArgumentException("requested length was too large for digest"))
            }
            if (vlength >= 0) {
                if (type == Multicodec.IDENTITY && vlength != sum.size) {
                    return Result.failure(IllegalArgumentException("the length of the identity hash must be equal to the length of the data"))
                }
                sum = sum.copyOfRange(0, vlength)
            }
            return fromTypeAndDigest(type, sum)
        }

        fun encode(digest: ByteArray, type: Multicodec): Result<Multihash> {
            return fromTypeAndDigest(type, digest)
        }

        fun encodeName(digest: ByteArray, name: String): Result<Multihash> {
            return Multicodec.nameToType(name)
                .flatMap { encode(digest, it) }
        }

        @Deprecated(
            "Have one consistent interface",
            ReplaceWith("fromBytes(bytes)", "org.erwinkok.libp2p.multiformat.multihash.Multihash.fromBytes"),
        )
        fun cast(buf: ByteArray): Result<Multihash> {
            return fromBytes(buf)
        }

        @Deprecated(
            "Have one consistent interface",
            ReplaceWith("fromBytes(bytes)", "org.erwinkok.libp2p.multiformat.multihash.Multihash.fromBytes"),
        )
        fun decode(bytes: ByteArray): Result<Multihash> {
            return fromBytes(bytes)
        }

        private fun deserialize(buf: ByteArray): Result<Multihash> {
            return deserialize(buf.toCustomStream())
        }

        private fun deserialize(din: CustomStream): Result<Multihash> {
            if (din.available() < 2) {
                return Result.failure(ErrTooShort)
            }
            val code = din.readUnsignedVarInt()
                .getOrElse { return Result.failure(it) }
                .toInt()
            val type = Multicodec.codeToType(code)
                .getOrElse { return Result.failure(it) }
            val length = din.readUnsignedVarInt()
                .getOrElse { return Result.failure(it) }
                .toInt()
            if (length < 0) {
                return Result.failure(IllegalArgumentException("Multihash invalid length: $length"))
            }
            if (length > Int.MAX_VALUE) {
                return Result.failure(IllegalArgumentException("digest too long, supporting only <= 2^31-1"))
            }
            if (din.available() != length) {
                return Result.failure(IllegalArgumentException("length greater than remaining number of bytes in buffer"))
            }

            val digest = din.readByteArray(length)

            /*if (length > 0 && din.read(digest, 0, length) != length) {
                return Result.failure(IllegalArgumentException("Error reading Multihash from buffer"))
            }*/
            return fromTypeAndDigest(type, digest)
        }
    }
}
