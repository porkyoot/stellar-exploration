package com.stellar.exploration.renderer

import com.stellar.exploration.waypoints.WaypointTracking
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.world.waypoints.TrackedWaypoint

/**
 * Renders the skull and crossbones emoji text with drop shadow on the locator bar for death waypoints.
 */
object DeathWaypointRenderer {
    private val SKULL_AND_CROSSBONES: Component = Component.literal("☠")
    private const val WHITE_TEXT_COLOR: Int = 0xFFFFFFFF.toInt()

    @JvmStatic
    @Suppress("LongParameterList")
    fun renderDeathIcon(
        minecraft: Minecraft,
        guiGraphics: GuiGraphicsExtractor,
        waypoint: TrackedWaypoint,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): Boolean {
        if (waypoint.icon().style != WaypointTracking.DEATH_STYLE) {
            return false
        }
        val font = minecraft.font
        val textWidth = font.width(SKULL_AND_CROSSBONES)
        val textX = x + (width - textWidth) / 2
        val textY = y + (height - font.lineHeight) / 2
        val color = waypoint.icon().color.orElse(WHITE_TEXT_COLOR)

        guiGraphics.text(font, SKULL_AND_CROSSBONES, textX, textY, color, true)
        return true
    }
}
