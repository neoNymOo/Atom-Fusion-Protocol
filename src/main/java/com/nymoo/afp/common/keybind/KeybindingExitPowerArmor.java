package com.nymoo.afp.common.keybind;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.util.UtilEntityExoskeleton;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

/**
 * Registers and handles the keybinding for exiting power armor.
 * Sends network message to server when pressed (hold handled in HandlerClientTickEvent).
 */
@ModElementRegistry.ModElement.Tag
public class KeybindingExitPowerArmor extends ModElementRegistry.ModElement {
    public static KeyBinding keys;

    public KeybindingExitPowerArmor(ModElementRegistry instance) {
        super(instance, 1);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        elements.addNetworkMessage(KeyBindingPressedMessageHandler.class, KeyBindingPressedMessage.class, Side.SERVER);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void init(FMLInitializationEvent event) {
        keys = new KeyBinding("key.afp.exit_power_armor", Keyboard.KEY_K, "key.category.afp");
        ClientRegistry.registerKeyBinding(keys);
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Network message for exit key press (completion handled via hold in tick event).
     */
    public static class KeyBindingPressedMessage implements IMessage {
        @Override
        public void toBytes(ByteBuf buf) {
        }

        @Override
        public void fromBytes(ByteBuf buf) {
        }
    }

    /**
     * Handler for exit key message on server.
     * Calls tryExitExoskeleton in UtilEntityExoskeleton.
     */
    public static class KeyBindingPressedMessageHandler implements IMessageHandler<KeyBindingPressedMessage, IMessage> {
        @Override
        public IMessage onMessage(KeyBindingPressedMessage message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player != null && !player.world.isRemote) {
                    UtilEntityExoskeleton.tryExitExoskeleton(player.world, player, false);
                }
            });
            return null;
        }
    }
}