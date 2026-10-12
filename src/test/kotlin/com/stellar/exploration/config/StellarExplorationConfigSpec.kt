package com.stellar.exploration.config

import com.stellar.core.config.ConfigManager
import com.stellar.exploration.StellarExplorationMod
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for StellarExplorationConfig.
 */
class StellarExplorationConfigSpec : FunSpec({
    test("config should register and initialize with expected defaults") {
        val config = ConfigManager.register(
            StellarExplorationMod.MOD_ID,
            "test_config",
            StellarExplorationConfig::class.java,
        )

        config.enabled.value() shouldBe true
        config.renderPlayerHeads.value() shouldBe true
        config.headSizeMultiplier.value() shouldBe StellarExplorationConfig.DEFAULT_HEAD_SIZE_INDEX
        config.alwaysShowXP.value() shouldBe false
        config.showCardinalPoints.value() shouldBe true
        config.showCardinalIntermediates.value() shouldBe true
        config.cardinalPointsColorHex.value() shouldBe "#FFFFFFFF"
        config.trackLodestones.value() shouldBe true
        config.trackRecoveryCompasses.value() shouldBe true
        config.trackBannerMarkers.value() shouldBe true
        config.trackBundles.value() shouldBe true
        config.hudEnabled.value() shouldBe true
        config.showCoordinates.value() shouldBe true
        config.showCompass.value() shouldBe true
        config.showBiome.value() shouldBe true
        config.showDimension.value() shouldBe true
    }

    test("head scale multiplier calculation should map correctly") {
        val config = ConfigManager.register(
            StellarExplorationMod.MOD_ID,
            "test_scale_config",
            StellarExplorationConfig::class.java,
        )

        config.headSizeMultiplier.setValue(1, false)
        config.getHeadScale() shouldBe StellarExplorationConfig.SCALE_0_5

        config.headSizeMultiplier.setValue(5, false)
        config.getHeadScale() shouldBe StellarExplorationConfig.SCALE_1_0

        config.headSizeMultiplier.setValue(9, false)
        config.getHeadScale() shouldBe StellarExplorationConfig.SCALE_1_5
    }
})
