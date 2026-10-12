package com.stellar.exploration.waypoints

import com.stellar.core.config.ConfigManager
import com.stellar.exploration.StellarExplorationMod
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
import net.minecraft.world.waypoints.WaypointStyleAsset
import net.minecraft.world.waypoints.WaypointStyleAssets
import java.nio.charset.StandardCharsets
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages tracking of client-side waypoints: Lodestones, Death Locations, and Map Banners.
 */
object WaypointTracking {
    val LODESTONE_STYLE: ResourceKey<WaypointStyleAsset> = WaypointStyleAssets.createId("stellar_exploration:lodestone")
    val DEATH_STYLE: ResourceKey<WaypointStyleAsset> = WaypointStyleAssets.createId("stellar_exploration:death")
    val BANNER_STYLE: ResourceKey<WaypointStyleAsset> = WaypointStyleAssets.createId("stellar_exploration:banner")

    private val trackedWaypoints: ConcurrentHashMap<UUID, TrackedWaypoint> = ConcurrentHashMap()
    private val waypointNames: ConcurrentHashMap<UUID, Component> = ConcurrentHashMap()

    @Volatile
    private var dirty: Boolean = true

    private var lastUpdateTick: Int = -1

    private const val UPDATE_INTERVAL_TICKS: Int = 20
    private const val DEFAULT_ALPHA: Int = 255
    private const val DEFAULT_LODESTONE_COLOR: Int = 0x80FF80
    private const val DEFAULT_RECOVERY_COLOR: Int = 0x55FFFF

    @JvmStatic
    fun markDirty() {
        dirty = true
    }

    fun getWaypointName(id: UUID): Component? = waypointNames[id]

    fun getAllWaypoints(): Collection<TrackedWaypoint> = trackedWaypoints.values

    @JvmStatic
    fun reset() {
        trackedWaypoints.clear()
        waypointNames.clear()
        lastUpdateTick = -1
        dirty = true
    }

    @JvmStatic
    fun updateWaypoints(player: LocalPlayer) {
        val config = ConfigManager.get<StellarExplorationConfig>(StellarExplorationMod.MOD_ID, "main")
        if (config == null || !config.enabled.value()) {
            if (trackedWaypoints.isNotEmpty()) {
                clearFromManager(player)
            }
            return
        }

        val currentTick = player.tickCount
        val isCooldownActive = lastUpdateTick != -1 && currentTick - lastUpdateTick < UPDATE_INTERVAL_TICKS
        if (!dirty && isCooldownActive) {
            return
        }

        lastUpdateTick = currentTick
        dirty = false

        val previousWaypoints = HashMap(trackedWaypoints)
        val newWaypoints = HashMap<UUID, TrackedWaypoint>()
        val newNames = HashMap<UUID, Component>()

        collectWaypointsFromPlayer(player, config, newWaypoints, newNames)

        trackedWaypoints.clear()
        trackedWaypoints.putAll(newWaypoints)

        waypointNames.clear()
        waypointNames.putAll(newNames)

        reconcileWithManager(player, previousWaypoints, newWaypoints)
    }

    private fun collectWaypointsFromPlayer(
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
            style = LODESTONE_STYLE
            this.color = Optional.of(color)
        }
        val waypoint = TrackedWaypoint.setPosition(uuid, icon, pos)
        context.destination[uuid] = waypoint

        val name = ColorHandler.extractDisplayName(stack)
            ?: Component.translatable("item.minecraft.lodestone_compass")
        context.names[uuid] = name
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
            style = DEATH_STYLE
            this.color = Optional.of(color)
        }
        val waypoint = TrackedWaypoint.setPosition(uuid, icon, pos)
        context.destination[uuid] = waypoint

        val name = ColorHandler.extractDisplayName(stack)
            ?: Component.translatable("item.minecraft.recovery_compass")
        context.names[uuid] = name
    }

    private fun processMapBanners(context: TrackingContext, stack: ItemStack) {
        val mapData = MapItem.getSavedData(stack, context.player.level()) ?: return
        if (mapData.dimension != context.dimension) return

        val banners = mapData.banners
        for (banner in banners) {
            val pos = banner.pos()
            val bannerId = banner.id
            val uuid = UUID.nameUUIDFromBytes(
                "banner:$bannerId:${pos.x}:${pos.y}:${pos.z}".toByteArray(StandardCharsets.UTF_8),
            )
            val bannerColor = banner.color()
            val diffuseColor = bannerColor.textureDiffuseColor
            val color = ARGB.color(DEFAULT_ALPHA, diffuseColor)

            val icon = Waypoint.Icon().apply {
                style = BANNER_STYLE
                this.color = Optional.of(color)
            }
            val waypoint = TrackedWaypoint.setPosition(uuid, icon, pos)
            context.destination[uuid] = waypoint

            val bannerName = banner.name().orElseGet {
                val colorName = bannerColor.name.lowercase()
                Component.literal("${colorName.replaceFirstChar { it.uppercase() }} Banner")
            }
            context.names[uuid] = bannerName
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

    private fun reconcileWithManager(
        player: LocalPlayer,
        oldWaypoints: Map<UUID, TrackedWaypoint>,
        newWaypoints: Map<UUID, TrackedWaypoint>,
    ) {
        val manager = player.connection.waypointManager

        for ((id, newWp) in newWaypoints) {
            if (oldWaypoints.containsKey(id)) {
                manager.updateWaypoint(newWp)
            } else {
                manager.trackWaypoint(newWp)
            }
        }

        for ((id, oldWp) in oldWaypoints) {
            if (!newWaypoints.containsKey(id)) {
                manager.untrackWaypoint(oldWp)
            }
        }
    }

    private fun clearFromManager(player: LocalPlayer) {
        val manager = player.connection.waypointManager
        for (wp in trackedWaypoints.values) {
            manager.untrackWaypoint(wp)
        }
        trackedWaypoints.clear()
        waypointNames.clear()
    }
}
