package com.asdflj.aefix.mixins.fc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.aefix.Util;
import com.glodblock.github.network.SPacketMEItemInvUpdate;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

@Mixin(SPacketMEItemInvUpdate.class)
public abstract class MixinSPacketMEItemInvUpdate implements IMessage {

    @Shadow(remap = false)
    private List<IAEItemStack> list = new ArrayList<>();
    @Shadow(remap = false)
    private byte ref = (byte) 0;
    @Shadow(remap = false)
    private boolean resort = true;

    @Inject(method = "fromBytes", at = @At("HEAD"), cancellable = true, remap = false)
    public void fromBytes(ByteBuf buf, CallbackInfo ci) {
        ref = buf.readByte();
        resort = buf.readBoolean();
        int amount = buf.readInt();
        list = new ArrayList<>(amount);
        try {
            Util.Gzip gzip = new Util.Gzip();
            for (IAEStack<?> is : gzip.extract(buf, false)) {
                list.add((IAEItemStack) is);
            }
        } catch (Exception io) {
            System.out.println("Error handling payload w/ " + amount + " items.");
            io.printStackTrace();
        }
        ci.cancel();
    }

    @Inject(method = "toBytes", at = @At("HEAD"), cancellable = true, remap = false)
    public void toBytes(ByteBuf buf, CallbackInfo ci) {
        buf.writeByte(ref);
        buf.writeBoolean(resort);
        buf.writeInt(list.size());
        try {
            Util.Gzip gzip = new Util.Gzip();
            List<IAEStack<?>> tmp = new ArrayList<>(list);
            gzip.compress(tmp, buf);
        } catch (IOException io) {
            io.printStackTrace();
        }
        ci.cancel();
    }
}
