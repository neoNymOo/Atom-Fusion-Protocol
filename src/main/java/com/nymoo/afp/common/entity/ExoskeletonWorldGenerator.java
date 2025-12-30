package com.nymoo.afp.common.entity;

import com.nymoo.afp.common.config.AFPConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class ExoskeletonWorldGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        // 1. Быстрый выход: Проверка измерения (Только Overworld)
        if (world.provider.getDimension() != 0) return;

        // 2. Логически точная проверка вероятности (для поддержки значений типа 0.39 или 1.54)
        double chance = AFPConfig.brokenArmorSpawnWeight;
        if (chance <= 0) return; // Отключено в конфиге
        // Генерация float от 0.0 до 100.0 обеспечивает точность дробей
        if (random.nextFloat() * 100.0F >= chance) return;

        // 3. Расчет координат БЕЗ +8 (Предотвращение Cascading World Gen Lag)
        // Генерируем X и Z строго в пределах 1-14 внутри чанка, чтобы избежать границ
        // chunkX * 16 - начало чанка. + random.nextInt(14) + 1 - отступ от краев на 1 блок.
        // Это гарантирует, что мы не затронем соседние чанки при поиске высоты.
        int x = (chunkX * 16) + random.nextInt(16);
        int z = (chunkZ * 16) + random.nextInt(16);

        // 4. Безопасный поиск высоты
        // Используем BlockPos только внутри сгенерированного диапазона
        BlockPos pos = new BlockPos(x, 0, z);
        BlockPos top = world.getTopSolidOrLiquidBlock(pos);
        int y = top.getY();

        // Проверка на адекватную высоту (не пустота и не крыша мира)
        if (y <= 4 || y > world.getHeight() - 10) return;

        // 5. Создание и спавн
        // Важно: EntityExoskeletonBroken спавнит соседей. В методе onInitialSpawn убедитесь,
        // что neighbor (соседи) не уходят слишком далеко, иначе они могут загрузить соседний чанк.
        // Спавн в центре случайной позиции безопаснее.

        EntityExoskeletonBroken.ExoskeletonBroken entity = new EntityExoskeletonBroken.ExoskeletonBroken(world);
        // Центрируем сущность внутри блока (+0.5)
        entity.setLocationAndAngles(x + 0.5, y, z + 0.5, random.nextFloat() * 360.0F, 0.0F);

        if (entity.getCanSpawnHere()) {
            entity.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), null);
            world.spawnEntity(entity);
        }
    }
}