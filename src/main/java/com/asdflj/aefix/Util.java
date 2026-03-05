package com.asdflj.aefix;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;

import appeng.api.storage.data.IAEStack;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Util {

    public static ExecutorService pool = Executors
        .newSingleThreadExecutor(r -> new Thread(r, "AE2 Fix interface terminal thread"));

    private static int AE_VERSION = -1;

    public static int getAEVersion() {
        if (AE_VERSION == -1) {
            Optional<ModContainer> mod = Loader.instance()
                .getActiveModList()
                .stream()
                .filter(
                    x -> x.getModId()
                        .equals("appliedenergistics2"))
                .findFirst();
            if (mod.isPresent()) {
                try {
                    AE_VERSION = Integer.parseInt(
                        mod.get()
                            .getVersion()
                            .split("-")[2]);
                } catch (Exception ignored) {
                    AE_VERSION = 0;
                }
            } else {
                AE_VERSION = 0;
            }
        }
        return AE_VERSION;
    }

    public static class Gzip {

        protected static final int UNCOMPRESSED_PACKET_BYTE_LIMIT = 16 * 1024 * 1024;
        protected static final int OPERATION_BYTE_LIMIT = 2 * 1024;
        protected static final int TEMP_BUFFER_SIZE = 1024;
        protected static final int STREAM_MASK = 0xff;
        @Nullable
        protected GZIPOutputStream compressFrame;
        protected int writtenBytes = 0;
        protected final ByteBuf data = Unpooled.buffer(OPERATION_BYTE_LIMIT);

        public void compress(List<IAEStack<?>> list, ByteBuf buf) throws IOException, BufferOverflowException {
            this.compressFrame = new GZIPOutputStream(new OutputStream() {

                @Override
                public void write(final int value) {
                    data.writeByte(value);
                }
            });
            for (IAEStack<?> is : list) {
                final ByteBuf tmp = Unpooled.buffer(OPERATION_BYTE_LIMIT);
                is.writeToPacket(tmp);
                assert this.compressFrame != null;
                this.compressFrame.flush();
                if (this.writtenBytes + tmp.readableBytes() > UNCOMPRESSED_PACKET_BYTE_LIMIT) {
                    throw new BufferOverflowException();
                } else {
                    this.writtenBytes += tmp.readableBytes();
                    this.compressFrame.write(tmp.array(), 0, tmp.readableBytes());
                }
            }
            assert this.compressFrame != null;
            this.compressFrame.finish();
            buf.writeBytes(this.data);
            this.compressFrame.close();
        }

        public List<IAEStack<?>> extract(ByteBuf buf, boolean isFluid) throws IOException, BufferOverflowException {
            final GZIPInputStream gzReader = new GZIPInputStream(new InputStream() {

                @Override
                public int read() {
                    if (buf.readableBytes() <= 0) {
                        return -1;
                    }
                    return buf.readByte() & STREAM_MASK;
                }
            });
            final ByteBuf uncompressed = Unpooled.buffer(buf.readableBytes());
            final byte[] tmp = new byte[TEMP_BUFFER_SIZE];
            while (gzReader.available() != 0) {
                final int bytes = gzReader.read(tmp);
                if (bytes > 0) {
                    uncompressed.writeBytes(tmp, 0, bytes);
                }
            }
            gzReader.close();
            List<IAEStack<?>> list = new ArrayList<>();
            while (uncompressed.readableBytes() > 0) {
                if (isFluid) {
                    list.add(AEFluidStack.loadFluidStackFromPacket(uncompressed));
                } else {
                    list.add(AEItemStack.loadItemStackFromPacket(uncompressed));
                }
            }
            return list;
        }
    }

}
