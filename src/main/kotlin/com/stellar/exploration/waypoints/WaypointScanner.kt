package com.stellar.exploration.waypoints

import com.stellar.exploration.config.StellarExplorationConfig
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.GlobalPos
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.MapItem
import net.minecraft.world.level.Level
import net.minecraft.world.waypoints.TrackedWaypoint
import net.minecraft.world.waypoints.Waypoint
import java.nio.charset.StandardCharsets
import java.util.Optional
import java.util.UUID

/**
 * Scans player inventory items to extract exploration waypoints (lodestones, recovery compasses, map banners).
 */
internal object WaypointScanner {
    private const val DEFAULT_ALPHA: Int = 255
    private const val DEFAULT_LODESTONE_COLOR: Int = 0x80FF80
    private const val DEFAULT_RECOVERY_COLOR: Int = 0x55FFFF

    fun scanPlayerInventory(
        player: LocalPlayer,
        config: StellarExplorationConfig,
        destination: MutableMap<UUID, TrackedWaypoint>,
        names: MutableMap<UUID, Component>,
    ) {
        val context = TrackingContext(
            player = player,
            dimension = player.level().dimension(),
            config = config,
            destination = destination,
            names = names,
        )
        val nonEquipment = player.inventory.nonEquipmentItems
        for (i in 0 until nonEquipment.size) {
            processItemStack(context, nonEquipment[i])
        }
        val offhand = player.offhandItem
        if (!offhand.isEmpty) {
            processItemStack(context, offhand)
        }
        val carried = player.containerMenu.carried
        if (!carried.isEmpty) {
            processItemStack(context, carried)
        }
    }

    private fun processItemStack(context: TrackingContext, stack: ItemStack) {
        if (stack.isEmpty) return

        if (context.config.trackLodestones.value()) {
            processLodestoneCompass(context, stack)
        }

        if (context.config.trackRecoveryCompasses.value()) {
            processRecoveryCompass(context, stack)
        }

        if (context.config.trackBannerMarkers.value()) {
            processMapBanners(context, stack)
        }

        if (context.config.trackBundles.value()) {
            processBundle(context, stack)
        }
    }

    private fun processLodestoneCompass(context: TrackingContext, stack: ItemStack) {
        val tracker = stack.get(DataComponents.LODESTONE_TRACKER) ?: return
        val targetOptional = tracker.target()
        if (!targetOptional.isPresent) return
        val target: GlobalPos = targetOptional.get()
        if (target.dimension() != context.dimension) return

        val pos = target.pos()
        val uuid = UUID.nameUUIDFromBytes(
            "lodestone:${pos.x}:${pos.y}:${pos.z}".toByteArray(StandardCharsets.UTF_8),
        )
        val color = ColorHandler.getColor(stack) ?: ARGB.color(DEFAULT_ALPHA, DEFAULT_LODESTONE_COLOR)
        val icon = Waypoint.Icon().apply {
            style = WaypointTracking.LODESTONE_STYLE
            this.color = Optional.of(color)
        }
        context.destination[uuid] = TrackedWaypoint.setPosition(uuid, icon, pos)
        context.names[uuid] = ColorHandler.extractDisplayName(stack)
            ?: Component.translatable("item.minecraft.lodestone_compass")
    }

    private fun processRecoveryCompass(context: TrackingContext, stack: ItemStack) {
        if (!stack.`is`(Items.RECOVERY_COMPASS)) return
        val deathLocOptional = context.player.lastDeathLocation
        if (!deathLocOptional.isPresent) return
        val deathPos: GlobalPos = deathLocOptional.get()
        if (deathPos.dimension() != context.dimension) return

        val pos = deathPos.pos()
        val uuid = UUID.nameUUIDFromBytes(
            "death:${pos.x}:${pos.y}:${pos.z}".toByteArray(StandardCharsets.UTF_8),
        )
        val color = ColorHandler.getColor(stack) ?: ARGB.color(DEFAULT_ALPHA, DEFAULT_RECOVERY_COLOR)
        val icon = Waypoint.Icon().apply {
            style = WaypointTracking.DEATH_STYLE
            this.color = Optional.of(color)
        }
        context.destination[uuid] = TrackedWaypoint.setPosition(uuid, icon, pos)
        context.names[uuid] = ColorHandler.extractDisplayName(stack)
            ?: Component.translatable("item.minecraft.recovery_compass")
    }

    private fun processMapBanners(context: TrackingContext, stack: ItemStack) {
        val mapData = MapItem.getSavedData(stack, context.player.level()) ?: return
        if (mapData.dimension != context.dimension) return

        for (banner in mapData.banners) {
            val pos = banner.pos()
            val bannerId = banner.id
            val uuid = UUID.nameUUIDFromBytes(
                "banner:$bannerId:${pos.x}:${pos.y}:${pos.z}".toByteArray(StandardCharsets.UTF_8),
            )
            val bannerColor = banner.color()
            val color = ARGB.color(DEFAULT_ALPHA, bannerColor.textureDiffuseColor)

            val icon = Waypoint.Icon().apply {
                style = WaypointTracking.BANNER_STYLE
                this.color = Optional.of(color)
            }
            context.destination[uuid] = TrackedWaypoint.setPosition(uuid, icon, pos)
            context.names[uuid] = banner.name().orElseGet {
                val colorName = bannerColor.name.lowercase()
                Component.literal("${colorName.replaceFirstChar { it.uppercase() }} Banner")
            }
        }
    }

    private fun processBundle(context: TrackingContext, stack: ItemStack) {
        val bundle = stack.get(DataComponents.BUNDLE_CONTENTS) ?: return
        bundle.itemCopyStream().forEach { bundledStack ->
            processItemStack(context, bundledStack)
        }
    }

    private data class TrackingContext(
        val player: LocalPlayer,
        val dimension: ResourceKey<Level>,
        val config: StellarExplorationConfig,
        val destination: MutableMap<UUID, TrackedWaypoint>,
        val names: MutableMap<UUID, Component>,
    )
}
