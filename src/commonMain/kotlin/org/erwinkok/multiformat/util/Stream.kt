// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.util


class CustomStream(
    val buffer: ArrayDeque<Int> = ArrayDeque(),
) {
    //val buffer = ArrayDeque<T>()

    fun size() = buffer.size

    @Deprecated("Is this correct?")
    fun available() = buffer.size

    fun write(value: Int) {
        buffer.addLast(value)
    }

    fun writeBytes(b: ByteArray) {
        buffer.addAll(b.toTypedArray().map { it.toInt() })
    }

    /*fun writeAll(array: Array<Int>) {
        array.forEach { write(it) }
    }*/


    fun readThrowing(): Byte {
        return buffer.removeFirst().toByte()
    }

    fun read(): Int {
        return runCatching { buffer.removeFirst().toUByte().toInt() }.getOrElse { -1 }
    }

    fun readByteArray(size: Int): ByteArray {
        val result = ArrayList<Byte>()
        repeat(size) {
            result.add(read().toByte())
        }
        return result.toByteArray()
    }

    fun toByteArray() = buffer.map { it.toByte() }.toByteArray()
}

fun main() {

}

fun ByteArray.toCustomStream() = CustomStream(ArrayDeque(toList().map { it.toInt() }))

/*fun CustomStream<UByte>.readUnsignedVarInt(): Result<ULong> {
    return UVarInt.readUnsignedVarInt { index ->
        try {
            val b = read()
            if (b < 0u) {
                if (index != 0) {
                    Result.failure(IllegalStateException("UnexpectedEndOfStream"))
                } else {
                    Result.failure(IllegalStateException("EndOfStream"))
                }
            } else {
                Result.success(b.toUByte())
            }
        } catch (e: NoSuchElementException) {
            Result.failure(IllegalStateException("EndOfStream"))
        }
    }
}*/

fun CustomStream.readUnsignedVarInt(): Result<ULong> {
    return UVarInt.readUnsignedVarInt { index ->
        try {
            val b = read()
            if (b < 0) {
                println("STREAM ERROR: Returned byte: $b, index is: $index -> thus ${if (index != 0) "UnexpectedEndOfStream" else "EndOfStream"} (in stream ${this.buffer.toList()})")
                if (index != 0) {
                    Result.failure(IllegalStateException("UnexpectedEndOfStream"))
                } else {
                    Result.failure(IllegalStateException("EndOfStream"))
                }
            } else {
                Result.success(b.toUByte())
            }
        } catch (e: NoSuchElementException) {
            Result.failure(IllegalStateException("EndOfStream"))
        }
    }
}

/*fun InputStream.readUnsignedVarInt(): Result<ULong> {
    return UVarInt.readUnsignedVarInt { index ->
        try {
            val b = read()
            if (b < 0) {
                if (index != 0) {
                    Result.failure(IllegalStateException("UnexpectedEndOfStream"))
                } else {
                    Result.failure(IllegalStateException("EndOfStream"))
                }
            } else {
                Result.success(b.toUByte())
            }
        } catch (e: IOException) {
            Result.failure(IllegalStateException("EndOfStream"))
        }
    }
}*/


fun CustomStream.writeUnsignedVarInt(x: Int): Result<Int> {
    return writeUnsignedVarInt(x.toULong())
}

/*fun OutputStream.writeUnsignedVarInt(x: Int): Result<Int> {
    return writeUnsignedVarInt(x.toULong())
}*/

fun CustomStream.writeUnsignedVarInt(x: Long): Result<Int> {
    return writeUnsignedVarInt(x.toULong())
}

/*fun OutputStream.writeUnsignedVarInt(x: Long): Result<Int> {
    return writeUnsignedVarInt(x.toULong())
}*/

fun CustomStream.writeUnsignedVarInt(x: ULong): Result<Int> {
    return UVarInt.writeUnsignedVarInt(x) {
        this.write(it.toInt())
        Result.success(Unit)
    }
}

/*fun OutputStream.writeUnsignedVarInt(x: ULong): Result<Int> {
    return UVarInt.writeUnsignedVarInt(x) {
        try {
            this.write(it.toInt())
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(IllegalStateException("EndOfStream"))
        }
    }
}*/
