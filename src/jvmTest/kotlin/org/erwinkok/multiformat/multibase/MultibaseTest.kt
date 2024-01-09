// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
package org.erwinkok.multiformat.multibase

import io.github.oshai.kotlinlogging.KotlinLogging
import org.erwinkok.multiformat.util.expectNoErrors
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.io.path.bufferedReader
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

private val logger = KotlinLogging.logger {}

internal class MultibaseTest {
    private data class MultibaseSpec(
        val name: String,
        val code: String,
        val description: String,
        val status: String,
    )

    @Test
    fun `test specification`() {
        val multibaseSpec = readMultibaseSpec()
        for (mb in Multibase.values()) {
            assertTrue(multibaseSpec.containsKey(mb.encoding), "Specification does define ${mb.encoding}")
            val spec = multibaseSpec[mb.encoding]!!
            assertEquals(spec.code, mb.code, "Code mismatch for ${mb.encoding}: ${spec.code} != ${mb.code}")
            assertEquals(spec.name, spec.name, "Name mismatch for ${mb.encoding}: ${spec.name} != ${mb.name}")
        }
    }

    @Test
    fun `test vectors`() {
        Files.walk(Paths.get("spec/multibase/tests/"), 1).forEach { file ->
            if (file.isRegularFile() && file.isReadable() && file.name.endsWith(".csv")) {
                logger.info { "Processing ${file.fileName}" }
                val reader = file.bufferedReader()
                val (header0, header1) = reader.readLine().split(',', ignoreCase = false, limit = 2)
                assertTrue(header0.trim() == "encoding" || header0.trim() == "non-canonical encoding", "Encoding $header0 is not supported")
                val testValue = header1.trim().removeSurrounding("\"").replace("\\x00", "\u0000").toByteArray()
                reader.forEachLine {
                    val (encoding, expected) = it.split(',', ignoreCase = false, limit = 2)
                    logger.info { "Check encoding $encoding" }
                    Multibase.nameToBase(encoding.trim())
                        .onSuccess { base ->
                            val expectedCleaned = expected.trim().removeSurrounding("\"")
                            if (header0.trim() != "non-canonical encoding") {
                                val actualEncoded = base.encode(testValue)
                                assertEquals(expectedCleaned, actualEncoded, "Mismatch for $base")
                            }
                            assertEquals(base, Multibase.encoding(expectedCleaned).expectNoErrors())
                            val actualDecoded = Multibase.decode(expectedCleaned).expectNoErrors()
                            assertArrayEquals(testValue, actualDecoded, "Mismatch for $base")
                        }
                        .onFailure {
                            logger.warn { "Encoding $encoding not supported" }
                        }
                }
            }
        }
    }

    @TestFactory
    fun `decode as string`(): Stream<DynamicTest> {
        return arguments.map { (name, input, output) ->
            DynamicTest.dynamicTest(name) {
                val bytes = Multibase.decode(output).expectNoErrors()
                assertEquals(input, String(bytes))
            }
        }.stream()
    }

    @TestFactory
    fun `encode name`(): Stream<DynamicTest> {
        return arguments.map { (name, _, output) ->
            DynamicTest.dynamicTest(name) {
                val multibase = Multibase.encoding(output).expectNoErrors()
                val base = Multibase.nameToBase(name).expectNoErrors()
                assertEquals(multibase, base)
            }
        }.stream()
    }

    @TestFactory
    fun `multibase encode`(): Stream<DynamicTest> {
        return arguments.map { (name, input, output) ->
            DynamicTest.dynamicTest(name) {
                val buf = input.toByteArray()
                val multibasedBuf = Multibase.encode(name, buf).expectNoErrors()
                assertEquals(output, multibasedBuf, "Mismatch for $input")
            }
        }.stream()
    }

    @TestFactory
    fun `multibase decode`(): Stream<DynamicTest> {
        return arguments.map { (name, input, output) ->
            DynamicTest.dynamicTest(name) {
                val bytes = Multibase.decode(output).expectNoErrors()
                assertArrayEquals(bytes, input.toByteArray(), "Mismatch for $input")
            }
        }.stream()
    }

    @Test
    fun `should allow Base32Pad`() {
        val bytes = Multibase.decode("ctimaq4ygg2iegci7").expectNoErrors()
        val encoded = Multibase.BASE32_PAD.encode(bytes)
        assertEquals("ctimaq4ygg2iegci7", encoded)
    }

