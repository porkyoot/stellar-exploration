package com.stellar.exploration.waypoints

import com.stellar.exploration.config.StellarExplorationConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.util.ARGB
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.MapItem
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.maps.MapItemSavedData
import net.minecraft.world.waypoints.TrackedWaypoint
import net.minecraft.world.waypoints.Waypoint
import java.nio.charset.StandardCharsets
import java.util.Optional
import java.util.UUID

/**
 * Scans player inventory items to extract exploration waypoints (lodestones, recovery compasses, map banners, spawn).
 */
internal object WaypointScanner {
    private const val DEFAULT_ALPHA: Int = 255
    private const val DEFAULT_LODESTONE_COLOR: Int = 0x80FF80
    private const val DEFAULT_DEATH_COLOR: Int = 0xFFFFFF
    private const val DEFAULT_SPAWN_COLOR: Int = 0xFFFFFF

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

        if (config.trackNormalCompasses.value() && isNormalCompassHeld(player, config)) {
            processNormalCompass(context)
        }

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

    private fun isNormalCompass(stack: ItemStack): Boolean {
        return stack.`is`(Items.COMPASS) && !stack.has(DataComponents.LODESTONE_TRACKER)
    }

    private fun isNormalCompassHeld(player: LocalPlayer, config: StellarExplorationConfig): Boolean {
        if (isNormalCompass(player.mainHandItem) || isNormalCompass(player.offhandItem)) {
            return true
        }
        if (!config.normalCompassRequiresHolding.value()) {
            return player.inventory.nonEquipmentItems.any { isNormalCompass(it) }
        }
        return false
    }

    private fun processNormalCompass(context: TrackingContext) {
        val respawnData = context.player.level().respawnData
        if (respawnData.dimension() != context.dimension) return

        val pos = respawnData.pos()
        val uuid = UUID.nameUUIDFromBytes(
            "spawn:${pos.x}:${pos.y}:${pos.z}".toByteArray(StandardCharsets.UTF_8),
        )
        val color = ARGB.color(DEFAULT_ALPHA, DEFAULT_SPAWN_COLOR)
        val icon = Waypoint.Icon().apply {
            style = WaypointTracking.SPAWN_STYLE
            this.color = Optional.of(color)
        }
        context.destination[uuid] = TrackedWaypoint.setPosition(uuid, icon, pos)
        context.names[uuid] = Component.translatable("waypoint.stellar_exploration.world_spawn")
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
        val color = ColorHandler.getColor(stack) ?: ARGB.color(DEFAULT_ALPHA, DEFAULT_DEATH_COLOR)
        val icon = Waypoint.Icon().apply {
            style = WaypointTracking.DEATH_STYLE
            this.color = Optional.of(color)
        }
        context.destination[uuid] = TrackedWaypoint.setPosition(uuid, icon, pos)
        context.names[uuid] = ColorHandler.extractDisplayName(stack)
            ?: Component.translatable("item.minecraft.recovery_compass")
    }

    private fun processMapBanners(context: TrackingContext, stack: ItemStack) {
        val mapId = stack.get(DataComponents.MAP_ID)
        val server = Minecraft.getInstance().singleplayerServer
        val mapData = if (server != null && mapId != null) {
            server.overworld().getMapData(mapId)
                ?: server.allLevels.firstNotNullOfOrNull { it.getMapData(mapId) }
        } else {
            MapItem.getSavedData(stack, context.player.level())
        }

        if (mapData != null && mapData.dimension == context.dimension) {
            if (mapData.banners.isNotEmpty()) {
                processMapDataBanners(context, mapData)
            } else {
                processMapDecorations(context, mapData)
            }
        }
        processItemDecorations(context, stack)
    }

    private fun processMapDataBanners(context: TrackingContext, mapData: MapItemSavedData) {
        for (banner in mapData.banners) {
            val bannerColor = banner.color()
            val color = ARGB.color(DEFAULT_ALPHA, bannerColor.textureDiffuseColor)
            val name = banner.name().orElseGet {
                val colorName = bannerColor.name.lowercase()
                Component.literal("${colorName.replaceFirstChar { it.uppercase() }} Banner")
            }
            addBannerWaypoint(context, banner.pos(), color, name)
        }
    }

    private fun processMapDecorations(context: TrackingContext, mapData: MapItemSavedData) {
        val scale = 1 shl mapData.scale.toInt()
        for (decoration in mapData.decorations) {
            val assetPath = decoration.type().value().assetId().path
            if (!assetPath.contains("banner")) continue

            val worldX = mapData.centerX + decoration.x().toInt() * scale / 2
            val worldZ = mapData.centerZ + decoration.y().toInt() * scale / 2
            val pos = BlockPos(worldX, context.player.blockY, worldZ)

            val dyeColor = extractBannerColor(assetPath)
            val color = ARGB.color(DEFAULT_ALPHA, dyeColor.textureDiffuseColor)
            val name = decoration.name().orElseGet {
                val colorName = dyeColor.name.lowercase()
                Component.literal("${colorName.replaceFirstChar { it.uppercase() }} Banner")
            }
            addBannerWaypoint(context, pos, color, name)
        }
    }

    private fun processItemDecorations(context: TrackingContext, stack: ItemStack) {
        val decorations = stack.get(DataComponents.MAP_DECORATIONS) ?: return
        for ((_, entry) in decorations.decorations()) {
            val assetPath = entry.type().value().assetId().path
            if (!assetPath.contains("banner")) continue

            val pos = BlockPos(entry.x().toInt(), context.player.blockY, entry.z().toInt())
            val dyeColor = extractBannerColor(assetPath)
            val color = ARGB.color(DEFAULT_ALPHA, dyeColor.textureDiffuseColor)
            val name = Component.literal("${dyeColor.name.lowercase().replaceFirstChar { it.uppercase() }} Banner")
            addBannerWaypoint(context, pos, color, name)
        }
    }

    private fun extractBannerColor(assetPath: String): DyeColor {
        val lower = assetPath.lowercase()
        for (color in DyeColor.entries.sortedByDescending { it.name.length }) {
            if (lower.contains(color.name.lowercase())) {
                return color
            }
        }
        return DyeColor.WHITE
    }

    private fun addBannerWaypoint(context: TrackingContext, pos: BlockPos, color: Int, name: Component) {
        val uuid = UUID.nameUUIDFromBytes(
            "banner:${pos.x}:${pos.y}:${pos.z}".toByteArray(StandardCharsets.UTF_8),
        )
        val icon = Waypoint.Icon().apply {
            style = WaypointTracking.BANNER_STYLE
            this.color = Optional.of(color)
        }
        context.destination[uuid] = TrackedWaypoint.setPosition(uuid, icon, pos)
        context.names[uuid] = name
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
