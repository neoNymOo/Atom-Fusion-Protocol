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

import java.util.ArrayList;
import java.util.List;

@ModElementRegistry.ModElement.Tag
public class CommandSolderArmor extends ModElementRegistry.ModElement {
    public CommandSolderArmor(ModElementRegistry instance) {
        super(instance, 1);
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
        public boolean checkPermission(MinecraftServer server, ICommandSender var1) {
            return true;
        }

        @Override
        public List getAliases() {
            return new ArrayList();
        }

        @Override
        public List getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
            return new ArrayList();
        }

        @Override
        public boolean isUsernameIndex(String[] string, int index) {
            return true;
        }

        @Override
        public String getName() {
            return "solderarmor";
        }

        @Override
        public String getUsage(ICommandSender var1) {
            return "/solderarmor [<arguments>]";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            // Проверка наличия аргумента
            if (args.length != 1) {
                throw new CommandException("Использование: " + getUsage(sender));
            }

            // Парсинг значения boolean
            boolean solderValue;
            if (args[0].equalsIgnoreCase("true")) {
                solderValue = true;
            } else if (args[0].equalsIgnoreCase("false")) {
                solderValue = false;
            } else {
                throw new CommandException("Недопустимое значение. Используйте true или false");
            }

            // Получение сущности-отправителя (игрока)
            Entity senderEntity = sender.getCommandSenderEntity();
            if (!(senderEntity instanceof EntityPlayer)) {
                throw new CommandException("Команда может быть выполнена только игроком");
            }
            EntityPlayer player = (EntityPlayer) senderEntity;

            // Поиск экзоскелетов в радиусе 10 блоков
            World world = player.world;
            AxisAlignedBB searchArea = new AxisAlignedBB(
                    player.posX - 10, player.posY - 10, player.posZ - 10,
                    player.posX + 10, player.posY + 10, player.posZ + 10
            );

            int modifiedCount = 0;
            for (EntityExoskeleton.Exoskeleton exo : world.getEntitiesWithinAABB(
                    EntityExoskeleton.Exoskeleton.class, searchArea)) {

                // Получение нагрудника экзоскелета
                ItemStack chestplate = exo.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                if (!chestplate.isEmpty()) {
                    // Установка NBT-тега
                    if (!chestplate.hasTagCompound()) {
                        chestplate.setTagCompound(new NBTTagCompound());
                    }
                    chestplate.getTagCompound().setBoolean("soldered", solderValue);
                    modifiedCount++;
                }
            }

            // Отправка сообщения игроку
            String message = TextFormatting.GREEN + "Изменено экзоскелетов: " + modifiedCount;
            player.sendMessage(new TextComponentString(message));
        }
    }
}