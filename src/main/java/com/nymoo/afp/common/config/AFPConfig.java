package com.nymoo.afp.common.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурационный класс мода Atom Fusion Protocol.
 * Управляет настройками силовой брони, экзоскелетов, энергопотребления и генерации в мире.
 * Все настройки разбиты по логическим категориям для удобства.
 * Дефолтные значения хранятся в константах, чтобы изменение в одном месте автоматически обновляло default в config и в описании.
 */
public class AFPConfig {

    /**
     * Карта наборов брони для быстрого доступа по имени.
     */
    private static final Map<String, ArmorSet> armorSets = new HashMap<>();

    // --- Константы дефолтных значений для всех настроек ---

    // Sounds
    private static final boolean DEFAULT_PLAY_SERVO_JUMP_SOUND = true;
    private static final boolean DEFAULT_PLAY_SERVO_STEP_SOUND = true;
    private static final float DEFAULT_SERVO_VOLUME = 0.55f;
    private static final float DEFAULT_FUSION_VOLUME = 1.0f;
    private static final float DEFAULT_ARMOR_VOLUME = 1.0f;

    // Interactions
    private static final boolean DEFAULT_HANDLE_PLAYER_DEATH = true;
    private static final boolean DEFAULT_CAN_DISPENSER_EQUIP_POWER_ARMOR = false;
    private static final boolean DEFAULT_CAN_PLAYER_UNEQUIP_POWER_ARMOR = false;
    private static final boolean DEFAULT_CAN_PLAYER_EQUIP_POWER_ARMOR = false;
    private static final boolean DEFAULT_CAN_EXOSKELETON_SWING_ARMS = false;

    // Hitboxes
    private static final float DEFAULT_EXOSKELETON_HITBOX_WIDTH = 0.65f;
    private static final float DEFAULT_EXOSKELETON_HITBOX_HEIGHT = 2.0f;
    private static final float DEFAULT_EXOSKELETON_BROKEN_HITBOX_WIDTH = 0.75f;
    private static final float DEFAULT_EXOSKELETON_BROKEN_HITBOX_HEIGHT = 0.5f;
    private static final float DEFAULT_POWER_ARMOR_WIDTH = 0.65f;
    private static final float DEFAULT_POWER_ARMOR_HEIGHT_STANDING = 2.0f;
    private static final float DEFAULT_POWER_ARMOR_HEIGHT_SNEAKING = 1.85f;
    private static final float DEFAULT_POWER_ARMOR_HEIGHT_ELYTRA = 0.6f;

    // Physics
    private static final float DEFAULT_POWER_ARMOR_SPEED_MULTIPLIER = 0.85f;
    private static final float DEFAULT_POWER_ARMOR_FALL_DAMAGE_MULTIPLIER = 0.2f;
    private static final float DEFAULT_POWER_ARMOR_FALL_THRESHOLD = 14.0f;
    private static final float DEFAULT_KNOCKBACK_DAMAGE_SCALE = 20.0f;
    private static final float DEFAULT_POWER_ARMOR_DEPLETED_SPEED_MULTIPLIER = 0.1f;
    private static final float DEFAULT_POWER_ARMOR_ROTATION_MULTIPLIER = 0.05f;
    private static final float DEFAULT_POWER_ARMOR_INTERACTION_SPEED = 0.1f;
    private static final float DEFAULT_EXOSKELETON_ENTRY_DISTANCE = 1.0f;
    private static final float DEFAULT_EXOSKELETON_ENTRY_YAW = 55.0f;

    // Energy
    private static final float DEFAULT_MAX_DEPLETION = 288000.0f;
    private static final float DEFAULT_STEP_DELTA_THRESHOLD = 0.06f;
    private static final float DEFAULT_BASE_DEPLETION_RATE = 1.0f;
    private static final float DEFAULT_SPRINT_DEPLETION_ADDER = 0.5f;
    private static final float DEFAULT_JUMP_DEPLETION_ADDER = 0.7f;
    private static final float DEFAULT_WATER_DEPLETION_ADDER = 0.3f;
    private static final float DEFAULT_USE_ITEM_DEPLETION_ADDER = 0.4f;
    private static final float DEFAULT_HURT_DEPLETION_ADDER = 1.0f;
    private static final float DEFAULT_JETPACK_DEPLETION_ADDER = 2.0f;
    private static final float DEFAULT_WALK_DEPLETION_ADDER = 0.2f;

    // Player Mechanics
    private static final int DEFAULT_UNDERWATER_BREATH_TICKS = 24000;
    private static final int DEFAULT_UNDERWATER_BREATH_AIR = 300;
    private static final float DEFAULT_POWER_ARMOR_WATER_JUMP_MOTION = 0.12f;
    private static final float DEFAULT_POWER_ARMOR_BOUNCE_TARGET = 0.45f;
    private static final float DEFAULT_POWER_ARMOR_BOUNCE_ACCELERATION = 0.055f;
    private static final float DEFAULT_POWER_ARMOR_BOUNCE_FRICTION = 0.98f;
    private static final int DEFAULT_POWER_ARMOR_BOUNCE_TIMER = 6;

