package com.nymoo.afp.dev;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.entity.EntityExoskeleton;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.util.Collections;
import java.util.List;

/**
 * Элемент мода для регистрации команды пайки брони экзоскелетов.
 * Предоставляет административную команду для управления состоянием пайки брони на экзоскелетах.
 */
@ModElementRegistry.ModElement.Tag
public class CommandSolderArmor extends ModElementRegistry.ModElement {
    public CommandSolderArmor(ModElementRegistry instance) {
        super(instance, 1);
    }

    @Override
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandHandler());
    }

    /**
     * Обработчик команды для управления состоянием пайки брони экзоскелетов.
     * Позволяет устанавливать или снимать флаг пайки со всех экзоскелетов в радиусе 10 блоков.
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
            // Return an immutable empty list to avoid allocations on each call
            return Collections.emptyList();
        }

        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos position) {
            // Return an immutable empty list to avoid allocations on each call
            return Collections.emptyList();
        }

        @Override
        public boolean isUsernameIndex(String[] arguments, int index) {
            return true;
        }

        @Override
        public String getName() {
            return "solderarmor";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "/solderarmor [<arguments>]";
        }

        /**
         * Выполняет команду управления пайкой брони экзоскелетов.
         * Изменяет состояние пайки на всех экзоскелетах в радиусе 10 блоков от игрока.
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

            boolean solderValue;
            if (args[0].equalsIgnoreCase("true")) {
                solderValue = true;
            } else if (args[0].equalsIgnoreCase("false")) {
                solderValue = false;
            } else {
                throw new CommandException("Недопустимое значение. Используйте true или false");
            }

            Entity senderEntity = sender.getCommandSenderEntity();
            if (!(senderEntity instanceof EntityPlayer)) {
                throw new CommandException("Команда может быть выполнена только игроком");
            }
            EntityPlayer player = (EntityPlayer) senderEntity;

            World world = player.world;
            AxisAlignedBB searchArea = new AxisAlignedBB(
                    player.posX - 10, player.posY - 10, player.posZ - 10,
                    player.posX + 10, player.posY + 10, player.posZ + 10
            );

            int modifiedCount = 0;
            for (EntityExoskeleton.Exoskeleton exoskeleton : world.getEntitiesWithinAABB(
                    EntityExoskeleton.Exoskeleton.class, searchArea)) {

                ItemStack chestplate = exoskeleton.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                if (!chestplate.isEmpty()) {
                    if (!chestplate.hasTagCompound()) {
                        chestplate.setTagCompound(new NBTTagCompound());
                    }
                    chestplate.getTagCompound().setBoolean("soldered", solderValue);
                    modifiedCount++;
                }
            }

            String message = TextFormatting.GREEN + "Изменено экзоскелетов: " + modifiedCount;
            player.sendMessage(new TextComponentString(message));
        }
    }
}