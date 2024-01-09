// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multihash


import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.util.collections.*
import org.erwinkok.multiformat.multicodec.Multicodec
import org.erwinkok.multiformat.multihash.hashers.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private val logger = KotlinLogging.logger {}

object MultihashRegistry {
    private val hashers: MutableMap<Multicodec, (Int) -> Result<Hasher>> = ConcurrentMap()
    private val defaultLengths: MutableMap<Multicodec, Int> = ConcurrentMap()

    init {
        registerVariableSize(Multicodec.IDENTITY) { Result.success(Identity()) }
        register(Multicodec.DBL_SHA2_256) { Result.success(DoubleSha256()) }
        registerMessageDigest(Multicodec.MD5, "MD5")
        registerMessageDigest(Multicodec.SHA1, "SHA1")
        registerMessageDigest(Multicodec.SHA2_224, "SHA-224")
        registerMessageDigest(Multicodec.SHA2_256, "SHA-256")
        registerMessageDigest(Multicodec.SHA2_384, "SHA-384")
        registerMessageDigest(Multicodec.SHA2_512, "SHA-512")
        registerMessageDigest(Multicodec.SHA2_512_224, "SHA-512/224")
        registerMessageDigest(Multicodec.SHA2_512_256, "SHA-512/256")

        register(Multicodec.SHA3_224) { Result.success(Sha3.sha224()) }
        register(Multicodec.SHA3_256) { Result.success(Sha3.sha256()) }
        register(Multicodec.SHA3_384) { Result.success(Sha3.sha384()) }
        register(Multicodec.SHA3_512) { Result.success(Sha3.sha512()) }
        register(Multicodec.SHAKE_128) { Result.success(Shake.shake128()) }
        register(Multicodec.SHAKE_256) { Result.success(Shake.shake256()) }
        register(Multicodec.KECCAK_256) { Result.success(Sha3.keccak256()) }
        register(Multicodec.KECCAK_512) { Result.success(Sha3.keccak512()) }

        registerBlake2()
        registerBlake3()
    }

    fun register(type: Multicodec, hasherConstructor: () -> Result<Hasher>): Result<Unit> {
        return hasherConstructor()
            .fold(
                { hasher ->
                    val maxSize = hasher.size()
                    hashers[type] = { size ->
                        if (size > maxSize) {
                            Result.failure(IllegalArgumentException("requested length was too large for digest of type: $type"))
                        } else {
                            hasherConstructor()
                        }
                    }
                    defaultLengths[type] = maxSize
                    Result.success(Unit)
                },
                {
                    val message = "Could not register hasher for $type: ${it.message}"
                    logger.warn(message)
                    Result.failure(IllegalArgumentException(message))
                },
            )
    }

    fun registerVariableSize(type: Multicodec, hasher: (Int) -> Result<Hasher>): Result<Unit> {
        return hasher(-1)
            .fold(
                {
                    hashers[type] = hasher
                    defaultLengths[type] = it.size()
                    Result.success(Unit)
                },
                {
                    val message = "Could not register hasher for $type: ${it.message}"
                    logger.warn(message)
                    Result.failure(IllegalArgumentException(message))
                },
            )
    }

    fun registerMessageDigest(type: Multicodec, hasher: String) {
        register(type) {
            try {
                Result.success(MessageDigestHasher(MessageDigest.getInstance(hasher)))
            } catch (e: NoSuchAlgorithmException) {
                Result.failure(IllegalArgumentException("No MessageDigest found for $hasher"))
            }
        }
    }

    fun getHasher(type: Multicodec, sizeHint: Int = -1): Result<Hasher> {
        val hasherFactory = hashers[type] ?: return Result.failure(IllegalArgumentException("No hasher found for: $type"))
        return hasherFactory(sizeHint)
    }

    fun defaultHashSize(codec: Multicodec): Result<Int> {
        val length = defaultLengths[codec] ?: return Result.failure(IllegalArgumentException("No defaulth length for $codec"))
        return Result.success(length)
    }

    private fun registerBlake2() {
        register(Multicodec.BLAKE2S_256) { Blake2s.fromKey(null) }

        for (i in Multicodec.BLAKE2B_8.code..Multicodec.BLAKE2B_512.code) {
            val size = i - Multicodec.BLAKE2B_8.code + 1
            Multicodec.codeToType(i)
                .onSuccess { code -> register(code) { Blake2b.fromKey(null, size) } }
                .onFailure { logger.warn { "Could not register blake2b_${size * 8}: ${it.message}" } }
        }
    }

    private fun registerBlake3() {
        registerVariableSize(Multicodec.BLAKE3) { size ->
            when (size) {
                -1 -> {
                    Result.success(Blake3.fromKey(null, 32))
                }

                in 1..128 -> {
                    Result.success(Blake3.fromKey(null, size))
                }

                else -> {
                    Result.failure(IllegalArgumentException("Unsupported size for Blake3: $size"))
                }
            }
        }
    }
}
