package com.stellar.exploration.framework

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests verifying cardinal direction resolving and yaw normalization.
 */
class CardinalDirectionSpec : FunSpec({
    test("should resolve exact bearings accurately") {
        CardinalDirection.fromYaw(0.0f) shouldBe CardinalDirection.SOUTH
        CardinalDirection.fromYaw(45.0f) shouldBe CardinalDirection.SOUTH_WEST
        CardinalDirection.fromYaw(90.0f) shouldBe CardinalDirection.WEST
        CardinalDirection.fromYaw(135.0f) shouldBe CardinalDirection.NORTH_WEST
        CardinalDirection.fromYaw(180.0f) shouldBe CardinalDirection.NORTH
        CardinalDirection.fromYaw(225.0f) shouldBe CardinalDirection.NORTH_EAST
        CardinalDirection.fromYaw(270.0f) shouldBe CardinalDirection.EAST
        CardinalDirection.fromYaw(315.0f) shouldBe CardinalDirection.SOUTH_EAST
    }

    test("should normalize negative angles and wraparound angles") {
        CardinalDirection.fromYaw(-90.0f) shouldBe CardinalDirection.EAST
        CardinalDirection.fromYaw(-180.0f) shouldBe CardinalDirection.NORTH
        CardinalDirection.fromYaw(360.0f) shouldBe CardinalDirection.SOUTH
        CardinalDirection.fromYaw(450.0f) shouldBe CardinalDirection.WEST
    }

    test("should resolve intermediate angles within sector tolerances") {
        CardinalDirection.fromYaw(10.0f) shouldBe CardinalDirection.SOUTH
        CardinalDirection.fromYaw(350.0f) shouldBe CardinalDirection.SOUTH
        CardinalDirection.fromYaw(80.0f) shouldBe CardinalDirection.WEST
        CardinalDirection.fromYaw(195.0f) shouldBe CardinalDirection.NORTH
    }
})
