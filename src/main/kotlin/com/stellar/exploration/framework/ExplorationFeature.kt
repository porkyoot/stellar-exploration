package com.stellar.exploration.framework

/**
 * Service Provider Interface (SPI) for modular client-side exploration components.
 */
interface ExplorationFeature {
    /**
     * Unique identifier for this exploration feature (e.g. "waypoint_tracker", "compass_hud").
     */
    val id: String

    /**
     * Human-readable name for logging and diagnostic purposes.
     */
    val name: String

    /**
     * Indicates whether this feature is currently active.
     */
    val isEnabled: Boolean get() = true

    /**
     * Invoked once when the feature is registered or the engine initializes.
     */
    fun onInitialize() {}

    /**
     * Periodic update hook called on client tick with latest telemetry.
     */
    fun onTick(telemetry: ExplorationTelemetry) {}

    /**
     * Invoked when an exploration lifecycle event occurs.
     */
    fun onEvent(event: ExplorationEvent) {}
}
