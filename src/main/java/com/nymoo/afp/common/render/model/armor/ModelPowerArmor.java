package com.nymoo.afp.common.render.model.armor;

import com.nymoo.afp.common.render.core.AdvancedModelLoader;
import com.nymoo.afp.common.render.core.IModelCustom;
import com.nymoo.afp.common.render.core.ModelRendererObj;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class ModelPowerArmor extends ModelArmorBase {
    private static final Map<String, IModelCustom> MODEL_CACHE = new HashMap<>();
    private static final Map<String, IModelCustom> JET_MODEL_CACHE = new HashMap<>();

    private final String armorType;
    private final boolean hasJetpackVariant;

    public ModelPowerArmor(int type, String armorType, boolean hasJetpackVariant) {
        super(type);
        this.armorType = armorType;
        this.hasJetpackVariant = hasJetpackVariant;
        loadModels();
    }

    private void loadModels() {
        String baseKey = armorType + "_" + type;
        boolean isJetpack = hasJetpackVariant && type == 1;

        if (!MODEL_CACHE.containsKey(baseKey)) {
            String modelPath = "models/armor/" + armorType + "/" + armorType + "_armor.obj";
            MODEL_CACHE.put(baseKey, AdvancedModelLoader.loadModel(new ResourceLocation("afp", modelPath)));
        }

        if (isJetpack && !JET_MODEL_CACHE.containsKey(baseKey)) {
            String jetModelPath = "models/armor/" + armorType + "/" + armorType + "_j_armor.obj";
            JET_MODEL_CACHE.put(baseKey, AdvancedModelLoader.loadModel(new ResourceLocation("afp", jetModelPath)));
        }

        IModelCustom model = isJetpack ? JET_MODEL_CACHE.get(baseKey) : MODEL_CACHE.get(baseKey);

        head = new ModelRendererObj(model, "Head");
        body = new ModelRendererObj(model, "Body");
        leftArm = new ModelRendererObj(model, "LeftArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
        rightArm = new ModelRendererObj(model, "RightArm").setRotationPoint(5.0F, 2.0F, 0.0F);
        leftLeg = new ModelRendererObj(model, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
        rightLeg = new ModelRendererObj(model, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
        leftFoot = new ModelRendererObj(model, "LeftBoot").setRotationPoint(1.9F, 12.0F, 0.0F);
        rightFoot = new ModelRendererObj(model, "RightBoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entity);

        GL11.glPushMatrix();
        GL11.glShadeModel(GL11.GL_SMOOTH);

        if (this.isChild) {
            GL11.glScalef(0.75F, 0.75F, 0.75F);
            GL11.glTranslatef(0.0F, 16.0F * scale, 0.0F);
        }

        ResourceLocation texture = new ResourceLocation("afp", "textures/armor/" + armorType + "/" + armorType + ".png");
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        switch (type) {
            case 0:
                head.render(scale * 1.001F);
                break;
            case 1:
                body.render(scale);
                leftArm.render(scale);
                rightArm.render(scale);
                break;
            case 2:
                leftLeg.render(scale);
                rightLeg.render(scale);
                break;
            case 3:
                leftFoot.render(scale);
                rightFoot.render(scale);
                break;
        }

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glPopMatrix();
    }
}