package com.asdflj.aefix.mixins.ae;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.crafting.MECraftingInventory;
import appeng.crafting.v2.CraftingContext;

@Mixin(CraftingContext.class)
public abstract class MixinCraftingContext {

    @Redirect(
        method = "<init>",
        at = @At(
            value = "NEW",
            target = "(Lappeng/api/storage/IMEMonitor;Lappeng/api/networking/security/BaseActionSource;ZZZ)Lappeng/crafting/MECraftingInventory;"),
        remap = false)
    public MECraftingInventory craftingContext(IMEMonitor imeMonitor, BaseActionSource target, boolean src,
        boolean logExtracted, boolean logInjections) {
        return new MECraftingInventory(imeMonitor, src, logExtracted, logInjections);
    }

}
