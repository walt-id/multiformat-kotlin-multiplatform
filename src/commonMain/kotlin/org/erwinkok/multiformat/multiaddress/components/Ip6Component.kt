// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multiaddress.components

/*
import inet.ipaddr.AddressValueException
import inet.ipaddr.IPAddressString
import inet.ipaddr.ipv6.IPv6Address
import org.erwinkok.multiformat.multiaddress.Protocol
import org.erwinkok.multiformat.multiaddress.Transcoder


class Ip6Component private constructor(private val ipAddress: IPv6Address) : Component(Protocol.IP6, ipAddress.bytes) {
    override val value: String
        get() = ipAddress.toString()

    companion object : Transcoder {
        override fun bytesToComponent(protocol: Protocol, bytes: ByteArray): Result<Ip6Component> {
            return try {
                Result.success(Ip6Component(IPv6Address(bytes)))
            } catch (e: AddressValueException) {
                Result.failure(IllegalArgumentException("Invalid IPv6 address"))
            }
        }

        override fun stringToComponent(protocol: Protocol, string: String): Result<Ip6Component> {
            val ipAddress = IPAddressString(string).address?.toIPv6()
                ?: return Result.failure(IllegalArgumentException("Invalid IPv6 address: $string"))
            return Result.success(Ip6Component(ipAddress))
        }

        fun fromIPv6Address(address: IPv6Address): Result<Ip6Component> {
            return Result.success(Ip6Component(address))
        }
    }
}
*/
