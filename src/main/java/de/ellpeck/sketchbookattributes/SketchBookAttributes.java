package de.ellpeck.sketchbookattributes;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SketchBookAttributes.ID)
public class SketchBookAttributes {

    public static final String ID = "sketchbookattributes";

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

        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        xpNeededMultiplier = configBuilder
                .comment("A modifier that is mulitplied with the amount of XP needed for a given level")
                .define("xp_needed_multiplier", 1D);
        healthRegenPerLevel = configBuilder
                .comment("The amount of health regen that an additional level of the ability adds per second")
                .define("health_regen_per_level", 0.04);
        manaRegenPerLevel = configBuilder
                .comment("The amount of mana regen that an additional level of the ability adds per second")
                .define("mana_regen_per_level", 0.02);
        meleeDamagePerLevel = configBuilder
                .comment("The amount of melee damage that an addditional level of the ability adds")
                .define("melee_damage_per_level", 0.25);
        rangedDamagePerLevel = configBuilder
                .comment("The amount of ranged damage that an addditional level of the ability adds")
                .define("ranged_damage_per_level", 0.25);
        healthBonusPerLevel = configBuilder
                .comment("The amount of additional health that an addditional level of the ability adds")
                .define("health_bonus_per_level", 1D);
        manaBonusPerLevel = configBuilder
                .comment("The amount of additional mana that an addditional level of the ability adds")
                .define("mana_bonus_per_level", 1D);
        meleeSpeedPerLevel = configBuilder
                .comment("The amount of melee speed that an addditional level of the ability adds")
                .define("melee_speed_per_level", 0.05);
        movementSpeedPerLevel = configBuilder
                .comment("The amount that movement speed is increased for each addditional level of the ability")
                .define("movement_speed_per_level", 0.0025);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, configBuilder.build());
    }

}