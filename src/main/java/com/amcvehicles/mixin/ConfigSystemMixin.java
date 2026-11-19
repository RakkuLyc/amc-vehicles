package com.amcvehicles.mixin;

import com.amcvehicles.FuelBalance;
import minecrafttransportsimulator.systems.ConfigSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the mod's lava potency out of IV's config file: IV saves {@code config/mtsconfig.json} while a world is open
 * (its controls and config screens, and when a player joins for the first time), and that save used to write the
 * mod's in-memory 0.05. Around every save, IV's own values are put back in memory, then the mod's again.
 */
@Mixin(value = ConfigSystem.class, remap = false)
public abstract class ConfigSystemMixin {
    @Inject(method = "saveToDisk", at = @At("HEAD"))
    private static void amcvehicles$beforeSave(CallbackInfo ci) {
        FuelBalance.beforeIvSave();
    }

    @Inject(method = "saveToDisk", at = @At("RETURN"))
    private static void amcvehicles$afterSave(CallbackInfo ci) {
        FuelBalance.afterIvSave();
    }
}
