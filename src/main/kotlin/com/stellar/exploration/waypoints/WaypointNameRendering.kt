package com.stellar.exploration.waypoints

import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import net.minecraft.world.waypoints.PartialTickSupplier
import net.minecraft.world.waypoints.TrackedWaypoint
import java.util.ArrayList
import kotlin.math.abs

/**
 * Renders waypoint name tags above the cardinal points on the locator bar.
 */
object WaypointNameRendering {
    private const val CREATIVE_TEXT_Y_OFFSET: Int = 13
    private const val STANDARD_TEXT_Y_OFFSET: Int = 21
    private const val EXTRA_ROWS_TEXT_Y_OFFSET: Int = 31
    private const val MAX_YAW_DEGREE: Double = 60.0
    private const val BAR_HALF_WIDTH: Double = 86.5
    private const val BG_ALPHA: Int = 180
    private const val BG_PADDING: Int = 3
    private const val BOX_PAD_Y_MIN: Int = 2
    private const val BOX_PAD_Y_MAX: Int = 1
    private const val TEXT_COLOR_WHITE: Int = 0xFFFFFFFF.toInt()

    @JvmStatic
    fun renderNames(client: Minecraft, graphics: GuiGraphicsExtractor, tickCounter: DeltaTracker, barTop: Int) {
        if (client.gui.screen() != null || !client.options.keyPlayerList.isDown) return

        val world = client.level ?: return
        val player = client.player ?: return
        if (player.isSpectator) return

        val entityTickProgress = PartialTickSupplier { tickedEntity ->
            tickCounter.getGameTimeDeltaPartialTick(!world.tickRateManager().isEntityFrozen(tickedEntity))
        }

        val textY = computeTextY(player, barTop)
        val waypoints = collectWaypoints(client, player)
        val context = NameRenderContext(client, graphics, world, entityTickProgress, textY)
        renderAllLabels(context, waypoints)
    }

    private fun computeTextY(player: LocalPlayer, barTop: Int): Int {
        if (player.isCreative) return barTop - CREATIVE_TEXT_Y_OFFSET
        val hasExtraRows = player.armorValue > 0 ||
            player.absorptionAmount > 0f ||
            player.airSupply < player.maxAirSupply
        return if (hasExtraRows) barTop - EXTRA_ROWS_TEXT_Y_OFFSET else barTop - STANDARD_TEXT_Y_OFFSET
    }

    private fun collectWaypoints(client: Minecraft, player: LocalPlayer): List<TrackedWaypoint> {
        val waypoints = ArrayList<TrackedWaypoint>()
        val manager = client.connection?.waypointManager
        if (manager != null) {
            manager.forEachWaypoint(client.cameraEntity ?: player) { waypoints.add(it) }
        } else {
            waypoints.addAll(WaypointTracking.getAllWaypoints())
        }
        return waypoints
    }

    private fun renderAllLabels(context: NameRenderContext, waypoints: List<TrackedWaypoint>) {
        val camera = context.client.gameRenderer.mainCamera()
        for (waypoint in waypoints) {
            val yaw = waypoint.yawAngleToCamera(context.world, camera, context.progress)
            if (abs(yaw) <= MAX_YAW_DEGREE) {
                val name = resolveWaypointName(context.client, waypoint) ?: continue
                val textX = getXFromYaw(context.graphics, yaw)
                drawWaypointNameTag(context.graphics, context.client.font, name, TagPos(textX, context.textY))
            }
        }
    }

    fun resolveWaypointName(client: Minecraft, waypoint: TrackedWaypoint): Component? {
        val uuid = waypoint.id().left().orElse(null)
        if (uuid != null) {
            val customName = WaypointTracking.getWaypointName(uuid)
            val playerName = client.connection?.getPlayerInfo(uuid)?.profile?.name?.let { Component.literal(it) }
            return customName ?: playerName
        }
        return waypoint.id().right().orElse(null)?.let { Component.literal(it) }
    }

    private fun drawWaypointNameTag(
        graphics: GuiGraphicsExtractor,
        font: net.minecraft.client.gui.Font,
        name: Component,
        pos: TagPos,
    ) {
        val textWidth = font.width(name)
        val minX = BG_PADDING
        val maxX = graphics.guiWidth() - textWidth - BG_PADDING
        val drawX = (pos.centerX - textWidth / 2).coerceIn(minX, maxX)

        val bgColor = ARGB.color(BG_ALPHA, 0, 0, 0)
        graphics.fill(
            drawX - BG_PADDING,
            pos.textY - BOX_PAD_Y_MIN,
            drawX + textWidth + BG_PADDING,
            pos.textY + font.lineHeight + BOX_PAD_Y_MAX,
            bgColor,
        )

        graphics.text(font, name, drawX, pos.textY, TEXT_COLOR_WHITE, true)
    }

    private data class NameRenderContext(
        val client: Minecraft,
        val graphics: GuiGraphicsExtractor,
        val world: net.minecraft.world.level.Level,
        val progress: PartialTickSupplier,
        val textY: Int,
    )

    private data class TagPos(val centerX: Int, val textY: Int)

    private fun getXFromYaw(graphics: GuiGraphicsExtractor, relativeYaw: Double): Int {
        val centerX = graphics.guiWidth() / 2
        val normalized = relativeYaw / MAX_YAW_DEGREE
        return centerX + (normalized * BAR_HALF_WIDTH).toInt()
    }
}
