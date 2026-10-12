package com.stellar.exploration.waypoints

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import java.util.UUID

/**
 * Unit tests for WaypointTracking registry and styling constants.
 */
class WaypointTrackingSpec : FunSpec({
    beforeTest {
        WaypointTracking.reset()
    }

    test("should have valid style asset resource keys") {
        WaypointTracking.LODESTONE_STYLE.identifier().toString() shouldBe "stellar_exploration:lodestone"
        WaypointTracking.DEATH_STYLE.identifier().toString() shouldBe "stellar_exploration:death"
        WaypointTracking.BANNER_STYLE.identifier().toString() shouldBe "stellar_exploration:banner"
        WaypointTracking.SPAWN_STYLE.identifier().toString() shouldBe "stellar_exploration:spawn"
    }

    test("should initialize with empty tracked waypoints") {
        WaypointTracking.getAllWaypoints().shouldBeEmpty()
        WaypointTracking.getWaypointName(UUID.randomUUID()) shouldBe null
    }

    test("reset should clear all state") {
        WaypointTracking.markDirty()
        WaypointTracking.reset()
        WaypointTracking.getAllWaypoints().shouldBeEmpty()
    }
})
