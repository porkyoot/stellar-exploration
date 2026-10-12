package com.stellar.exploration.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi

/**
 * ModMenu integration for Stellar Exploration.
 */
class StellarExplorationModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent ->
            ExplorationClothConfigScreen.create(parent)
        }
    }
}
