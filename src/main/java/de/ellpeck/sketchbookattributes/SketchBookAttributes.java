package de.ellpeck.sketchbookattributes;

import de.ellpeck.sketchbookattributes.StaffItem.Mode;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(SketchBookAttributes.ID)
public class SketchBookAttributes {

    public static final String ID = "sketchbookattributes";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, ID);

    public static final RegistryObject<StaffItem> STAFF_TIER_1 = ITEMS.register("staff_tier_1",
            () -> new StaffItem(Mode.FIRE_BALL));
    public static final RegistryObject<StaffItem> STAFF_TIER_2 = ITEMS.register("staff_tier_2",
            () -> new StaffItem(Mode.FIRE_BALL, Mode.JUMP, Mode.ICE_BALL));
    public static final RegistryObject<StaffItem> STAFF_TIER_3 = ITEMS.register("staff_tier_3",
            () -> new StaffItem(Mode.FIRE_BALL, Mode.JUMP, Mode.ICE_BALL, Mode.HEAL, Mode.SPEED));
    public static final RegistryObject<StaffItem> STAFF_MASTER = ITEMS.register("staff_master",
            () -> new StaffItem(Mode.FIRE_BALL, Mode.JUMP, Mode.ICE_BALL, Mode.HEAL, Mode.SPEED, Mode.STRENGTH, Mode.METEORS));

    public static final RegistryObject<EntityType<IceBallEntity>> ICE_BALL = ENTITIES.register("ice_ball",
            () -> EntityType.Builder.<IceBallEntity>of(IceBallEntity::new, EntityClassification.MISC).build("ice_ball"));

    public static ConfigValue<Double> xpNeededMultiplier;
    public static ConfigValue<Double> healthRegenPerLevel;
    public static ConfigValue<Double> manaRegenPerLevel;
    public static ConfigValue<Double> meleeDamagePerLevel;
    public static ConfigValue<Double> rangedDamagePerLevel;
    public static ConfigValue<Double> healthBonusPerLevel;
    public static ConfigValue<Double> manaBonusPerLevel;
    public static ConfigValue<Double> meleeSpeedPerLevel;
    public static ConfigValue<Double> movementSpeedPerLevel;

    public SketchBookAttributes() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(Registry::setup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> bus.addListener(Registry.Client::setup));
        ITEMS.register(bus);
        ENTITIES.register(bus);

        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        xpNeededMultiplier = configBuilder
                .comment("A modifier that is mulitplied with the amount of XP needed for a given level")
                .define("xp_needed_multiplier", 1D);
        healthRegenPerLevel = configBuilder
                .comment("The amount of health regen that an additional level of the ability adds per second")
                .define("health_regen_per_level", 0.04);
        manaRegenPerLevel = configBuilder
                .comment("The amount of mana regen that an additional level of the ability adds per second")
                .define("mana_regen_per_level", 0.04);
        meleeDamagePerLevel = configBuilder
                .comment("The amount of melee damage that an addditional level of the ability adds")
                .define("melee_damage_per_level", 0.25);
        rangedDamagePerLevel = configBuilder
                .comment("The amount of ranged damage that an addditional level of the ability adds")
                .define("ranged_damage_per_level", 0.15);
        healthBonusPerLevel = configBuilder
                .comment("The amount of additional health that an addditional level of the ability adds")
                .define("health_bonus_per_level", 1D);
        manaBonusPerLevel = configBuilder
                .comment("The amount of additional mana that an addditional level of the ability adds")
                .define("mana_bonus_per_level", 1.5D);
        meleeSpeedPerLevel = configBuilder
                .comment("The amount of melee speed that an addditional level of the ability adds")
                .define("melee_speed_per_level", 0.05);
        movementSpeedPerLevel = configBuilder
                .comment("The amount that movement speed is increased for each addditional level of the ability")
                .define("movement_speed_per_level", 0.0025);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, configBuilder.build());
    }

}