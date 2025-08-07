package com.nymoo.afp.common.keybinds;

import com.nymoo.afp.AtomFusionProtocol;
import com.nymoo.afp.ElementsAFP;
import com.nymoo.afp.common.utils.EntityExoskeletonUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@ElementsAFP.ModElement.Tag
public class KeybindingExitPowerArmor extends ElementsAFP.ModElement {
    private KeyBinding keys;

    public KeybindingExitPowerArmor(ElementsAFP instance) {
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

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().currentScreen == null && keys.isPressed()) {
            AtomFusionProtocol.PACKET_HANDLER.sendToServer(new KeyBindingPressedMessage());
        }
    }

    public static class KeyBindingPressedMessage implements IMessage {
        @Override
        public void toBytes(ByteBuf buf) {}

        @Override
        public void fromBytes(ByteBuf buf) {}
    }

    public static class KeyBindingPressedMessageHandler implements IMessageHandler<KeyBindingPressedMessage, IMessage> {
        @Override
        public IMessage onMessage(KeyBindingPressedMessage message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player != null && !player.world.isRemote) {
                    EntityExoskeletonUtil.tryExitExoskeleton(player.world, player);
                }
            });
            return null;
        }
    }
}