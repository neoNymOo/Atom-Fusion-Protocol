package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.entity.EntityExoskeleton.Exoskeleton;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public abstract class MixinModelBiped {
    @Inject(method = "setRotationAngles", at = @At("RETURN"))
    private void onSetRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo ci) {
        if (entityIn instanceof Exoskeleton) {
            ModelBiped self = (ModelBiped) (Object) this;

            if (self.bipedRightArm != null) {
                self.bipedRightArm.rotateAngleX = 0.0F;
                self.bipedRightArm.rotateAngleY = 0.0F;
                self.bipedRightArm.rotateAngleZ = 0.05F;
            }

            if (self.bipedLeftArm != null) {
                self.bipedLeftArm.rotateAngleX = 0.0F;
                self.bipedLeftArm.rotateAngleY = 0.0F;
                self.bipedLeftArm.rotateAngleZ = -0.05F;
            }
        }
    }
}
