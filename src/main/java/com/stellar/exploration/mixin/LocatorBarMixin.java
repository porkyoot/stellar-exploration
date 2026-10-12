package com.stellar.exploration.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.stellar.exploration.renderer.CardinalPointsRenderer;
import com.stellar.exploration.renderer.DeathWaypointRenderer;
import com.stellar.exploration.renderer.PlayerHeadRenderer;
import com.stellar.exploration.waypoints.WaypointNameRendering;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.client.gui.contextualbar.LocatorBar;
import net.minecraft.resources.Identifier;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocatorBar.class)
public abstract class LocatorBarMixin implements ContextualBar {
    @Shadow @Final private Minecraft minecraft;

    @WrapOperation(
        method = "*",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"
        )
    )
    private void stellarExploration$wrapWaypointSprite(
        GuiGraphicsExtractor guiGraphics,
        RenderPipeline pipeline,
        Identifier sprite,
        int x,
        int y,
        int width,
        int height,
        int color,
        Operation<Void> original,
        @Local TrackedWaypoint waypoint
    ) {
        if (this.minecraft.gui.screen() != null) {
            return;
        }

        boolean renderedHead = PlayerHeadRenderer.renderPlayerHead(
            this.minecraft,
            guiGraphics,
            waypoint,
            x,
            y,
            width,
            height
        );
        if (renderedHead) {
            return;
        }

        if (DeathWaypointRenderer.renderDeathIcon(this.minecraft, guiGraphics, waypoint, sprite, x, y, width, height)) {
            return;
        }

        original.call(guiGraphics, pipeline, sprite, x, y, width, height, color);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void stellarExploration$onExtractRenderState(
        GuiGraphicsExtractor guiGraphics,
        DeltaTracker deltaTracker,
        CallbackInfo ci
    ) {
        if (this.minecraft.gui.screen() != null) {
            return;
        }

        int barLeft = this.left(this.minecraft.getWindow());
        int barTop = this.top(this.minecraft.getWindow());

        WaypointNameRendering.renderNames(this.minecraft, guiGraphics, deltaTracker, barTop);
        CardinalPointsRenderer.render(this.minecraft, guiGraphics, barLeft, barTop, ContextualBar.WIDTH);
    }
}
