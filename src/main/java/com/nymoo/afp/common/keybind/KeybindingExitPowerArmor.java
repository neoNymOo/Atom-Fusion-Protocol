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
 * Элемент мода для регистрации и обработки клавиши выхода из силовой брони.
 * Управляет привязкой клавиши и отправкой сетевых сообщений при нажатии.
 */
@ModElementRegistry.ModElement.Tag
public class KeybindingExitPowerArmor extends ModElementRegistry.ModElement {
    /**
     * Привязка клавиши для выхода из силовой брони
     */
    public static KeyBinding keys;

    public KeybindingExitPowerArmor(ModElementRegistry instance) {
        super(instance, 1);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        elements.addNetworkMessage(KeyBindingPressedMessageHandler.class, KeyBindingPressedMessage.class, Side.SERVER);
    }

    /**
     * Инициализирует привязку клавиши на клиентской стороне.
     * Регистрирует клавишу K по умолчанию для выхода из силовой брони.
     *
     * @param event Событие инициализации
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void init(FMLInitializationEvent event) {
        keys = new KeyBinding("key.afp.exit_power_armor", Keyboard.KEY_K, "key.category.afp");
        ClientRegistry.registerKeyBinding(keys);
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Сетевое сообщение о нажатии клавиши выхода из силовой брони.
     * Не содержит данных, так как служит только триггером.
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
     * Обработчик сетевого сообщения на стороне сервера.
     * Вызывает процедуру выхода из экзоскелета для игрока.
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