package com.asdflj.aefix.mixins.ae;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import appeng.util.item.MeaningfulItemIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

@Mixin(ItemList.class)
public abstract class MixinItemList implements IItemList<IAEItemStack> {

    @Shadow(remap = false)
    @Final
    private ObjectOpenHashSet<IAEItemStack> setRecords;
    @Nullable
    private TreeSet<IAEItemStack> treeRecords = null;

    @Inject(method = "putItemRecord", at = @At("HEAD"), cancellable = true, remap = false)
    private void putItemRecord(IAEItemStack itemStack, CallbackInfo ci) {
        this.setRecords.add(itemStack);
        if (this.treeRecords != null) {
            this.treeRecords.add(itemStack);
        }
        ci.cancel();
    }

    @Inject(method = "clear", at = @At("HEAD"), remap = false)
    public void clear(CallbackInfo ci) {
        if (this.treeRecords != null) {
            this.treeRecords.clear();
        }
    }

    @Inject(method = "findFuzzyDamage", at = @At("HEAD"), remap = false, cancellable = true)
    private void findFuzzyDamage(AEItemStack filter, FuzzyMode fuzzy, boolean ignoreMeta,
        CallbackInfoReturnable<Collection<IAEItemStack>> cir) {
        if (this.setRecords.isEmpty()) {
            cir.setReturnValue(Collections.emptySet());
            return;
        }
        if (this.treeRecords == null) {
            this.treeRecords = new TreeSet<>();
            this.treeRecords.addAll(this.setRecords);
        }
        final IAEItemStack low = ((AccessorAEItemStack) (Object) filter).getItemLow(fuzzy, ignoreMeta);
        final IAEItemStack high = ((AccessorAEItemStack) (Object) filter).getItemHigh(fuzzy, ignoreMeta);
        cir.setReturnValue(
            this.treeRecords.subSet(low, true, high, true)
                .descendingSet());
    }

    @Inject(method = "iterator", at = @At("HEAD"), cancellable = true, remap = false)
    public void iterator(CallbackInfoReturnable<Iterator<IAEItemStack>> cir) {
        cir.setReturnValue(new MeaningfulItemIterator<>(new Iterator<>() {

            private final Iterator<IAEItemStack> i = setRecords.iterator();
            private IAEItemStack next = null;

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public IAEItemStack next() {
                return (next = i.next());
            }

            @Override
            public void remove() {
                i.remove();
                if (treeRecords != null) {
                    treeRecords.remove(next);
                }
            }
        }));
    }

}
