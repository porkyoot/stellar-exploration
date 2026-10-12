package com.stellar.exploration.renderer

import com.stellar.exploration.waypoints.WaypointTracking
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.waypoints.TrackedWaypoint

/**
 * Renders the skull and crossbones emoji text with distance-based size variation
 * and drop shadow on the locator bar for death waypoints.
 */
object DeathWaypointRenderer {
    private val SKULL_AND_CROSSBONES: Component = Component.literal("☠")
    private const val WHITE_TEXT_COLOR: Int = 0xFFFFFFFF.toInt()
    private const val SCALE_STAGE_0: Float = 1.0f
    private const val SCALE_STAGE_1: Float = 0.78f
    private const val SCALE_STAGE_2: Float = 0.56f
    private const val SCALE_STAGE_3: Float = 0.40f

    @JvmStatic
    @Suppress("LongParameterList")
    fun renderDeathIcon(
        minecraft: Minecraft,
        guiGraphics: GuiGraphicsExtractor,
        waypoint: TrackedWaypoint,
        sprite: Identifier,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): Boolean {
        if (minecraft.gui.screen() != null || waypoint.icon().style != WaypointTracking.DEATH_STYLE) {
            return false
        }
        val font = minecraft.font
        val textWidth = font.width(SKULL_AND_CROSSBONES)
        val textX = x + (width - textWidth) / 2
        val textY = y + (height - font.lineHeight) / 2
        val color = waypoint.icon().color.orElse(WHITE_TEXT_COLOR)

        val scale = resolveScale(sprite)
        val centerX = x + width / 2.0f
        val centerY = y + height / 2.0f

        val pose = guiGraphics.pose()
        pose.pushMatrix()
        pose.scaleAround(scale, scale, centerX, centerY)
        guiGraphics.text(font, SKULL_AND_CROSSBONES, textX, textY, color, true)
        pose.popMatrix()
        return true
    }

    private fun resolveScale(sprite: Identifier): Float {
        val path = sprite.path
        return when {
            path.endsWith("0") -> SCALE_STAGE_0
            path.endsWith("1") -> SCALE_STAGE_1
            path.endsWith("2") -> SCALE_STAGE_2
            path.endsWith("3") -> SCALE_STAGE_3
            else -> SCALE_STAGE_0
        }
    }
}