    // Timings & UI
    private static final float DEFAULT_FUSION_HOLD_TIME = 1.45f;
    private static final float DEFAULT_ARMOR_HOLD_TIME = 6.0f;
    private static final float DEFAULT_FADE_DELAY = -1.0f;
    private static final float DEFAULT_FADE_DURATION_IN = 0.5f;
    private static final float DEFAULT_FADE_HOLD = 0.5f;
    private static final float DEFAULT_FADE_DURATION_OUT = 0.5f;
    private static final float DEFAULT_FUSION_COOLDOWN = 1.0f;

    // World Gen
    private static final float DEFAULT_BROKEN_EXOSKELETON_SPAWN_WEIGHT = 0.05f;
    private static final float DEFAULT_BROKEN_ARMOR_SPAWN_WEIGHT = 10.0f;
    private static final float DEFAULT_PROBABILITY_X03 = 0.33f;
    private static final float DEFAULT_PROBABILITY_X02 = 0.33f;
    private static final float DEFAULT_PROBABILITY_X01 = 0.33f;
    private static final float DEFAULT_PROBABILITY_T60 = 0.33f;
    private static final float DEFAULT_PROBABILITY_T51 = 0.33f;
    private static final float DEFAULT_PROBABILITY_T45 = 0.33f;
    private static final float DEFAULT_PROBABILITY_HELMET = 0.45f;
    private static final float DEFAULT_PROBABILITY_CHESTPLATE = 0.45f;
    private static final float DEFAULT_PROBABILITY_LEGGINGS = 0.45f;
    private static final float DEFAULT_PROBABILITY_BOOTS = 0.45f;

    // --- Настройки (инициализируются в loadFromConfig) ---

    // Sounds
    public static boolean playServoJumpSound;
    public static boolean playServoStepSound;
    public static float servoVolume;
    public static float fusionVolume;
    public static float armorVolume;

    // Interactions
    public static boolean handlePlayerDeath;
    public static boolean canDispenserEquipPowerArmor;
    public static boolean canPlayerUnequipPowerArmor;
    public static boolean canPlayerEquipPowerArmor;
    public static boolean canExoskeletonSwingArms;

    // Hitboxes
    public static float exoskeletonHitboxWidth;
    public static float exoskeletonHitboxHeight;
    public static float exoskeletonBrokenHitboxWidth;
    public static float exoskeletonBrokenHitboxHeight;
    public static float powerArmorWidth;
    public static float powerArmorHeightStanding;
    public static float powerArmorHeightSneaking;
    public static float powerArmorHeightElytra;

    // Physics
    public static float powerArmorSpeedMultiplier;
    public static float powerArmorFallDamageMultiplier;
    public static float powerArmorFallThreshold;
    public static float knockbackDamageScale;
    public static float powerArmorDepletedSpeedMultiplier;
    public static float powerArmorRotationMultiplier;
    public static float powerArmorInteractionSpeed;
    public static float exoskeletonEntryDistance;
    public static float exoskeletonEntryYaw;

    // Energy
    public static float maxDepletion;
    public static float stepDeltaThreshold;
    public static float baseDepletionRate;
    public static float sprintDepletionAdder;
    public static float jumpDepletionAdder;
    public static float waterDepletionAdder;
    public static float useItemDepletionAdder;
    public static float hurtDepletionAdder;
    public static float jetpackDepletionAdder;
    public static float walkDepletionAdder;

    // Player Mechanics
    public static int underwaterBreathTicks;
    public static int underwaterBreathAir;
    public static float powerArmorWaterJumpMotion;
    public static float powerArmorBounceTarget;
    public static float powerArmorBounceAcceleration;
    public static float powerArmorBounceFriction;
    public static int powerArmorBounceTimer;

    // Timings & UI
    public static float fusionHoldTime;
    public static float armorHoldTime;
    public static float fadeDelay;
    public static float fadeDurationIn;
    public static float fadeHold;
    public static float fadeDurationOut;
    public static float fusionCooldown;

    // World Gen
    public static float brokenExoskeletonSpawnWeight;
    public static float brokenArmorSpawnWeight;
    public static float probabilityX03;
    public static float probabilityX02;
    public static float probabilityX01;
    public static float probabilityT60;
    public static float probabilityT51;
    public static float probabilityT45;
    public static float probabilityHelmet;
    public static float probabilityChestplate;
    public static float probabilityLeggings;
    public static float probabilityBoots;

    /**
     * Получает набор характеристик брони по имени.
     * @param name Имя набора брони (в нижнем регистре).
     * @return Объект ArmorSet или null, если набор не найден.
     */
    public static ArmorSet getArmorSet(String name) {
        return armorSets.get(name.toLowerCase());
    }

