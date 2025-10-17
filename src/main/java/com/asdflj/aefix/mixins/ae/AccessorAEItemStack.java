package com.asdflj.aefix.mixins.ae;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

@Mixin(AEItemStack.class)
public interface AccessorAEItemStack {

    @Invoker(value = "getLow", remap = false)
    IAEItemStack getItemLow(final FuzzyMode fuzzy, final boolean ignoreMeta);

    @Invoker(value = "getHigh", remap = false)
    IAEItemStack getItemHigh(final FuzzyMode fuzzy, final boolean ignoreMeta);
}
