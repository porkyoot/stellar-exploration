package com.stellar.exploration.waypoints

import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
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
    private const val MAX_YAW_DEGREE: Double = 60.0
    private const val LOOKING_AT_THRESHOLD: Double = 12.0
    private const val BAR_HALF_WIDTH: Double = 86.5
    private const val BG_ALPHA: Int = 180
    private const val BG_PADDING: Int = 3
    private const val BOX_PAD_Y_MIN: Int = 2
    private const val BOX_PAD_Y_MAX: Int = 1
    private const val TEXT_COLOR_WHITE: Int = 0xFFFFFFFF.toInt()
    private const val TAG_MARGIN_BOTTOM: Int = 3
    private const val CARDINAL_TOP_OFFSET: Int = 13
    private const val XP_ELEVATION_OFFSET: Int = 5

    @JvmStatic
    fun renderNames(client: Minecraft, graphics: GuiGraphicsExtractor, tickCounter: DeltaTracker, barTop: Int) {
        val world = client.level ?: return
        val player = client.player ?: return
        if (player.isSpectator) return

        val entityTickProgress = PartialTickSupplier { tickedEntity ->
            tickCounter.getGameTimeDeltaPartialTick(!world.tickRateManager().isEntityFrozen(tickedEntity))
        }

        val showAll = client.options.keyPlayerList.isDown
        val hasExp = player.experienceLevel > 0
        val cardinalOffset = if (hasExp) CARDINAL_TOP_OFFSET + XP_ELEVATION_OFFSET else CARDINAL_TOP_OFFSET
        val textY = barTop - cardinalOffset - client.font.lineHeight - TAG_MARGIN_BOTTOM

        val waypoints = ArrayList<TrackedWaypoint>()
        val manager = client.connection?.waypointManager
        if (manager != null) {
            manager.forEachWaypoint(client.cameraEntity ?: player) { waypoints.add(it) }
        } else {
            waypoints.addAll(WaypointTracking.getAllWaypoints())
        }

        val context = NameRenderContext(client, graphics, world, entityTickProgress, textY)
        if (showAll) {
            renderAllLabels(context, waypoints)
        } else {
            renderFocusedLabel(context, waypoints)
        }
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

    private fun renderFocusedLabel(context: NameRenderContext, waypoints: List<TrackedWaypoint>) {
        val camera = context.client.gameRenderer.mainCamera()
        var bestWaypoint: TrackedWaypoint? = null
        var bestYaw = LOOKING_AT_THRESHOLD + 1.0

        for (waypoint in waypoints) {
            val yaw = waypoint.yawAngleToCamera(context.world, camera, context.progress)
            val absYaw = abs(yaw)
            if (absYaw < abs(bestYaw)) {
                bestYaw = yaw
                bestWaypoint = waypoint
            }
        }

        val target = bestWaypoint?.takeIf { abs(bestYaw) <= LOOKING_AT_THRESHOLD } ?: return
        val name = resolveWaypointName(context.client, target) ?: return
        val textX = getXFromYaw(context.graphics, bestYaw)
        drawWaypointNameTag(context.graphics, context.client.font, name, TagPos(textX, context.textY))
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
