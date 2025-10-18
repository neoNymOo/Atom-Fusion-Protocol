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
 * Handles client-side tick events for detecting and processing hold actions related to exoskeleton interactions.
 * Monitors mouse and key inputs, manages progress timing, plays/stops sounds, and sends network messages via ModDataSyncManager.
 * Displays progress through integration with HandlerRenderGameOverlayEvent.
 * Extended to support fusion core installation/removal on other players' chestplates.
 */
@Mod.EventBusSubscriber
public class HandlerClientTickEvent {
    // Duration for holding to install/remove fusion core (in seconds)
    public static final float FUSION_HOLD_TIME = 1.8F;
    // Duration for holding to enter/exit armor (in seconds)
    public static final float ARMOR_HOLD_TIME = 2.5F;
    // Fade effect timings (in seconds)
    public static final float FADE_DELAY = -1.0F; // Delay before fade starts
    public static final float FADE_DURATION_IN = 0.5F; // Fade in to black
    public static final float FADE_HOLD = 0.5F; // Hold black screen
    public static final float FADE_DURATION_OUT = 0.5F; // Fade out from black

    // Sound event for interaction process
    private static final SoundEvent INTERACT_SOUND = new SoundEvent(new ResourceLocation("afp", "fusion_core_interact"));

    // Set of armor slots for efficient iteration
    private static final EnumSet<EntityEquipmentSlot> ARMOR_SLOTS = EnumSet.of(
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET
    );

    // Hold start time
    public static long startHoldTime = 0L;
    // Max hold time for current action
    public static float currentMaxHoldTime = 0F;
    // Current action mode: -1 none, 0 install fusion, 1 uninstall fusion, 2 enter, 3 exit
    public static int currentMode = -1;
    // Target entity ID (exoskeleton or other player)
    public static int currentEntityId = -1;
    // Target equipment slot
    public static EntityEquipmentSlot currentSlot = null;
    // Currently playing loading sound
    public static LoadingSound currentSound = null;
    // Fade effect start time for entering/exiting power armor
    public static long fadeStartTime = 0L;

