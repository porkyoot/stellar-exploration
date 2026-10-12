package com.stellar.exploration.waypoints

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.minecraft.util.ARGB

/**
 * Unit tests for ColorHandler utility.
 */
class ColorHandlerSpec : FunSpec({
    test("should parse valid hex codes") {
        ColorHandler.parseHexCode("#FF0000") shouldBe ARGB.color(255, 0xFF0000)
        ColorHandler.parseHexCode("#00FF00") shouldBe ARGB.color(255, 0x00FF00)
        ColorHandler.parseHexCode("#0000FF") shouldBe ARGB.color(255, 0x0000FF)
        ColorHandler.parseHexCode("#AABBCC") shouldBe ARGB.color(255, 0xAABBCC)
    }

    test("should return null on invalid hex codes") {
        ColorHandler.parseHexCode("invalid") shouldBe null
        ColorHandler.parseHexCode("#12") shouldBe null
        ColorHandler.parseHexCode(null) shouldBe null
    }

    test("should extract color from string with bracketed hex") {
        ColorHandler.getColor("My Base [#00FF88]") shouldBe ARGB.color(255, 0x00FF88)
        ColorHandler.getColor("Spawn #FF5555") shouldBe ARGB.color(255, 0xFF5555)
    }

    test("should strip color codes from item names") {
        ColorHandler.removeColorCodes("My Base [#00FF88]") shouldBe "My Base"
        ColorHandler.removeColorCodes("Portal &aGreen") shouldBe "Portal Green"
        ColorHandler.removeColorCodes("Clean Name") shouldBe "Clean Name"
    }
})
