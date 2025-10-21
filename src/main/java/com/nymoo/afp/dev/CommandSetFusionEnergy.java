package com.nymoo.afp.dev;

import com.nymoo.afp.ModElementRegistry;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Элемент мода для регистрации команды установки энергии ядра синтеза.
 * Предоставляет административную команду для настройки уровня энергии в ядрах синтеза.
 */
@ModElementRegistry.ModElement.Tag
public class CommandSetFusionEnergy extends ModElementRegistry.ModElement {
    public CommandSetFusionEnergy(ModElementRegistry instance) {
        super(instance, 2);
    }

    @Override
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandHandler());
    }

    /**
     * Обработчик команды для установки уровня энергии ядра синтеза.
     * Позволяет администраторам устанавливать произвольные значения энергии в ядре синтеза.
     */
    public static class CommandHandler implements ICommand {
        @Override
        public int compareTo(ICommand command) {
            return getName().compareTo(command.getName());
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            return true;
        }

        @Override
        public List<String> getAliases() {
            return new ArrayList<>();
        }

        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
            return new ArrayList<>();
        }

        @Override
        public boolean isUsernameIndex(String[] args, int index) {
            return false;
        }

        @Override
        public String getName() {
            return "setfusionenergy";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "/setfusionenergy <energy_value>";
        }

        /**
         * Выполняет команду установки энергии ядра синтеза.
         * Проверяет аргументы, валидирует входные данные и устанавливает значение энергии в предмете.
         *
         * @param server Сервер Minecraft
         * @param sender Отправитель команды
         * @param args   Аргументы команды
         * @throws CommandException При ошибках выполнения команды
         */
        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (args.length != 1) {
                throw new CommandException("Использование: " + getUsage(sender));
            }

            int energyValue;
            try {
                energyValue = Integer.parseInt(args[0]);
                if (energyValue < 0) {
                    throw new CommandException("Значение энергии не может быть отрицательным");
                }
            } catch (NumberFormatException exception) {
                throw new CommandException("Недопустимое числовое значение. Используйте целое число");
            }

            if (!(sender.getCommandSenderEntity() instanceof EntityPlayer)) {
                throw new CommandException("Команда может быть выполнена только игроком");
            }
            EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();

            ItemStack heldItem = player.getHeldItemMainhand();

            if (heldItem.isEmpty() || !heldItem.getItem().getRegistryName().toString().equals("afp:fusion_core")) {
                throw new CommandException("Вы должны держать fusion core в основной руке");
            }

            if (!heldItem.hasTagCompound()) {
                heldItem.setTagCompound(new NBTTagCompound());
            }
            heldItem.getTagCompound().setFloat("fusion_depletion", energyValue);

            String message = TextFormatting.GREEN + "Установлено значение энергии: " + energyValue + " для fusion core в руке";
            player.sendMessage(new TextComponentString(message));
        }
    }
}