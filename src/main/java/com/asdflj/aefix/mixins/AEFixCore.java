package com.asdflj.aefix.mixins;

import java.util.Map;

import javax.annotation.Nullable;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("AEFixCore")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions("com.asdflj.aefix.mixins")
public class AEFixCore implements IFMLLoadingPlugin {

    private static boolean DEV_ENVIRONMENT;

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {};
    }

    @Nullable
    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // NO-OP
        DEV_ENVIRONMENT = !(boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Nullable
    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    public static boolean isDevEnv() {
        return DEV_ENVIRONMENT;
    }
}
