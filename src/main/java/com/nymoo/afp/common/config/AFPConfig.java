package com.nymoo.afp.common.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурационный класс мода Atom Fusion Protocol.
 * Управляет настройками силовой брони, экзоскелетов и энергопотребления.
 * Добавлены дополнительные параметры для тонкой настройки поведения игрока.
 */
public class AFPConfig {
    /**
     * Карта наборов брони для быстрого доступа по имени
     */
    private static final Map<String, ArmorSet> armorSets = new HashMap<>();

    // Общие настройки звуков
    public static boolean playServoJumpSound = true;
    public static boolean playServoStepSound = true;
    public static boolean handlePlayerDeath = true;

    // Настройки взаимодействия с бронёй
    public static boolean canDispenserEquipPowerArmor = false;
    public static boolean canPlayerUnequipPowerArmor = false;
    public static boolean canPlayerEquipPowerArmor = false;
    public static boolean canExoskeletonSwingArms = false;

    // Настройки физики и хитбоксов
    public static float exoskeletonHitboxWidth = 0.65F;
    public static float exoskeletonHitboxHeight = 2.0F;
    public static float powerArmorSpeedMultiplier = 0.85F;
    public static float servoVolume = 0.55F;
    public static float powerArmorFallDamageMultiplier = 0.2F;
    public static float powerArmorFallThreshold = 14.0F;
    public static float knockbackDamageScale = 20.0F;

    // Настройки энергопотребления
    public static float maxDepletion = 288000f;
    public static float stepDeltaThreshold = 0.06f;
    public static float baseDepletionRate = 1.0f;
    public static float sprintDepletionAdder = 0.5f;
    public static float jumpDepletionAdder = 0.7f;
    public static float waterDepletionAdder = 0.3f;
    public static float useItemDepletionAdder = 0.4f;
    public static float hurtDepletionAdder = 1.0f;
    public static float jetpackDepletionAdder = 2.0f;
    public static float walkDepletionAdder = 0.2f;

    // ===== Дополнительные настройки для игроков =====
    /** Скорость игрока при полностью разряжённом ядре синтеза (множитель) */
    public static float powerArmorDepletedSpeedMultiplier = 0.1F;
    /** Количество тиков, в течение которых игрок может дышать под водой в силовой броне */
    public static int underwaterBreathTicks = 24000;
    /** Количество единиц воздуха, устанавливаемое при нахождении игрока под водой */
    public static int underwaterBreathAir = 300;
    /** Множитель замедления поворота (yaw/pitch) при взаимодействии или разряжённом ядре */
    public static float powerArmorRotationMultiplier = 0.05F;
    /** Множитель замедления движения при удержании кнопки взаимодействия */
    public static float powerArmorInteractionSpeed = 0.1F;
    /** Максимальная квадратная дистанция (distanceSq) для входа в экзоскелет */
    public static float exoskeletonEntryDistance = 1.0F;
    /** Максимальный угол (градусы) между взглядом игрока и экзоскелетом для входа/выхода */
    public static float exoskeletonEntryYaw = 55.0F;
    /** Ширина игрока в силовой броне */
    public static float powerArmorWidth = 0.65F;
    /** Высота игрока стоя в силовой броне */
    public static float powerArmorHeightStanding = 2.0F;
    /** Высота игрока в состоянии приседания в силовой броне */
    public static float powerArmorHeightSneaking = 1.85F;
    /** Высота игрока при полете с элитрой в силовой броне */
    public static float powerArmorHeightElytra = 0.6F;
    /** Базовая вертикальная скорость при отталкивании от дна в воде/лаве */
    public static float powerArmorWaterJumpMotion = 0.12F;
    /** Целевая вертикальная скорость для плавного отскока в воде/лаве */
    public static float powerArmorBounceTarget = 0.45F;
    /** Ускорение к целевой скорости отскока в воде/лаве */
    public static float powerArmorBounceAcceleration = 0.055F;
    /** Коэффициент замедления горизонтального движения при отскоке в воде/лаве */
    public static float powerArmorBounceFriction = 0.98F;
    /** Длительность отскока в тиках */
    public static int powerArmorBounceTimer = 6;
    /** Время удержания (секунды) при установке/снятии ядра синтеза */
    public static float fusionHoldTime = 1.45F;
    /** Время удержания (секунды) при входе/выходе из силовой брони */
    public static float armorHoldTime = 6.0F;
    /** Задержка перед началом затемнения экрана (секунды); отрицательное значение отключает задержку */
    public static float fadeDelay = -1.0F;
    /** Длительность плавного затемнения экрана (секунды) */
    public static float fadeDurationIn = 0.5F;
    /** Длительность удержания чёрного экрана (секунды) */
    public static float fadeHold = 0.5F;
    /** Длительность плавного осветления экрана (секунды) */
    public static float fadeDurationOut = 0.5F;
    /** Время перезарядки между операциями с ядром синтеза (секунды) */
    public static float fusionCooldown = 1.0F;
    /** Громкость звуков работы с ядром синтеза */
    public static float fusionVolume = 1.0F;
    /** Громкость звуков работы с бронёй при входе/выходе */
    public static float armorVolume = 1.0F;

