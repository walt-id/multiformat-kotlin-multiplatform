// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multiaddress.components

/*import inet.ipaddr.AddressValueException
import inet.ipaddr.IPAddressString
import inet.ipaddr.ipv4.IPv4Address
import org.erwinkok.multiformat.multiaddress.Protocol
import org.erwinkok.multiformat.multiaddress.Transcoder


class Ip4Component private constructor(private val ipAddress: IPv4Address) : Component(Protocol.IP4, ipAddress.bytes) {
    override val value: String
        get() = ipAddress.toString()

    companion object : Transcoder {
        override fun bytesToComponent(protocol: Protocol, bytes: ByteArray): Result<Ip4Component> {
            return try {
                Result.success(Ip4Component(IPv4Address(bytes)))
            } catch (e: AddressValueException) {
                Result.failure(IllegalArgumentException("Invalid IPv4 address"))
            }
        }

        override fun stringToComponent(protocol: Protocol, string: String): Result<Ip4Component> {
            val ipAddress = IPAddressString(string).address?.toIPv4()
                ?: return Result.failure(IllegalArgumentException("Invalid IPv4 address: $string"))
            return Result.success(Ip4Component(ipAddress))
        }

        fun fromIPv4Address(address: IPv4Address): Result<Ip4Component> {
            return Result.success(Ip4Component(address))
        }
    }
}*/
