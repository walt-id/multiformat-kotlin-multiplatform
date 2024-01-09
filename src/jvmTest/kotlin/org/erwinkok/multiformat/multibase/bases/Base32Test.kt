// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multibase.bases

import org.erwinkok.expectError
import org.erwinkok.multiformat.util.expectNoErrors



import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.util.Random
import java.util.stream.Stream

internal class Base32Test {
    private val pairsUpper = listOf(
        // Pair("", ""),
        Pair("f", "MY======"),
        Pair("fo", "MZXQ===="),
        Pair("foo", "MZXW6==="),
        Pair("foob", "MZXW6YQ="),
        Pair("fooba", "MZXW6YTB"),
        Pair("foobar", "MZXW6YTBOI======"),

        // Wikipedia examples, converted to base32
        Pair("sure.", "ON2XEZJO"),
        Pair("sure", "ON2XEZI="),
        Pair("sur", "ON2XE==="),
        Pair("su", "ON2Q===="),
        Pair("leasure.", "NRSWC43VOJSS4==="),
        Pair("easure.", "MVQXG5LSMUXA===="),
        Pair("asure.", "MFZXK4TFFY======"),
        Pair("sure.", "ON2XEZJO"),
    )

    @TestFactory
    fun encode(): Stream<DynamicTest> {
        return pairsUpper.map { (decoded, encoded) ->
            DynamicTest.dynamicTest("Test $decoded") {
                val got = Base32.encodeStdUpperPad(decoded.toByteArray())
                assertEquals(encoded, got)
            }
        }.stream()
    }

    @TestFactory
    fun decode(): Stream<DynamicTest> {
        return pairsUpper.map { (decoded, encoded) ->
            DynamicTest.dynamicTest("Test $decoded") {
                val got = Base32.decodeStdPad(encoded).expectNoErrors()
                assertEquals(decoded, String(got))
            }
        }.stream()
    }

    @TestFactory
    fun decodeCorrupt(): Stream<DynamicTest> {
        return listOf(
            Pair("", -1),
            Pair("!!!!", 0),
            Pair("!===", 0),
            Pair("AA=A====", 2),
            Pair("AAA=AAAA", 3),
            Pair("MMMMMMMMM", 8),
            Pair("MMMMMM", 0),
            Pair("A=", 1),
            Pair("AA=", 3),
            Pair("AA==", 4),
            Pair("AA===", 5),
            Pair("AAAA=", 5),
            Pair("AAAA==", 6),
            Pair("AAAAA=", 6),
            Pair("AAAAA==", 7),
            Pair("A=======", 1),
            Pair("AA======", -1),
            Pair("AAA=====", 3),
            Pair("AAAA====", -1),
            Pair("AAAAA===", -1),
            Pair("AAAAAA==", 6),
            Pair("AAAAAAA=", -1),
            Pair("AAAAAAAA", -1),
        ).map { (inp, offset) ->
            DynamicTest.dynamicTest("Test $inp") {
                val result = Base32.decodeStdPad(inp)
                if (offset > -1) {
                    expectError("illegal base32 data at input byte $offset") { result }
                } else {
                    result.expectNoErrors()
                }
            }
        }.stream()
    }

    @TestFactory
    fun newLine(): Stream<DynamicTest> {
        return listOf(
            Pair("ON2XEZI=", "sure"),
            Pair("ON2XEZI=\r", "sure"),
            Pair("ON2XEZI=\n", "sure"),
            Pair("ON2XEZI=\r\n", "sure"),
            Pair("ON2XEZ\r\nI=", "sure"),
            Pair("ON2X\rEZ\nI=", "sure"),
            Pair("ON2X\nEZ\rI=", "sure"),
            Pair("ON2XEZ\nI=", "sure"),
            Pair("ON2XEZI\n=", "sure"),
            Pair("MZXW6YTBOI======", "foobar"),
            Pair("MZXW6YTBOI=\r\n=====", "foobar"),
        ).map { (inp, expected) ->
            DynamicTest.dynamicTest("Test $inp") {
                val result = Base32.decodeStdPad(inp).expectNoErrors()
                assertEquals(expected, String(result))
            }
        }.stream()
    }

    @TestFactory
    fun noPadding(): Stream<DynamicTest> {
        return listOf(
            "a",
            "ab",
            "abc",
            "abcd",
            "abcde",
            "",
            String(byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
        ).map { inp ->
            DynamicTest.dynamicTest("Test $inp") {
                val enc = Base32.encodeStdUpperNoPad(inp.toByteArray())
                assertFalse(enc.contains("="))
                val out = Base32.decodeStdNoPad(enc).expectNoErrors()
                assertEquals(inp, String(out))
            }
        }.stream()
    }

    @Test
    fun noPaddingRand() {
        val rand = Random()
        for (i in 0 until 1000) {
            val l = rand.nextInt(1024)
            val buf = ByteArray(l)
            rand.nextBytes(buf)
            val enc = Base32.encodeStdUpperNoPad(buf)
            assertFalse(enc.contains("="))
            val out = Base32.decodeStdNoPad(enc).expectNoErrors()
            assertArrayEquals(buf, out)
        }
    }
}
