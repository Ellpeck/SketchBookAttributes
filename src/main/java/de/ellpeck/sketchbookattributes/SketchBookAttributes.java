package de.ellpeck.sketchbookattributes;

import de.ellpeck.sketchbookattributes.entities.IceBallEntity;
import de.ellpeck.sketchbookattributes.entities.TridentLikeEntity;
import de.ellpeck.sketchbookattributes.items.*;
import de.ellpeck.sketchbookattributes.items.StaffItem.Mode;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;
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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Mod(SketchBookAttributes.ID)
public class SketchBookAttributes {

    public static final String ID = "sketchbookattributes";
    public static final Pattern ITEM_REQUIREMENT_REGEX = Pattern.compile("([^,]+), (strength|dexterity|constitution|intelligence|agility), (\\d+)");

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final RegistryObject<StaffItem> STAFF_TIER_1 = ITEMS.register("staff_tier_1",
            () -> new StaffItem(Mode.FIRE_BALL));
    public static final RegistryObject<StaffItem> STAFF_TIER_2 = ITEMS.register("staff_tier_2",
            () -> new StaffItem(Mode.FIRE_BALL, Mode.JUMP, Mode.ICE_BALL));
    public static final RegistryObject<StaffItem> STAFF_TIER_3 = ITEMS.register("staff_tier_3",
            () -> new StaffItem(Mode.FIRE_BALL, Mode.JUMP, Mode.ICE_BALL, Mode.HEAL, Mode.SPEED));
    public static final RegistryObject<StaffItem> STAFF_MASTER = ITEMS.register("staff_master",
            () -> new StaffItem(Mode.FIRE_BALL, Mode.JUMP, Mode.ICE_BALL, Mode.HEAL, Mode.SPEED, Mode.STRENGTH, Mode.METEORS));
    public static final RegistryObject<TridentLikeItem> DAGGER = ITEMS.register("dagger", () -> new TridentLikeItem(180, 3, -2.1, 4));
    public static final RegistryObject<TridentLikeItem> ROGUE_BLADE = ITEMS.register("rogue_blade", () -> new TridentLikeItem(300, 5, -2.1, 7));
    public static final RegistryObject<GreatswordItem> IRON_GREATSWORD = ITEMS.register("iron_greatsword", () -> new GreatswordItem(ItemTier.IRON, 4, -3, 280));
    public static final RegistryObject<GreatswordItem> DIAMOND_GREATSWORD = ITEMS.register("diamond_greatsword", () -> new GreatswordItem(ItemTier.DIAMOND, 4, -3, 1800));
    public static final RegistryObject<GreatswordItem> NETHERITE_GREATSWORD = ITEMS.register("netherite_greatsword", () -> new GreatswordItem(ItemTier.NETHERITE, 5, -3, 2200));
    public static final RegistryObject<SpecialSwordItem> FLAME_GODS_BLADE = ITEMS.register("flame_gods_blade", () -> new SpecialSwordItem(11, -2.3F, 2800));
    public static final RegistryObject<GildedCrossbowItem> GILDED_CROSSBOW = ITEMS.register("gilded_crossbow", GildedCrossbowItem::new);
    public static final RegistryObject<SpecialBowItem> QUICK_STRING_BOW = ITEMS.register("quick_string_bow", () -> new SpecialBowItem(400, 1.2F, 1));
    public static final RegistryObject<SpecialBowItem> HUNTERS_MARK_BOW = ITEMS.register("hunters_mark_bow", () -> new SpecialBowItem(500, 1, 1.5F));
    public static final RegistryObject<SpecialBowItem> THUNDER_KING_BOW = ITEMS.register("thunder_king_bow", () -> new SpecialBowItem(1500, 1.5F, 2));
    public static final RegistryObject<HasteBootsItem> HASTE_BOOTS = ITEMS.register("haste_boots", HasteBootsItem::new);

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, ID);
    public static final RegistryObject<EntityType<IceBallEntity>> ICE_BALL = ENTITIES.register("ice_ball",
            () -> EntityType.Builder.<IceBallEntity>of(IceBallEntity::new, EntityClassification.MISC).build("ice_ball"));
    public static final RegistryObject<EntityType<TridentLikeEntity>> TRIDENT_LIKE = ENTITIES.register("trident_like",
            () -> EntityType.Builder.<TridentLikeEntity>of(TridentLikeEntity::new, EntityClassification.MISC).build("trident_like"));

    public static ConfigValue<Double> xpNeededMultiplier;
    public static ConfigValue<Double> healthRegenPerLevel;
    public static ConfigValue<Double> manaRegenPerLevel;
    public static ConfigValue<Double> meleeDamagePerLevel;
    public static ConfigValue<Double> rangedDamagePerLevel;
    public static ConfigValue<Double> healthBonusPerLevel;
    public static ConfigValue<Double> manaBonusPerLevel;
    public static ConfigValue<Double> meleeSpeedPerLevel;
    public static ConfigValue<Double> movementSpeedPerLevel;
    public static ConfigValue<List<? extends String>> attributeItemRequirements;

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
        attributeItemRequirements = configBuilder
                .comment("A list of items that require certain ability levels, where each entry is formatted as 'item_id, ability, amount' with possible abilities strength, dexterity, constitution, intelligence, agility, and item ID can use regex")
                .defineList("attribute_item_requirements", Arrays.asList(
                        "minecraft:stone_sword, strength, 10",
                        "minecraft:iron_sword, strength, 15",
                        "minecraft:diamond_sword, strength, 25",
                        "minecraft:netherite_sword, strength, 35",
                        "minecraft:.*_axe, strength, 20",
                        "minecraft:bow, dexterity, 20",
                        "minecraft:crossbow, dexterity, 25",
                        "sketchbookattributes:staff_tier_1, intelligence, 15",
                        "sketchbookattributes:staff_tier_2, intelligence, 25",
                        "sketchbookattributes:staff_tier_3, intelligence, 35",
                        "sketchbookattributes:staff_master, intelligence, 50"
                ), o -> ITEM_REQUIREMENT_REGEX.matcher(String.valueOf(o)).matches());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, configBuilder.build());
    }
}