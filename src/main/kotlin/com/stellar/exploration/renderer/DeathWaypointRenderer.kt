package com.stellar.exploration.renderer

import com.stellar.exploration.waypoints.WaypointTracking
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.world.waypoints.TrackedWaypoint

/**
 * Renders the skull emoji text with drop shadow on the locator bar for death waypoints.
 */
object DeathWaypointRenderer {
    private val SKULL_TEXT: Component = Component.literal("💀")
    private val FALLBACK_SKULL: Component = Component.literal("☠")
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
        val text = if (font.width(SKULL_TEXT) > 0) SKULL_TEXT else FALLBACK_SKULL
        val textWidth = font.width(text)
        val textX = x + (width - textWidth) / 2
        val textY = y + (height - font.lineHeight) / 2

        guiGraphics.text(font, text, textX, textY, WHITE_TEXT_COLOR, true)
        return true
    }
}
