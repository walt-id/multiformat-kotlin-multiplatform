// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.util


object UVarInt {
    private val ErrOverflow = IllegalArgumentException("varints larger than uint63 not supported")
    private val ErrNotMinimal = IllegalArgumentException("varint not minimally encoded")

    // MaxLenUvarint63 is the maximum number of bytes representing an uvarint in
    // this encoding, supporting a maximum value of 2^63 (uint63), aka
    // MaxValueUvarint63.
    private const val MaxLenUvarint63 = 9L

    const val MaxVarintLen16 = 3
    const val MaxVarintLen32 = 5

    fun writeUnsignedVarInt(x: ULong, write: (Byte) -> Result<Unit>): Result<Int> {
        var value = x
        var i = 0
        while (value >= 0x80u) {
            write(((value and 0x7Fu) or 0x80u).toByte())
                .getOrElse { return Result.failure(it) }
            value = (value.toLong() ushr 7).toULong()
            i++
        }
        write((value and 0x7Fu).toByte())
            .getOrElse { return Result.failure(it) }
        return Result.success(i + 1)
    }

    suspend fun coWriteUnsignedVarInt(x: ULong, write: suspend (Byte) -> Result<Unit>): Result<Int> {
        var value = x
        var i = 0
        while (value >= 0x80u) {
            write(((value and 0x7Fu) or 0x80u).toByte())
                .getOrElse { return Result.failure(it) }
            value = (value.toLong() ushr 7).toULong()
            i++
        }
        write((value and 0x7Fu).toByte())
            .getOrElse { return Result.failure(it) }
        return Result.success(i + 1)
    }

    fun readUnsignedVarInt(read: (Int) -> Result<UByte>): Result<ULong> {
        var value: ULong = 0uL
        var s = 0
        var i = 0
        while (true) {
            val uByte = read(i)
                .getOrElse { return Result.failure(it) }
            if ((i == 8 && uByte >= 0x80u) || i >= MaxLenUvarint63) {
                // this is the 9th and last byte we're willing to read, but it
                // signals there's more (1 in MSB).
                // or this is the >= 10th byte, and for some reason we're still here.
                return Result.failure(ErrOverflow)
            }
            if (uByte < 0x80u) {
                if (uByte == 0u.toUByte() && s > 0) {
                    return Result.failure(ErrNotMinimal)
                }
                return Result.success(value or (uByte.toULong() shl s))
            }
            value = value or ((uByte and 0x7fu).toULong() shl s)
            s += 7
            i++
        }
    }

    suspend fun coReadUnsignedVarInt(read: suspend (Int) -> Result<UByte>): Result<ULong> {
        var value: ULong = 0uL
        var s = 0
        var i = 0
        while (true) {
            val uByte = read(i)
                .getOrElse { return Result.failure(it) }
            if ((i == 8 && uByte >= 0x80u) || i >= MaxLenUvarint63) {
                // this is the 9th and last byte we're willing to read, but it
                // signals there's more (1 in MSB).
                // or this is the >= 10th byte, and for some reason we're still here.
                return Result.failure(ErrOverflow)
            }
            if (uByte < 0x80u) {
                if (uByte == 0u.toUByte() && s > 0) {
                    return Result.failure(ErrNotMinimal)
                }
                return Result.success(value or (uByte.toULong() shl s))
            }
            value = value or ((uByte and 0x7fu).toULong() shl s)
            s += 7
            i++
        }
    }
}
