package com.nymoo.afp.common.handler;

import com.nymoo.afp.AtomFusionProtocol;
import com.nymoo.afp.ModDataSyncManager;
import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.Tags;
import com.nymoo.afp.common.entity.EntityExoskeleton;
import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.item.ItemFusionCore;
import com.nymoo.afp.common.keybind.KeybindingExitPowerArmor;
import com.nymoo.afp.common.util.UtilEntityExoskeleton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.util.EnumSet;

import static com.nymoo.afp.common.util.UtilEntityExoskeleton.hasBooleanTag;

/**
 * Обработчик клиентских тиковых событий для управления взаимодействиями с экзоскелетом и силовой бронёй.
 * Отслеживает ввод с мыши и клавиатуры, управляет прогрессом удержания, воспроизводит звуки и синхронизирует данные с сервером.
 */
@Mod.EventBusSubscriber
public class HandlerClientTickEvent {
    /**
     * Время удержания для установки/извлечения ядра синтеза в секундах
     */
    public static final float FUSION_HOLD_TIME = 1.45F;
    /**
     * Время удержания для входа/выхода из силовой брони в секундах
     */
    public static final float ARMOR_HOLD_TIME = 6.0F;
    /**
     * Задержка перед началом затемнения экрана в секундах
     */
    public static final float FADE_DELAY = -1.0F;
    /**
     * Длительность плавного затемнения экрана в секундах
     */
    public static final float FADE_DURATION_IN = 0.5F;
    /**
     * Длительность удержания чёрного экрана в секундах
     */
    public static final float FADE_HOLD = 0.5F;
    /**
     * Длительность плавного осветления экрана в секундах
     */
    public static final float FADE_DURATION_OUT = 0.5F;
    /**
     * Время перезарядки между операциями с ядром синтеза в секундах
     */
    public static final float FUSION_COOLDOWN = 1.0F;
    /**
     * Набор слотов экипировки брони для оптимизации проверок
     */
    private static final EnumSet<EntityEquipmentSlot> ARMOR_SLOTS = EnumSet.of(
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET
    );
    /**
     * Уровень громкости звуков работы с ядром синтеза
     */
    public static float FUSION_VOLUME = 1.0F;
    /**
     * Уровень громкости звуков работы с бронёй
     */
    public static float ARMOR_VOLUME = 1.0F;
    /**
     * Время начала текущего удержания
     */
    public static long startHoldTime = 0L;
    /**
     * Максимальное время для завершения текущего удержания
     */
    public static float currentMaxHoldTime = 0F;
    /**
     * Текущий режим действия: -1 - нет, 0 - установка ядра, 1 - извлечение ядра, 2 - вход в броню, 3 - выход из брони
     */
    public static int currentMode = -1;
    /**
     * ID целевой сущности для взаимодействия
     */
    public static int currentEntityId = -1;
    /**
     * Целевой слот экипировки
     */
    public static EntityEquipmentSlot currentSlot = null;
    /**
     * Текущий воспроизводимый звук процесса
     */
    public static LoadingSound currentSound = null;
    /**
     * Время начала эффекта затемнения экрана
     */
    public static long fadeStartTime = 0L;
    /**
     * Время последней операции с ядром синтеза
     */
    private static long lastFusionActionTime = 0L;
    /**
     * Координаты X для позиционирования звука
     */
    private static double soundX = 0D;
    /**
     * Координаты Y для позиционирования звука
     */
    private static double soundY = 0D;
    /**
     * Координаты Z для позиционирования звука
     */
    private static double soundZ = 0D;

