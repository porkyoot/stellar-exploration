package com.stellar.exploration.renderer

import com.stellar.core.config.ConfigManager
import com.stellar.exploration.StellarExplorationMod
import com.stellar.exploration.config.StellarExplorationConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.resources.Identifier
import net.minecraft.world.waypoints.TrackedWaypoint

/**
 * Handles rendering player skin heads and borders on the Locator Bar.
 */
object PlayerHeadRenderer {
    private const val DEFAULT_BORDER_COLOR: Int = 0xFF00E5FF.toInt()
    private const val UV_FACE_MIN: Float = 1.0f / 8.0f
    private const val UV_FACE_MAX: Float = 2.0f / 8.0f
    private const val UV_HAT_MIN: Float = 5.0f / 8.0f
    private const val UV_HAT_MAX: Float = 6.0f / 8.0f
    private const val BORDER_THICKNESS: Int = 1

    @JvmStatic
    @Suppress("LongParameterList")
    fun renderPlayerHead(
        minecraft: Minecraft,
        guiGraphics: GuiGraphicsExtractor,
        trackedWaypoint: TrackedWaypoint,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): Boolean {
        val config = ConfigManager.get<StellarExplorationConfig>(StellarExplorationMod.MOD_ID, "main") ?: return false
        val playerInfo = resolvePlayerInfo(minecraft, trackedWaypoint)
        val isEnabled = config.enabled.value() && config.renderPlayerHeads.value()
        if (!isEnabled || playerInfo == null) {
            return false
        }

        val skinPath: Identifier = playerInfo.skin.body().texturePath()
        val playerName = playerInfo.profile.name

        val scale = config.getHeadScale()
        val scaledWidth = (width * scale).toInt()
        val scaledHeight = (height * scale).toInt()
        val centerX = x + (width - scaledWidth) / 2
        val centerY = y + (height - scaledHeight) / 2
        val bounds = HeadBounds(centerX, centerY, scaledWidth, scaledHeight)

        val borderColor = resolveBorderColor(minecraft, config, playerName)

        renderHeadBorders(guiGraphics, bounds, borderColor)

        guiGraphics.blit(
            skinPath, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
            UV_FACE_MIN, UV_FACE_MAX, UV_FACE_MIN, UV_FACE_MAX,
        )
        guiGraphics.blit(
            skinPath, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
            UV_HAT_MIN, UV_HAT_MAX, UV_FACE_MIN, UV_FACE_MAX,
        )

        return true
    }

    private fun resolvePlayerInfo(minecraft: Minecraft, trackedWaypoint: TrackedWaypoint): PlayerInfo? {
        val playerUuid = trackedWaypoint.id().left().orElse(null) ?: return null
        return minecraft.connection?.getPlayerInfo(playerUuid)
    }

    private fun resolveBorderColor(minecraft: Minecraft, config: StellarExplorationConfig, playerName: String): Int {
        if (!config.enableTeamBorder.value()) return DEFAULT_BORDER_COLOR
        val level = minecraft.level ?: return DEFAULT_BORDER_COLOR
        val team = level.scoreboard.getPlayersTeam(playerName)
        val colorOpt = team?.color
        return if (colorOpt != null && colorOpt.isPresent) {
            colorOpt.get().rgb() or 0xFF000000.toInt()
        } else {
            DEFAULT_BORDER_COLOR
        }
    }

    private fun renderHeadBorders(guiGraphics: GuiGraphicsExtractor, bounds: HeadBounds, borderColor: Int) {
        val bt = BORDER_THICKNESS
        guiGraphics.fill(bounds.x - bt, bounds.y - bt, bounds.x + bounds.width + bt, bounds.y, borderColor)
        guiGraphics.fill(
            bounds.x - bt,
            bounds.y + bounds.height,
            bounds.x + bounds.width + bt,
            bounds.y + bounds.height + bt,
            borderColor,
        )
        guiGraphics.fill(bounds.x - bt, bounds.y, bounds.x, bounds.y + bounds.height, borderColor)
        guiGraphics.fill(
            bounds.x + bounds.width,
            bounds.y,
            bounds.x + bounds.width + bt,
            bounds.y + bounds.height,
            borderColor,
        )
    }

    private data class HeadBounds(val x: Int, val y: Int, val width: Int, val height: Int)
}
