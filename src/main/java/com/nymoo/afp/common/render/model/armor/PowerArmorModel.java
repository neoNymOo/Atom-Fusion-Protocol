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

/**
 * Модель силовой брони для рендеринга кастомных 3D-моделей.
 * Загружает модели из OBJ-файлов и управляет их отображением с поддержкой кеширования.
 */
public class PowerArmorModel extends AbstractArmorModel {
    /**
     * Кеш загруженных моделей для оптимизации производительности
     */
    private static final Map<String, IModelCustom> MODEL_CACHE = new HashMap<>();
    /**
     * Кеш загруженных моделей с реактивным ранцем
     */
    private static final Map<String, IModelCustom> JET_MODEL_CACHE = new HashMap<>();

    /**
     * Тип силовой брони для определения путей к ресурсам
     */
    private final String armorType;
    /**
     * Флаг наличия варианта модели с реактивным ранцем
     */
    private final boolean hasJetpackVariant;

    /**
     * Создает новую модель силовой брони с указанными параметрами.
     *
     * @param type              Тип слота брони (0 - голова, 1 - тело, 2 - ноги, 3 - ступни)
     * @param armorType         Тип брони для загрузки соответствующих ресурсов
     * @param hasJetpackVariant Наличие варианта с реактивным ранцем
     */
    public PowerArmorModel(int type, String armorType, boolean hasJetpackVariant) {
        super(type);
        this.armorType = armorType;
        this.hasJetpackVariant = hasJetpackVariant;
        loadModels();
    }

    /**
     * Загружает модели из файлов и инициализирует рендереры частей брони.
     * Использует кеширование для избежания повторной загрузки одинаковых моделей.
     */
    private void loadModels() {
        String key = armorType + "_" + type + (hasJetpackVariant ? "_jet" : "");

        if (hasJetpackVariant && !JET_MODEL_CACHE.containsKey(key)) {
            String modelPath = "models/armor/" + armorType + "/" + armorType + "_j_armor.obj";
            JET_MODEL_CACHE.put(key, AdvancedModelLoader.loadModel(new ResourceLocation("afp", modelPath)));
        } else if (!MODEL_CACHE.containsKey(key)) {
            String modelPath = "models/armor/" + armorType + "/" + armorType + "_armor.obj";
            MODEL_CACHE.put(key, AdvancedModelLoader.loadModel(new ResourceLocation("afp", modelPath)));
        }

        IModelCustom model = hasJetpackVariant ? JET_MODEL_CACHE.get(key) : MODEL_CACHE.get(key);

        head = new ModelRendererObj(model, "Head");
        body = new ModelRendererObj(model, "Body");
        leftArm = new ModelRendererObj(model, "LeftArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
        rightArm = new ModelRendererObj(model, "RightArm").setRotationPoint(5.0F, 2.0F, 0.0F);
        leftLeg = new ModelRendererObj(model, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
        rightLeg = new ModelRendererObj(model, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
        leftFoot = new ModelRendererObj(model, "LeftBoot").setRotationPoint(1.9F, 12.0F, 0.0F);
        rightFoot = new ModelRendererObj(model, "RightBoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
    }

    /**
     * Отрисовывает модель силовой брони с учетом типа слота и состояния сущности.
     * Управляет трансформациями, привязкой текстур и рендерингом соответствующих частей модели.
     *
     * @param entity          Сущность для рендеринга
     * @param limbSwing       Взмах конечностей для анимации
     * @param limbSwingAmount Интенсивность взмаха конечностей
     * @param ageInTicks      Время существования сущности в тиках
     * @param headYaw         Угол поворота головы по горизонтали
     * @param headPitch       Угол поворота головы по вертикали
     * @param scale           Масштабный коэффициент рендеринга
     */
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
                head.render(scale * 1);
                break;
            case 1:
                body.render(scale * 1);
                leftArm.render(scale * 1);
                rightArm.render(scale * 1);
                break;
            case 2:
                leftLeg.render(scale * 1);
                rightLeg.render(scale * 1);
                break;
            case 3:
                leftFoot.render(scale * 1);
                rightFoot.render(scale * 1);
                break;
        }

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glPopMatrix();
    }
}