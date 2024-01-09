// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.cid

import org.erwinkok.multiformat.multibase.Multibase
import org.erwinkok.multiformat.multicodec.Multicodec
import org.erwinkok.multiformat.multihash.Multihash


class CidBuilder {
    private var version: Int = 1
    private var multicodec: Multicodec? = null
    private var multihash: Multihash? = null
    private var multibase: Multibase? = null
    fun withVersion(version: Int): CidBuilder {
        this.version = version
        return this
    }

    fun withMulticodec(multicodec: Multicodec): CidBuilder {
        this.multicodec = multicodec
        return this
    }

    fun withMultihash(multihash: Multihash): CidBuilder {
        this.multihash = multihash
        return this
    }

    fun withMultibase(multibase: Multibase): CidBuilder {
        this.multibase = multibase
        return this
    }

    fun build(): Result<Cid> {
        if (version != 0 && version != 1) {
            return Result.failure(IllegalArgumentException("Invalid version, must be a number equal to 1 or 0"))
        }
        if (version == 0) {
            if (multicodec != null && multicodec != Multicodec.DAG_PB) {
                return Result.failure(IllegalArgumentException("codec must be 'dag-pb' for CIDv0"))
            }
            if (multibase != null && multibase != Multibase.BASE58_BTC) {
                return Result.failure(IllegalArgumentException("multibase must be 'base58btc' for CIDv0"))
            }
            val h = multihash ?: return Result.failure(IllegalArgumentException("hash must be non-null"))
            return Result.success(CidV0(h))
        }
        val c = multicodec ?: return Result.failure(IllegalArgumentException("codec must be non-null"))
        val h = multihash ?: return Result.failure(IllegalArgumentException("hash must be non-null"))
        return Result.success(CidV1(h, c, multibase ?: Multibase.BASE32))
    }
}
