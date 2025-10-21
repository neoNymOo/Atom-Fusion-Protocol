package com.nymoo.afp.common.handler;

import com.nymoo.afp.AtomFusionProtocol;
import com.nymoo.afp.ModDataSyncManager;
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
 * Обработчик клиентских тиковых событий для обнаружения и обработки действий удержания, связанных с взаимодействием с экзоскелетом.
 * Мониторит ввод с мыши и клавиатуры, управляет временем прогресса, воспроизводит/останавливает звуки и отправляет сетевые сообщения через ModDataSyncManager.
 * Отображает прогресс через интеграцию с HandlerRenderGameOverlayEvent.
 * Расширен для поддержки установки/удаления ядер fusion на нагрудниках других игроков.
 */
@Mod.EventBusSubscriber
public class HandlerClientTickEvent {
    // Продолжительность удержания для установки/удаления ядер fusion (в секундах)
    public static final float FUSION_HOLD_TIME = 1.45F;
    // Продолжительность удержания для входа/выхода из брони (в секундах)
    public static final float ARMOR_HOLD_TIME = 6.0F;
    // Времена эффекта затухания (в секундах)
    public static final float FADE_DELAY = -1.0F; // Задержка перед началом затухания
    public static final float FADE_DURATION_IN = 0.5F; // Затухание в черный
    public static final float FADE_HOLD = 0.5F; // Удержание черного экрана
    public static final float FADE_DURATION_OUT = 0.5F; // Выход из черного

    // Задержка между установкой и снятием fusion_core (в секундах)
    public static final float FUSION_COOLDOWN = 1.0F;

    // Звуковые события для различных процессов взаимодействия
    private static final SoundEvent FUSION_SOUND = new SoundEvent(new ResourceLocation("afp", "fusion_core_in_out"));
    private static final SoundEvent ARMOR_SOUND = new SoundEvent(new ResourceLocation("afp", "power_armor_in_out"));

    // Регулировка громкости звуков (от 0.0F до 1.0F или выше, в зависимости от нужного уровня)
    public static float FUSION_VOLUME = 1.0F; // Громкость для звуков fusion
    public static float ARMOR_VOLUME = 1.0F; // Громкость для звуков брони

    // Набор слотов брони для эффективной итерации
    private static final EnumSet<EntityEquipmentSlot> ARMOR_SLOTS = EnumSet.of(
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET
    );

    // Время начала удержания
    public static long startHoldTime = 0L;
    // Максимальное время удержания для текущего действия
    public static float currentMaxHoldTime = 0F;
    // Текущий режим действия: -1 - нет, 0 - установка ядер, 1 - удаление ядер, 2 - вход, 3 - выход
    public static int currentMode = -1;
    // ID целевой сущности (экзоскелет или другой игрок)
    public static int currentEntityId = -1;
    // Целевой слот экипировки
    public static EntityEquipmentSlot currentSlot = null;
    // Текущий воспроизводимый звук загрузки
    public static LoadingSound currentSound = null;
    // Время начала эффекта затухания для входа/выхода из силовой брони
    public static long fadeStartTime = 0L;
    // Время последней успешной операции с fusion_core
    private static long lastFusionActionTime = 0L;
    // Позиция для звука (хранится для отправки сообщений на сервер)
    private static double soundX = 0D;
    private static double soundY = 0D;
    private static double soundZ = 0D;

