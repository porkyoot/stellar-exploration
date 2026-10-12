package com.stellar.exploration.framework

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Central framework engine coordinating exploration features, telemetry updates, and lifecycle events.
 */
object ExplorationEngine {
    private val logger: Logger = LoggerFactory.getLogger(ExplorationEngine::class.java)

    private val features: ConcurrentHashMap<String, ExplorationFeature> = ConcurrentHashMap()
    private val listeners: CopyOnWriteArrayList<(ExplorationEvent) -> Unit> = CopyOnWriteArrayList()

    @Volatile
    private var currentTelemetry: ExplorationTelemetry = ExplorationTelemetry.EMPTY

    val telemetry: ExplorationTelemetry
        get() = currentTelemetry

    /**
     * Initializes the exploration engine framework.
     */
    fun initialize() {
        logger.info("Initializing ExplorationEngine framework with {} registered features", features.size)
        for (feature in features.values) {
            feature.onInitialize()
        }
    }

    /**
     * Registers a new exploration feature into the framework.
     */
    fun registerFeature(feature: ExplorationFeature): Boolean {
        val previous = features.putIfAbsent(feature.id, feature)
        return if (previous == null) {
            logger.info("Registered exploration feature: {} ({})", feature.name, feature.id)
            feature.onInitialize()
            dispatchEvent(ExplorationEvent.FeatureRegistered(feature.id))
            true
        } else {
            logger.warn("Exploration feature with ID '{}' is already registered", feature.id)
            false
        }
    }

    /**
     * Unregisters an exploration feature by its identifier.
     */
    fun unregisterFeature(featureId: String): Boolean {
        val removed = features.remove(featureId)
        return if (removed != null) {
            logger.info("Unregistered exploration feature: {} ({})", removed.name, removed.id)
            dispatchEvent(ExplorationEvent.FeatureUnregistered(featureId))
            true
        } else {
            false
        }
    }

    /**
     * Retrieves all registered exploration features as an unmodifiable list.
     */
    fun getFeatures(): List<ExplorationFeature> = features.values.toList()

    /**
     * Subscribes a listener to receive exploration events.
     * Returns an [AutoCloseable] that cancels the subscription when closed.
     */
    fun subscribe(listener: (ExplorationEvent) -> Unit): AutoCloseable {
        listeners.add(listener)
        return AutoCloseable { listeners.remove(listener) }
    }

    /**
     * Updates client exploration telemetry and dispatches relevant lifecycle events.
     */
    fun updateTelemetry(newTelemetry: ExplorationTelemetry) {
        val previous = currentTelemetry
        currentTelemetry = newTelemetry

        checkDimensionChange(previous.dimension, newTelemetry.dimension)
        checkBiomeChange(previous.biome, newTelemetry.biome)

        dispatchEvent(ExplorationEvent.TelemetryUpdated(newTelemetry))

        dispatchFeatureTicks(newTelemetry)
    }

    private fun checkDimensionChange(previous: String, current: String) {
        if (previous != current) {
            dispatchEvent(ExplorationEvent.DimensionChanged(previous, current))
        }
    }

    private fun checkBiomeChange(previous: String, current: String) {
        if (previous != current) {
            dispatchEvent(ExplorationEvent.BiomeChanged(previous, current))
        }
    }

    private fun dispatchFeatureTicks(telemetry: ExplorationTelemetry) {
        for (feature in features.values) {
            if (feature.isEnabled) {
                feature.onTick(telemetry)
            }
        }
    }

    private fun dispatchEvent(event: ExplorationEvent) {
        for (listener in listeners) {
            listener(event)
        }
        for (feature in features.values) {
            if (feature.isEnabled) {
                feature.onEvent(event)
            }
        }
    }

    /**
     * Resets engine state for testing or disconnection teardown.
     */
    fun reset() {
        features.clear()
        listeners.clear()
        currentTelemetry = ExplorationTelemetry.EMPTY
    }
}
