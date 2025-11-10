package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Миксин для изменения поведения игрока при ношении силовой брони.
 * Корректирует скорость, взаимодействие с водой, воздух и габариты игрока.
 * Добавлены ссылки на AFPConfig для возможности конфигурирования поведения.
 */
@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends Entity {

    private MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    /**
     * Модифицирует возвращаемую AI-скорость игрока.
     * Если ядро синтеза разряжено, применяется коэффициент powerArmorDepletedSpeedMultiplier,
     * иначе применяется powerArmorSpeedMultiplier из конфигурации.
     *
     * @param cir Колбэк с исходным значением скорости и возможностью заменить его
     */
    @Inject(method = "getAIMoveSpeed", at = @At("RETURN"), cancellable = true)
    private void onGetAIMoveSpeed(CallbackInfoReturnable<Float> cir) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        // Применяем только когда в слоте груди силовая броня
        if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
            NBTTagCompound nbt = chestplateStack.getTagCompound();
            // Если NBT отсутствует или броня "пустая" (fusion_depletion >= лимит), замедляем до конфигурируемого множителя
            if (nbt == null || !nbt.hasKey("fusion_depletion") || nbt.getFloat("fusion_depletion") >= AFPConfig.maxDepletion) {
                float originalSpeed = cir.getReturnValue();
                float modifiedSpeed = originalSpeed * AFPConfig.powerArmorDepletedSpeedMultiplier;
                cir.setReturnValue(modifiedSpeed);
                return;
            }
            // Иначе применяем конфигурационный множитель скорости для заряжённой брони
            float originalSpeed = cir.getReturnValue();
            float modifiedSpeed = originalSpeed * AFPConfig.powerArmorSpeedMultiplier;
            cir.setReturnValue(modifiedSpeed);
        }
    }

    /**
     * Отключает толкание водой, если игрок в силовой броне.
     * Логика: проверяет грудную ячейку на IPowerArmor и возвращает false.
     *
     * @param cir Колбэк для установки результата (true/false)
     */
    @Inject(method = "isPushedByWater", at = @At("HEAD"), cancellable = true)
    public void onIsPushedByWater(CallbackInfoReturnable<Boolean> cir) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        // Если надета силовая броня, вода не толкает игрока
        if (!chestplate.isEmpty() && chestplate.getItem() instanceof IPowerArmor) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Корректирует движение игрока в воде после выполнения travel.
     * Если ядро разряжено, применяется powerArmorDepletedSpeedMultiplier,
     * иначе powerArmorSpeedMultiplier.
     *
     * @param strafe   Страиф-параметр движения (не используется напрямую)
     * @param vertical Вертикальная составляющая движения (не используется напрямую)
     * @param forward  Вперёд (не используется напрямую)
     * @param ci       Колбэк для внедрения после выполнения метода
     */
    @Inject(method = "travel", at = @At("RETURN"))
    private void onTravel(float strafe, float vertical, float forward, CallbackInfo ci) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        // Если броня не надета или не является силовой, ничего не делаем
        if (chestplate.isEmpty() || !(chestplate.getItem() instanceof IPowerArmor)) return;
        NBTTagCompound nbt = chestplate.getTagCompound();
        float multiplier;
        // Выбор множителя в зависимости от состояния fusion core
        if (nbt == null || !nbt.hasKey("fusion_depletion") || nbt.getFloat("fusion_depletion") >= AFPConfig.maxDepletion) {
            multiplier = AFPConfig.powerArmorDepletedSpeedMultiplier;
        } else {
            multiplier = AFPConfig.powerArmorSpeedMultiplier;
        }
        // Применяем уменьшение скорости по осям X и Z, если игрок в воде
        if (player.isInWater()) {
            player.motionX *= multiplier;
            player.motionZ *= multiplier;
        }
    }

    /**
     * Отслеживает время нахождения под водой и даёт дыхание при наличии силовой брони.
     * Использует конфигурационные параметры underwaterBreathTicks и underwaterBreathAir.
     *
     * @param ci Колбэк для внедрения в конец onUpdate
     */
    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void onOnUpdate(CallbackInfo ci) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        // Только если надета силовая броня
        if (chestplate.isEmpty() || !(chestplate.getItem() instanceof IPowerArmor)) return;
        NBTTagCompound nbt = chestplate.getTagCompound();
        // Инициализируем NBT, если его нет
        if (nbt == null) {
            nbt = new NBTTagCompound();
            chestplate.setTagCompound(nbt);
        }
        // Если игрок вышел из воды, удаляем счётчик и выходим
        if (!player.isInWater()) {
            if (nbt.hasKey("afp_underwater_ticks")) {
                nbt.removeTag("afp_underwater_ticks");
            }
            return;
        }
        // Инициализация счётчика при первом заходе в воду
        if (!nbt.hasKey("afp_underwater_ticks")) {
            nbt.setInteger("afp_underwater_ticks", 0);
        }
        int ticks = nbt.getInteger("afp_underwater_ticks");
        // Пока счётчик меньше лимита, даём воздух и инкрементируем
        if (ticks < AFPConfig.underwaterBreathTicks) {
            // Восстанавливаем запас воздуха в соответствии с конфигурацией
            player.setAir(AFPConfig.underwaterBreathAir);
            nbt.setInteger("afp_underwater_ticks", ticks + 1);
        }
    }

    /**
     * Обновляет размеры игрока в зависимости от состояния и ношения силовой брони.
     * Значения ширины и высоты для силовой брони берутся из конфигурации.
     *
     * @param ci Колбэк, позволяющий отменить оригинальное выполнение
     */
    @Inject(method = "updateSize", at = @At("HEAD"), cancellable = true)
    private void onUpdateSize(CallbackInfo ci) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        // Флаг ношения силовой брони
        boolean wearingPowerArmor = !chest.isEmpty() && chest.getItem() instanceof IPowerArmor;
        float f;
        float f1;
        // Рассчитываем размеры по состоянию игрока с учётом брони
        if (player.isElytraFlying()) {
            // При полёте с элитрой используем специальные размеры из конфигурации
            f = wearingPowerArmor ? AFPConfig.powerArmorWidth : 0.6F;
            f1 = wearingPowerArmor ? AFPConfig.powerArmorHeightElytra : 0.6F;
        } else if (player.isPlayerSleeping()) {
            // При сне размеры не зависят от брони
            f = 0.2F;
            f1 = 0.2F;
        } else if (player.isSneaking()) {
            // При приседании используем sneaking-значения
            f = wearingPowerArmor ? AFPConfig.powerArmorWidth : 0.6F;
            f1 = wearingPowerArmor ? AFPConfig.powerArmorHeightSneaking : 1.65F;
        } else {
            // В обычном состоянии используем стоячие размеры
            f = wearingPowerArmor ? AFPConfig.powerArmorWidth : 0.6F;
            f1 = wearingPowerArmor ? AFPConfig.powerArmorHeightStanding : 1.8F;
        }
        // Применяем новые размеры, если в мире нет коллизий для bounding box
        if (f != player.width || f1 != player.height) {
            AxisAlignedBB axisalignedbb = player.getEntityBoundingBox();
            axisalignedbb = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
                    axisalignedbb.minX + (double) f,
                    axisalignedbb.minY + (double) f1,
                    axisalignedbb.minZ + (double) f);
            if (!player.world.collidesWithAnyBlock(axisalignedbb)) {
                this.setSize(f, f1);
            }
        }
        ci.cancel();
    }
}