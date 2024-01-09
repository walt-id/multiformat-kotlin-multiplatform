// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multibase.bases


import kotlin.experimental.and

object Base2 {
    fun encode(data: ByteArray): String {
        val out = StringBuilder()
        for (b in data) {
            for (j in 0 until 8) {
                if (b and (1 shl (7 - j)).toByte() == 0u.toByte()) {
                    out.append('0')
                } else {
                    out.append('1')
                }
            }
        }
        return out.toString()
    }

    fun decode(data: String): Result<ByteArray> {
        try {
            val vdata = if ((data.length and 7) != 0) {
                // prepend the padding
                "0".repeat(8 - (data.length and 7)) + data
            } else {
                data
            }
            val out = ByteArray(vdata.length shr 3)
            for ((dstIndex, i) in (vdata.indices step 8).withIndex()) {
                val value = Integer.parseInt(vdata, i, i + 8, 2)
                out[dstIndex] = value.toByte()
            }
            return Result.success(out)
        } catch (e: Exception) {
            return Result.failure(IllegalArgumentException("Could not convert $data to ByteArray: ${e.message}"))
        }
    }
}
