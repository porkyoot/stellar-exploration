package com.stellar.exploration.config

import org.quiltmc.config.api.ReflectiveConfig
import org.quiltmc.config.api.annotations.Comment
import org.quiltmc.config.api.values.TrackedValue

/**
 * Quilt ReflectiveConfig schema for Stellar Exploration client-side navigation framework.
 */
class StellarExplorationConfig : ReflectiveConfig() {
    @Comment("Master switch enabling or disabling client-side exploration features")
    val enabled: TrackedValue<Boolean> = value(true)

    // Locator Bar & Player Heads Settings
    @Comment("Render player heads instead of generic locator dots")
    val renderPlayerHeads: TrackedValue<Boolean> = value(true)

    @Comment("Head size multiplier scale (1 = 0.5x, 5 = 1.0x, 9 = 1.5x)")
    val headSizeMultiplier: TrackedValue<Int> = value(DEFAULT_HEAD_SIZE_INDEX)

    @Comment("Always show the Experience Bar even when the Locator Bar is active")
    val alwaysShowXP: TrackedValue<Boolean> = value(false)

    @Comment("Enable team-colored border around player heads")
    val enableTeamBorder: TrackedValue<Boolean> = value(true)

    @Comment("Hexadecimal static border color (#AARRGGBB) when team border is inactive")
    val staticBorderColorHex: TrackedValue<String> = value("#FF00E5FF")

    @Comment("Player name visibility mode: OFF, ALWAYS, LOOKING_AT, PLAYER_LIST")
    val showPlayerNames: TrackedValue<String> = value("LOOKING_AT")

    // Cardinal Points Navigation
    @Comment("Display NSEW Cardinal Points with Minecraft font slightly above the bar")
    val showCardinalPoints: TrackedValue<Boolean> = value(true)

    @Comment("Render intermediate compass points (NE, NW, SE, SW) in subtle tint")
    val showCardinalIntermediates: TrackedValue<Boolean> = value(true)

    @Comment("Hexadecimal color format for cardinal points (#AARRGGBB)")
    val cardinalPointsColorHex: TrackedValue<String> = value("#FFFFFFFF")

    // Waypoint Tracking (Lodestone, Death, Banners, Spawn)
    @Comment("Track lodestones from compasses in inventory on the locator bar")
    val trackLodestones: TrackedValue<Boolean> = value(true)

    @Comment("Track last death location from recovery compasses in inventory")
    val trackRecoveryCompasses: TrackedValue<Boolean> = value(true)

    @Comment("Track banner markers from filled maps in inventory on the locator bar")
    val trackBannerMarkers: TrackedValue<Boolean> = value(true)

    @Comment("Track world spawn location from normal compasses on the locator bar")
    val trackNormalCompasses: TrackedValue<Boolean> = value(true)

    @Comment("Only show spawn waypoint when holding a normal compass in mainhand or offhand")
    val normalCompassRequiresHolding: TrackedValue<Boolean> = value(false)

    @Comment("Scan nested items inside bundles in inventory for compasses and maps")
    val trackBundles: TrackedValue<Boolean> = value(true)

    // HUD Telemetry
    @Comment("Toggle the exploration telemetry HUD overlay")
    val hudEnabled: TrackedValue<Boolean> = value(true)

    @Comment("Display player XYZ coordinates on screen")
    val showCoordinates: TrackedValue<Boolean> = value(true)

    @Comment("Display current cardinal direction and degree bearing on HUD")
    val showCompass: TrackedValue<Boolean> = value(true)

    @Comment("Display current biome name")
    val showBiome: TrackedValue<Boolean> = value(true)

    @Comment("Display current world dimension identifier")
    val showDimension: TrackedValue<Boolean> = value(true)

    fun getHeadScale(): Float = when (headSizeMultiplier.value()) {
        INDEX_1 -> SCALE_0_5
        INDEX_2 -> SCALE_0_6
        INDEX_3 -> SCALE_0_7
        INDEX_4 -> SCALE_0_8
        INDEX_5 -> SCALE_1_0
        INDEX_6 -> SCALE_1_1
        INDEX_7 -> SCALE_1_2
        INDEX_8 -> SCALE_1_3
        INDEX_9 -> SCALE_1_5
        else -> SCALE_1_0
    }

    companion object {
        const val DEFAULT_HEAD_SIZE_INDEX: Int = 5

        const val INDEX_1: Int = 1
        const val INDEX_2: Int = 2
        const val INDEX_3: Int = 3
        const val INDEX_4: Int = 4
        const val INDEX_5: Int = 5
        const val INDEX_6: Int = 6
        const val INDEX_7: Int = 7
        const val INDEX_8: Int = 8
        const val INDEX_9: Int = 9

        const val SCALE_0_5: Float = 0.5f
        const val SCALE_0_6: Float = 0.6f
        const val SCALE_0_7: Float = 0.7f
        const val SCALE_0_8: Float = 0.8f
        const val SCALE_1_0: Float = 1.0f
        const val SCALE_1_1: Float = 1.1f
        const val SCALE_1_2: Float = 1.2f
        const val SCALE_1_3: Float = 1.3f
        const val SCALE_1_5: Float = 1.5f
    }
}
