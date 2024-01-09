// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multiaddress.components

import org.erwinkok.multiformat.multiaddress.Protocol
import org.erwinkok.multiformat.util.CustomStream
import org.erwinkok.multiformat.util.toByteArray


abstract class Component protected constructor(val protocol: Protocol, val addressBytes: ByteArray) {
    abstract val value: String

    fun writeTo(outputStream: CustomStream<Byte>) {
        Protocol.writeTo(outputStream, protocol, addressBytes)
    }

    fun writeTo(stringBuilder: StringBuilder) {
        Protocol.writeTo(stringBuilder, protocol, value)
    }

    fun bytes(): ByteArray {
        val out = CustomStream<Byte>()
        writeTo(out)
        return out.toByteArray()
    }

    override fun toString(): String {
        val sb = StringBuilder()
        writeTo(sb)
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Component) {
            return false
        }
        return addressBytes.contentEquals(other.addressBytes)
    }

    override fun hashCode(): Int {
        return addressBytes.hashCode()
    }
}
