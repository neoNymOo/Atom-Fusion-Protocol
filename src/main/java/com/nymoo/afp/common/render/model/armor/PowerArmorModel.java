package com.nymoo.afp.common.render.model.armor;

import com.nymoo.afp.common.render.core.AdvancedModelLoader;
import com.nymoo.afp.common.render.core.IModelCustom;
import com.nymoo.afp.common.render.core.ModelRendererObj;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

/**
 * Модель силовой брони для рендеринга кастомных OBJ-моделей.
 * Загружает модели, кэширует их и создаёт рендереры частей брони.
 */
public class PowerArmorModel extends AbstractArmorModel {
    // Кэш загруженных моделей без реактивного ранца
    private static final Map<String, IModelCustom> MODEL_CACHE = new HashMap<>();
    // Кэш загруженных моделей с реактивным ранцем (jetpack)
    private static final Map<String, IModelCustom> JET_MODEL_CACHE = new HashMap<>();

    // Тип брони, используется в путях к ресурсам
    private final String armorType;
    // Наличие варианта с реактивным ранцем
    private final boolean hasJetpackVariant;

    /**
     * Создаёт модель силовой брони для указанного слота.
     *
     * @param type              Тип слота брони (0=head,1=body,2=legs,3=feet)
     * @param armorType         Строковый идентификатор типа брони для ресурсов
     * @param hasJetpackVariant true если доступен вариант с реактивным ранцем
     */
    public PowerArmorModel(int type, String armorType, boolean hasJetpackVariant) {
        super(type);
        this.armorType = armorType;
        this.hasJetpackVariant = hasJetpackVariant;
        loadModels();
    }

    /**
     * Загружает модель из OBJ-файла и инициализирует части модели.
     * Использует кэши MODEL_CACHE и JET_MODEL_CACHE для избежания повторной загрузки.
     */
    private void loadModels() {
        // Ключ для кэша зависит от типа брони, слота и наличия jetpack
        String key = armorType + "_" + type + (hasJetpackVariant ? "_jet" : "");

        // Загружаем в соответствующий кэш при отсутствии записи
        if (hasJetpackVariant && !JET_MODEL_CACHE.containsKey(key)) {
            String modelPath = "models/armor/" + armorType + "/" + armorType + "_j_armor.obj";
            JET_MODEL_CACHE.put(key, AdvancedModelLoader.loadModel(new ResourceLocation("afp", modelPath)));
        } else if (!MODEL_CACHE.containsKey(key)) {
            String modelPath = "models/armor/" + armorType + "/" + armorType + "_armor.obj";
            MODEL_CACHE.put(key, AdvancedModelLoader.loadModel(new ResourceLocation("afp", modelPath)));
        }

        // Выбираем модель из нужного кэша
        IModelCustom model = hasJetpackVariant ? JET_MODEL_CACHE.get(key) : MODEL_CACHE.get(key);

        // Инициализация рендереров частей модели (голова, тело, руки, ноги, ботинки)
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
     * Отрисовывает части брони в зависимости от слота и состояния сущности.
     * Применяет масштабирование для ребёнка и специфическое увеличение для игрока,
     * привязывает текстуру и рендерит только нужные части.
     *
     * @param entity          Сущность, для которой рендерится броня
     * @param limbSwing       Значение для анимации взмаха конечностей
     * @param limbSwingAmount Амплитуда взмаха конечностей
     * @param ageInTicks      Время в тиках, используется в анимации
     * @param headYaw         Поворот головы по горизонтали
     * @param headPitch       Поворот головы по вертикали
     * @param scale           Базовый коэффициент масштаба рендеринга
     */
    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entity);

        GL11.glPushMatrix();
        GL11.glShadeModel(GL11.GL_SMOOTH);

        // Корректировка для детской модели
        if (this.isChild) {
            GL11.glScalef(0.75F, 0.75F, 0.75F);
            GL11.glTranslatef(0.0F, 16.0F * scale, 0.0F);
        }

        // Специальный масштаб только для игрока, чтобы броня выглядела правильно на игроке
        if (entity instanceof EntityPlayer) {
            float playerScale = 1.052F;
            GL11.glScalef(playerScale, playerScale, playerScale);
            GL11.glTranslatef(0.0F, -0.075F, 0.0F);
        }

        // Привязка текстуры брони по типу
        ResourceLocation texture = new ResourceLocation("afp", "textures/armor/" + armorType + "/" + armorType + ".png");
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        // Рендеринг частей в зависимости от слота брони
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
