// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.util


class CustomStream<T>(
    val buffer: ArrayDeque<T> = ArrayDeque()
) {
    //val buffer = ArrayDeque<T>()

    fun available() = buffer.size

    fun write(value: T) {
        buffer.addLast(value)
    }

    fun writeAll(array: Array<T>) {
        array.forEach { write(it) }
    }

    fun read(): T {
        return buffer.removeFirst()
    }

}

fun ByteArray.toCustomStream() = CustomStream(ArrayDeque(toList()))
fun CustomStream<Byte>.toByteArray() = buffer.toByteArray()

fun CustomStream<UByte>.readUnsignedVarInt(): Result<ULong> {
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
}

fun CustomStream<Byte>.readUnsignedVarInt(): Result<ULong> {
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


fun CustomStream<Byte>.writeUnsignedVarInt(x: Int): Result<Int> {
    return writeUnsignedVarInt(x.toULong())
}

/*fun OutputStream.writeUnsignedVarInt(x: Int): Result<Int> {
    return writeUnsignedVarInt(x.toULong())
}*/

fun CustomStream<Byte>.writeUnsignedVarInt(x: Long): Result<Int> {
    return writeUnsignedVarInt(x.toULong())
}

/*fun OutputStream.writeUnsignedVarInt(x: Long): Result<Int> {
    return writeUnsignedVarInt(x.toULong())
}*/

fun CustomStream<Byte>.writeUnsignedVarInt(x: ULong): Result<Int> {
    return UVarInt.writeUnsignedVarInt(x) {
        this.write(it)
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
