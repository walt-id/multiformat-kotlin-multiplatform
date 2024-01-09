// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multihash.hashers


import java.security.MessageDigest

class MessageDigestHasher(private val messageDigest: MessageDigest) : Hasher {
    override fun write(p: ByteArray): Result<Int> {
        return try {
            messageDigest.update(p)
            Result.success(p.size)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Could not update for MessageDigest: ${e.message}"))
        }
    }

    override fun sum(): ByteArray {
        return messageDigest.digest()
    }

    override fun reset() {
        messageDigest.reset()
    }

    override fun size(): Int {
        return messageDigest.digestLength
    }

    override fun blockSize(): Int {
        return 32
    }
}
