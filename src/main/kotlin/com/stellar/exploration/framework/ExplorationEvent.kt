package com.stellar.exploration.framework

/**
 * Sealed event hierarchy representing exploration framework lifecycle and telemetry updates.
 */
sealed interface ExplorationEvent {
    data class TelemetryUpdated(val telemetry: ExplorationTelemetry) : ExplorationEvent
    data class DimensionChanged(val previousDimension: String, val currentDimension: String) : ExplorationEvent
    data class BiomeChanged(val previousBiome: String, val currentBiome: String) : ExplorationEvent
    data class FeatureRegistered(val featureId: String) : ExplorationEvent
    data class FeatureUnregistered(val featureId: String) : ExplorationEvent
}
