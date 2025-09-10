package com.nymoo.afp.common.mixin.impl;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("nextStepDistance")
    int getNextStepDistance();

    @Accessor("distanceWalkedOnStepModified")
    float getDistanceWalkedOnStepModified();
}
