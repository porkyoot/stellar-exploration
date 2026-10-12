package com.stellar.exploration.waypoints

import com.stellar.core.config.ConfigManager
import com.stellar.exploration.StellarExplorationMod
import com.stellar.exploration.config.StellarExplorationConfig
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.waypoints.TrackedWaypoint
import net.minecraft.world.waypoints.WaypointStyleAsset
import net.minecraft.world.waypoints.WaypointStyleAssets
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages tracking of client-side waypoints: Lodestones, Death Locations, and Map Banners.
 */
object WaypointTracking {
    val LODESTONE_STYLE: ResourceKey<WaypointStyleAsset> = ResourceKey.create(
        WaypointStyleAssets.ROOT_ID,
        Identifier.fromNamespaceAndPath(StellarExplorationMod.MOD_ID, "lodestone"),
    )
    val DEATH_STYLE: ResourceKey<WaypointStyleAsset> = ResourceKey.create(
        WaypointStyleAssets.ROOT_ID,
        Identifier.fromNamespaceAndPath(StellarExplorationMod.MOD_ID, "death"),
    )
    val BANNER_STYLE: ResourceKey<WaypointStyleAsset> = ResourceKey.create(
        WaypointStyleAssets.ROOT_ID,
        Identifier.fromNamespaceAndPath(StellarExplorationMod.MOD_ID, "banner"),
    )
    val SPAWN_STYLE: ResourceKey<WaypointStyleAsset> = ResourceKey.create(
        WaypointStyleAssets.ROOT_ID,
        Identifier.fromNamespaceAndPath(StellarExplorationMod.MOD_ID, "spawn"),
    )

    private val trackedWaypoints: ConcurrentHashMap<UUID, TrackedWaypoint> = ConcurrentHashMap()
    private val waypointNames: ConcurrentHashMap<UUID, Component> = ConcurrentHashMap()

    @Volatile
    private var dirty: Boolean = true

    private var lastUpdateTick: Int = -1
    private var lastPlayerUUID: UUID? = null
    private var lastPlayerTickCount: Int = -1

    private const val UPDATE_INTERVAL_TICKS: Int = 20

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
        lastPlayerUUID = null
        lastPlayerTickCount = -1
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

        checkPlayerSession(player)
        if (shouldSkipUpdate(player.tickCount)) {
            return
        }

        lastUpdateTick = player.tickCount
        dirty = false

        val previousWaypoints = HashMap(trackedWaypoints)
        val newWaypoints = HashMap<UUID, TrackedWaypoint>()
        val newNames = HashMap<UUID, Component>()

        WaypointScanner.scanPlayerInventory(player, config, newWaypoints, newNames)

        trackedWaypoints.clear()
        trackedWaypoints.putAll(newWaypoints)

        waypointNames.clear()
        waypointNames.putAll(newNames)

        reconcileWithManager(player, previousWaypoints, newWaypoints)
    }

    private fun checkPlayerSession(player: LocalPlayer) {
        val currentTick = player.tickCount
        if (player.uuid != lastPlayerUUID || currentTick < lastPlayerTickCount) {
            lastPlayerUUID = player.uuid
            lastUpdateTick = -1
            dirty = true
            clearFromManager(player)
        }
        lastPlayerTickCount = currentTick
    }

    private fun shouldSkipUpdate(currentTick: Int): Boolean {
        if (dirty) return false
        if (lastUpdateTick == -1 || currentTick < lastUpdateTick) return false
        return currentTick - lastUpdateTick < UPDATE_INTERVAL_TICKS
    }

    private fun reconcileWithManager(
        player: LocalPlayer,
        oldWaypoints: Map<UUID, TrackedWaypoint>,
        newWaypoints: Map<UUID, TrackedWaypoint>,
    ) {
        val manager = player.connection.waypointManager

        for ((_, newWp) in newWaypoints) {
            manager.trackWaypoint(newWp)
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
