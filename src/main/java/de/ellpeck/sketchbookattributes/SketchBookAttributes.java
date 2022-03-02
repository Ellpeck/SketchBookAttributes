package de.ellpeck.sketchbookattributes;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SketchBookAttributes.ID)
public class SketchBookAttributes {

    public static final String ID = "sketchbookattributes";
    @CapabilityInject(AttributeData.class)
    public static final Capability<AttributeData> ATTRIBUTE_DATA_CAPABILITY = null;

    public static ConfigValue<Float> xpNeededMultiplier;
    public static ConfigValue<Float> healthRegenPerLevel;
    public static ConfigValue<Float> manaRegenPerLevel;
    public static ConfigValue<Float> meleeDamagePerLevel;
    public static ConfigValue<Float> rangedDamagePerLevel;
    public static ConfigValue<Float> healthBonusPerLevel;
    public static ConfigValue<Float> meleeSpeedPerLevel;
    public static ConfigValue<Float> rangedSpeedPerLevel;
    public static ConfigValue<Float> movementSpeedPerLevel;

    public SketchBookAttributes() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(Registry::setup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> bus.addListener(Registry.Client::setup));

        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        xpNeededMultiplier = configBuilder
                .comment("A modifier that is mulitplied with the amount of XP needed for a given level")
                .define("xp_needed_multiplier", 1F);
        healthRegenPerLevel = configBuilder
                .comment("The amount of health regen that an additional level of the ability adds per second")
                .define("health_regen_per_level", 0.02F);
        manaRegenPerLevel = configBuilder
                .comment("The amount of mana regen that an additional level of the ability adds per second")
                .define("mana_regen_per_level", 0.02F);
        meleeDamagePerLevel = configBuilder
                .comment("The amount of melee damage that an addditional level of the ability adds")
                .define("melee_damage_per_level", 0.25F);
        rangedDamagePerLevel = configBuilder
                .comment("The amount of ranged damage that an addditional level of the ability adds")
                .define("ranged_damage_per_level", 0.25F);
        healthBonusPerLevel = configBuilder
                .comment("The amount of additional health that an addditional level of the ability adds")
                .define("health_bonus_per_level", 1F);
        meleeSpeedPerLevel = configBuilder
                .comment("The amount of melee speed that an addditional level of the ability adds")
                .define("melee_speed_per_level", 0.1F);
        rangedSpeedPerLevel = configBuilder
                .comment("The amount of ranged speed that an addditional level of the ability adds")
                .define("ranged_speed_per_level", 0.1F);
        movementSpeedPerLevel = configBuilder
                .comment("The amount that movement speed is increased for each addditional level of the ability")
                .define("movement_speed_per_level", 0.1F);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, configBuilder.build());
    }

}