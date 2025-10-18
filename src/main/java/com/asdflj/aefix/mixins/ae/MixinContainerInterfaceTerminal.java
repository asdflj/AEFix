package com.asdflj.aefix.mixins.ae;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.aefix.mixins.Util;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.parts.IPart;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInterfaceTerminalUpdate;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.util.Platform;

@Mixin(ContainerInterfaceTerminal.class)
public abstract class MixinContainerInterfaceTerminal extends AEBaseContainer {

    @Shadow(remap = false)
    private IGrid grid;

    @Shadow(remap = false)
    private IActionHost anchor;

    @Shadow(remap = false)
    private boolean wasOff;

    @Shadow(remap = false)
    protected abstract PacketInterfaceTerminalUpdate updateList();

    @Shadow(remap = false)
    private boolean isDirty;

    @Shadow(remap = false)
    private PacketInterfaceTerminalUpdate dirty;

    public MixinContainerInterfaceTerminal(InventoryPlayer ip, TileEntity myTile, IPart myPart, IGuiItemObject gio) {
        super(ip, myTile, myPart, gio);
    }

    @Inject(method = "detectAndSendChanges", at = @At("HEAD"), cancellable = true)
    public void detectAndSendChanges(CallbackInfo ci) {
        ci.cancel();
        if (Platform.isClient()) {
            return;
        }

        super.detectAndSendChanges();
        if (this.grid == null) {
            return;
        }
        final IGridNode agn = this.anchor.getActionableNode();

        if (!agn.isActive()) {
            if (!this.wasOff) {
                Util.pool.submit(() -> {
                    PacketInterfaceTerminalUpdate update = new PacketInterfaceTerminalUpdate();
                    update.setDisconnect();
                    update.encode();
                    wasOff = true;
                    NetworkHandler.instance.sendTo(update, (EntityPlayerMP) getPlayerInv().player);
                });
            }
            return;
        }
        this.wasOff = false;

        if (anchor instanceof PartInterfaceTerminal terminal && terminal.needsUpdate()) {
            PacketInterfaceTerminalUpdate update = this.updateList();
            if (update != null) {
                Util.pool.submit(() -> {
                    update.encode();
                    NetworkHandler.instance.sendTo(update, (EntityPlayerMP) getPlayerInv().player);
                });
            }
        } else if (isDirty) {
            Util.pool.submit(() -> {
                this.dirty.encode();
                NetworkHandler.instance.sendTo(this.dirty, (EntityPlayerMP) this.getPlayerInv().player);
                this.dirty = new PacketInterfaceTerminalUpdate();
                this.isDirty = false;
            });
        }
    }
}
