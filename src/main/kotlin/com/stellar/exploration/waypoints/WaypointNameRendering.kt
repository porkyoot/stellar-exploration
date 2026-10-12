package com.stellar.exploration.waypoints

import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import net.minecraft.world.waypoints.PartialTickSupplier
import net.minecraft.world.waypoints.TrackedWaypoint
import kotlin.math.abs

/**
 * Renders waypoint name tags above the locator bar when looking at waypoints or pressing Tab.
 */
object WaypointNameRendering {
    private const val MAX_YAW_DEGREE: Double = 60.0
    private const val BAR_HALF_WIDTH: Double = 86.5
    private const val BG_ALPHA: Int = 180
    private const val BG_PADDING: Int = 3
    private const val TEXT_Y_OFFSET: Int = 12
    private const val BOX_PAD_Y_MIN: Int = 2
    private const val BOX_PAD_Y_MAX: Int = 1
    private const val TEXT_COLOR_WHITE: Int = 0xFFFFFFFF.toInt()

    @JvmStatic
    fun renderNames(client: Minecraft, graphics: GuiGraphicsExtractor, tickCounter: DeltaTracker, barTop: Int) {
        val world = if (client.options.keyPlayerList.isDown) client.level else null
        if (world == null) return

        val entityTickProgress = PartialTickSupplier { tickedEntity ->
            tickCounter.getGameTimeDeltaPartialTick(!world.tickRateManager().isEntityFrozen(tickedEntity))
        }

        var bestWaypoint: TrackedWaypoint? = null
        var bestYaw = MAX_YAW_DEGREE + 1.0

        for (waypoint in WaypointTracking.getAllWaypoints()) {
            val yaw = waypoint.yawAngleToCamera(world, client.gameRenderer.mainCamera(), entityTickProgress)
            val absYaw = abs(yaw)
            if (absYaw < abs(bestYaw)) {
                bestYaw = yaw
                bestWaypoint = waypoint
            }
        }

        val target = bestWaypoint?.takeIf { abs(bestYaw) <= MAX_YAW_DEGREE } ?: return
        val idUuid = target.id().left().orElse(null)
        val waypointName = if (idUuid != null) WaypointTracking.getWaypointName(idUuid) else null
        val nameComponent: Component = waypointName ?: return

        val textX = getXFromYaw(graphics, bestYaw)
        val pos = WaypointTagPos(textX, barTop)
        drawWaypointNameTag(client.font, graphics, nameComponent, pos)
    }

    private fun drawWaypointNameTag(
        font: net.minecraft.client.gui.Font,
        graphics: GuiGraphicsExtractor,
        name: Component,
        pos: WaypointTagPos,
    ) {
        val textWidth = font.width(name)
        val textX = pos.textX - textWidth / 2
        val textY = pos.barTop - TEXT_Y_OFFSET

        val bgColor = ARGB.color(BG_ALPHA, 0, 0, 0)
        graphics.fill(
            textX - BG_PADDING,
            textY - BOX_PAD_Y_MIN,
            textX + textWidth + BG_PADDING,
            textY + font.lineHeight + BOX_PAD_Y_MAX,
            bgColor,
        )

        graphics.text(font, name, textX, textY, TEXT_COLOR_WHITE, true)
    }

    private fun getXFromYaw(graphics: GuiGraphicsExtractor, relativeYaw: Double): Int {
        val centerX = graphics.guiWidth() / 2
        val normalized = relativeYaw / MAX_YAW_DEGREE
        return centerX + (normalized * BAR_HALF_WIDTH).toInt()
    }

    private data class WaypointTagPos(val textX: Int, val barTop: Int)
}
