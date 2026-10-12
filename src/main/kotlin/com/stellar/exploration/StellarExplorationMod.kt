package com.stellar.exploration

import com.stellar.core.StellarCore
import com.stellar.core.config.ConfigManager
import com.stellar.exploration.config.StellarExplorationConfig
import com.stellar.exploration.framework.ExplorationEngine
import net.fabricmc.api.ClientModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Dedicated client entry point for the Stellar Exploration mod (100% client-side).
 */
class StellarExplorationMod : ClientModInitializer {
    private val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitializeClient() {
        StellarCore.logInfo("Initializing Stellar Exploration under namespace ${StellarCore.NAMESPACE}")
        logger.info("Initializing client mod Stellar Exploration")

        val config = ConfigManager.register(MOD_ID, "main", StellarExplorationConfig::class.java)
        ExplorationEngine.initialize()

        logger.info("Stellar Exploration framework initialized (enabled={})", config.enabled.value())
    }

    companion object {
        const val MOD_ID: String = "stellar_exploration"

        @JvmStatic
        fun getConfig(): StellarExplorationConfig? = ConfigManager.get(MOD_ID, "main")
    }
}
