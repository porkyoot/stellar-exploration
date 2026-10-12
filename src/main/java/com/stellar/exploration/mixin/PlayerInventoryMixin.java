package com.stellar.exploration.mixin;

import com.stellar.exploration.waypoints.WaypointTracking;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin {
    @Inject(method = "setChanged", at = @At("RETURN"))
    private void stellarExploration$onInventoryChanged(CallbackInfo ci) {
        WaypointTracking.markDirty();
    }

    @Inject(method = "removeFromSelected", at = @At("RETURN"))
    private void stellarExploration$onItemDropped(boolean entireStack, CallbackInfoReturnable<ItemStack> cir) {
        WaypointTracking.markDirty();
    }
}
