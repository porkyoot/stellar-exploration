package com.stellar.exploration.renderer

import com.stellar.core.config.ConfigManager
import com.stellar.exploration.StellarExplorationMod
import com.stellar.exploration.config.StellarExplorationConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Player
import net.minecraft.world.waypoints.TrackedWaypoint
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.cos
import kotlin.math.pow

/**
 * Handles rendering player skin heads and animated name tags on the Locator Bar.
 */
object PlayerHeadRenderer {
    private val nameAnimStartTimes: ConcurrentHashMap<String, Long> = ConcurrentHashMap()
    private val nameAnimDirection: ConcurrentHashMap<String, Boolean> = ConcurrentHashMap()

    private const val ANIMATION_DURATION_MS: Float = 150.0f
    private const val DEFAULT_BORDER_COLOR: Int = 0xFF00E5FF.toInt()
    private const val LOOKING_AT_THRESHOLD_DEG: Double = 20.0
    private const val UV_FACE_MIN: Float = 1.0f / 8.0f
    private const val UV_FACE_MAX: Float = 2.0f / 8.0f
    private const val UV_HAT_MIN: Float = 5.0f / 8.0f
    private const val UV_HAT_MAX: Float = 6.0f / 8.0f

    private const val MAX_ALPHA: Int = 0xBF
    private const val FULL_ALPHA: Int = 255
    private const val COLOR_MASK_RGB: Int = 0x00FFFFFF
    private const val ALPHA_SHIFT: Int = 24
    private const val NAME_TAG_Y_GAP: Int = 3
    private const val POWER_EASING: Float = 3.0f
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

        renderPlayerNameTag(
            minecraft,
            guiGraphics,
            config,
            trackedWaypoint,
            playerName,
            bounds,
            borderColor,
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

    @Suppress("LongParameterList")
    private fun renderPlayerNameTag(
        minecraft: Minecraft,
        guiGraphics: GuiGraphicsExtractor,
        config: StellarExplorationConfig,
        trackedWaypoint: TrackedWaypoint,
        playerName: String,
        bounds: HeadBounds,
        textColor: Int,
    ) {
        val shouldShow = shouldShowPlayerName(minecraft, config, trackedWaypoint)
        val isAnimating = updateAnimationState(playerName, shouldShow)
        if (!shouldShow && !isAnimating) return

        val (alpha, isFinished) = computeAnimatedAlpha(playerName)
        if (isFinished) {
            nameAnimStartTimes.remove(playerName)
            nameAnimDirection.remove(playerName)
        }

        if (alpha <= 0) return

        val font = minecraft.font
        val textWidth = font.width(playerName)
        val textX = bounds.x + (bounds.width - textWidth) / 2
        val textY = bounds.y - font.lineHeight - NAME_TAG_Y_GAP
        val finalColor = alpha shl ALPHA_SHIFT or (textColor and COLOR_MASK_RGB)
        guiGraphics.text(font, playerName, textX, textY, finalColor, true)
    }

    private fun computeAnimatedAlpha(playerName: String): Pair<Int, Boolean> {
        val now = System.currentTimeMillis()
        val start = nameAnimStartTimes[playerName] ?: now
        val elapsed = (now - start).toFloat()
        val animProg = (elapsed / ANIMATION_DURATION_MS).coerceIn(0.0f, 1.0f)
        val dir = nameAnimDirection[playerName] ?: true
        val eff = if (dir) animProg else 1.0f - animProg
        val eased = 1.0f - (1.0f - eff).pow(POWER_EASING)
        val alpha = (MAX_ALPHA * eased).toInt().coerceIn(0, FULL_ALPHA)
        val isFinished = !dir && animProg >= 1.0f
        return Pair(alpha, isFinished)
    }

    private data class HeadBounds(val x: Int, val y: Int, val width: Int, val height: Int)

    private fun updateAnimationState(playerName: String, shouldShow: Boolean): Boolean {
        val now = System.currentTimeMillis()
        val isAnimating = nameAnimStartTimes.containsKey(playerName)

        if (shouldShow && !isAnimating) {
            nameAnimStartTimes[playerName] = now
            nameAnimDirection[playerName] = true
        } else if (!shouldShow && isAnimating) {
            val dir = nameAnimDirection[playerName] ?: false
            if (dir) {
                val start = nameAnimStartTimes[playerName] ?: now
                val elapsed = (now - start).toFloat()
                val progress = (elapsed / ANIMATION_DURATION_MS).coerceIn(0.0f, 1.0f)
                val newStart = now - ((1.0f - progress) * ANIMATION_DURATION_MS).toLong()
                nameAnimStartTimes[playerName] = newStart
                nameAnimDirection[playerName] = false
            }
        }
        return isAnimating
    }

    private fun shouldShowPlayerName(
        minecraft: Minecraft,
        config: StellarExplorationConfig,
        trackedWaypoint: TrackedWaypoint,
    ): Boolean = when (config.showPlayerNames.value().uppercase()) {
        "ALWAYS" -> true
        "PLAYER_LIST" -> minecraft.options.keyPlayerList.isDown
        "LOOKING_AT" -> isLookingAtPlayer(minecraft, trackedWaypoint)
        else -> false
    }

    private fun isLookingAtPlayer(minecraft: Minecraft, trackedWaypoint: TrackedWaypoint): Boolean {
        val camera = minecraft.cameraEntity ?: return false
        val level = minecraft.level ?: return false
        val uuid = trackedWaypoint.id().left().orElse(null) ?: return false
        val target: Player = level.getPlayerByUUID(uuid) ?: return false

        val cameraPos = camera.position()
        val look = camera.getViewVector(1.0f)
        val dir = target.position().subtract(cameraPos).normalize()
        val dot = look.dot(dir)
        val threshold = cos(Math.toRadians(LOOKING_AT_THRESHOLD_DEG))
        return dot >= threshold
    }
}
