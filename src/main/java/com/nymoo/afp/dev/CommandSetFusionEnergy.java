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

@ModElementRegistry.ModElement.Tag
public class CommandSetFusionEnergy extends ModElementRegistry.ModElement {
    public CommandSetFusionEnergy(ModElementRegistry instance) {
        super(instance, 2);
    }

    @Override
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandHandler());
    }

    public static class CommandHandler implements ICommand {
        @Override
        public int compareTo(ICommand c) {
            return getName().compareTo(c.getName());
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

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            // Проверка наличия аргумента
            if (args.length != 1) {
                throw new CommandException("Использование: " + getUsage(sender));
            }

            // Парсинг значения энергии
            int energyValue;
            try {
                energyValue = Integer.parseInt(args[0]);
                if (energyValue < 0) {
                    throw new CommandException("Значение энергии не может быть отрицательным");
                }
            } catch (NumberFormatException e) {
                throw new CommandException("Недопустимое числовое значение. Используйте целое число");
            }

            // Получение сущности-отправителя (игрока)
            if (!(sender.getCommandSenderEntity() instanceof EntityPlayer)) {
                throw new CommandException("Команда может быть выполнена только игроком");
            }
            EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();

            // Получение предмета в руке игрока
            ItemStack heldItem = player.getHeldItemMainhand();

            // Проверка, что предмет является fusion core
            if (heldItem.isEmpty() || !heldItem.getItem().getRegistryName().toString().equals("afp:fusion_core")) {
                throw new CommandException("Вы должны держать fusion core в основной руке");
            }

            // Установка NBT-тега
            if (!heldItem.hasTagCompound()) {
                heldItem.setTagCompound(new NBTTagCompound());
            }
            heldItem.getTagCompound().setFloat("fusion_depletion", energyValue);

            // Отправка сообщения игроку
            String message = TextFormatting.GREEN + "Установлено значение энергии: " + energyValue + " для fusion core в руке";
            player.sendMessage(new TextComponentString(message));
        }
    }
}