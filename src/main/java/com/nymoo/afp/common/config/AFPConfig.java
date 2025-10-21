package com.nymoo.afp.common.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурационный класс мода Atom Fusion Protocol.
 * Управляет настройками силовой брони, экзоскелетов и энергопотребления.
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
    public static float powerArmorKnockbackMultiplier = 0.0F;
    public static float powerArmorFallDamageMultiplier = 0.2F;
    public static float powerArmorFallThreshold = 14.0F;

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

    /**
     * Получает набор характеристик брони по имени.
     *
     * @param name Имя набора брони (например, "x-03", "t-60")
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
                "power_armor_knockback_multiplier",
                "power_armor_fall_damage_multiplier",
                "power_armor_fall_threshold",
                "max_depletion",
                "step_delta_threshold",
                "base_depletion_rate",
                "sprint_depletion_adder",
                "jump_depletion_adder",
                "water_depletion_adder",
                "use_item_depletion_adder",
                "hurt_depletion_adder",
                "jetpack_depletion_adder",
                "walk_depletion_adder"
        ));

        config.setCategoryComment(CATEGORY_POWER_ARMOR, "Property format: helmet_protection, chestplate_protection, leggings_protection, boots_protection, durability, enchantability, toughness");
        config.setCategoryPropertyOrder(CATEGORY_POWER_ARMOR, Arrays.asList(
                "x-03",
                "x-02",
                "x-01",
                "t-60",
                "t-51",
                "t-45",
                "exo"
        ));

        // Загрузка характеристик наборов брони
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "x-03", "3, 7, 5, 2, 350, 12, 3.2");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "x-02", "2, 8, 6, 3, 420, 8, 2.7");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "x-01", "4, 9, 4, 1, 280, 15, 4.1");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "t-60", "3, 6, 5, 2, 380, 9, 3.8");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "t-51", "2, 7, 4, 3, 320, 11, 2.9");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "t-45", "4, 8, 6, 2, 450, 7, 3.5");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "exo", "3, 9, 5, 1, 500, 14, 4.3");

        // Загрузка общих настроек
        playServoJumpSound = config.getBoolean("play_servo_jump_sound", CATEGORY_GENERAL, true, "Should servo sounds play when the player jumps while wearing power armor?");
        playServoStepSound = config.getBoolean("play_servo_step_sound", CATEGORY_GENERAL, true, "Should servo sounds play when the player walks while wearing power armor?");
        servoVolume = (float) config.get(CATEGORY_GENERAL, "servo_volume", 0.55, "Volume for servo step sounds. [default: 0.55]").getDouble();
        handlePlayerDeath = config.getBoolean("handle_player_death", CATEGORY_GENERAL, true, "Should the exoskeleton spawn at the player's death location if they are wearing power armor?");
        canDispenserEquipPowerArmor = config.getBoolean("can_dispenser_equip_power_armor", CATEGORY_GENERAL, false, "Can a dispenser equip power armor on the player?");
        canPlayerUnequipPowerArmor = config.getBoolean("can_player_unequip_power_armor", CATEGORY_GENERAL, false, "Can the player manually unequip power armor?");
        canPlayerEquipPowerArmor = config.getBoolean("can_player_equip_power_armor", CATEGORY_GENERAL, false, "Can the player manually equip power armor?");
        canExoskeletonSwingArms = config.getBoolean("can_exoskeleton_swing_arms", CATEGORY_GENERAL, false, "Should the exoskeleton swing its arms?");
        exoskeletonHitboxWidth = (float) config.get(CATEGORY_GENERAL, "exoskeleton_hitbox_width", 0.65f, "Exoskeleton hitbox width. [default: 0.65]").getDouble();
        exoskeletonHitboxHeight = (float) config.get(CATEGORY_GENERAL, "exoskeleton_hitbox_height", 2.0f, "Exoskeleton hitbox height. [default: 2.0]").getDouble();
        powerArmorSpeedMultiplier = (float) config.get(CATEGORY_GENERAL, "power_armor_speed_multiplier", 0.85f, "The player's speed multiplier when wearing power armor is multiplied by the player's current speed. [default: 0.85]").getDouble();
        powerArmorKnockbackMultiplier = (float) config.get(CATEGORY_GENERAL, "power_armor_knockback_multiplier", 0.0f, "The discard force multiplier when hitting a player in power armor is multiplied by the current discard force. [default: 0.0]").getDouble();
        powerArmorFallDamageMultiplier = (float) config.get(CATEGORY_GENERAL, "power_armor_fall_damage_multiplier", 0.2f, "Multiplier for fall damage when wearing power armor. [default: 0.2]").getDouble();
        powerArmorFallThreshold = (float) config.get(CATEGORY_GENERAL, "power_armor_fall_threshold", 14.0f, "The fall height threshold before the player starts taking fall damage. [default: 14.0]").getDouble();
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
        if (parts.length != 7) {
            throw new IllegalArgumentException("Invalid armor set configuration: expected 7 comma-separated values.");
        }
        ArmorSet set = new ArmorSet();
        set.helmetProtection = Integer.parseInt(parts[0].trim());
        set.chestplateProtection = Integer.parseInt(parts[1].trim());
        set.leggingsProtection = Integer.parseInt(parts[2].trim());
        set.bootsProtection = Integer.parseInt(parts[3].trim());
        set.durability = Integer.parseInt(parts[4].trim());
        set.enchantability = Integer.parseInt(parts[5].trim());
        set.toughness = Float.parseFloat(parts[6].trim());
        return set;
    }

    /**
     * Класс для хранения характеристик набора брони.
     * Содержит параметры защиты, прочности и зачаровываемости.
     */
    public static class ArmorSet {
        /**
         * Уровень защиты шлема
         */
        public int helmetProtection;
        /**
         * Уровень защиты нагрудника
         */
        public int chestplateProtection;
        /**
         * Уровень защиты поножей
         */
        public int leggingsProtection;
        /**
         * Уровень защиты ботинок
         */
        public int bootsProtection;
        /**
         * Прочность брони
         */
        public int durability;
        /**
         * Уровень зачаровываемости
         */
        public int enchantability;
        /**
         * Жёсткость брони
         */
        public float toughness;
    }
}