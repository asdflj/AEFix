package com.asdflj.aefix.mixins;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

@LateMixin
public class AE2LatePlugin implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.aefix.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        List<String> mixins = new ArrayList<>();
        if (Util.getAEVersion() < 714) {
            mixins.add("ae.AccessorAEItemStack");
            mixins.add("ae.MixinItemList");
            mixins.add("ae.MixinCraftingContext");
        }
        return mixins;
    }

}
