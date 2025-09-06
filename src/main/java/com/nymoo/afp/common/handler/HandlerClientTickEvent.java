package com.nymoo.afp.common.handler;

import com.nymoo.afp.ModDataSyncManager;
import com.nymoo.afp.common.entity.EntityExoskeleton;
import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.item.ItemFusionCore;
import com.nymoo.afp.common.keybind.KeybindingExitPowerArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSound;
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
import com.nymoo.afp.AtomFusionProtocol;

import java.util.EnumSet;

import static com.nymoo.afp.common.util.UtilEntityExoskeleton.hasBooleanTag;

/**
 * Handles client-side tick events for detecting and processing hold actions related to exoskeleton interactions.
 * Monitors mouse and key inputs, manages progress timing, plays/stops sounds, and sends network messages via ModDataSyncManager.
 * Renders progress via interconnection with HandlerRenderGameOverlayEvent.
 */
@Mod.EventBusSubscriber
public class HandlerClientTickEvent {
    // Duration for holding to install/uninstall fusion core (in seconds)
    public static final float FUSION_HOLD_TIME = 1.5F;
    // Duration for holding to enter/exit armor (in seconds)
    public static final float ARMOR_HOLD_TIME = 3.0F;
    // Sound event for interaction process
    private static final SoundEvent INTERACT_SOUND = new SoundEvent(new ResourceLocation("afp", "fusion_core_interact"));
    // Time when hold started
    public static long startHoldTime = 0L;
    // Maximum hold time for current action
    public static float currentMaxHoldTime = 0F;
    // Current action mode: -1 none, 0 install fusion, 1 uninstall fusion, 2 enter, 3 exit
    public static int currentMode = -1;
    // ID of the targeted exoskeleton entity
    public static int currentEntityId = -1;
    // Targeted equipment slot
    public static EntityEquipmentSlot currentSlot = null;
    // Currently playing loading sound
    public static LoadingSound currentSound = null;
    // Set of armor slots for quick iteration
    private static final EnumSet<EntityEquipmentSlot> ARMOR_SLOTS = EnumSet.of(
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET
    );

    /**
     * Client tick event handler.
     * Processes hold actions at the end of each tick.
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
            resetHoldAndSoundIfNeeded(mc);
            return;
        }

        EntityPlayer player = mc.player;
        World world = mc.world;
        boolean isHoldingRightClick = Mouse.isButtonDown(1);

        if (startHoldTime != 0L) {
            long now = System.currentTimeMillis();
            float elapsed = (now - startHoldTime) / 1000.0F;

            if (!isHoldStillValid(player, world, isHoldingRightClick)) {
                resetHoldAndSoundIfNeeded(mc);
                return;
            }

            if (elapsed >= currentMaxHoldTime) {
                processCompletedHold(player);
                resetHoldAndSoundIfNeeded(mc);
            }
        } else {
            attemptStartHold(player, world, isHoldingRightClick, mc);
        }
    }

    /**
     * Checks if the current hold action is still valid based on input and targeting.
     * @param player The player.
     * @param world The world.
     * @param isHoldingRightClick Whether right mouse is held.
     * @return True if hold is valid.
     */
    private static boolean isHoldStillValid(EntityPlayer player, World world, boolean isHoldingRightClick) {
        if (currentMode == 3) {
            return KeybindingExitPowerArmor.keys.isKeyDown() && !player.isSneaking() && isWearingPowerArmor(player);
        }

        if (!isHoldingRightClick) {
            return false;
        }

        RayTraceResult mop = Minecraft.getMinecraft().objectMouseOver;
        if (mop == null || mop.typeOfHit != RayTraceResult.Type.ENTITY) {
            return false;
        }
        Entity entity = mop.entityHit;
        if (!(entity instanceof EntityExoskeleton.Exoskeleton) || entity.getEntityId() != currentEntityId) {
            return false;
        }
        EntityExoskeleton.Exoskeleton exo = (EntityExoskeleton.Exoskeleton) entity;
        ItemStack chestStack = exo.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestStack.isEmpty()) {
            return false;
        }

        float hitY = (float) (mop.hitVec.y - exo.posY);
        EntityEquipmentSlot clickedSlotTemp = getClickedSlot(hitY);

        if (clickedSlotTemp != EntityEquipmentSlot.CHEST || clickedSlotTemp != currentSlot) {
            return false;
        }

        double distanceSq = player.getDistanceSq(exo);
        if (distanceSq > 1.0D) {
            return false;
        }

