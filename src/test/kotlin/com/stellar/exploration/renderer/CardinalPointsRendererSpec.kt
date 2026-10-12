package com.stellar.exploration.renderer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for CardinalPointsRenderer.
 */
class CardinalPointsRendererSpec : FunSpec({
    test("resolveScale should return full scale for major cardinals and smaller scale for intermediates") {
        CardinalPointsRenderer.resolveScale(isMajor = true) shouldBe CardinalPointsRenderer.MAJOR_SCALE
        CardinalPointsRenderer.resolveScale(isMajor = false) shouldBe CardinalPointsRenderer.INTERMEDIATE_SCALE
        CardinalPointsRenderer.MAJOR_SCALE shouldBe 1.0f
        CardinalPointsRenderer.INTERMEDIATE_SCALE shouldBe 0.75f
    }
})
