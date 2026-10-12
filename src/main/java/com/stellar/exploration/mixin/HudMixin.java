package com.stellar.exploration.mixin;

import com.stellar.core.config.ConfigManager;
import com.stellar.exploration.StellarExplorationMod;
import com.stellar.exploration.config.StellarExplorationConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "willPrioritizeExperienceInfo", at = @At("RETURN"), cancellable = true)
    private void stellarExploration$alwaysShowExperienceBar(CallbackInfoReturnable<Boolean> cir) {
        StellarExplorationConfig config = StellarExplorationMod.getConfig();
        if (config != null && config.getEnabled().value() && config.getAlwaysShowXP().value()
                && this.minecraft.level != null
                && this.minecraft.getCameraEntity() != null
                && this.minecraft.player != null
                && !this.minecraft.player.isPassenger()) {
            cir.setReturnValue(true);
        }
    }
}