    /**
     * Получает набор характеристик брони по имени.
     *
     * @param name Имя набора брони (например, "x03", "t60")
     * @return Набор характеристик брони или null если не найден
     */
    public static ArmorSet getArmorSet(String name) {
        return armorSets.get(name.toLowerCase());
    }

    /**
     * Загружает конфигурацию из файла и инициализирует все настройки.
     *
     * @param config Объект конфигурации Forge
     */
    public static void loadFromConfig(Configuration config) {
        final String CATEGORY_SETTINGS = "settings";
        final String CATEGORY_POWER_ARMOR = CATEGORY_SETTINGS + ".power_armor";
        final String CATEGORY_GENERAL = CATEGORY_SETTINGS + ".general";

        config.setCategoryComment(CATEGORY_SETTINGS, "Minecraft restart is required for these settings to take effect. Make sure parameters are set in the correct format.");

        config.setCategoryPropertyOrder(CATEGORY_GENERAL, Arrays.asList(
                "play_servo_jump_sound",
                "play_servo_step_sound",
                "servo_volume",
                "handle_player_death",
                "can_dispenser_equip_power_armor",
                "can_player_unequip_power_armor",
                "can_player_equip_power_armor",
                "can_exoskeleton_swing_arms",
                "exoskeleton_hitbox_width",
                "exoskeleton_hitbox_height",
                "power_armor_speed_multiplier",
                "power_armor_fall_damage_multiplier",
                "power_armor_fall_threshold",
                "power_armor_knockback_damage_scale",
                "max_depletion",
                "step_delta_threshold",
                "base_depletion_rate",
                "sprint_depletion_adder",
                "jump_depletion_adder",
                "water_depletion_adder",
                "use_item_depletion_adder",
                "hurt_depletion_adder",
                "jetpack_depletion_adder",
                "walk_depletion_adder",
                "power_armor_depleted_speed_multiplier",
                "underwater_breath_ticks",
                "underwater_breath_air",
                "power_armor_rotation_multiplier",
                "power_armor_interaction_speed",
                "exoskeleton_entry_distance",
                "exoskeleton_entry_yaw",
                "power_armor_width",
                "power_armor_height_standing",
                "power_armor_height_sneaking",
                "power_armor_height_elytra",
                "power_armor_water_jump_motion",
                "power_armor_bounce_target",
                "power_armor_bounce_acceleration",
                "power_armor_bounce_friction",
                "power_armor_bounce_timer",
                "fusion_hold_time",
                "armor_hold_time",
                "fade_delay",
                "fade_duration_in",
                "fade_hold",
                "fade_duration_out",
                "fusion_cooldown",
                "fusion_volume",
                "armor_volume"
        ));

        config.setCategoryComment(CATEGORY_POWER_ARMOR, "Property format: helmet_protection, chestplate_protection, leggings_protection, boots_protection, durability, enchantability, toughness, knockback_multiplier");
        config.setCategoryPropertyOrder(CATEGORY_POWER_ARMOR, Arrays.asList(
                "x03",
                "x02",
                "x01",
                "t60",
                "t51",
                "t45",
                "exo"
        ));

        // Загрузка характеристик наборов брони
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "x03", "3, 7, 5, 2, 350, 12, 3.2, 0.05");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "x02", "2, 8, 6, 3, 420, 8, 2.7, 0.10");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "x01", "4, 9, 4, 1, 280, 15, 4.1, 0.15");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "t60", "3, 6, 5, 2, 380, 9, 3.8, 0.20");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "t51", "2, 7, 4, 3, 320, 11, 2.9, 0.25");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "t45", "4, 8, 6, 2, 450, 7, 3.5, 0.30");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "exo", "3, 9, 5, 1, 500, 14, 4.3, 0.40");

        // Загрузка общих настроек
        playServoJumpSound = config.getBoolean("play_servo_jump_sound", CATEGORY_GENERAL, true, "Should servo sounds play when the player jumps while wearing power armor?");
        playServoStepSound = config.getBoolean("play_servo_step_sound", CATEGORY_GENERAL, true, "Should servo sounds play when the player walks while wearing power armor?");
        servoVolume = (float) config.get(CATEGORY_GENERAL, "servo_volume", 0.55, "Volume for servo step sounds. [default: 0.55]").getDouble();
        handlePlayerDeath = config.getBoolean("handle_player_death", CATEGORY_GENERAL, true, "Should the exoskeleton spawn at the player's death location if they are wearing power armor?");
        canDispenserEquipPowerArmor = config.getBoolean("can_dispenser_equip_power_armor", CATEGORY_GENERAL, false, "Can a dispenser equip power armor on the player?");
        canPlayerUnequipPowerArmor = config.getBoolean("can_player_unequip_power_armor", CATEGORY_GENERAL, false, "Can the player manually unequip power armor?");
        canPlayerEquipPowerArmor = config.getBoolean("can_player_equip_power_armor", CATEGORY_GENERAL, false, "Can the player manually equip power armor?");
        canExoskeletonSwingArms = config.getBoolean("can_exoskeleton_swing_arms", CATEGORY_GENERAL, false, "Should the exoskeleton swing its arms?");
        exoskeletonHitboxWidth = (float) config.get(CATEGORY_GENERAL, "exoskeleton_hitbox_width", 0.65, "Exoskeleton hitbox width. [default: 0.65]").getDouble();
        exoskeletonHitboxHeight = (float) config.get(CATEGORY_GENERAL, "exoskeleton_hitbox_height", 2.0, "Exoskeleton hitbox height. [default: 2.0]").getDouble();
        powerArmorSpeedMultiplier = (float) config.get(CATEGORY_GENERAL, "power_armor_speed_multiplier", 0.85, "The player's speed multiplier when wearing power armor is multiplied by the player's current speed. [default: 0.85]").getDouble();
        powerArmorFallDamageMultiplier = (float) config.get(CATEGORY_GENERAL, "power_armor_fall_damage_multiplier", 0.2, "Multiplier for fall damage when wearing power armor. [default: 0.2]").getDouble();
        powerArmorFallThreshold = (float) config.get(CATEGORY_GENERAL, "power_armor_fall_threshold", 14.0, "The fall height threshold before the player starts taking fall damage. [default: 14.0]").getDouble();
        knockbackDamageScale = (float) config.get(CATEGORY_GENERAL, "power_armor_knockback_damage_scale", 20.0, "Damage scale for calculating knockback multiplier in power armor. Higher values reduce knockback sensitivity to damage. [default: 20.0]").getDouble();
        maxDepletion = (float) config.get(CATEGORY_GENERAL, "max_depletion", 288000.0, "Maximum fusion depletion value for power armor energy management. [default: 288000.0]").getDouble();
        stepDeltaThreshold = (float) config.get(CATEGORY_GENERAL, "step_delta_threshold", 0.06, "Threshold for detecting horizontal movement in energy depletion calculation. [default: 0.06]").getDouble();
        baseDepletionRate = (float) config.get(CATEGORY_GENERAL, "base_depletion_rate", 1.0, "Base rate for power armor energy depletion. [default: 1.0]").getDouble();
        sprintDepletionAdder = (float) config.get(CATEGORY_GENERAL, "sprint_depletion_adder", 0.5, "Additional depletion rate when sprinting in power armor. [default: 0.5]").getDouble();
        jumpDepletionAdder = (float) config.get(CATEGORY_GENERAL, "jump_depletion_adder", 0.7, "Additional depletion rate when jumping without jetpack in power armor. [default: 0.7]").getDouble();
        waterDepletionAdder = (float) config.get(CATEGORY_GENERAL, "water_depletion_adder", 0.3, "Additional depletion rate when in water in power armor. [default: 0.3]").getDouble();
        useItemDepletionAdder = (float) config.get(CATEGORY_GENERAL, "use_item_depletion_adder", 0.4, "Additional depletion rate when using an item or swinging in power armor. [default: 0.4]").getDouble();
        hurtDepletionAdder = (float) config.get(CATEGORY_GENERAL, "hurt_depletion_adder", 1.0, "Additional depletion rate when recently hurt or attacked in power armor. [default: 1.0]").getDouble();
        jetpackDepletionAdder = (float) config.get(CATEGORY_GENERAL, "jetpack_depletion_adder", 2.0, "Additional depletion rate when using jetpack in power armor. [default: 2.0]").getDouble();
        walkDepletionAdder = (float) config.get(CATEGORY_GENERAL, "walk_depletion_adder", 0.2, "Additional depletion rate for horizontal ground movement (non-sprinting) in power armor. [default: 0.2]").getDouble();
        powerArmorDepletedSpeedMultiplier = (float) config.get(CATEGORY_GENERAL, "power_armor_depleted_speed_multiplier", 0.1, "Multiplier applied to player speed when the fusion core is fully depleted. [default: 0.1]").getDouble();
        underwaterBreathTicks = config.getInt("underwater_breath_ticks", CATEGORY_GENERAL, 24000, 0, Integer.MAX_VALUE, "Number of ticks a player can breathe underwater while wearing power armor before suffocation begins. [default: 24000]");
        underwaterBreathAir = config.getInt("underwater_breath_air", CATEGORY_GENERAL, 300, 0, Integer.MAX_VALUE, "Air value restored each tick when underwater in power armor. [default: 300]");
        powerArmorRotationMultiplier = (float) config.get(CATEGORY_GENERAL, "power_armor_rotation_multiplier", 0.05, "Multiplier to slow down player rotation (yaw/pitch) when interacting with power armor or when the fusion core is depleted. [default: 0.05]").getDouble();
        powerArmorInteractionSpeed = (float) config.get(CATEGORY_GENERAL, "power_armor_interaction_speed", 0.1, "Movement speed multiplier while holding the interaction key for exoskeleton entry/exit. [default: 0.1]").getDouble();
        exoskeletonEntryDistance = (float) config.get(CATEGORY_GENERAL, "exoskeleton_entry_distance", 1.0, "Squared distance threshold (distanceSq) within which the player can enter an exoskeleton. [default: 1.0]").getDouble();
        exoskeletonEntryYaw = (float) config.get(CATEGORY_GENERAL, "exoskeleton_entry_yaw", 55.0, "Maximum yaw difference (degrees) between the player's view and the exoskeleton for entry/exit. [default: 55.0]").getDouble();
        powerArmorWidth = (float) config.get(CATEGORY_GENERAL, "power_armor_width", 0.65, "The player width when wearing power armor. [default: 0.65]").getDouble();
        powerArmorHeightStanding = (float) config.get(CATEGORY_GENERAL, "power_armor_height_standing", 2.0, "The player height when standing and wearing power armor. [default: 2.0]").getDouble();
        powerArmorHeightSneaking = (float) config.get(CATEGORY_GENERAL, "power_armor_height_sneaking", 1.85, "The player height when sneaking and wearing power armor. [default: 1.85]").getDouble();
        powerArmorHeightElytra = (float) config.get(CATEGORY_GENERAL, "power_armor_height_elytra", 0.6, "The player height when flying with elytra and wearing power armor. [default: 0.6]").getDouble();
        powerArmorWaterJumpMotion = (float) config.get(CATEGORY_GENERAL, "power_armor_water_jump_motion", 0.12, "Base upward motion applied when jumping out of water/lava in power armor. [default: 0.12]").getDouble();
        powerArmorBounceTarget = (float) config.get(CATEGORY_GENERAL, "power_armor_bounce_target", 0.45, "Target upward velocity for water bounce in power armor. [default: 0.45]").getDouble();
        powerArmorBounceAcceleration = (float) config.get(CATEGORY_GENERAL, "power_armor_bounce_acceleration", 0.055, "Acceleration added each tick towards the water bounce target velocity. [default: 0.055]").getDouble();
        powerArmorBounceFriction = (float) config.get(CATEGORY_GENERAL, "power_armor_bounce_friction", 0.98, "Horizontal friction applied during water bounce while wearing power armor. [default: 0.98]").getDouble();
        powerArmorBounceTimer = config.getInt("power_armor_bounce_timer", CATEGORY_GENERAL, 6, 0, Integer.MAX_VALUE, "Duration in ticks for the water bounce effect in power armor. [default: 6]");
        fusionHoldTime = (float) config.get(CATEGORY_GENERAL, "fusion_hold_time", 1.45, "Hold duration (seconds) when inserting or removing a fusion core. [default: 1.45]").getDouble();
        armorHoldTime = (float) config.get(CATEGORY_GENERAL, "armor_hold_time", 6.0, "Hold duration (seconds) when entering or exiting power armor/exoskeleton. [default: 6.0]").getDouble();
        fadeDelay = (float) config.get(CATEGORY_GENERAL, "fade_delay", -1.0, "Delay before fade effect starts after successful armor entry/exit. Negative disables fade. [default: -1.0]").getDouble();
        fadeDurationIn = (float) config.get(CATEGORY_GENERAL, "fade_duration_in", 0.5, "Duration of fade-in effect when entering/exiting power armor/exoskeleton. [default: 0.5]").getDouble();
        fadeHold = (float) config.get(CATEGORY_GENERAL, "fade_hold", 0.5, "Hold time for the fade effect between fade-in and fade-out. [default: 0.5]").getDouble();
        fadeDurationOut = (float) config.get(CATEGORY_GENERAL, "fade_duration_out", 0.5, "Duration of fade-out effect when entering/exiting power armor/exoskeleton. [default: 0.5]").getDouble();
        fusionCooldown = (float) config.get(CATEGORY_GENERAL, "fusion_cooldown", 1.0, "Cooldown time (seconds) between fusion core interactions. [default: 1.0]").getDouble();
        fusionVolume = (float) config.get(CATEGORY_GENERAL, "fusion_volume", 1.0, "Volume for fusion core interaction sounds. [default: 1.0]").getDouble();
        armorVolume = (float) config.get(CATEGORY_GENERAL, "armor_volume", 1.0, "Volume for power armor entry/exit sounds. [default: 1.0]").getDouble();
    }

    /**
     * Загружает характеристики набора брони из конфигурации.
     *
     * @param config       Объект конфигурации Forge
     * @param category     Категория конфигурации
     * @param armorName    Имя набора брони
     * @param defaultValue Значение по умолчанию в формате CSV
     */
    private static void loadArmorSet(Configuration config, String category, String armorName, String defaultValue) {
        String description = armorName.toUpperCase() + " power armor configuration. [default: " + defaultValue + "]";
        Property property = config.get(category, armorName, defaultValue, description);
        ArmorSet armorSet = parseArmorSet(property.getString());
        armorSets.put(armorName, armorSet);
    }

    /**
     * Парсит строку с характеристиками брони в объект ArmorSet.
     *
     * @param value Строка с характеристиками в формате CSV
     * @return Объект с характеристиками брони
     * @throws IllegalArgumentException При неверном формате строки
     */
    private static ArmorSet parseArmorSet(String value) {
        String[] parts = value.split(",");
        if (parts.length != 8) {
            throw new IllegalArgumentException("Invalid armor set configuration: expected 8 comma-separated values.");
        }
        ArmorSet set = new ArmorSet();
        set.helmetProtection = Integer.parseInt(parts[0].trim());
        set.chestplateProtection = Integer.parseInt(parts[1].trim());
        set.leggingsProtection = Integer.parseInt(parts[2].trim());
        set.bootsProtection = Integer.parseInt(parts[3].trim());
        set.durability = Integer.parseInt(parts[4].trim());
        set.enchantability = Integer.parseInt(parts[5].trim());
        set.toughness = Float.parseFloat(parts[6].trim());
        set.knockbackMultiplier = Float.parseFloat(parts[7].trim());
        return set;
    }

    /**
     * Класс для хранения характеристик набора брони.
     * Содержит параметры защиты, прочности и зачаровываемости.
     */
    public static class ArmorSet {
        /** Уровень защиты шлема */
        public int helmetProtection;
        /** Уровень защиты нагрудника */
        public int chestplateProtection;
        /** Уровень защиты поножей */
        public int leggingsProtection;
        /** Уровень защиты ботинок */
        public int bootsProtection;
        /** Прочность брони */
        public int durability;
        /** Уровень зачаровываемости */
        public int enchantability;
        /** Жёсткость брони */
        public float toughness;
        /** Множитель отбрасывания для этого типа брони */
        public float knockbackMultiplier;
    }
}