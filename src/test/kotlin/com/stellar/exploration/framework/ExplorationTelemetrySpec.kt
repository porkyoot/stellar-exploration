package com.stellar.exploration.framework

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests verifying ExplorationTelemetry immutability and formatting.
 */
class ExplorationTelemetrySpec : FunSpec({
    test("factory should compute block coordinates and cardinal direction") {
        val telemetry = ExplorationTelemetry.create(
            x = 128.75,
            y = 64.0,
            z = -256.25,
            dimension = "minecraft:overworld",
            biome = "minecraft:desert",
            yaw = 180.0f,
            pitch = 15.0f,
        )

        telemetry.x shouldBe 128.75
        telemetry.y shouldBe 64.0
        telemetry.z shouldBe -256.25
        telemetry.blockX shouldBe 128
        telemetry.blockY shouldBe 64
        telemetry.blockZ shouldBe -257
        telemetry.dimension shouldBe "minecraft:overworld"
        telemetry.biome shouldBe "minecraft:desert"
        telemetry.cardinalDirection shouldBe CardinalDirection.NORTH
        telemetry.formatCoordinates() shouldBe "128, 64, -257"
        telemetry.formatCompass() shouldBe "N (180°)"
    }

    test("empty constant should provide safe defaults") {
        val empty = ExplorationTelemetry.EMPTY

        empty.blockX shouldBe 0
        empty.blockY shouldBe 0
        empty.blockZ shouldBe 0
        empty.dimension shouldBe ExplorationTelemetry.DEFAULT_DIMENSION
        empty.biome shouldBe ExplorationTelemetry.DEFAULT_BIOME
        empty.cardinalDirection shouldBe CardinalDirection.SOUTH
    }
})
