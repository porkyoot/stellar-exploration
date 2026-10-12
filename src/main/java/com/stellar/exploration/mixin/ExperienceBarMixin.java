package com.stellar.exploration.mixin;

import com.stellar.core.config.ConfigManager;
import com.stellar.exploration.StellarExplorationMod;
import com.stellar.exploration.config.StellarExplorationConfig;
import com.stellar.exploration.renderer.CardinalPointsRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.client.gui.contextualbar.ExperienceBar;
import net.minecraft.client.gui.contextualbar.LocatorBar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceBar.class)
public abstract class ExperienceBarMixin implements ContextualBar {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void stellarExploration$onExtractExperienceRenderState(
        GuiGraphicsExtractor guiGraphics,
        DeltaTracker deltaTracker,
        CallbackInfo ci
    ) {
        StellarExplorationConfig config = StellarExplorationMod.getConfig();
        if (config == null || !config.getEnabled().value()) return;

        int barLeft = this.left(this.minecraft.getWindow());
        int barTop = this.top(this.minecraft.getWindow());

        if (config.getAlwaysShowXP().value()) {
            LocatorBar locatorBar = new LocatorBar(this.minecraft);
            locatorBar.extractRenderState(guiGraphics, deltaTracker);
        } else {
            CardinalPointsRenderer.render(this.minecraft, guiGraphics, barLeft, barTop, ContextualBar.WIDTH);
        }
    }
}
