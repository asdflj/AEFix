package com.asdflj.aefix.mixins.ae;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.api.networking.IGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.interfaces.ICraftingCPUSelectorContainer;

@Mixin(ContainerCraftConfirm.class)
public abstract class MixinContainerCraftConfirm extends AEBaseContainer implements ICraftingCPUSelectorContainer {

    @Shadow(remap = false)
    protected abstract IGrid getGrid();

    public MixinContainerCraftConfirm(InventoryPlayer ip, TileEntity myTile, IPart myPart) {
        super(ip, myTile, myPart);
    }

    @Redirect(
        method = "detectAndSendChanges",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/api/storage/IMEInventory;getAvailableItem(Lappeng/api/storage/data/IAEStack;I)Lappeng/api/storage/data/IAEStack;"),
        remap = false)
    public IAEStack<IAEItemStack> getAvailableItem(IMEInventory instance, IAEStack<IAEItemStack> request,
        int iteration) {
        final IStorageGrid sg = this.getGrid()
            .getCache(IStorageGrid.class);
        return sg.getItemInventory()
            .getStorageList()
            .findPrecise((IAEItemStack) request);
    }
}