    /**
     * Обрабатывает клиентские тиковые события в конце каждого тика.
     * Управляет процессами удержания и взаимодействиями с экзоскелетом.
     *
     * @param event Событие тика клиента
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.player == null || minecraft.world == null) {
            resetHoldAndSound(minecraft, false);
            return;
        }

        EntityPlayer player = minecraft.player;
        World world = minecraft.world;
        boolean isHoldingRightClick = Mouse.isButtonDown(1);

        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        RayTraceResult rayTrace = minecraft.objectMouseOver;

        if (startHoldTime != 0L) {
            long currentTime = System.currentTimeMillis();
            float elapsedTime = (currentTime - startHoldTime) / 1000.0F;

            if (!isHoldValid(player, world, isHoldingRightClick, minecraft, heldItem, rayTrace)) {
                resetHoldAndSound(minecraft, false);
                return;
            }

            if (elapsedTime >= currentMaxHoldTime) {
                processHoldCompletion(player);
                resetHoldAndSound(minecraft, true);
            }
        } else {
            tryStartHold(player, world, isHoldingRightClick, minecraft, heldItem, rayTrace);
        }
    }

    /**
     * Проверяет валидность текущего действия удержания.
     *
     * @param player              Игрок, выполняющий действие
     * @param world               Мир, в котором происходит действие
     * @param isHoldingRightClick Флаг удержания правой кнопки мыши
     * @param minecraft           Экземпляр клиента Minecraft
     * @param heldItem            Предмет в основной руке игрока
     * @param rayTrace            Результат трассировки луча взгляда
     * @return true если действие остаётся валидным, false в противном случае
     */
    private static boolean isHoldValid(EntityPlayer player, World world, boolean isHoldingRightClick, Minecraft minecraft, ItemStack heldItem, RayTraceResult rayTrace) {
        if (currentMode == 3) {
            return KeybindingExitPowerArmor.keys.isKeyDown()
                    && !player.isSneaking()
                    && isWearingPowerArmor(player)
                    && !player.isRiding()
                    && UtilEntityExoskeleton.isSpaceBehindPlayerClear(world, player, player.getHorizontalFacing())
                    && !isPlayerMoving(minecraft)
                    && !minecraft.gameSettings.keyBindJump.isKeyDown();
        }

        if (!isHoldingRightClick) {
            return false;
        }

        if (rayTrace == null || rayTrace.typeOfHit != RayTraceResult.Type.ENTITY) {
            return false;
        }

        Entity entity = rayTrace.entityHit;
        if (entity.getEntityId() != currentEntityId) {
            return false;
        }

        if (!validateTargetPositionAndYaw(player, entity)) {
            return false;
        }

        ItemStack chestStack = getChestStack(entity);
        if (chestStack.isEmpty()) {
            return false;
        }

        float hitY = (float) (rayTrace.hitVec.y - entity.posY);
        EntityEquipmentSlot clickedSlot = getClickedSlot(hitY);
        if (clickedSlot != EntityEquipmentSlot.CHEST || clickedSlot != currentSlot) {
            return false;
        }

        if (currentMode == 0 || currentMode == 1) {
            if (!player.isSneaking() || !hasBooleanTag(chestStack, "soldered")) {
                return false;
            }
            NBTTagCompound tagCompound = chestStack.getTagCompound();
            boolean hasEnergy = tagCompound != null && tagCompound.hasKey("fusion_depletion");

            if (currentMode == 0) {
                return !heldItem.isEmpty() && heldItem.getItem() == ItemFusionCore.itemFusionCore && !hasEnergy;
            } else {
                return heldItem.isEmpty() && hasEnergy;
            }
        } else if (currentMode == 2) {
            if (!(entity instanceof EntityExoskeleton.Exoskeleton) || player.isSneaking() || !heldItem.isEmpty()) {
                return false;
            }
            return hasBooleanTag(chestStack, "soldered") || isAllExo((EntityExoskeleton.Exoskeleton) entity);
        }

        return false;
    }

