package com.stellar.exploration

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

/**
 * Basic unit test verifying Stellar Exploration metadata and constants.
 */
class StellarExplorationSpec : FunSpec({
    test("stellar exploration mod id should be valid") {
        StellarExplorationMod.MOD_ID.shouldNotBeBlank()
        StellarExplorationMod.MOD_ID shouldBe "stellar_exploration"
    }
})
