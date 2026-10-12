package com.stellar.exploration.config

import com.stellar.core.config.ConfigManager
import com.stellar.exploration.StellarExplorationMod
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

/**
 * Factory for creating the Cloth Config GUI screen bound to StellarExplorationConfig.
 */
object ExplorationClothConfigScreen {
    private const val MIN_HEAD_INDEX: Int = 1
    private const val MAX_HEAD_INDEX: Int = 9

    fun create(parent: Screen?): Screen {
        val config = ConfigManager.get<StellarExplorationConfig>(StellarExplorationMod.MOD_ID, "main")
            ?: ConfigManager.register(StellarExplorationMod.MOD_ID, "main", StellarExplorationConfig::class.java)

        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("stellar_exploration.config.title"))

        builder.setSavingRunnable {
            config.save()
        }

        val entryBuilder = builder.entryBuilder()
        buildGeneralCategory(builder, entryBuilder, config)
        buildLocatorCategory(builder, entryBuilder, config)
        ExplorationClothSecondaryCategories.buildCardinalCategory(builder, entryBuilder, config)
        ExplorationClothSecondaryCategories.buildWaypointsCategory(builder, entryBuilder, config)
        ExplorationClothSecondaryCategories.buildHudCategory(builder, entryBuilder, config)

        return builder.build()
    }

    private fun buildGeneralCategory(
        builder: ConfigBuilder,
        entries: ConfigEntryBuilder,
        config: StellarExplorationConfig,
    ) {
        val category = builder.getOrCreateCategory(
            Component.translatable("stellar_exploration.config.category.general"),
        )

        val enabledEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.enabled"),
                config.enabled.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.enabled.tooltip"))
            .setSaveConsumer { value -> config.enabled.setValue(value, true) }
            .build()

        val alwaysShowXpEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.always_show_xp"),
                config.alwaysShowXP.value(),
            )
            .setDefaultValue(false)
            .setTooltip(Component.translatable("stellar_exploration.config.always_show_xp.tooltip"))
            .setSaveConsumer { value -> config.alwaysShowXP.setValue(value, true) }
            .build()

        category.addEntry(enabledEntry)
        category.addEntry(alwaysShowXpEntry)
    }

    private fun buildLocatorCategory(
        builder: ConfigBuilder,
        entries: ConfigEntryBuilder,
        config: StellarExplorationConfig,
    ) {
        val category = builder.getOrCreateCategory(
            Component.translatable("stellar_exploration.config.category.locator_heads"),
        )
        addLocatorHeadEntries(category, entries, config)
        addLocatorBorderEntries(category, entries, config)
    }

    private fun addLocatorHeadEntries(
        category: ConfigCategory,
        entries: ConfigEntryBuilder,
        config: StellarExplorationConfig,
    ) {
        val renderHeadsEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.render_player_heads"),
                config.renderPlayerHeads.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.render_player_heads.tooltip"))
            .setSaveConsumer { value -> config.renderPlayerHeads.setValue(value, true) }
            .build()

        val headSizeEntry = entries
            .startIntSlider(
                Component.translatable("stellar_exploration.config.head_size"),
                config.headSizeMultiplier.value(),
                MIN_HEAD_INDEX,
                MAX_HEAD_INDEX,
            )
            .setDefaultValue(StellarExplorationConfig.DEFAULT_HEAD_SIZE_INDEX)
            .setTooltip(Component.translatable("stellar_exploration.config.head_size.tooltip"))
            .setSaveConsumer { value -> config.headSizeMultiplier.setValue(value, true) }
            .build()

        category.addEntry(renderHeadsEntry)
        category.addEntry(headSizeEntry)
    }

    private fun addLocatorBorderEntries(
        category: ConfigCategory,
        entries: ConfigEntryBuilder,
        config: StellarExplorationConfig,
    ) {
        val teamBorderEntry = entries
            .startBooleanToggle(
                Component.translatable("stellar_exploration.config.enable_team_border"),
                config.enableTeamBorder.value(),
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("stellar_exploration.config.enable_team_border.tooltip"))
            .setSaveConsumer { value -> config.enableTeamBorder.setValue(value, true) }
            .build()

        val staticBorderEntry = entries
            .startStrField(
                Component.translatable("stellar_exploration.config.static_border_color"),
                config.staticBorderColorHex.value(),
            )
            .setDefaultValue("#FF00E5FF")
            .setTooltip(Component.translatable("stellar_exploration.config.static_border_color.tooltip"))
            .setSaveConsumer { value -> config.staticBorderColorHex.setValue(value.trim(), true) }
            .build()

        val namesEntry = entries
            .startSelector(
                Component.translatable("stellar_exploration.config.show_player_names"),
                arrayOf("OFF", "ALWAYS", "LOOKING_AT", "PLAYER_LIST"),
                config.showPlayerNames.value(),
            )
            .setDefaultValue("LOOKING_AT")
            .setTooltip(Component.translatable("stellar_exploration.config.show_player_names.tooltip"))
            .setSaveConsumer { value -> config.showPlayerNames.setValue(value, true) }
            .build()

        category.addEntry(teamBorderEntry)
        category.addEntry(staticBorderEntry)
        category.addEntry(namesEntry)
    }
}
