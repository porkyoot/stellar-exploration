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

        if (config.trackNormalCompasses.value()) {
            val normalCompass = findNormalCompass(player, config)
            if (normalCompass != null) {
                processNormalCompass(context, normalCompass)
            }
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

    private fun findNormalCompass(player: LocalPlayer, config: StellarExplorationConfig): ItemStack? {
        val hands = listOf(player.mainHandItem, player.offhandItem)
        val held = hands.firstOrNull { it.isNormalCompass() }
        if (held != null) return held
        if (config.normalCompassRequiresHolding.value()) return null
        return player.inventory.nonEquipmentItems.firstOrNull { it.isNormalCompass() }
    }

    private fun processNormalCompass(context: TrackingContext, compass: ItemStack) {
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
        val customName = ColorHandler.extractCustomName(compass)
        if (customName != null) {
            context.names[uuid] = customName
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
            stack.get(DataComponents.BUNDLE_CONTENTS)?.itemCopyStream()?.forEach { bundled ->
                processItemStack(context, bundled)
            }
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
        val customName = ColorHandler.extractCustomName(stack)
        if (customName != null) {
            context.names[uuid] = customName
        }
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
        val customName = ColorHandler.extractCustomName(stack)
        if (customName != null) {
            context.names[uuid] = customName
        }
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
            val color = ARGB.color(DEFAULT_ALPHA, banner.color().textureDiffuseColor)
            val customName = extractCleanBannerName(banner.name().orElse(null))
            addBannerWaypoint(context, banner.pos(), color, customName)
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
            val customName = extractCleanBannerName(decoration.name().orElse(null))
            addBannerWaypoint(context, pos, color, customName)
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
            addBannerWaypoint(context, pos, color, null)
        }
    }

    private fun addBannerWaypoint(context: TrackingContext, pos: BlockPos, color: Int, name: Component?) {
        val uuid = UUID.nameUUIDFromBytes(
            "banner:${pos.x}:${pos.y}:${pos.z}".toByteArray(StandardCharsets.UTF_8),
        )
        val icon = Waypoint.Icon().apply {
            style = WaypointTracking.BANNER_STYLE
            this.color = Optional.of(color)
        }
        context.destination[uuid] = TrackedWaypoint.setPosition(uuid, icon, pos)
        if (name != null) {
            context.names[uuid] = name
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

private fun ItemStack.isNormalCompass(): Boolean =
    this.`is`(Items.COMPASS) && !this.has(DataComponents.LODESTONE_TRACKER)

private fun extractCleanBannerName(component: Component?): Component? {
    val raw = component?.string ?: return null
    val cleaned = ColorHandler.removeColorCodes(raw)
    return if (cleaned.isNotBlank()) Component.literal(cleaned) else null
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
