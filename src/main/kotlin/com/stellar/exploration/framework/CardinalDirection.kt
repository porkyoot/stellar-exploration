package com.stellar.exploration.framework

private const val CONST_SOUTH: Float = 0.0f
private const val CONST_SOUTH_WEST: Float = 45.0f
private const val CONST_WEST: Float = 90.0f
private const val CONST_NORTH_WEST: Float = 135.0f
private const val CONST_NORTH: Float = 180.0f
private const val CONST_NORTH_EAST: Float = 225.0f
private const val CONST_EAST: Float = 270.0f
private const val CONST_SOUTH_EAST: Float = 315.0f

/**
 * 8-point compass navigation heading for exploration telemetry.
 */
enum class CardinalDirection(
    val code: String,
    val displayName: String,
    val centerBearing: Float,
) {
    SOUTH("S", "South", CONST_SOUTH),
    SOUTH_WEST("SW", "South-West", CONST_SOUTH_WEST),
    WEST("W", "West", CONST_WEST),
    NORTH_WEST("NW", "North-West", CONST_NORTH_WEST),
    NORTH("N", "North", CONST_NORTH),
    NORTH_EAST("NE", "North-East", CONST_NORTH_EAST),
    EAST("E", "East", CONST_EAST),
    SOUTH_EAST("SE", "South-East", CONST_SOUTH_EAST),
    ;

    companion object {
        const val FULL_CIRCLE_DEGREES: Float = 360.0f
        const val SECTOR_DEGREES: Float = 45.0f
        const val HALF_SECTOR_DEGREES: Float = 22.5f

        const val BEARING_SOUTH: Float = CONST_SOUTH
        const val BEARING_SOUTH_WEST: Float = CONST_SOUTH_WEST
        const val BEARING_WEST: Float = CONST_WEST
        const val BEARING_NORTH_WEST: Float = CONST_NORTH_WEST
        const val BEARING_NORTH: Float = CONST_NORTH
        const val BEARING_NORTH_EAST: Float = CONST_NORTH_EAST
        const val BEARING_EAST: Float = CONST_EAST
        const val BEARING_SOUTH_EAST: Float = CONST_SOUTH_EAST

        private val ENTRIES_ARRAY: Array<CardinalDirection> = entries.toTypedArray()

        /**
         * Resolves the cardinal direction from a Minecraft yaw angle.
         * Minecraft yaw: 0 is South, 90 is West, 180 is North, 270/-90 is East.
         */
        fun fromYaw(yaw: Float): CardinalDirection {
            val normalizedYaw = (yaw % FULL_CIRCLE_DEGREES + FULL_CIRCLE_DEGREES) % FULL_CIRCLE_DEGREES
            val sectorIndex = ((normalizedYaw + HALF_SECTOR_DEGREES) / SECTOR_DEGREES).toInt() % ENTRIES_ARRAY.size
            return ENTRIES_ARRAY[sectorIndex]
        }
    }
}
