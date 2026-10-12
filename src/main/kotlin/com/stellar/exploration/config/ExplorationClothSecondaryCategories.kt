package com.stellar.exploration.config

import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.network.chat.Component

/**
 * Builds secondary categories (Cardinal Points, Waypoints, and HUD) for ExplorationClothConfigScreen.
 */
internal object ExplorationClothSecondaryCategories {
    fun buildCardinalCategory(builder: ConfigBuilder, entries: ConfigEntryBuilder, config: StellarExplorationConfig) {
        val category = builder.getOrCreateCategory(
            Component.translatable("stellar_exploration.config.category.cardinal_points"),
        )

        val cardinalEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.show_cardinal_points"),
                config.showCardinalPoints.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.show_cardinal_points.tooltip"))
            .setSaveConsumer { value -> config.showCardinalPoints.setValue(value, true) }
            .build()

        val intermediatesEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.show_cardinal_intermediates"),
                config.showCardinalIntermediates.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.show_cardinal_intermediates.tooltip"))
            .setSaveConsumer { value -> config.showCardinalIntermediates.setValue(value, true) }
            .build()

        val colorEntry = entries
            .startStrField(
                Component.translatable("stellar_exploration.config.cardinal_points_color"),
                config.cardinalPointsColorHex.value(),
            )
            .setDefaultValue("#FFFFFFFF")
            .setTooltip(Component.translatable("stellar_exploration.config.cardinal_points_color.tooltip"))
            .setSaveConsumer { value -> config.cardinalPointsColorHex.setValue(value.trim(), true) }
            .build()

        category.addEntry(cardinalEntry)
        category.addEntry(intermediatesEntry)
        category.addEntry(colorEntry)
    }

    fun buildWaypointsCategory(builder: ConfigBuilder, entries: ConfigEntryBuilder, config: StellarExplorationConfig) {
        val category = builder.getOrCreateCategory(
            Component.translatable("stellar_exploration.config.category.waypoints"),
        )
        addCompassWaypointEntries(category, entries, config)
        addInventoryWaypointEntries(category, entries, config)
    }

    private fun addCompassWaypointEntries(
        category: ConfigCategory,
        entries: ConfigEntryBuilder,
        config: StellarExplorationConfig,
    ) {
        val lodestoneEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.track_lodestones"),
                config.trackLodestones.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.track_lodestones.tooltip"))
            .setSaveConsumer { value -> config.trackLodestones.setValue(value, true) }
            .build()

        val recoveryEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.track_recovery"),
                config.trackRecoveryCompasses.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.track_recovery.tooltip"))
            .setSaveConsumer { value -> config.trackRecoveryCompasses.setValue(value, true) }
            .build()

        category.addEntry(lodestoneEntry)
        category.addEntry(recoveryEntry)
    }

    private fun addInventoryWaypointEntries(
        category: ConfigCategory,
        entries: ConfigEntryBuilder,
        config: StellarExplorationConfig,
    ) {
        val bannersEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.track_banner_markers"),
                config.trackBannerMarkers.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.track_banner_markers.tooltip"))
            .setSaveConsumer { value -> config.trackBannerMarkers.setValue(value, true) }
            .build()

        val bundleEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.track_bundles"),
                config.trackBundles.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.track_bundles.tooltip"))
            .setSaveConsumer { value -> config.trackBundles.setValue(value, true) }
            .build()

        category.addEntry(bannersEntry)
        category.addEntry(bundleEntry)
    }

    fun buildHudCategory(builder: ConfigBuilder, entries: ConfigEntryBuilder, config: StellarExplorationConfig) {
        val category = builder.getOrCreateCategory(
            Component.translatable("stellar_exploration.config.category.hud"),
        )

        val hudEnabledEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.hud_enabled"),
                config.hudEnabled.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.hud_enabled.tooltip"))
            .setSaveConsumer { value -> config.hudEnabled.setValue(value, true) }
            .build()

        category.addEntry(hudEnabledEntry)
        addHudSpatialEntries(category, entries, config)
        addHudEnvironmentEntries(category, entries, config)
    }

    private fun addHudSpatialEntries(
        category: ConfigCategory,
        entries: ConfigEntryBuilder,
        config: StellarExplorationConfig,
    ) {
        val showCoordsEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.show_coordinates"),
                config.showCoordinates.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.show_coordinates.tooltip"))
            .setSaveConsumer { value -> config.showCoordinates.setValue(value, true) }
            .build()

        val showCompassEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.show_compass"),
                config.showCompass.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.show_compass.tooltip"))
            .setSaveConsumer { value -> config.showCompass.setValue(value, true) }
            .build()

        category.addEntry(showCoordsEntry)
        category.addEntry(showCompassEntry)
    }

    private fun addHudEnvironmentEntries(
        category: ConfigCategory,
        entries: ConfigEntryBuilder,
        config: StellarExplorationConfig,
    ) {
        val showBiomeEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.show_biome"),
                config.showBiome.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.show_biome.tooltip"))
            .setSaveConsumer { value -> config.showBiome.setValue(value, true) }
            .build()

        val showDimensionEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.show_dimension"),
                config.showDimension.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.show_dimension.tooltip"))
            .setSaveConsumer { value -> config.showDimension.setValue(value, true) }
            .build()

        category.addEntry(showBiomeEntry)
        category.addEntry(showDimensionEntry)
    }
}
