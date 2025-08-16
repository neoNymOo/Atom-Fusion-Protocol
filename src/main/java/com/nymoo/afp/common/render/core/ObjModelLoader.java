package com.nymoo.afp.common.render.core;

import net.minecraft.util.ResourceLocation;

public class ObjModelLoader implements IModelCustomLoader {

    private static final String[] types = {"obj"};

    @Override
    public String getType() {
        return "OBJ model";
    }

    @Override
    public String[] getSuffixes() {
        return types;
    }

    @Override
    public IModelCustom loadInstance(ResourceLocation resource) throws ModelFormatException {
        return new WavefrontObject(resource);
    }
}
