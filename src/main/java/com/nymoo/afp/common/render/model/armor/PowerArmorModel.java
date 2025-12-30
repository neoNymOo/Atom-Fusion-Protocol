package com.nymoo.afp.common.render.model.armor;

import com.nymoo.afp.common.render.core.AdvancedModelLoader;
import com.nymoo.afp.common.render.core.IModelCustom;
import com.nymoo.afp.common.render.core.ModelRendererObj;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import java.util.HashMap;
import java.util.Map;
import com.nymoo.afp.common.item.ArmorExo;

/**
 * Модель силовой брони для рендеринга кастомных OBJ-моделей.
 * Поддерживает нормальный вариант (с опциональным реактивным ранцем) и поломанную версию.
 * Загружает модели, кэширует их и создаёт рендереры частей брони.
 */
public class PowerArmorModel extends AbstractArmorModel {
    // Кэш загруженных моделей без реактивного ранца
    private static final Map<String, IModelCustom> MODEL_CACHE = new HashMap<>();
    // Кэш загруженных моделей с реактивным ранцем (jetpack)
    private static final Map<String, IModelCustom> JET_MODEL_CACHE = new HashMap<>();
    // Кэш загруженных моделей для поломанной версии
    private static final Map<String, IModelCustom> BROKEN_MODEL_CACHE = new HashMap<>();
    // Тип брони, используется в путях к ресурсам
    private final String armorType;
    // Базовый тип брони (без _broken для поломанной версии)
    private final String baseArmorType;
    // Наличие варианта с реактивным ранцем (игнорируется для поломанной версии)
    private final boolean hasJetpackVariant;
    // Флаг поломанной версии брони
    private final boolean isBroken;
    /**
     * Создаёт модель силовой брони для указанного слота.
     *
     * @param type Тип слота брони (0=head,1=body,2=legs,3=feet)
     * @param armorType Строковый идентификатор типа брони для ресурсов
     * @param hasJetpackVariant true если доступен вариант с реактивным ранцем (только для нормальной версии)
     * @param isBroken true если это поломанная версия брони
     */
    public PowerArmorModel(int type, String armorType, boolean hasJetpackVariant, boolean isBroken) {
        super(type);
        this.armorType = armorType;
        this.baseArmorType = isBroken ? armorType.replace("_broken", "") : armorType;
        this.hasJetpackVariant = hasJetpackVariant;
        this.isBroken = isBroken;
        loadModels();
    }
    public String getBaseArmorType() {
        return baseArmorType;
    }
    public boolean isBroken() {
        return isBroken;
    }
    /**
     * Загружает модель из OBJ-файла и инициализирует части модели.
     * Использует соответствующие кэши для избежания повторной загрузки.
     * Для поломанной версии игнорирует hasJetpackVariant.
     */
    private void loadModels() {
        String suffix;
        Map<String, IModelCustom> cacheToUse;
        if (isBroken) {
            suffix = "_broken";
            cacheToUse = BROKEN_MODEL_CACHE;
        } else if (hasJetpackVariant) {
            suffix = "_jetpack";
            cacheToUse = JET_MODEL_CACHE;
        } else {
            suffix = "";
            cacheToUse = MODEL_CACHE;
        }
        // Ключ для кэша зависит от типа брони, слота и суффикса
        String key = armorType + "_" + type + suffix;
        // Загружаем модель при отсутствии записи в кэше
        if (!cacheToUse.containsKey(key)) {
            String modelPath = "models/armor/" + baseArmorType + "/" + baseArmorType + "_full" + suffix + ".obj";
            cacheToUse.put(key, AdvancedModelLoader.loadModel(new ResourceLocation("afp", modelPath)));
        }
        // Получаем модель из кэша
        IModelCustom model = cacheToUse.get(key);
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

    private EntityEquipmentSlot getSlotFromType(int type) {
        switch (type) {
            case 0:
                return EntityEquipmentSlot.HEAD;
            case 1:
                return EntityEquipmentSlot.CHEST;
            case 2:
                return EntityEquipmentSlot.LEGS;
            case 3:
                return EntityEquipmentSlot.FEET;
            default:
                return null;
        }
    }

    /**
     * Отрисовывает части брони в зависимости от слота и состояния сущности.
     * Применяет масштабирование для ребёнка и специфическое увеличение для игрока,
     * привязывает текстуру и рендерит только нужные части.
     *
     * @param entity Сущность, для которой рендерится броня
     * @param limbSwing Значение для анимации взмаха конечностей
     * @param limbSwingAmount Амплитуда взмаха конечностей
     * @param ageInTicks Время в тиках, используется в анимации
     * @param headYaw Поворот головы по горизонтали
     * @param headPitch Поворот головы по вертикали
     * @param scale Базовый коэффициент масштаба рендеринга
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

        // Привязка текстуры брони по типу (суффикс только для broken)
        String textureSuffix = isBroken ? "_broken" : "";
        ResourceLocation texture = new ResourceLocation("afp", "textures/models/armor/" + baseArmorType + "/" + baseArmorType + "_full" + textureSuffix + ".png");

        if (isBroken && entity instanceof EntityPlayer) {
            // Для broken рендерим дополнительно biped с нормальной текстурой
            ResourceLocation normalTexture = new ResourceLocation("afp", "textures/models/armor/exo/exo_full.png");
            Minecraft.getMinecraft().renderEngine.bindTexture(normalTexture);
            EntityEquipmentSlot slot = getSlotFromType(type);
            if (slot != null) {
                ModelBiped biped = ArmorExo.getBipedModel((EntityLivingBase) entity, slot);
                biped.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entity);
                biped.render(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
            }
        }

        // Привязываем основную текстуру (broken или normal)
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