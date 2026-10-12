package com.stellar.exploration.framework

import kotlin.math.floor

/**
 * Immutable snapshot of player navigation and spatial telemetry.
 */
data class ExplorationTelemetry(
    val x: Double,
    val y: Double,
    val z: Double,
    val blockX: Int,
    val blockY: Int,
    val blockZ: Int,
    val dimension: String,
    val biome: String,
    val yaw: Float,
    val pitch: Float,
    val cardinalDirection: CardinalDirection,
) {
    companion object {
        const val DEFAULT_DIMENSION: String = "minecraft:overworld"
        const val DEFAULT_BIOME: String = "minecraft:plains"

        val EMPTY: ExplorationTelemetry = ExplorationTelemetry(
            x = 0.0,
            y = 0.0,
            z = 0.0,
            blockX = 0,
            blockY = 0,
            blockZ = 0,
            dimension = DEFAULT_DIMENSION,
            biome = DEFAULT_BIOME,
            yaw = 0.0f,
            pitch = 0.0f,
            cardinalDirection = CardinalDirection.SOUTH,
        )

        @Suppress("LongParameterList")
        fun create(
            x: Double,
            y: Double,
            z: Double,
            dimension: String,
            biome: String,
            yaw: Float,
            pitch: Float,
        ): ExplorationTelemetry = ExplorationTelemetry(
            x = x,
            y = y,
            z = z,
            blockX = floor(x).toInt(),
            blockY = floor(y).toInt(),
            blockZ = floor(z).toInt(),
            dimension = dimension,
            biome = biome,
            yaw = yaw,
            pitch = pitch,
            cardinalDirection = CardinalDirection.fromYaw(yaw),
        )
    }
}

/**
 * Formats XYZ coordinates to standard display string.
 */
fun ExplorationTelemetry.formatCoordinates(): String = "$blockX, $blockY, $blockZ"

/**
 * Formats compass heading with code and degree bearing.
 */
fun ExplorationTelemetry.formatCompass(): String {
    val normalizedYaw = (yaw % CardinalDirection.FULL_CIRCLE_DEGREES + CardinalDirection.FULL_CIRCLE_DEGREES) %
        CardinalDirection.FULL_CIRCLE_DEGREES
    return "${cardinalDirection.code} (${normalizedYaw.toInt()}°)"
}
