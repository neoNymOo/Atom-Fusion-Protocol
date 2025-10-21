package com.nymoo.afp.common.mixin;

import com.nymoo.afp.Tags;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Загрузчик миксинов для мода Atom Fusion Protocol.
 * Реализует интерфейсы IFMLLoadingPlugin и IEarlyMixinLoader для интеграции с MixinBooter.
 * Отвечает за загрузку конфигурации миксинов на ранней стадии инициализации Minecraft.
 */
@IFMLLoadingPlugin.MCVersion(Tags.MC_VERSION)
@IFMLLoadingPlugin.Name(Tags.MOD_ID)
public class MixinLoader implements IFMLLoadingPlugin, IEarlyMixinLoader {

    /**
     * Возвращает список конфигурационных файлов миксинов для загрузки.
     *
     * @return Список с одним элементом - именем конфигурационного файла миксинов
     */
    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.afp.json");
    }

    /**
     * Возвращает классы трансформеров ASM (не используется).
     *
     * @return Пустой массив строк, так как трансформеры не используются
     */
    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    /**
     * Возвращает класс контейнера мода (не используется).
     *
     * @return null, так как контейнер мода не требуется
     */
    @Override
    public String getModContainerClass() {
        return null;
    }

    /**
     * Возвращает класс настройки (не используется).
     *
     * @return null, так как класс настройки не требуется
     */
    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    /**
     * Внедряет данные в загрузчик миксинов.
     *
     * @param data Карта данных для внедрения
     */
    @Override
    public void injectData(Map<String, Object> data) {
    }

    /**
     * Возвращает класс трансформера доступа (не используется).
     *
     * @return null, так как трансформер доступа не требуется
     */
    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}