    /**
     * Handles client tick events.
     * Processes hold actions at the end of each tick.
     *
     * @param event The tick event.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.player == null || mc.world == null) {
            resetHoldAndSound(mc);
            return;
        }

        EntityPlayer player = mc.player;
        World world = mc.world;
        boolean isHoldingRightClick = Mouse.isButtonDown(1);

        if (startHoldTime != 0L) {
            long now = System.currentTimeMillis();
            float elapsed = (now - startHoldTime) / 1000.0F;

            if (!isHoldValid(player, world, isHoldingRightClick, mc)) {
                resetHoldAndSound(mc);
                return;
            }

            if (elapsed >= currentMaxHoldTime) {
                processHoldCompletion(player);
                resetHoldAndSound(mc);
            }
        } else {
            tryStartHold(player, world, isHoldingRightClick, mc);
        }
    }

    /**
     * Validates if the current hold action remains valid based on input and target.
     * Supports both exoskeleton entities and other players for fusion core actions.
     *
     * @param player The player.
     * @param world The world.
     * @param isHoldingRightClick If right mouse button is held.
     * @param mc The Minecraft instance.
     * @return True if hold is still valid.
     */
    private static boolean isHoldValid(EntityPlayer player, World world, boolean isHoldingRightClick, Minecraft mc) {
        if (currentMode == 3) { // Exit mode
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

        RayTraceResult rayTrace = mc.objectMouseOver;
        if (rayTrace == null || rayTrace.typeOfHit != RayTraceResult.Type.ENTITY) {
            return false;
        }

        Entity entity = rayTrace.entityHit;
        boolean isExo = entity instanceof EntityExoskeleton.Exoskeleton && entity.getEntityId() == currentEntityId;
        boolean isPlayerTarget = entity instanceof EntityPlayer && entity != player && entity.getEntityId() == currentEntityId;
        if (! (isExo || (isPlayerTarget && currentMode != 2))) {
            return false;
        }

        ItemStack chestStack;
        if (isExo) {
            EntityExoskeleton.Exoskeleton exo = (EntityExoskeleton.Exoskeleton) entity;
            chestStack = exo.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        } else {
            EntityPlayer target = (EntityPlayer) entity;
            chestStack = target.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (chestStack.isEmpty() || !(chestStack.getItem() instanceof IPowerArmor)) {
                return false;
            }
        }
        if (chestStack.isEmpty()) {
            return false;
        }

        float hitY = (float) (rayTrace.hitVec.y - entity.posY);
        EntityEquipmentSlot clickedSlot = getClickedSlot(hitY);
        if (clickedSlot != EntityEquipmentSlot.CHEST || clickedSlot != currentSlot) {
            return false;
        }

        double distanceSq = player.getDistanceSq(entity);
        if (distanceSq > 1.0D) {
            return false;
        }

        float yawDiff = MathHelper.wrapDegrees(player.rotationYaw - entity.rotationYaw);
        if (yawDiff < -55.0F || yawDiff > 55.0F) {
            return false;
        }

        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (currentMode == 0 || currentMode == 1) { // Fusion core modes
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
        } else if (currentMode == 2) { // Enter mode (only for exoskeleton)
            if (player.isSneaking() || !heldItem.isEmpty()) {
                return false;
            }
            return hasBooleanTag(chestStack, "soldered") || isAllExo((EntityExoskeleton.Exoskeleton) entity);
        }

        return false;
    }

    /**
     * Processes a completed hold action by sending a network message.
     * Integrates with ModDataSyncManager for messages and KeybindingExitPowerArmor for exit.
     *
     * @param player The player.
     */
    private static void processHoldCompletion(EntityPlayer player) {
        if (currentMode == 0 || currentMode == 1 || currentMode == 2) {
            ModDataSyncManager.INTERACT_NETWORK.sendToServer(new ModDataSyncManager.InteractMessage(currentMode, currentEntityId, currentSlot));
        } else if (currentMode == 3) {
            AtomFusionProtocol.PACKET_HANDLER.sendToServer(new KeybindingExitPowerArmor.KeyBindingPressedMessage());
        }

        // Start fade effect for enter (2) or exit (3)
        if (currentMode == 2 || currentMode == 3) {
            fadeStartTime = System.currentTimeMillis() + (long) (FADE_DELAY * 1000);
        }
    }

    /**
     * Attempts to start a new hold action based on input.
     * Supports both exoskeleton entities and other players for fusion core actions.
     *
     * @param player The player.
     * @param world The world.
     * @param isHoldingRightClick If right mouse button is held.
     * @param mc The Minecraft instance.
     */
    private static void tryStartHold(EntityPlayer player, World world, boolean isHoldingRightClick, Minecraft mc) {
        // Check for exit action first
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

        RayTraceResult rayTrace = mc.objectMouseOver;
        if (rayTrace == null || rayTrace.typeOfHit != RayTraceResult.Type.ENTITY) {
            return;
        }

        Entity entity = rayTrace.entityHit;
        ItemStack chestStack;
        boolean isExo = entity instanceof EntityExoskeleton.Exoskeleton;
        boolean isPlayerTarget = entity instanceof EntityPlayer && entity != player;

        if (! (isExo || isPlayerTarget)) {
            return;
        }

        if (isExo) {
            EntityExoskeleton.Exoskeleton exo = (EntityExoskeleton.Exoskeleton) entity;
            chestStack = exo.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        } else {
            EntityPlayer target = (EntityPlayer) entity;
            chestStack = target.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (chestStack.isEmpty() || !(chestStack.getItem() instanceof IPowerArmor)) {
                return;
            }
        }
        if (chestStack.isEmpty()) {
            return;
        }

        float hitY = (float) (rayTrace.hitVec.y - entity.posY);
        EntityEquipmentSlot clickedSlot = getClickedSlot(hitY);
        if (clickedSlot != EntityEquipmentSlot.CHEST) {
            return;
        }

        double distanceSq = player.getDistanceSq(entity);
        if (distanceSq > 1.0D) {
            return;
        }

        float yawDiff = MathHelper.wrapDegrees(player.rotationYaw - entity.rotationYaw);
        if (yawDiff < -55.0F || yawDiff > 55.0F) {
            return;
        }

        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (player.isSneaking()) {
            if (hasBooleanTag(chestStack, "soldered")) {
                NBTTagCompound tag = chestStack.getTagCompound();
                boolean hasEnergy = tag != null && tag.hasKey("fusion_depletion");

                if (!heldItem.isEmpty() && heldItem.getItem() == ItemFusionCore.itemFusionCore && !hasEnergy) {
                    startHold(0, FUSION_HOLD_TIME, entity.getEntityId(), clickedSlot, entity.posX, entity.posY, entity.posZ, mc);
                } else if (heldItem.isEmpty() && hasEnergy) {
                    startHold(1, FUSION_HOLD_TIME, entity.getEntityId(), clickedSlot, entity.posX, entity.posY, entity.posZ, mc);
                }
            }
        } else if (isExo) { // Enter only for exoskeleton
            if (heldItem.isEmpty() && (hasBooleanTag(chestStack, "soldered") || isAllExo((EntityExoskeleton.Exoskeleton) entity)) && isPlayerArmorEmpty(player)) {
                startHold(2, ARMOR_HOLD_TIME, entity.getEntityId(), clickedSlot, entity.posX, entity.posY, entity.posZ, mc);
            }
        }
    }

    /**
     * Starts a hold action, setting variables and playing sound.
     *
     * @param mode Action mode.
     * @param maxTime Max hold time.
     * @param entityId Entity ID.
     * @param slot Equipment slot.
     * @param x Sound position X.
     * @param y Sound position Y.
     * @param z Sound position Z.
     * @param mc The Minecraft instance.
     */
    private static void startHold(int mode, float maxTime, int entityId, EntityEquipmentSlot slot, double x, double y, double z, Minecraft mc) {
        startHoldTime = System.currentTimeMillis();
        currentMode = mode;
        currentMaxHoldTime = maxTime;
        currentEntityId = entityId;
        currentSlot = slot;

        if (currentSound == null || !mc.getSoundHandler().isSoundPlaying(currentSound)) {
            currentSound = new LoadingSound(INTERACT_SOUND, (float) x, (float) y, (float) z);
            mc.getSoundHandler().playSound(currentSound);
        }
    }

    /**
     * Determines the clicked slot based on hit Y position.
     * Uses thresholds from EntityExoskeleton.
     *
     * @param hitY Relative hit Y position.
     * @return Equipment slot.
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
     * Checks if all armor slots on exoskeleton are of type 'exo'.
     *
     * @param exo The exoskeleton.
     * @return True if all are 'exo'.
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
     * Checks if player is wearing power armor.
     *
     * @param player The player.
     * @return True if wearing.
     */
    private static boolean isWearingPowerArmor(EntityPlayer player) {
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        return !chest.isEmpty() && chest.getItem() instanceof IPowerArmor;
    }

    /**
     * Resets hold state and stops sound if needed.
     * Called on interruption or completion.
     *
     * @param mc The Minecraft instance.
     */
    private static void resetHoldAndSound(Minecraft mc) {
        if (startHoldTime != 0L) {
            if (currentSound != null) {
                mc.getSoundHandler().stopSound(currentSound);
                currentSound = null;
            }
            startHoldTime = 0L;
            currentMode = -1;
            currentMaxHoldTime = 0F;
            currentEntityId = -1;
            currentSlot = null;
        }
    }

    /**
     * Checks if all player armor slots are empty.
     *
     * @param player The player.
     * @return True if all empty.
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
     * Checks if player is inputting movement (WASD).
     *
     * @param mc The Minecraft instance.
     * @return True if moving.
     */
    private static boolean isPlayerMoving(Minecraft mc) {
        KeyBinding forward = mc.gameSettings.keyBindForward;
        KeyBinding back = mc.gameSettings.keyBindBack;
        KeyBinding left = mc.gameSettings.keyBindLeft;
        KeyBinding right = mc.gameSettings.keyBindRight;
        return forward.isKeyDown() || back.isKeyDown() || left.isKeyDown() || right.isKeyDown();
    }

    /**
     * Custom positioned sound for loading process.
     * Non-repeating, full volume.
     */
    public static class LoadingSound extends net.minecraft.client.audio.PositionedSound {
        public LoadingSound(SoundEvent soundIn, float x, float y, float z) {
            super(soundIn, SoundCategory.PLAYERS);
            this.repeat = false;
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.xPosF = x;
            this.yPosF = y;
            this.zPosF = z;
        }
    }
}