    /**
     * Обрабатывает завершение успешного удержания.
     * Отправляет соответствующие сетевые сообщения и запускает визуальные эффекты.
     *
     * @param player Игрок, завершивший действие
     */
    private static void processHoldCompletion(EntityPlayer player) {
        if (currentMode == 0 || currentMode == 1 || currentMode == 2) {
            ModDataSyncManager.INTERACT_NETWORK.sendToServer(new ModDataSyncManager.InteractMessage(currentMode, currentEntityId, currentSlot));
        } else if (currentMode == 3) {
            AtomFusionProtocol.PACKET_HANDLER.sendToServer(new KeybindingExitPowerArmor.KeyBindingPressedMessage());
        }

        if (currentMode == 2 || currentMode == 3) {
            fadeStartTime = System.currentTimeMillis() + (long) (FADE_DELAY * 1000);
        }

        if (currentMode == 0 || currentMode == 1) {
            lastFusionActionTime = System.currentTimeMillis();
        }
    }

    /**
     * Пытается начать новое действие удержания на основе текущего ввода игрока.
     *
     * @param player              Игрок, инициирующий действие
     * @param world               Мир, в котором происходит действие
     * @param isHoldingRightClick Флаг удержания правой кнопки мыши
     * @param minecraft           Экземпляр клиента Minecraft
     * @param heldItem            Предмет в основной руке игрока
     * @param rayTrace            Результат трассировки луча взгляда
     */
    private static void tryStartHold(EntityPlayer player, World world, boolean isHoldingRightClick, Minecraft minecraft, ItemStack heldItem, RayTraceResult rayTrace) {
        if (KeybindingExitPowerArmor.keys.isKeyDown()
                && !player.isSneaking()
                && isWearingPowerArmor(player)
                && !player.isRiding()
                && UtilEntityExoskeleton.isSpaceBehindPlayerClear(world, player, player.getHorizontalFacing())
                && !isPlayerMoving(minecraft)
                && !minecraft.gameSettings.keyBindJump.isKeyDown()) {
            startHold(3, ARMOR_HOLD_TIME, -1, null, player.posX, player.posY, player.posZ, minecraft);
            return;
        }

        if (!isHoldingRightClick) {
            return;
        }

        if (rayTrace == null || rayTrace.typeOfHit != RayTraceResult.Type.ENTITY) {
            return;
        }

        Entity entity = rayTrace.entityHit;
        boolean isExoskeleton = entity instanceof EntityExoskeleton.Exoskeleton;
        boolean isOtherPlayer = entity instanceof EntityPlayer && entity != player;

        if (!(isExoskeleton || isOtherPlayer)) {
            return;
        }

        if (!validateTargetPositionAndYaw(player, entity)) {
            return;
        }

        ItemStack chestStack = getChestStack(entity);
        if (chestStack.isEmpty() || (isOtherPlayer && !(chestStack.getItem() instanceof IPowerArmor))) {
            return;
        }

        float hitY = (float) (rayTrace.hitVec.y - entity.posY);
        EntityEquipmentSlot clickedSlot = getClickedSlot(hitY);
        if (clickedSlot != EntityEquipmentSlot.CHEST) {
            return;
        }

        if (player.isSneaking()) {
            if (hasBooleanTag(chestStack, "soldered")) {
                NBTTagCompound tagCompound = chestStack.getTagCompound();
                boolean hasEnergy = tagCompound != null && tagCompound.hasKey("fusion_depletion");

                if (!heldItem.isEmpty() && heldItem.getItem() == ItemFusionCore.itemFusionCore && !hasEnergy) {
                    if ((System.currentTimeMillis() - lastFusionActionTime) / 1000.0F < FUSION_COOLDOWN) {
                        return;
                    }
                    startHold(0, FUSION_HOLD_TIME, entity.getEntityId(), clickedSlot, entity.posX, entity.posY, entity.posZ, minecraft);
                } else if (heldItem.isEmpty() && hasEnergy) {
                    if ((System.currentTimeMillis() - lastFusionActionTime) / 1000.0F < FUSION_COOLDOWN) {
                        return;
                    }
                    startHold(1, FUSION_HOLD_TIME, entity.getEntityId(), clickedSlot, entity.posX, entity.posY, entity.posZ, minecraft);
                }
            }
        } else if (isExoskeleton && heldItem.isEmpty() && (hasBooleanTag(chestStack, "soldered") || isAllExo((EntityExoskeleton.Exoskeleton) entity)) && isPlayerArmorEmpty(player)) {
            startHold(2, ARMOR_HOLD_TIME, entity.getEntityId(), clickedSlot, entity.posX, entity.posY, entity.posZ, minecraft);
        }
    }

