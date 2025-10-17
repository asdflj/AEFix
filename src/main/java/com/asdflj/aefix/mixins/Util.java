package com.asdflj.aefix.mixins;

import java.util.Optional;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class Util {

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
}