    /**
     * Обработчик клиентских тиковых событий.
     * Обрабатывает действия удержания в конце каждого тика.
     *
     * @param event Событие тика.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.player == null || mc.world == null) {
            resetHoldAndSound(mc, false);
            return;
        }

        EntityPlayer player = mc.player;
        World world = mc.world;
        boolean isHoldingRightClick = Mouse.isButtonDown(1);

        // Кэшируем часто используемые значения для оптимизации
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        RayTraceResult rayTrace = mc.objectMouseOver;

        if (startHoldTime != 0L) {
            long now = System.currentTimeMillis();
            float elapsed = (now - startHoldTime) / 1000.0F;

            if (!isHoldValid(player, world, isHoldingRightClick, mc, heldItem, rayTrace)) {
                resetHoldAndSound(mc, false);
                return;
            }

            if (elapsed >= currentMaxHoldTime) {
                processHoldCompletion(player);
                resetHoldAndSound(mc, true);
            }
        } else {
            tryStartHold(player, world, isHoldingRightClick, mc, heldItem, rayTrace);
        }
    }

    /**
     * Проверяет, остается ли текущее действие удержания валидным на основе ввода и цели.
     * Поддерживает как сущности экзоскелета, так и других игроков для действий с ядрами fusion.
     * Оптимизировано: вынесена общая валидация в отдельный метод.
     *
     * @param player Игрок.
     * @param world Мир.
     * @param isHoldingRightClick Удерживается ли правая кнопка мыши.
     * @param mc Экземпляр Minecraft.
     * @param heldItem Предмет в главной руке (кэширован).
     * @param rayTrace Результат трассировки луча (кэширован).
     * @return True, если удержание все еще валидно.
     */
    private static boolean isHoldValid(EntityPlayer player, World world, boolean isHoldingRightClick, Minecraft mc, ItemStack heldItem, RayTraceResult rayTrace) {
        if (currentMode == 3) { // Режим выхода
            return KeybindingExitPowerArmor.keys.isKeyDown()
                    && !player.isSneaking()
                    && isWearingPowerArmor(player)
                    && !player.isRiding()
                    && UtilEntityExoskeleton.isSpaceBehindPlayerClear(world, player, player.getHorizontalFacing())
                    && !isPlayerMoving(mc)
                    && !mc.gameSettings.keyBindJump.isKeyDown();
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

        // Валидация позиции, поворота и расстояния
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

        if (currentMode == 0 || currentMode == 1) { // Режимы ядер fusion
            if (!player.isSneaking() || !hasBooleanTag(chestStack, "soldered")) {
                return false;
            }
            NBTTagCompound tag = chestStack.getTagCompound();
            boolean hasEnergy = tag != null && tag.hasKey("fusion_depletion");

            if (currentMode == 0) {
                return !heldItem.isEmpty() && heldItem.getItem() == ItemFusionCore.itemFusionCore && !hasEnergy;
            } else {
                return heldItem.isEmpty() && hasEnergy;
            }
        } else if (currentMode == 2) { // Режим входа (только для экзоскелета)
            if (!(entity instanceof EntityExoskeleton.Exoskeleton) || player.isSneaking() || !heldItem.isEmpty()) {
                return false;
            }
            return hasBooleanTag(chestStack, "soldered") || isAllExo((EntityExoskeleton.Exoskeleton) entity);
        }

        return false;
    }

    /**
     * Обрабатывает завершенное действие удержания, отправляя сетевое сообщение.
     * Интегрируется с ModDataSyncManager для сообщений и KeybindingExitPowerArmor для выхода.
     *
     * @param player Игрок.
     */
    private static void processHoldCompletion(EntityPlayer player) {
        if (currentMode == 0 || currentMode == 1 || currentMode == 2) {
            ModDataSyncManager.INTERACT_NETWORK.sendToServer(new ModDataSyncManager.InteractMessage(currentMode, currentEntityId, currentSlot));
        } else if (currentMode == 3) {
            AtomFusionProtocol.PACKET_HANDLER.sendToServer(new KeybindingExitPowerArmor.KeyBindingPressedMessage());
        }

        // Запуск эффекта затухания для входа (2) или выхода (3)
        if (currentMode == 2 || currentMode == 3) {
            fadeStartTime = System.currentTimeMillis() + (long) (FADE_DELAY * 1000);
        }

        // Установка времени последней операции для fusion_core
        if (currentMode == 0 || currentMode == 1) {
            lastFusionActionTime = System.currentTimeMillis();
        }
    }

    /**
     * Пытается начать новое действие удержания на основе ввода.
     * Поддерживает как сущности экзоскелета, так и других игроков для действий с ядрами fusion.
     * Оптимизировано: вынесена общая валидация в отдельный метод.
     *
     * @param player Игрок.
     * @param world Мир.
     * @param isHoldingRightClick Удерживается ли правая кнопка мыши.
     * @param mc Экземпляр Minecraft.
     * @param heldItem Предмет в главной руке (кэширован).
     * @param rayTrace Результат трассировки луча (кэширован).
     */
    private static void tryStartHold(EntityPlayer player, World world, boolean isHoldingRightClick, Minecraft mc, ItemStack heldItem, RayTraceResult rayTrace) {
        // Сначала проверка на действие выхода
        if (KeybindingExitPowerArmor.keys.isKeyDown()
                && !player.isSneaking()
                && isWearingPowerArmor(player)
                && !player.isRiding()
                && UtilEntityExoskeleton.isSpaceBehindPlayerClear(world, player, player.getHorizontalFacing())
                && !isPlayerMoving(mc)
                && !mc.gameSettings.keyBindJump.isKeyDown()) {
            startHold(3, ARMOR_HOLD_TIME, -1, null, player.posX, player.posY, player.posZ, mc);
            return;
        }

        if (!isHoldingRightClick) {
            return;
        }

        if (rayTrace == null || rayTrace.typeOfHit != RayTraceResult.Type.ENTITY) {
            return;
        }

        Entity entity = rayTrace.entityHit;
        boolean isExo = entity instanceof EntityExoskeleton.Exoskeleton;
        boolean isPlayerTarget = entity instanceof EntityPlayer && entity != player;

        if (!(isExo || isPlayerTarget)) {
            return;
        }

        // Валидация позиции, поворота и расстояния
        if (!validateTargetPositionAndYaw(player, entity)) {
            return;
        }

        ItemStack chestStack = getChestStack(entity);
        if (chestStack.isEmpty() || (isPlayerTarget && !(chestStack.getItem() instanceof IPowerArmor))) {
            return;
        }

        float hitY = (float) (rayTrace.hitVec.y - entity.posY);
        EntityEquipmentSlot clickedSlot = getClickedSlot(hitY);
        if (clickedSlot != EntityEquipmentSlot.CHEST) {
            return;
        }

        if (player.isSneaking()) {
            if (hasBooleanTag(chestStack, "soldered")) {
                NBTTagCompound tag = chestStack.getTagCompound();
                boolean hasEnergy = tag != null && tag.hasKey("fusion_depletion");

                if (!heldItem.isEmpty() && heldItem.getItem() == ItemFusionCore.itemFusionCore && !hasEnergy) {
                    if ((System.currentTimeMillis() - lastFusionActionTime) / 1000.0F < FUSION_COOLDOWN) {
                        return;
                    }
                    startHold(0, FUSION_HOLD_TIME, entity.getEntityId(), clickedSlot, entity.posX, entity.posY, entity.posZ, mc);
                } else if (heldItem.isEmpty() && hasEnergy) {
                    if ((System.currentTimeMillis() - lastFusionActionTime) / 1000.0F < FUSION_COOLDOWN) {
                        return;
                    }
                    startHold(1, FUSION_HOLD_TIME, entity.getEntityId(), clickedSlot, entity.posX, entity.posY, entity.posZ, mc);
                }
            }
        } else if (isExo && heldItem.isEmpty() && (hasBooleanTag(chestStack, "soldered") || isAllExo((EntityExoskeleton.Exoskeleton) entity)) && isPlayerArmorEmpty(player)) {
            startHold(2, ARMOR_HOLD_TIME, entity.getEntityId(), clickedSlot, entity.posX, entity.posY, entity.posZ, mc);
        }
    }

    /**
     * Запускает действие удержания, устанавливая переменные и воспроизводя звук.
     * Звук выбирается в зависимости от режима: fusion_core_in_out для 0/1, power_armor_in_out для 2/3.
     * Отправляет сообщение на сервер для воспроизведения звука для других игроков.
     *
     * @param mode Режим действия.
     * @param maxTime Максимальное время удержания.
     * @param entityId ID сущности.
     * @param slot Слот экипировки.
     * @param x Позиция X для звука.
     * @param y Позиция Y для звука.
     * @param z Позиция Z для звука.
     * @param mc Экземпляр Minecraft.
     */
    private static void startHold(int mode, float maxTime, int entityId, EntityEquipmentSlot slot, double x, double y, double z, Minecraft mc) {
        startHoldTime = System.currentTimeMillis();
        currentMode = mode;
        currentMaxHoldTime = maxTime;
        currentEntityId = entityId;
        currentSlot = slot;
        soundX = x;
        soundY = y;
        soundZ = z;

        SoundEvent soundToPlay;
        float volume;
        if (mode == 0 || mode == 1) {
            soundToPlay = FUSION_SOUND;
            volume = FUSION_VOLUME;
        } else {
            soundToPlay = ARMOR_SOUND;
            volume = ARMOR_VOLUME;
        }

        if (currentSound == null || !mc.getSoundHandler().isSoundPlaying(currentSound)) {
            currentSound = new LoadingSound(soundToPlay, (float) x, (float) y, (float) z, volume);
            mc.getSoundHandler().playSound(currentSound);
        }

        // Отправка сообщения на сервер для воспроизведения звука для других игроков
        ModDataSyncManager.INTERACT_NETWORK.sendToServer(new ModDataSyncManager.StartSoundMessage(mode, x, y, z));
    }

    /**
     * Определяет кликнутый слот на основе позиции Y попадания.
     * Использует пороги из EntityExoskeleton.
     *
     * @param hitY Относительная позиция Y попадания.
     * @return Слот экипировки.
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
     * Проверяет, все ли слоты брони на экзоскелете типа 'exo'.
     *
     * @param exo Экзоскелет.
     * @return True, если все 'exo'.
     */
    private static boolean isAllExo(EntityExoskeleton.Exoskeleton exo) {
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = exo.getItemStackFromSlot(slot);
            if (stack.isEmpty() || !"exo".equals(((IPowerArmor) stack.getItem()).getPowerArmorType())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверяет, носит ли игрок силовую броню.
     *
     * @param player Игрок.
     * @return True, если носит.
     */
    private static boolean isWearingPowerArmor(EntityPlayer player) {
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        return !chest.isEmpty() && chest.getItem() instanceof IPowerArmor;
    }

    /**
     * Сбрасывает состояние удержания и останавливает звук, если нужно.
     * Если allowFadeOut true (завершение взаимодействия), звук не прерывается, позволяя затуханию.
     * В других случаях (прерывание), звук останавливается.
     * Отправляет сообщение на сервер для остановки звука для других игроков при прерывании.
     * Вызывается при прерывании или завершении.
     *
     * @param mc Экземпляр Minecraft.
     * @param allowFadeOut Разрешить затухание звука (true при успешном завершении).
     */
    private static void resetHoldAndSound(Minecraft mc, boolean allowFadeOut) {
        if (startHoldTime != 0L) {
            if (currentSound != null && mc.getSoundHandler().isSoundPlaying(currentSound)) {
                if (!allowFadeOut) {
                    mc.getSoundHandler().stopSound(currentSound);
                }
            }
            if (!allowFadeOut && currentMode != -1) {
                // Отправка сообщения на сервер для остановки звука для других игроков
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
     * @param player Игрок.
     * @return True, если все пусты.
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
     * Проверяет, вводит ли игрок движение (WASD).
     *
     * @param mc Экземпляр Minecraft.
     * @return True, если движется.
     */
    private static boolean isPlayerMoving(Minecraft mc) {
        KeyBinding forward = mc.gameSettings.keyBindForward;
        KeyBinding back = mc.gameSettings.keyBindBack;
        KeyBinding left = mc.gameSettings.keyBindLeft;
        KeyBinding right = mc.gameSettings.keyBindRight;
        return forward.isKeyDown() || back.isKeyDown() || left.isKeyDown() || right.isKeyDown();
    }

    /**
     * Вспомогательный метод для получения нагрудника сущности.
     * Поддерживает экзоскелет и игроков.
     *
     * @param entity Сущность.
     * @return Стек нагрудника.
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
     * Валидирует позицию, поворот и расстояние до цели.
     * Выделено для избежания дублирования кода.
     *
     * @param player Игрок.
     * @param entity Цель.
     * @return True, если валидно.
     */
    private static boolean validateTargetPositionAndYaw(EntityPlayer player, Entity entity) {
        double distanceSq = player.getDistanceSq(entity);
        if (distanceSq > 1.0D) {
            return false;
        }

        float yawDiff = MathHelper.wrapDegrees(player.rotationYaw - entity.rotationYaw);
        return yawDiff >= -55.0F && yawDiff <= 55.0F;
    }

    /**
     * Пользовательский позиционированный звук для процесса загрузки.
     * Без повторения, полный объем.
     */
    public static class LoadingSound extends net.minecraft.client.audio.PositionedSound {
        public LoadingSound(SoundEvent soundIn, float x, float y, float z, float volume) {
            super(soundIn, SoundCategory.PLAYERS);
            this.repeat = false;
            this.volume = volume;
            this.pitch = 1.0F;
            this.xPosF = x;
            this.yPosF = y;
            this.zPosF = z;
        }
    }
}