    /**
     * Начинает новое действие удержания с указанными параметрами.
     *
     * @param mode      Режим действия
     * @param maxTime   Максимальное время удержания в секундах
     * @param entityId  ID целевой сущности
     * @param slot      Целевой слот экипировки
     * @param x         Координата X для звука
     * @param y         Координата Y для звука
     * @param z         Координата Z для звука
     * @param minecraft Экземпляр клиента Minecraft
     */
    private static void startHold(int mode, float maxTime, int entityId, EntityEquipmentSlot slot, double x, double y, double z, Minecraft minecraft) {
        startHoldTime = System.currentTimeMillis();
        currentMode = mode;
        currentMaxHoldTime = maxTime;
        currentEntityId = entityId;
        currentSlot = slot;
        soundX = x;
        soundY = y;
        soundZ = z;

        String soundName = (mode == 0 || mode == 1) ? "fusion_core_in_out" : "power_armor_in_out";
        SoundEvent soundEvent = ModElementRegistry.getSound(new ResourceLocation(Tags.MOD_ID, soundName));
        float volume = (mode == 0 || mode == 1) ? FUSION_VOLUME : ARMOR_VOLUME;

        if (currentSound == null || !minecraft.getSoundHandler().isSoundPlaying(currentSound)) {
            currentSound = new LoadingSound(soundEvent, (float) x, (float) y, (float) z, volume);
            minecraft.getSoundHandler().playSound(currentSound);
        }

        ModDataSyncManager.INTERACT_NETWORK.sendToServer(new ModDataSyncManager.StartSoundMessage(mode, x, y, z));
    }

    /**
     * Определяет слот экипировки на основе высоты попадания.
     *
     * @param hitY Относительная координата Y попадания
     * @return Определённый слот экипировки
     */
    private static EntityEquipmentSlot getClickedSlot(float hitY) {
        EntityEquipmentSlot clickedSlot = EntityExoskeleton.Exoskeleton.SLOT_ORDER[3];
        for (int i = 0; i < EntityExoskeleton.Exoskeleton.SLOT_HEIGHT_THRESHOLDS.length; i++) {
            if (hitY > EntityExoskeleton.Exoskeleton.SLOT_HEIGHT_THRESHOLDS[i]) {
                clickedSlot = EntityExoskeleton.Exoskeleton.SLOT_ORDER[i];
                break;
            }
        }
        return clickedSlot;
    }

