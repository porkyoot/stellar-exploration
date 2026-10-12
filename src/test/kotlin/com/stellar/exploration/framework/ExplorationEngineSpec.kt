package com.stellar.exploration.framework

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

/**
 * Unit tests verifying ExplorationEngine lifecycle, feature dispatch, and event bus.
 */
class ExplorationEngineSpec : FunSpec({
    beforeEach {
        ExplorationEngine.reset()
    }

    test("should register and unregister features") {
        val feature = object : ExplorationFeature {
            override val id: String = "test_feature"
            override val name: String = "Test Feature"
        }

        ExplorationEngine.registerFeature(feature) shouldBe true
        ExplorationEngine.registerFeature(feature) shouldBe false
        ExplorationEngine.getFeatures().map { it.id } shouldContainExactly listOf("test_feature")

        ExplorationEngine.unregisterFeature("test_feature") shouldBe true
        ExplorationEngine.unregisterFeature("test_feature") shouldBe false
        ExplorationEngine.getFeatures().isEmpty() shouldBe true
    }

    test("should dispatch telemetry updates and lifecycle events to listeners") {
        val receivedEvents = mutableListOf<ExplorationEvent>()
        val subscription = ExplorationEngine.subscribe { receivedEvents.add(it) }

        val initial = ExplorationTelemetry.create(
            x = 0.0,
            y = 70.0,
            z = 0.0,
            dimension = "minecraft:overworld",
            biome = "minecraft:plains",
            yaw = 0.0f,
            pitch = 0.0f,
        )
        ExplorationEngine.updateTelemetry(initial)

        val movedToNether = ExplorationTelemetry.create(
            x = 10.0,
            y = 60.0,
            z = 10.0,
            dimension = "minecraft:the_nether",
            biome = "minecraft:nether_wastes",
            yaw = 90.0f,
            pitch = 0.0f,
        )
        ExplorationEngine.updateTelemetry(movedToNether)

        subscription.close()

        val eventTypes = receivedEvents.map { it::class.simpleName }
        eventTypes.contains("DimensionChanged") shouldBe true
        eventTypes.contains("BiomeChanged") shouldBe true
        eventTypes.contains("TelemetryUpdated") shouldBe true
    }

    test("should dispatch tick updates to enabled features") {
        var tickCount = 0
        val feature = object : ExplorationFeature {
            override val id: String = "tick_test"
            override val name: String = "Tick Test"
            override fun onTick(telemetry: ExplorationTelemetry) {
                tickCount++
            }
        }

        ExplorationEngine.registerFeature(feature)
        ExplorationEngine.updateTelemetry(ExplorationTelemetry.EMPTY)
        ExplorationEngine.updateTelemetry(ExplorationTelemetry.EMPTY)

        tickCount shouldBe 2
    }
})