        float yawDiff = MathHelper.wrapDegrees(player.rotationYaw - exo.rotationYaw);
        if (yawDiff < -55.0F || yawDiff > 55.0F) {
            return false;
        }

        ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);

        if (currentMode == 0 || currentMode == 1) {
            if (!player.isSneaking() || !hasBooleanTag(chestStack, "soldered")) {
                return false;
            }
            NBTTagCompound tag = chestStack.getTagCompound();
            boolean hasEnergy = tag != null && tag.hasKey("fusion_energy");
            if (currentMode == 0) {
                return !held.isEmpty() && held.getItem() == ItemFusionCore.itemFusionCore && !hasEnergy;
            } else {
                return held.isEmpty() && hasEnergy;
            }
        } else if (currentMode == 2) {
            if (player.isSneaking() || !held.isEmpty()) {
                return false;
            }
            return hasBooleanTag(chestStack, "soldered") || isAllExo(exo);
        }
        return false;
    }

    /**
     * Processes the completed hold action by sending network message.
     * Interconnects with ModDataSyncManager for sending messages and KeybindingExitPowerArmor for exit.
     * @param player The player.
     */
    private static void processCompletedHold(EntityPlayer player) {
        if (currentMode == 0 || currentMode == 1 || currentMode == 2) {
            ModDataSyncManager.INTERACT_NETWORK.sendToServer(new ModDataSyncManager.InteractMessage(currentMode, currentEntityId, currentSlot));
        } else if (currentMode == 3) {
            AtomFusionProtocol.PACKET_HANDLER.sendToServer(new KeybindingExitPowerArmor.KeyBindingPressedMessage());
        }
    }

    /**
     * Attempts to start a new hold action based on input.
     * @param player The player.
     * @param world The world.
     * @param isHoldingRightClick Whether right mouse is held.
     * @param mc The Minecraft instance.
     */
    private static void attemptStartHold(EntityPlayer player, World world, boolean isHoldingRightClick, Minecraft mc) {
        if (KeybindingExitPowerArmor.keys.isKeyDown() && !player.isSneaking() && isWearingPowerArmor(player)) {
            startHold(3, ARMOR_HOLD_TIME, -1, null, player.posX, player.posY, player.posZ, mc);
            return;
        }

        if (!isHoldingRightClick) {
            return;
        }

        RayTraceResult mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != RayTraceResult.Type.ENTITY) {
            return;
        }
        Entity entity = mop.entityHit;
        if (!(entity instanceof EntityExoskeleton.Exoskeleton)) {
            return;
        }
        EntityExoskeleton.Exoskeleton exo = (EntityExoskeleton.Exoskeleton) entity;
        ItemStack chestStack = exo.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestStack.isEmpty()) {
            return;
        }

        float hitY = (float) (mop.hitVec.y - exo.posY);
        EntityEquipmentSlot clickedSlotTemp = getClickedSlot(hitY);
        if (clickedSlotTemp != EntityEquipmentSlot.CHEST) {
            return;
        }

        double distanceSq = player.getDistanceSq(exo);
        if (distanceSq > 1.0D) {
            return;
        }

        float yawDiff = MathHelper.wrapDegrees(player.rotationYaw - exo.rotationYaw);
        if (yawDiff < -55.0F || yawDiff > 55.0F) {
            return;
        }

        ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
        if (player.isSneaking()) {
            if (hasBooleanTag(chestStack, "soldered")) {
                NBTTagCompound tag = chestStack.getTagCompound();
                boolean hasEnergy = tag != null && tag.hasKey("fusion_energy");
                if (!held.isEmpty() && held.getItem() == ItemFusionCore.itemFusionCore && !hasEnergy) {
                    startHold(0, FUSION_HOLD_TIME, exo.getEntityId(), clickedSlotTemp, exo.posX, exo.posY, exo.posZ, mc);
                } else if (held.isEmpty() && hasEnergy) {
                    startHold(1, FUSION_HOLD_TIME, exo.getEntityId(), clickedSlotTemp, exo.posX, exo.posY, exo.posZ, mc);
                }
            }
        } else {
            if (held.isEmpty() && (hasBooleanTag(chestStack, "soldered") || isAllExo(exo))) {
                startHold(2, ARMOR_HOLD_TIME, exo.getEntityId(), clickedSlotTemp, exo.posX, exo.posY, exo.posZ, mc);
            }
        }
    }

    /**
     * Starts a hold action, setting variables and playing sound.
     * @param mode The action mode.
     * @param maxTime The max hold time.
     * @param entityId The entity ID.
     * @param slot The slot.
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
     * @param hitY The relative hit Y.
     * @return The equipment slot.
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
     * Checks if all armor slots on exoskeleton are base 'exo' type.
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
     * @param player The player.
     * @return True if wearing.
     */
    private static boolean isWearingPowerArmor(EntityPlayer player) {
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        return !chest.isEmpty() && chest.getItem() instanceof IPowerArmor;
    }

    /**
     * Resets hold state and stops sound.
     * Called on interruption or completion.
     * @param mc The Minecraft instance.
     */
    private static void resetHoldAndSoundIfNeeded(Minecraft mc) {
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
     * Custom positioned sound for loading process.
     * Non-repeating, full volume.
     */
    public static class LoadingSound extends PositionedSound {
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