    /**
     * Проверяет, состоит ли весь комплект экзоскелета из компонентов типа 'exo'.
     *
     * @param exoskeleton Экземпляр экзоскелета для проверки
     * @return true если все компоненты типа 'exo', false в противном случае
     */
    private static boolean isAllExo(EntityExoskeleton.Exoskeleton exoskeleton) {
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = exoskeleton.getItemStackFromSlot(slot);
            if (stack.isEmpty() || !"exo".equals(((IPowerArmor) stack.getItem()).getPowerArmorType())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверяет, экипирован ли игрок в силовую броню.
     *
     * @param player Игрок для проверки
     * @return true если игрок носит силовую броню, false в противном случае
     */
    private static boolean isWearingPowerArmor(EntityPlayer player) {
        ItemStack chestStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        return !chestStack.isEmpty() && chestStack.getItem() instanceof IPowerArmor;
    }

    /**
     * Сбрасывает состояние удержания и управляет воспроизведением звуков.
     *
     * @param minecraft    Экземпляр клиента Minecraft
     * @param allowFadeOut Разрешить плавное затухание звука при успешном завершении
     */
    private static void resetHoldAndSound(Minecraft minecraft, boolean allowFadeOut) {
        if (startHoldTime != 0L) {
            if (currentSound != null && minecraft.getSoundHandler().isSoundPlaying(currentSound)) {
                if (!allowFadeOut) {
                    minecraft.getSoundHandler().stopSound(currentSound);
                }
            }
            if (!allowFadeOut && currentMode != -1) {
                ModDataSyncManager.INTERACT_NETWORK.sendToServer(new ModDataSyncManager.StopSoundMessage(currentMode, soundX, soundY, soundZ));
            }
            currentSound = null;
            startHoldTime = 0L;
            currentMode = -1;
            currentMaxHoldTime = 0F;
            currentEntityId = -1;
            currentSlot = null;
            soundX = 0D;
            soundY = 0D;
            soundZ = 0D;
        }
    }

    /**
     * Проверяет, пусты ли все слоты брони игрока.
     *
     * @param player Игрок для проверки
     * @return true если все слоты брони пусты, false в противном случае
     */
    private static boolean isPlayerArmorEmpty(EntityPlayer player) {
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            if (!player.getItemStackFromSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверяет, движется ли игрок с помощью клавиш управления.
     *
     * @param minecraft Экземпляр клиента Minecraft
     * @return true если игрок движется, false в противном случае
     */
    private static boolean isPlayerMoving(Minecraft minecraft) {
        KeyBinding forwardKey = minecraft.gameSettings.keyBindForward;
        KeyBinding backKey = minecraft.gameSettings.keyBindBack;
        KeyBinding leftKey = minecraft.gameSettings.keyBindLeft;
        KeyBinding rightKey = minecraft.gameSettings.keyBindRight;
        return forwardKey.isKeyDown() || backKey.isKeyDown() || leftKey.isKeyDown() || rightKey.isKeyDown();
    }

    /**
     * Получает предмет из слота нагрудника целевой сущности.
     *
     * @param entity Сущность для проверки
     * @return Предмет из слота нагрудника или пустой стек
     */
    private static ItemStack getChestStack(Entity entity) {
        if (entity instanceof EntityExoskeleton.Exoskeleton) {
            return ((EntityExoskeleton.Exoskeleton) entity).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        } else if (entity instanceof EntityPlayer) {
            return ((EntityPlayer) entity).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Проверяет корректность позиции и ориентации игрока относительно цели.
     *
     * @param player Игрок для проверки
     * @param entity Целевая сущность
     * @return true если позиция и ориентация валидны, false в противном случае
     */
    private static boolean validateTargetPositionAndYaw(EntityPlayer player, Entity entity) {
        double distanceSquared = player.getDistanceSq(entity);
        if (distanceSquared > 1.0D) {
            return false;
        }

        float yawDifference = MathHelper.wrapDegrees(player.rotationYaw - entity.rotationYaw);
        return yawDifference >= -55.0F && yawDifference <= 55.0F;
    }

    /**
     * Позиционированный звук для процессов удержания с настраиваемой громкостью.
     */
    public static class LoadingSound extends net.minecraft.client.audio.PositionedSound {
        /**
         * Создает новый позиционированный звук процесса.
         *
         * @param soundEvent Звуковое событие для воспроизведения
         * @param x          Координата X воспроизведения
         * @param y          Координата Y воспроизведения
         * @param z          Координата Z воспроизведения
         * @param volume     Уровень громкости звука
         */
        public LoadingSound(SoundEvent soundEvent, float x, float y, float z, float volume) {
            super(soundEvent, SoundCategory.PLAYERS);
            this.repeat = false;
            this.volume = volume;
            this.pitch = 1.0F;
            this.xPosF = x;
            this.yPosF = y;
            this.zPosF = z;
        }
    }
}