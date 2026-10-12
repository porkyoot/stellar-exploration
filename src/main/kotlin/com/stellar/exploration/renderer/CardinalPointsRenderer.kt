package com.stellar.exploration.renderer

import com.stellar.core.config.ConfigManager
import com.stellar.exploration.StellarExplorationMod
import com.stellar.exploration.config.StellarExplorationConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.abs

/**
 * Renders smooth Cardinal Points (N, S, E, W) using Minecraft font slightly above the contextual bar,
 * carefully avoiding collisions with health, hunger, experience levels, and action bars.
 */
object CardinalPointsRenderer {
    private const val VISIBLE_DEGREE_RANGE: Float = 60.0f
    private const val FULL_ROTATION_DEGREES: Float = 360.0f
    private const val HALF_ROTATION_DEGREES: Float = 180.0f
    private const val NORMALIZATION_OFFSET: Float = 540.0f

    private const val COLOR_NORTH: Int = 0xFFFF5555.toInt()
    private const val COLOR_CARDINAL: Int = 0xFFFFFFFF.toInt()
    private const val COLOR_INTERMEDIATE: Int = 0xFFAAAAAA.toInt()

    private const val BEARING_SOUTH: Float = 0.0f
    private const val BEARING_SOUTH_WEST: Float = 45.0f
    private const val BEARING_WEST: Float = 90.0f
    private const val BEARING_NORTH_WEST: Float = 135.0f
    private const val BEARING_NORTH: Float = 180.0f
    private const val BEARING_NORTH_EAST: Float = 225.0f
    private const val BEARING_EAST: Float = 270.0f
    private const val BEARING_SOUTH_EAST: Float = 315.0f

    private const val BAR_Y_OFFSET: Int = 2
    private const val LEVEL_AVOIDANCE_RADIUS_MIN: Int = 14
    private const val LEVEL_AVOIDANCE_PADDING: Int = 6
    private const val BAR_EDGE_INSET: Float = 6.0f
    private const val BAR_CLAMP_PADDING: Int = 2

    private data class CardinalPoint(val label: String, val bearing: Float, val color: Int, val isMajor: Boolean)

    private val CARDINAL_POINTS: List<CardinalPoint> = listOf(
        CardinalPoint("S", BEARING_SOUTH, COLOR_CARDINAL, true),
        CardinalPoint("SW", BEARING_SOUTH_WEST, COLOR_INTERMEDIATE, false),
        CardinalPoint("W", BEARING_WEST, COLOR_CARDINAL, true),
        CardinalPoint("NW", BEARING_NORTH_WEST, COLOR_INTERMEDIATE, false),
        CardinalPoint("N", BEARING_NORTH, COLOR_NORTH, true),
        CardinalPoint("NE", BEARING_NORTH_EAST, COLOR_INTERMEDIATE, false),
        CardinalPoint("E", BEARING_EAST, COLOR_CARDINAL, true),
        CardinalPoint("SE", BEARING_SOUTH_EAST, COLOR_INTERMEDIATE, false),
    )

    @JvmStatic
    @Suppress("LongParameterList")
    fun render(minecraft: Minecraft, guiGraphics: GuiGraphicsExtractor, barLeft: Int, barTop: Int, barWidth: Int) {
        val config = ConfigManager.get<StellarExplorationConfig>(StellarExplorationMod.MOD_ID, "main")
        if (!canRender(minecraft, config) || config == null) return

        val player = minecraft.player ?: return
        val cameraYaw = minecraft.gameRenderer.mainCamera().yRot()
        val layout = createBarLayout(minecraft, barLeft, barTop, barWidth)

        renderAllPoints(guiGraphics, cameraYaw, config.showCardinalIntermediates.value(), layout)
    }

    private fun createBarLayout(minecraft: Minecraft, barLeft: Int, barTop: Int, barWidth: Int): BarLayout {
        val barCenterX = barLeft + barWidth / 2
        val usableHalfWidth = barWidth / 2.0f - BAR_EDGE_INSET
        val experienceLevel = minecraft.player?.experienceLevel ?: 0
        val levelAvoidanceRadius = if (experienceLevel > 0) {
            val levelWidth = minecraft.font.width(experienceLevel.toString())
            maxOf(LEVEL_AVOIDANCE_RADIUS_MIN, levelWidth / 2 + LEVEL_AVOIDANCE_PADDING)
        } else {
            0
        }
        return BarLayout(
            barLeft = barLeft,
            barTop = barTop,
            barWidth = barWidth,
            barCenterX = barCenterX,
            usableHalfWidth = usableHalfWidth,
            levelAvoidanceRadius = levelAvoidanceRadius,
            font = minecraft.font,
        )
    }

    private fun renderAllPoints(
        guiGraphics: GuiGraphicsExtractor,
        cameraYaw: Float,
        showIntermediates: Boolean,
        layout: BarLayout,
    ) {
        for (point in CARDINAL_POINTS) {
            if (!point.isMajor && !showIntermediates) continue

            val rawDiff = (point.bearing - cameraYaw + NORMALIZATION_OFFSET) % FULL_ROTATION_DEGREES
            val diff = rawDiff - HALF_ROTATION_DEGREES

            if (diff in -VISIBLE_DEGREE_RANGE..VISIBLE_DEGREE_RANGE) {
                renderPoint(guiGraphics, point, diff, layout)
            }
        }
    }

    private fun isConfigDisabled(config: StellarExplorationConfig?): Boolean {
        if (config == null) return true
        val isEnabled = config.enabled.value()
        val showPoints = config.showCardinalPoints.value()
        return !isEnabled || !showPoints
    }

    private fun canRender(minecraft: Minecraft, config: StellarExplorationConfig?): Boolean {
        if (isConfigDisabled(config)) return false
        if (minecraft.gui.screen() != null) return false
        val player = minecraft.player
        return player != null && !player.isSpectator
    }

    private fun renderPoint(guiGraphics: GuiGraphicsExtractor, point: CardinalPoint, diff: Float, layout: BarLayout) {
        val progress = diff / VISIBLE_DEGREE_RANGE
        val posX = layout.barCenterX + (progress * layout.usableHalfWidth).toInt()
        val textWidth = layout.font.width(point.label)
        val minX = layout.barLeft + BAR_CLAMP_PADDING
        val maxX = layout.barLeft + layout.barWidth - textWidth - BAR_CLAMP_PADDING
        val drawX = (posX - textWidth / 2).coerceIn(minX, maxX)

        val distFromCenter = abs(drawX + textWidth / 2 - layout.barCenterX)
        if (layout.levelAvoidanceRadius > 0 && distFromCenter < layout.levelAvoidanceRadius) {
            return
        }

        val drawY = layout.barTop - BAR_Y_OFFSET

        guiGraphics.text(layout.font, point.label, drawX, drawY, point.color, true)
    }

    private data class BarLayout(
        val barLeft: Int,
        val barTop: Int,
        val barWidth: Int,
        val barCenterX: Int,
        val usableHalfWidth: Float,
        val levelAvoidanceRadius: Int,
        val font: net.minecraft.client.gui.Font,
    )
}