    /**
     * Загружает конфигурацию из файла и инициализирует все настройки.
     * Дефолтные значения берутся из констант, чтобы избежать дублирования.
     * @param config Объект конфигурации Forge.
     */
    public static void loadFromConfig(Configuration config) {
        final String CATEGORY_SETTINGS = "settings";
        final String CATEGORY_POWER_ARMOR = CATEGORY_SETTINGS + ".power_armor";
        final String CATEGORY_SOUNDS = CATEGORY_SETTINGS + ".sounds";
        final String CATEGORY_INTERACTIONS = CATEGORY_SETTINGS + ".interactions";
        final String CATEGORY_HITBOXES = CATEGORY_SETTINGS + ".hitboxes";
        final String CATEGORY_PHYSICS = CATEGORY_SETTINGS + ".physics";
        final String CATEGORY_ENERGY = CATEGORY_SETTINGS + ".energy";
        final String CATEGORY_PLAYER_MECHANICS = CATEGORY_SETTINGS + ".player_mechanics";
        final String CATEGORY_TIMINGS_UI = CATEGORY_SETTINGS + ".timings_ui";
        final String CATEGORY_WORLD_GEN = CATEGORY_SETTINGS + ".world_gen";

        config.setCategoryComment(CATEGORY_SETTINGS, "Для применения этих настроек требуется перезапуск Minecraft. Убедитесь, что параметры указаны в правильном формате.");

        // Комментарии для подкатегорий
        config.setCategoryComment(CATEGORY_POWER_ARMOR, "Настройки характеристик наборов силовой брони. Формат свойств: helmet_protection, chestplate_protection, leggings_protection, boots_protection, durability, enchantability, toughness, knockback_multiplier.");
        config.setCategoryComment(CATEGORY_SOUNDS, "Настройки звуковых эффектов для силовой брони и экзоскелетов.");
        config.setCategoryComment(CATEGORY_INTERACTIONS, "Настройки взаимодействия с бронёй и экзоскелетами.");
        config.setCategoryComment(CATEGORY_HITBOXES, "Настройки размеров хитбоксов для экзоскелетов и брони.");
        config.setCategoryComment(CATEGORY_PHYSICS, "Настройки физики движения, урона и скорости в броне.");
        config.setCategoryComment(CATEGORY_ENERGY, "Настройки энергопотребления и истощения энергии в броне.");
        config.setCategoryComment(CATEGORY_PLAYER_MECHANICS, "Дополнительные механики для игроков в броне (дыхание под водой, отскок и т.д.).");
        config.setCategoryComment(CATEGORY_TIMINGS_UI, "Настройки таймингов взаимодействия и эффектов UI (затухание, задержки).");
        config.setCategoryComment(CATEGORY_WORLD_GEN, "Настройки генерации сломанных экзоскелетов и брони в мире.");

        // Порядок свойств в GUI для каждой категории
        config.setCategoryPropertyOrder(CATEGORY_POWER_ARMOR, Arrays.asList("x03", "x02", "x01", "t60", "t51", "t45", "exo"));
        config.setCategoryPropertyOrder(CATEGORY_SOUNDS, Arrays.asList(
                "play_servo_jump_sound", "play_servo_step_sound", "servo_volume", "fusion_volume", "armor_volume"
        ));
        config.setCategoryPropertyOrder(CATEGORY_INTERACTIONS, Arrays.asList(
                "handle_player_death", "can_dispenser_equip_power_armor", "can_player_unequip_power_armor",
                "can_player_equip_power_armor", "can_exoskeleton_swing_arms"
        ));
        config.setCategoryPropertyOrder(CATEGORY_HITBOXES, Arrays.asList(
                "exoskeleton_hitbox_width", "exoskeleton_hitbox_height", "broken_armor_piece_hitbox_width",
                "broken_armor_piece_hitbox_height", "power_armor_width", "power_armor_height_standing",
                "power_armor_height_sneaking", "power_armor_height_elytra"
        ));
        config.setCategoryPropertyOrder(CATEGORY_PHYSICS, Arrays.asList(
                "power_armor_speed_multiplier", "power_armor_fall_damage_multiplier", "power_armor_fall_threshold",
                "power_armor_knockback_damage_scale", "power_armor_depleted_speed_multiplier",
                "power_armor_rotation_multiplier", "power_armor_interaction_speed",
                "exoskeleton_entry_distance", "exoskeleton_entry_yaw"
        ));
        config.setCategoryPropertyOrder(CATEGORY_ENERGY, Arrays.asList(
                "max_depletion", "step_delta_threshold", "base_depletion_rate", "sprint_depletion_adder",
                "jump_depletion_adder", "water_depletion_adder", "use_item_depletion_adder",
                "hurt_depletion_adder", "jetpack_depletion_adder", "walk_depletion_adder"
        ));
        config.setCategoryPropertyOrder(CATEGORY_PLAYER_MECHANICS, Arrays.asList(
                "underwater_breath_ticks", "underwater_breath_air", "power_armor_water_jump_motion",
                "power_armor_bounce_target", "power_armor_bounce_acceleration", "power_armor_bounce_friction",
                "power_armor_bounce_timer"
        ));
        config.setCategoryPropertyOrder(CATEGORY_TIMINGS_UI, Arrays.asList(
                "fusion_hold_time", "armor_hold_time", "fade_delay", "fade_duration_in",
                "fade_hold", "fade_duration_out", "fusion_cooldown"
        ));
        config.setCategoryPropertyOrder(CATEGORY_WORLD_GEN, Arrays.asList(
                "broken_exoskeleton_spawn_weight", "broken_armor_spawn_weight",
                "probability_x03", "probability_x02", "probability_x01",
                "probability_t60", "probability_t51", "probability_t45",
                "probability_helmet", "probability_chestplate", "probability_leggings", "probability_boots"
        ));

        // --- Загрузка характеристик наборов брони (Power Armor) ---
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "x03", "3, 7, 5, 2, 350, 12, 3.2, 0.05");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "x02", "2, 8, 6, 3, 420, 8, 2.7, 0.10");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "x01", "4, 9, 4, 1, 280, 15, 4.1, 0.15");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "t60", "3, 6, 5, 2, 380, 9, 3.8, 0.20");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "t51", "2, 7, 4, 3, 320, 11, 2.9, 0.25");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "t45", "4, 8, 6, 2, 450, 7, 3.5, 0.30");
        loadArmorSet(config, CATEGORY_POWER_ARMOR, "exo", "3, 9, 5, 1, 500, 14, 4.3, 0.40");

        // --- Загрузка настроек звуков (Sounds) ---
        playServoJumpSound = config.getBoolean("play_servo_jump_sound", CATEGORY_SOUNDS, DEFAULT_PLAY_SERVO_JUMP_SOUND, "Воспроизводить ли звук сервопривода при прыжке в силовой броне? [по умолчанию: " + DEFAULT_PLAY_SERVO_JUMP_SOUND + "]");
        playServoStepSound = config.getBoolean("play_servo_step_sound", CATEGORY_SOUNDS, DEFAULT_PLAY_SERVO_STEP_SOUND, "Воспроизводить ли звук сервопривода при ходьбе в силовой броне? [по умолчанию: " + DEFAULT_PLAY_SERVO_STEP_SOUND + "]");
        servoVolume = (float) config.get(CATEGORY_SOUNDS, "servo_volume", DEFAULT_SERVO_VOLUME, "Громкость звуков сервопривода. [по умолчанию: " + DEFAULT_SERVO_VOLUME + "]").getDouble();
        fusionVolume = (float) config.get(CATEGORY_SOUNDS, "fusion_volume", DEFAULT_FUSION_VOLUME, "Громкость звуков взаимодействия с ядерным синтезом. [по умолчанию: " + DEFAULT_FUSION_VOLUME + "]").getDouble();
        armorVolume = (float) config.get(CATEGORY_SOUNDS, "armor_volume", DEFAULT_ARMOR_VOLUME, "Громкость звуков входа/выхода из силовой брони. [по умолчанию: " + DEFAULT_ARMOR_VOLUME + "]").getDouble();

        // --- Загрузка настроек взаимодействия (Interactions) ---
        handlePlayerDeath = config.getBoolean("handle_player_death", CATEGORY_INTERACTIONS, DEFAULT_HANDLE_PLAYER_DEATH, "Спавнить ли экзоскелет на месте смерти игрока в силовой броне? [по умолчанию: " + DEFAULT_HANDLE_PLAYER_DEATH + "]");
        canDispenserEquipPowerArmor = config.getBoolean("can_dispenser_equip_power_armor", CATEGORY_INTERACTIONS, DEFAULT_CAN_DISPENSER_EQUIP_POWER_ARMOR, "Может ли раздатчик надевать силовую броню на игрока? [по умолчанию: " + DEFAULT_CAN_DISPENSER_EQUIP_POWER_ARMOR + "]");
        canPlayerUnequipPowerArmor = config.getBoolean("can_player_unequip_power_armor", CATEGORY_INTERACTIONS, DEFAULT_CAN_PLAYER_UNEQUIP_POWER_ARMOR, "Может ли игрок вручную снимать силовую броню? [по умолчанию: " + DEFAULT_CAN_PLAYER_UNEQUIP_POWER_ARMOR + "]");
        canPlayerEquipPowerArmor = config.getBoolean("can_player_equip_power_armor", CATEGORY_INTERACTIONS, DEFAULT_CAN_PLAYER_EQUIP_POWER_ARMOR, "Может ли игрок вручную надевать силовую броню? [по умолчанию: " + DEFAULT_CAN_PLAYER_EQUIP_POWER_ARMOR + "]");
        canExoskeletonSwingArms = config.getBoolean("can_exoskeleton_swing_arms", CATEGORY_INTERACTIONS, DEFAULT_CAN_EXOSKELETON_SWING_ARMS, "Должен ли экзоскелет махать руками? [по умолчанию: " + DEFAULT_CAN_EXOSKELETON_SWING_ARMS + "]");

        // --- Загрузка настроек хитбоксов (Hitboxes) ---
        exoskeletonHitboxWidth = (float) config.get(CATEGORY_HITBOXES, "exoskeleton_hitbox_width", DEFAULT_EXOSKELETON_HITBOX_WIDTH, "Ширина хитбокса экзоскелета. [по умолчанию: " + DEFAULT_EXOSKELETON_HITBOX_WIDTH + "]").getDouble();
        exoskeletonHitboxHeight = (float) config.get(CATEGORY_HITBOXES, "exoskeleton_hitbox_height", DEFAULT_EXOSKELETON_HITBOX_HEIGHT, "Высота хитбокса экзоскелета. [по умолчанию: " + DEFAULT_EXOSKELETON_HITBOX_HEIGHT + "]").getDouble();
        exoskeletonBrokenHitboxWidth = (float) config.get(CATEGORY_HITBOXES, "broken_armor_piece_hitbox_width", DEFAULT_EXOSKELETON_BROKEN_HITBOX_WIDTH, "Ширина хитбокса сломанной части брони. [по умолчанию: " + DEFAULT_EXOSKELETON_BROKEN_HITBOX_WIDTH + "]").getDouble();
        exoskeletonBrokenHitboxHeight = (float) config.get(CATEGORY_HITBOXES, "broken_armor_piece_hitbox_height", DEFAULT_EXOSKELETON_BROKEN_HITBOX_HEIGHT, "Высота хитбокса сломанной части брони. [по умолчанию: " + DEFAULT_EXOSKELETON_BROKEN_HITBOX_HEIGHT + "]").getDouble();
        powerArmorWidth = (float) config.get(CATEGORY_HITBOXES, "power_armor_width", DEFAULT_POWER_ARMOR_WIDTH, "Ширина игрока в силовой броне. [по умолчанию: " + DEFAULT_POWER_ARMOR_WIDTH + "]").getDouble();
        powerArmorHeightStanding = (float) config.get(CATEGORY_HITBOXES, "power_armor_height_standing", DEFAULT_POWER_ARMOR_HEIGHT_STANDING, "Высота игрока стоя в силовой броне. [по умолчанию: " + DEFAULT_POWER_ARMOR_HEIGHT_STANDING + "]").getDouble();
        powerArmorHeightSneaking = (float) config.get(CATEGORY_HITBOXES, "power_armor_height_sneaking", DEFAULT_POWER_ARMOR_HEIGHT_SNEAKING, "Высота игрока в приседании в силовой броне. [по умолчанию: " + DEFAULT_POWER_ARMOR_HEIGHT_SNEAKING + "]").getDouble();
        powerArmorHeightElytra = (float) config.get(CATEGORY_HITBOXES, "power_armor_height_elytra", DEFAULT_POWER_ARMOR_HEIGHT_ELYTRA, "Высота игрока в полёте с элитрами в силовой броне. [по умолчанию: " + DEFAULT_POWER_ARMOR_HEIGHT_ELYTRA + "]").getDouble();

        // --- Загрузка настроек физики (Physics) ---
        powerArmorSpeedMultiplier = (float) config.get(CATEGORY_PHYSICS, "power_armor_speed_multiplier", DEFAULT_POWER_ARMOR_SPEED_MULTIPLIER, "Множитель скорости игрока в силовой броне. [по умолчанию: " + DEFAULT_POWER_ARMOR_SPEED_MULTIPLIER + "]").getDouble();
        powerArmorFallDamageMultiplier = (float) config.get(CATEGORY_PHYSICS, "power_armor_fall_damage_multiplier", DEFAULT_POWER_ARMOR_FALL_DAMAGE_MULTIPLIER, "Множитель урона от падения в силовой броне. [по умолчанию: " + DEFAULT_POWER_ARMOR_FALL_DAMAGE_MULTIPLIER + "]").getDouble();
        powerArmorFallThreshold = (float) config.get(CATEGORY_PHYSICS, "power_armor_fall_threshold", DEFAULT_POWER_ARMOR_FALL_THRESHOLD, "Порог высоты падения для начала урона в силовой броне. [по умолчанию: " + DEFAULT_POWER_ARMOR_FALL_THRESHOLD + "]").getDouble();
        knockbackDamageScale = (float) config.get(CATEGORY_PHYSICS, "power_armor_knockback_damage_scale", DEFAULT_KNOCKBACK_DAMAGE_SCALE, "Масштаб урона для расчёта отдачи в силовой броне. Большие значения уменьшают чувствительность отдачи. [по умолчанию: " + DEFAULT_KNOCKBACK_DAMAGE_SCALE + "]").getDouble();
        powerArmorDepletedSpeedMultiplier = (float) config.get(CATEGORY_PHYSICS, "power_armor_depleted_speed_multiplier", DEFAULT_POWER_ARMOR_DEPLETED_SPEED_MULTIPLIER, "Множитель скорости при полностью истощённом ядре синтеза. [по умолчанию: " + DEFAULT_POWER_ARMOR_DEPLETED_SPEED_MULTIPLIER + "]").getDouble();
        powerArmorRotationMultiplier = (float) config.get(CATEGORY_PHYSICS, "power_armor_rotation_multiplier", DEFAULT_POWER_ARMOR_ROTATION_MULTIPLIER, "Множитель замедления поворота (yaw/pitch) при взаимодействии с бронёй или истощённом ядре. [по умолчанию: " + DEFAULT_POWER_ARMOR_ROTATION_MULTIPLIER + "]").getDouble();
        powerArmorInteractionSpeed = (float) config.get(CATEGORY_PHYSICS, "power_armor_interaction_speed", DEFAULT_POWER_ARMOR_INTERACTION_SPEED, "Множитель скорости движения при удержании клавиши взаимодействия для входа/выхода. [по умолчанию: " + DEFAULT_POWER_ARMOR_INTERACTION_SPEED + "]").getDouble();
        exoskeletonEntryDistance = (float) config.get(CATEGORY_PHYSICS, "exoskeleton_entry_distance", DEFAULT_EXOSKELETON_ENTRY_DISTANCE, "Квадрат порогового расстояния для входа в экзоскелет. [по умолчанию: " + DEFAULT_EXOSKELETON_ENTRY_DISTANCE + "]").getDouble();
        exoskeletonEntryYaw = (float) config.get(CATEGORY_PHYSICS, "exoskeleton_entry_yaw", DEFAULT_EXOSKELETON_ENTRY_YAW, "Максимальная разница в yaw (градусы) между взглядом игрока и экзоскелетом для входа/выхода. [по умолчанию: " + DEFAULT_EXOSKELETON_ENTRY_YAW + "]").getDouble();

        // --- Загрузка настроек энергопотребления (Energy) ---
        maxDepletion = (float) config.get(CATEGORY_ENERGY, "max_depletion", DEFAULT_MAX_DEPLETION, "Максимальное значение истощения энергии для силовой брони. [по умолчанию: " + DEFAULT_MAX_DEPLETION + "]").getDouble();
        stepDeltaThreshold = (float) config.get(CATEGORY_ENERGY, "step_delta_threshold", DEFAULT_STEP_DELTA_THRESHOLD, "Порог обнаружения горизонтального движения для расчёта истощения. [по умолчанию: " + DEFAULT_STEP_DELTA_THRESHOLD + "]").getDouble();
        baseDepletionRate = (float) config.get(CATEGORY_ENERGY, "base_depletion_rate", DEFAULT_BASE_DEPLETION_RATE, "Базовая скорость истощения энергии в силовой броне. [по умолчанию: " + DEFAULT_BASE_DEPLETION_RATE + "]").getDouble();
        sprintDepletionAdder = (float) config.get(CATEGORY_ENERGY, "sprint_depletion_adder", DEFAULT_SPRINT_DEPLETION_ADDER, "Дополнительное истощение при спринте в силовой броне. [по умолчанию: " + DEFAULT_SPRINT_DEPLETION_ADDER + "]").getDouble();
        jumpDepletionAdder = (float) config.get(CATEGORY_ENERGY, "jump_depletion_adder", DEFAULT_JUMP_DEPLETION_ADDER, "Дополнительное истощение при прыжке без джетпака в силовой броне. [по умолчанию: " + DEFAULT_JUMP_DEPLETION_ADDER + "]").getDouble();
        waterDepletionAdder = (float) config.get(CATEGORY_ENERGY, "water_depletion_adder", DEFAULT_WATER_DEPLETION_ADDER, "Дополнительное истощение в воде в силовой броне. [по умолчанию: " + DEFAULT_WATER_DEPLETION_ADDER + "]").getDouble();
        useItemDepletionAdder = (float) config.get(CATEGORY_ENERGY, "use_item_depletion_adder", DEFAULT_USE_ITEM_DEPLETION_ADDER, "Дополнительное истощение при использовании предмета или замахе в силовой броне. [по умолчанию: " + DEFAULT_USE_ITEM_DEPLETION_ADDER + "]").getDouble();
        hurtDepletionAdder = (float) config.get(CATEGORY_ENERGY, "hurt_depletion_adder", DEFAULT_HURT_DEPLETION_ADDER, "Дополнительное истощение при получении урона в силовой броне. [по умолчанию: " + DEFAULT_HURT_DEPLETION_ADDER + "]").getDouble();
        jetpackDepletionAdder = (float) config.get(CATEGORY_ENERGY, "jetpack_depletion_adder", DEFAULT_JETPACK_DEPLETION_ADDER, "Дополнительное истощение при использовании джетпака в силовой броне. [по умолчанию: " + DEFAULT_JETPACK_DEPLETION_ADDER + "]").getDouble();
        walkDepletionAdder = (float) config.get(CATEGORY_ENERGY, "walk_depletion_adder", DEFAULT_WALK_DEPLETION_ADDER, "Дополнительное истощение при ходьбе (не спринт) в силовой броне. [по умолчанию: " + DEFAULT_WALK_DEPLETION_ADDER + "]").getDouble();

        // --- Загрузка настроек механик игрока (Player Mechanics) ---
        underwaterBreathTicks = config.getInt("underwater_breath_ticks", CATEGORY_PLAYER_MECHANICS, DEFAULT_UNDERWATER_BREATH_TICKS, 0, Integer.MAX_VALUE, "Количество тиков дыхания под водой в силовой броне до начала удушья. [по умолчанию: " + DEFAULT_UNDERWATER_BREATH_TICKS + "]");
        underwaterBreathAir = config.getInt("underwater_breath_air", CATEGORY_PLAYER_MECHANICS, DEFAULT_UNDERWATER_BREATH_AIR, 0, Integer.MAX_VALUE, "Значение воздуха, восстанавливаемое каждый тик под водой в силовой броне. [по умолчанию: " + DEFAULT_UNDERWATER_BREATH_AIR + "]");
        powerArmorWaterJumpMotion = (float) config.get(CATEGORY_PLAYER_MECHANICS, "power_armor_water_jump_motion", DEFAULT_POWER_ARMOR_WATER_JUMP_MOTION, "Базовое восходящее движение при прыжке из воды/лавы в силовой броне. [по умолчанию: " + DEFAULT_POWER_ARMOR_WATER_JUMP_MOTION + "]").getDouble();
        powerArmorBounceTarget = (float) config.get(CATEGORY_PLAYER_MECHANICS, "power_armor_bounce_target", DEFAULT_POWER_ARMOR_BOUNCE_TARGET, "Целевая восходящая скорость для отскока от воды в силовой броне. [по умолчанию: " + DEFAULT_POWER_ARMOR_BOUNCE_TARGET + "]").getDouble();
        powerArmorBounceAcceleration = (float) config.get(CATEGORY_PLAYER_MECHANICS, "power_armor_bounce_acceleration", DEFAULT_POWER_ARMOR_BOUNCE_ACCELERATION, "Ускорение, добавляемое каждый тик к скорости отскока от воды. [по умолчанию: " + DEFAULT_POWER_ARMOR_BOUNCE_ACCELERATION + "]").getDouble();
        powerArmorBounceFriction = (float) config.get(CATEGORY_PLAYER_MECHANICS, "power_armor_bounce_friction", DEFAULT_POWER_ARMOR_BOUNCE_FRICTION, "Горизонтальное трение во время отскока от воды в силовой броне. [по умолчанию: " + DEFAULT_POWER_ARMOR_BOUNCE_FRICTION + "]").getDouble();
        powerArmorBounceTimer = config.getInt("power_armor_bounce_timer", CATEGORY_PLAYER_MECHANICS, DEFAULT_POWER_ARMOR_BOUNCE_TIMER, 0, Integer.MAX_VALUE, "Продолжительность эффекта отскока от воды в тиках. [по умолчанию: " + DEFAULT_POWER_ARMOR_BOUNCE_TIMER + "]");

        // --- Загрузка настроек таймингов и UI (Timings & UI) ---
        fusionHoldTime = (float) config.get(CATEGORY_TIMINGS_UI, "fusion_hold_time", DEFAULT_FUSION_HOLD_TIME, "Время удержания (секунды) при вставке/извлечении ядра синтеза. [по умолчанию: " + DEFAULT_FUSION_HOLD_TIME + "]").getDouble();
        armorHoldTime = (float) config.get(CATEGORY_TIMINGS_UI, "armor_hold_time", DEFAULT_ARMOR_HOLD_TIME, "Время удержания (секунды) при входе/выходе из силовой брони/экзоскелета. [по умолчанию: " + DEFAULT_ARMOR_HOLD_TIME + "]").getDouble();
        fadeDelay = (float) config.get(CATEGORY_TIMINGS_UI, "fade_delay", DEFAULT_FADE_DELAY, "Задержка перед началом эффекта затухания после входа/выхода. Отрицательное значение отключает. [по умолчанию: " + DEFAULT_FADE_DELAY + "]").getDouble();
        fadeDurationIn = (float) config.get(CATEGORY_TIMINGS_UI, "fade_duration_in", DEFAULT_FADE_DURATION_IN, "Продолжительность затухания в (fade-in) при входе/выходе. [по умолчанию: " + DEFAULT_FADE_DURATION_IN + "]").getDouble();
        fadeHold = (float) config.get(CATEGORY_TIMINGS_UI, "fade_hold", DEFAULT_FADE_HOLD, "Время удержания затухания между fade-in и fade-out. [по умолчанию: " + DEFAULT_FADE_HOLD + "]").getDouble();
        fadeDurationOut = (float) config.get(CATEGORY_TIMINGS_UI, "fade_duration_out", DEFAULT_FADE_DURATION_OUT, "Продолжительность затухания из (fade-out) при входе/выходе. [по умолчанию: " + DEFAULT_FADE_DURATION_OUT + "]").getDouble();
        fusionCooldown = (float) config.get(CATEGORY_TIMINGS_UI, "fusion_cooldown", DEFAULT_FUSION_COOLDOWN, "Время перезарядки (секунды) между взаимодействиями с ядром синтеза. [по умолчанию: " + DEFAULT_FUSION_COOLDOWN + "]").getDouble();

        // --- Загрузка настроек спавна в мире (World Gen) ---
        brokenExoskeletonSpawnWeight = (float) config.get(CATEGORY_WORLD_GEN, "broken_exoskeleton_spawn_weight", DEFAULT_BROKEN_EXOSKELETON_SPAWN_WEIGHT, "Вес вероятности спавна сломанного экзоскелета (выше = чаще). 0 отключает спавн. [по умолчанию: " + DEFAULT_BROKEN_EXOSKELETON_SPAWN_WEIGHT + "]").getDouble();
        brokenArmorSpawnWeight = (float) config.get(CATEGORY_WORLD_GEN, "broken_armor_spawn_weight", DEFAULT_BROKEN_ARMOR_SPAWN_WEIGHT, "Вес вероятности спавна сломанной брони (выше = чаще). 0 отключает спавн. [по умолчанию: " + DEFAULT_BROKEN_ARMOR_SPAWN_WEIGHT + "]").getDouble();
        probabilityX03 = (float) config.get(CATEGORY_WORLD_GEN, "probability_x03", DEFAULT_PROBABILITY_X03, "Вероятность (0.0 - 1.0) наличия части X03 при спавне сломанного экзоскелета. [по умолчанию: " + DEFAULT_PROBABILITY_X03 + "]").getDouble();
        probabilityX02 = (float) config.get(CATEGORY_WORLD_GEN, "probability_x02", DEFAULT_PROBABILITY_X02, "Вероятность (0.0 - 1.0) наличия части X02 при спавне сломанного экзоскелета. [по умолчанию: " + DEFAULT_PROBABILITY_X02 + "]").getDouble();
        probabilityX01 = (float) config.get(CATEGORY_WORLD_GEN, "probability_x01", DEFAULT_PROBABILITY_X01, "Вероятность (0.0 - 1.0) наличия части X01 при спавне сломанного экзоскелета. [по умолчанию: " + DEFAULT_PROBABILITY_X01 + "]").getDouble();
        probabilityT60 = (float) config.get(CATEGORY_WORLD_GEN, "probability_t60", DEFAULT_PROBABILITY_T60, "Вероятность (0.0 - 1.0) наличия части T60 при спавне сломанного экзоскелета. [по умолчанию: " + DEFAULT_PROBABILITY_T60 + "]").getDouble();
        probabilityT51 = (float) config.get(CATEGORY_WORLD_GEN, "probability_t51", DEFAULT_PROBABILITY_T51, "Вероятность (0.0 - 1.0) наличия части T51 при спавне сломанного экзоскелета. [по умолчанию: " + DEFAULT_PROBABILITY_T51 + "]").getDouble();
        probabilityT45 = (float) config.get(CATEGORY_WORLD_GEN, "probability_t45", DEFAULT_PROBABILITY_T45, "Вероятность (0.0 - 1.0) наличия части T45 при спавне сломанного экзоскелета. [по умолчанию: " + DEFAULT_PROBABILITY_T45 + "]").getDouble();
        probabilityHelmet = (float) config.get(CATEGORY_WORLD_GEN, "probability_helmet", DEFAULT_PROBABILITY_HELMET, "Вероятность (0.0 - 1.0) наличия шлема при спавне сломанного экзоскелета. [по умолчанию: " + DEFAULT_PROBABILITY_HELMET + "]").getDouble();
        probabilityChestplate = (float) config.get(CATEGORY_WORLD_GEN, "probability_chestplate", DEFAULT_PROBABILITY_CHESTPLATE, "Вероятность (0.0 - 1.0) наличия нагрудника при спавне сломанного экзоскелета. [по умолчанию: " + DEFAULT_PROBABILITY_CHESTPLATE + "]").getDouble();
        probabilityLeggings = (float) config.get(CATEGORY_WORLD_GEN, "probability_leggings", DEFAULT_PROBABILITY_LEGGINGS, "Вероятность (0.0 - 1.0) наличия поножей при спавне сломанного экзоскелета. [по умолчанию: " + DEFAULT_PROBABILITY_LEGGINGS + "]").getDouble();
        probabilityBoots = (float) config.get(CATEGORY_WORLD_GEN, "probability_boots", DEFAULT_PROBABILITY_BOOTS, "Вероятность (0.0 - 1.0) наличия ботинок при спавне сломанного экзоскелета. [по умолчанию: " + DEFAULT_PROBABILITY_BOOTS + "]").getDouble();
    }

    /**
     * Загружает характеристики набора брони из конфигурации.
     * @param config Объект конфигурации.
     * @param category Категория в конфиге.
     * @param armorName Имя набора брони.
     * @param defaultValue Дефолтная строка характеристик.
     */
    private static void loadArmorSet(Configuration config, String category, String armorName, String defaultValue) {
        String description = "Конфигурация силовой брони " + armorName.toUpperCase() + ". [по умолчанию: " + defaultValue + "]";
        Property property = config.get(category, armorName, defaultValue, description);
        ArmorSet armorSet = parseArmorSet(property.getString());
        armorSets.put(armorName, armorSet);
    }

    /**
     * Парсит строку с характеристиками брони в объект ArmorSet.
     * @param value Строка с значениями, разделёнными запятыми.
     * @return Объект ArmorSet.
     * @throws IllegalArgumentException Если формат неверный.
     */
    private static ArmorSet parseArmorSet(String value) {
        String[] parts = value.split(",");
        if (parts.length != 8) {
            throw new IllegalArgumentException("Неверный формат конфигурации набора брони: ожидается 8 значений, разделённых запятыми.");
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
     */
    public static class ArmorSet {
        public int helmetProtection;
        public int chestplateProtection;
        public int leggingsProtection;
        public int bootsProtection;
        public int durability;
        public int enchantability;
        public float toughness;
        public float knockbackMultiplier;
    }
}