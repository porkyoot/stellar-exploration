package com.stellar.exploration.mixin;

import com.stellar.exploration.waypoints.WaypointTracking;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void stellarExploration$onPlayerTick(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        WaypointTracking.updateWaypoints(player);
    }
}