    companion object {
        var arguments = listOf(
            Triple("base16", "f", "f66"),
            Triple("base16", "fo", "f666f"),
            Triple("base16", "foo", "f666f6f"),
            Triple("base16", "foob", "f666f6f62"),
            Triple("base16", "fooba", "f666f6f6261"),
            Triple("base16", "foobar", "f666f6f626172"),
            Triple("base32", "yes mani !", "bpfsxgidnmfxgsibb"),
            Triple("base32", "f", "bmy"),
            Triple("base32", "fo", "bmzxq"),
            Triple("base32", "foo", "bmzxw6"),
            Triple("base32", "foob", "bmzxw6yq"),
            Triple("base32", "fooba", "bmzxw6ytb"),
            Triple("base32", "foobar", "bmzxw6ytboi"),
            Triple("base32pad", "yes mani !", "cpfsxgidnmfxgsibb"),
            Triple("base32pad", "f", "cmy======"),
            Triple("base32pad", "fo", "cmzxq===="),
            Triple("base32pad", "foo", "cmzxw6==="),
            Triple("base32pad", "foob", "cmzxw6yq="),
            Triple("base32pad", "fooba", "cmzxw6ytb"),
            Triple("base32pad", "foobar", "cmzxw6ytboi======"),
            Triple("base32hex", "yes mani !", "vf5in683dc5n6i811"),
            Triple("base32hex", "f", "vco"),
            Triple("base32hex", "fo", "vcpng"),
            Triple("base32hex", "foo", "vcpnmu"),
            Triple("base32hex", "foob", "vcpnmuog"),
            Triple("base32hex", "fooba", "vcpnmuoj1"),
            Triple("base32hex", "foobar", "vcpnmuoj1e8"),
            Triple("base32hexpad", "yes mani !", "tf5in683dc5n6i811"),
            Triple("base32hexpad", "f", "tco======"),
            Triple("base32hexpad", "fo", "tcpng===="),
            Triple("base32hexpad", "foo", "tcpnmu==="),
            Triple("base32hexpad", "foob", "tcpnmuog="),
            Triple("base32hexpad", "fooba", "tcpnmuoj1"),
            Triple("base32hexpad", "foobar", "tcpnmuoj1e8======"),
            Triple("base32z", "yes mani !", "hxf1zgedpcfzg1ebb"),
            Triple("base58flickr", "yes mani !", "Z7Pznk19XTTzBtx"),
            Triple("base58btc", "yes mani !", "z7paNL19xttacUY"),
            Triple("base64", "÷ïÿ", "mw7fDr8O/"),
            Triple("base64", "f", "mZg"),
            Triple("base64", "fo", "mZm8"),
            Triple("base64", "foo", "mZm9v"),
            Triple("base64", "foob", "mZm9vYg"),
            Triple("base64", "fooba", "mZm9vYmE"),
            Triple("base64", "foobar", "mZm9vYmFy"),
            Triple("base64", "÷ïÿ🥰÷ïÿ😎🥶🤯", "mw7fDr8O/8J+lsMO3w6/Dv/CfmI7wn6W28J+krw"),
            Triple("base64pad", "f", "MZg=="),
            Triple("base64pad", "fo", "MZm8="),
            Triple("base64pad", "foo", "MZm9v"),
            Triple("base64pad", "foob", "MZm9vYg=="),
            Triple("base64pad", "fooba", "MZm9vYmE="),
            Triple("base64pad", "foobar", "MZm9vYmFy"),
            Triple("base64url", "÷ïÿ", "uw7fDr8O_"),
            Triple("base64url", "÷ïÿ🥰÷ïÿ😎🥶🤯", "uw7fDr8O_8J-lsMO3w6_Dv_CfmI7wn6W28J-krw"),
            Triple("base64urlpad", "f", "UZg=="),
            Triple("base64urlpad", "fo", "UZm8="),
            Triple("base64urlpad", "foo", "UZm9v"),
            Triple("base64urlpad", "foob", "UZm9vYg=="),
            Triple("base64urlpad", "fooba", "UZm9vYmE="),
            Triple("base64urlpad", "foobar", "UZm9vYmFy"),
            Triple("base64urlpad", "÷ïÿ🥰÷ïÿ😎🥶🤯", "Uw7fDr8O_8J-lsMO3w6_Dv_CfmI7wn6W28J-krw=="),
            Triple("identity", "Decentralize everything!!", "\u0000Decentralize everything!!"),
            Triple(
                "base2",
                "Decentralize everything!!",
                "001000100011001010110001101100101011011100111010001110010011000010110110001101001011110100110010100100000011001010111011001100101011100100111100101110100011010000110100101101110011001110010000100100001",
            ),
            Triple("base8", "Decentralize everything!!", "72106254331267164344605543227514510062566312711713506415133463441102"),
            Triple("base10", "Decentralize everything!!", "9429328951066508984658627669258025763026247056774804621697313"),
            Triple("base16", "Decentralize everything!!", "f446563656e7472616c697a652065766572797468696e672121"),
            Triple("base16upper", "Decentralize everything!!", "F446563656E7472616C697A652065766572797468696E672121"),
            Triple("base32", "Decentralize everything!!", "birswgzloorzgc3djpjssazlwmvzhs5dinfxgoijb"),
            Triple("base32upper", "Decentralize everything!!", "BIRSWGZLOORZGC3DJPJSSAZLWMVZHS5DINFXGOIJB"),
            Triple("base32hex", "Decentralize everything!!", "v8him6pbeehp62r39f9ii0pbmclp7it38d5n6e891"),
            Triple("base32hexupper", "Decentralize everything!!", "V8HIM6PBEEHP62R39F9II0PBMCLP7IT38D5N6E891"),
            Triple("base32pad", "Decentralize everything!!", "cirswgzloorzgc3djpjssazlwmvzhs5dinfxgoijb"),
            Triple("base32padupper", "Decentralize everything!!", "CIRSWGZLOORZGC3DJPJSSAZLWMVZHS5DINFXGOIJB"),
            Triple("base32hexpad", "Decentralize everything!!", "t8him6pbeehp62r39f9ii0pbmclp7it38d5n6e891"),
            Triple("base32hexpadupper", "Decentralize everything!!", "T8HIM6PBEEHP62R39F9II0PBMCLP7IT38D5N6E891"),
            Triple("base32z", "Decentralize everything!!", "het1sg3mqqt3gn5djxj11y3msci3817depfzgqejb"),
            Triple("base36", "Decentralize everything!!", "k343ixo7d49hqj1ium15pgy1wzww5fxrid21td7l"),
            Triple("base36upper", "Decentralize everything!!", "K343IXO7D49HQJ1IUM15PGY1WZWW5FXRID21TD7L"),
            Triple("base58flickr", "Decentralize everything!!", "Ztwe7gVTeK8wswS1gf8hrgAua9fcw9reboD"),
            Triple("base58btc", "Decentralize everything!!", "zUXE7GvtEk8XTXs1GF8HSGbVA9FCX9SEBPe"),
            Triple("base64", "Decentralize everything!!", "mRGVjZW50cmFsaXplIGV2ZXJ5dGhpbmchIQ"),
            Triple("base64pad", "Decentralize everything!!", "MRGVjZW50cmFsaXplIGV2ZXJ5dGhpbmchIQ=="),
            Triple("base64url", "Decentralize everything!!", "uRGVjZW50cmFsaXplIGV2ZXJ5dGhpbmchIQ"),
            Triple("base64urlpad", "Decentralize everything!!", "URGVjZW50cmFsaXplIGV2ZXJ5dGhpbmchIQ=="),
            Triple("identity", "hello world", "\u0000hello world"),
            Triple("base2", "hello world", "00110100001100101011011000110110001101111001000000111011101101111011100100110110001100100"),
            Triple("base8", "hello world", "7320625543306744035667562330620"),
            Triple("base10", "hello world", "9126207244316550804821666916"),
            Triple("base16", "hello world", "f68656c6c6f20776f726c64"),
            Triple("base16upper", "hello world", "F68656C6C6F20776F726C64"),
            Triple("base32", "hello world", "bnbswy3dpeb3w64tmmq"),
            Triple("base32upper", "hello world", "BNBSWY3DPEB3W64TMMQ"),
            Triple("base32hex", "hello world", "vd1imor3f41rmusjccg"),
            Triple("base32hexupper", "hello world", "VD1IMOR3F41RMUSJCCG"),
            Triple("base32pad", "hello world", "cnbswy3dpeb3w64tmmq======"),
            Triple("base32padupper", "hello world", "CNBSWY3DPEB3W64TMMQ======"),
            Triple("base32hexpad", "hello world", "td1imor3f41rmusjccg======"),
            Triple("base32hexpadupper", "hello world", "TD1IMOR3F41RMUSJCCG======"),
            Triple("base32z", "hello world", "hpb1sa5dxrb5s6hucco"),
            Triple("base36", "hello world", "kfuvrsivvnfrbjwajo"),
            Triple("base36upper", "hello world", "KFUVRSIVVNFRBJWAJO"),
            Triple("base58flickr", "hello world", "ZrTu1dk6cWsRYjYu"),
            Triple("base58btc", "hello world", "zStV1DL6CwTryKyV"),
            Triple("base64", "hello world", "maGVsbG8gd29ybGQ"),
            Triple("base64pad", "hello world", "MaGVsbG8gd29ybGQ="),
            Triple("base64url", "hello world", "uaGVsbG8gd29ybGQ"),
            Triple("base64urlpad", "hello world", "UaGVsbG8gd29ybGQ="),
        )
    }

    private fun readMultibaseSpec(): Map<String, MultibaseSpec> {
        val reader = File("spec/multibase/multibase.csv").bufferedReader()
        reader.readLine()
        return reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                val (name, code, description, status) = it.split(',', ignoreCase = false, limit = 4)
                val sCode = code.trim()
                val cCode = if (sCode.startsWith("0x")) {
                    Integer.decode(sCode).toChar().toString()
                } else {
                    sCode
                }
                MultibaseSpec(name.trim(), cCode, description.trim(), status.trim())
            }.map { it.name to it }.toMap()
    }
}
