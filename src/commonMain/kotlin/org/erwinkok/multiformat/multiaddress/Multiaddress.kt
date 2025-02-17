// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multiaddress

import org.erwinkok.multiformat.multiaddress.components.Component
import org.erwinkok.multiformat.multibase.Multibase
import org.erwinkok.multiformat.multihash.Multihash
import org.erwinkok.multiformat.util.CustomStream
import org.erwinkok.multiformat.util.flatMap
import org.erwinkok.multiformat.util.toCustomStream

class Multiaddress private constructor(val components: List<Component>) {
    private val _string: String by lazy { constructString() }
    val bytes: ByteArray by lazy { constructBytes() }

    fun encapsulate(address: String): Result<Multiaddress> {
        return fromString(address)
            .flatMap { encapsulate(it) }
    }

    fun encapsulate(multiAddress: Multiaddress): Result<Multiaddress> {
        val thisPath = this.toString()
        return if (thisPath == "/") {
            fromMultiaddress(multiAddress)
        } else {
            fromString(thisPath + multiAddress)
        }
    }

    fun decapsulate(multiAddress: Multiaddress): Result<Multiaddress> {
        return decapsulate(multiAddress.toString())
    }

    fun decapsulate(address: String): Result<Multiaddress> {
        val s = this.toString()
        val i = s.lastIndexOf(address)
        return if (i < 0) {
            fromMultiaddress(this)
        } else {
            fromString(s.substring(0, i))
        }
    }

    fun protocols(): List<Protocol> {
        return components.map { obj -> obj.protocol }
    }

    fun valueForProtocol(p: Protocol): String? {
        return components.filter { i -> i.protocol == p }.map { it.value }.firstOrNull()
    }

    fun hasProtocol(p: Protocol): Boolean {
        return components.any { i -> i.protocol == p }
    }

    override fun toString(): String {
        return _string
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is Multiaddress) {
            return super.equals(other)
        }
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    private fun constructBytes(): ByteArray {
        val outputStream = CustomStream()
        components.forEach {
            Protocol.writeTo(outputStream, it.protocol, it.addressBytes)
        }
        return outputStream.toByteArray()
    }

    private fun constructString(): String {
        val stringBuilder = StringBuilder()
        components.forEach {
            Protocol.writeTo(stringBuilder, it.protocol, it.value)
        }
        return cleanPath(stringBuilder.toString())
    }

    private fun cleanPath(str: String): String {
        val split = str.trim { it <= ' ' }.split("/")
        return "/" + split.filter { cs -> cs.isNotBlank() }.joinToString("/")
    }

    companion object {
        private const val MaxBytes = 1024

        fun fromMultihash(hash: Multihash): Result<Multiaddress> {
            val encoded = Multibase.BASE58_BTC.encode(hash.digest)
            return fromString("/ipfs/" + encoded.substring(1))
        }

        fun fromString(address: String): Result<Multiaddress> {
            if (address.length > MaxBytes) {
                return Result.failure(IllegalArgumentException("Multiaddress is too long, over $MaxBytes bytes. Rejecting."))
            }
            if (address.isNotEmpty() && address[0] != '/') {
                return Result.failure(IllegalArgumentException("multiaddress $address must start with a '/'"))
            }
            val parts = address.trimEnd('/').split("/").toMutableList()
            if (parts.removeFirst().isNotBlank()) {
                return Result.failure(IllegalArgumentException("failed to parse Multiaddress ${address.trimEnd('/')}: must begin with /"))
            }
            val components = mutableListOf<Component>()
            while (parts.isNotEmpty()) {
                val (protocol, addressString) = Protocol.readFrom(parts)
                    .getOrElse { return Result.failure(IllegalArgumentException("failed to parse Multiaddress $address: ${it.message}")) }
                val component = protocol.stringToComponent(addressString)
                    .getOrElse { return Result.failure(IllegalArgumentException("failed to parse Multiaddress $address: ${it.message}")) }
                components.add(component)
            }
            return Result.success(Multiaddress(components))
        }

        fun fromBytes(bytes: ByteArray): Result<Multiaddress> {
            if (bytes.size > MaxBytes) {
                return Result.failure(IllegalArgumentException("Multiaddress is too long, over $MaxBytes bytes. Rejecting."))
            }
            val stream = bytes.toCustomStream()
            val components = mutableListOf<Component>()
            while (stream.available() > 0) {
                val (protocol, addressBytes) = Protocol.readFrom(stream)
                    .getOrElse { return Result.failure(it) }
                val component = protocol.bytesToComponent(addressBytes)
                    .getOrElse { return Result.failure(it) }
                components.add(component)
            }
            return Result.success(Multiaddress(components))
        }

        private fun fromMultiaddress(other: Multiaddress): Result<Multiaddress> {
            return Result.success(Multiaddress(other.components))
        }
    }
}
