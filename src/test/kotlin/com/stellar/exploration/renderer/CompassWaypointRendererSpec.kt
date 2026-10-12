package com.stellar.exploration.renderer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.minecraft.resources.Identifier

/**
 * Unit tests for CompassWaypointRenderer.
 */
class CompassWaypointRendererSpec : FunSpec({
    test("resolveScale should return corresponding scale stages based on sprite path") {
        val stage0Id = Identifier.fromNamespaceAndPath("stellar_exploration", "spawn_0")
        val stage1Id = Identifier.fromNamespaceAndPath("stellar_exploration", "spawn_1")
        val stage2Id = Identifier.fromNamespaceAndPath("stellar_exploration", "spawn_2")
        val stage3Id = Identifier.fromNamespaceAndPath("stellar_exploration", "spawn_3")
        val fallbackId = Identifier.fromNamespaceAndPath("stellar_exploration", "spawn_unknown")

        CompassWaypointRenderer.resolveScale(stage0Id) shouldBe CompassWaypointRenderer.SCALE_STAGE_0
        CompassWaypointRenderer.resolveScale(stage1Id) shouldBe CompassWaypointRenderer.SCALE_STAGE_1
        CompassWaypointRenderer.resolveScale(stage2Id) shouldBe CompassWaypointRenderer.SCALE_STAGE_2
        CompassWaypointRenderer.resolveScale(stage3Id) shouldBe CompassWaypointRenderer.SCALE_STAGE_3
        CompassWaypointRenderer.resolveScale(fallbackId) shouldBe CompassWaypointRenderer.SCALE_STAGE_0
    }
})
