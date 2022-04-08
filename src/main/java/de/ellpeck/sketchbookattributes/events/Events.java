package de.ellpeck.sketchbookattributes.events;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import de.ellpeck.sketchbookattributes.data.AttributeData;
import de.ellpeck.sketchbookattributes.data.PlayerAttributes;
import de.ellpeck.sketchbookattributes.items.SpecialBowItem;
import de.ellpeck.sketchbookattributes.network.PacketHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public final class Events {

    @SubscribeEvent
    public static void entityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (!player.level.isClientSide) {
                AttributeData data = AttributeData.get(player.level);
                PlayerAttributes attributes = data.getAttributes(player);
                attributes.reapplyAttributes(player);
                PacketHandler.sendTo(player, data.getPacket());
            }
        }
    }

    @SubscribeEvent
    public static void entityLeaveWorld(EntityLeaveWorldEvent event) {
        Entity entity = event.getEntity();
        // staff meteor sound
        if (entity.getPersistentData().getBoolean(SketchBookAttributes.ID + ":meteor"))
            entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.75F, 1);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerXpChange(PlayerXpEvent.XpChange event) {
        PlayerEntity player = event.getPlayer();
        int amount = event.getAmount();
        if (player.level.isClientSide || amount <= 0)
            return;
        AttributeData data = AttributeData.get(player.level);
        PlayerAttributes attributes = data.getAttributes(player);
        if (attributes.gainXp(amount))
            PacketHandler.sendToAll(data.getPacket());
    }

    @SubscribeEvent
    public static void serverStarting(FMLServerStartingEvent event) {
        event.getServer().getCommands().getDispatcher().register(Commands.literal(SketchBookAttributes.ID).requires(s -> s.hasPermission(2))
                .then(Commands.literal("level").then(Commands.argument("level", IntegerArgumentType.integer(0, PlayerAttributes.MAX_LEVEL)).executes(c -> {
                    CommandSource source = c.getSource();
                    PlayerEntity player = source.getPlayerOrException();
                    AttributeData data = AttributeData.get(player.level);
                    PlayerAttributes attributes = data.getAttributes(player);
                    attributes.level = IntegerArgumentType.getInteger(c, "level");
                    attributes.pointsToNextLevel = 0;
                    PacketHandler.sendToAll(data.getPacket());
                    source.sendSuccess(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".level_set", source.getDisplayName(), attributes.level), true);
                    return 0;
                })))
                .then(Commands.literal("points").then(Commands.argument("points", IntegerArgumentType.integer(0)).executes(c -> {
                    CommandSource source = c.getSource();
                    PlayerEntity player = source.getPlayerOrException();
                    AttributeData data = AttributeData.get(player.level);
                    PlayerAttributes attributes = data.getAttributes(player);
                    attributes.skillPoints = IntegerArgumentType.getInteger(c, "points");
                    PacketHandler.sendToAll(data.getPacket());
                    source.sendSuccess(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".points_set", source.getDisplayName(), attributes.skillPoints), true);
                    return 0;
                })))
                .then(Commands.literal("reset").executes(c -> {
                    CommandSource source = c.getSource();
                    PlayerEntity player = source.getPlayerOrException();
                    AttributeData data = AttributeData.get(player.level);
                    data.resetAttributes(player.getUUID());
                    data.getAttributes(player).reapplyAttributes(player);
                    PacketHandler.sendToAll(data.getPacket());
                    source.sendSuccess(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".reset", source.getDisplayName()), true);
                    return 0;
                })));
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (event.player.level.isClientSide)
            return;
        AttributeData data = AttributeData.get(event.player.level);
        PlayerAttributes attributes = data.getAttributes(event.player);

        // update mana and additional health regen
        if (event.player.tickCount % 20 == 0) {
            float newMana = Math.min(attributes.getMaxMana(), attributes.mana + attributes.getManaRegenPerSecond());
            if (newMana != attributes.mana) {
                attributes.mana = newMana;
                PacketHandler.sendTo(event.player, data.getPacket());
            }
            event.player.heal(attributes.getHealthRegenPerSecond());
        }

        // give speed effect for haste boots
        ItemStack boots = event.player.getItemBySlot(EquipmentSlotType.FEET);
        if (boots.getItem() == SketchBookAttributes.HASTE_BOOTS.get())
            event.player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 1, 1));
    }

    @SubscribeEvent
    public static void livingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (source == null)
            return;

        Entity attacker = source.getEntity();
        if (attacker instanceof PlayerEntity && !PlayerAttributes.canUseHeldItem((PlayerEntity) attacker, false)) {
            event.setAmount(1);
            return;
        }

        if (source.isProjectile()) {
            Entity projectile = source.getDirectEntity();

            // ranged damage bonus
            if (!(projectile instanceof ThrowableEntity) && !(projectile instanceof AbstractFireballEntity)) {
                Entity shooter = source.getEntity();
                if (shooter instanceof PlayerEntity) {
                    PlayerAttributes attributes = AttributeData.get(shooter.level).getAttributes((PlayerEntity) shooter);
                    event.setAmount(event.getAmount() + attributes.getRangedDamageBonus());
                }
            }

            // staff meteor damage increase
            if (projectile.getPersistentData().getBoolean(SketchBookAttributes.ID + ":meteor"))
                event.setAmount(80);
        }
    }

    // lowest priority so that resulting effects aren't engaged when someone else cancels the event
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void livingDamage(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (source == null)
            return;

        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity) {
            ItemStack held = ((LivingEntity) attacker).getMainHandItem();
            if (held.getItem() == SketchBookAttributes.FLAME_GODS_BLADE.get())
                event.getEntity().setSecondsOnFire(4);
        }

        if (source.isProjectile()) {
            Entity projectile = source.getDirectEntity();

            // thunder king bow slowness
            String bowName = projectile.getPersistentData().getString(SketchBookAttributes.ID + ":bow");
            Item bow = ForgeRegistries.ITEMS.getValue(new ResourceLocation(bowName));
            if (bow == SketchBookAttributes.THUNDER_KING_BOW.get())
                event.getEntityLiving().addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 2 * 20));
        }
    }

    @SubscribeEvent
    public static void arrowLoose(ArrowLooseEvent event) {
        Item item = event.getBow().getItem();
        if (item instanceof SpecialBowItem)
            event.setCharge((int) (event.getCharge() * ((SpecialBowItem) item).drawSpeedMultiplier));
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!PlayerAttributes.canUseHeldItem(event.getPlayer(), false))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof PlayerEntity && !PlayerAttributes.canUseHeldItem((PlayerEntity) event.getEntity(), true)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!PlayerAttributes.canUseHeldItem(event.getPlayer(), true))
            event.setUseItem(Event.Result.DENY);
    }

    @SubscribeEvent
    public static void onItemInteract(PlayerInteractEvent.RightClickItem event) {
        if (!PlayerAttributes.canUseHeldItem(event.getPlayer(), true))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!PlayerAttributes.canUseHeldItem(event.getPlayer(), false))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!PlayerAttributes.canUseHeldItem(event.getPlayer(), true))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void criticalHit(CriticalHitEvent event) {
        PlayerEntity player = event.getPlayer();
        // always crit when using the rogue blade and sneaking
        if (player.isCrouching()) {
            ItemStack held = player.getMainHandItem();
            if (held.getItem() == SketchBookAttributes.ROGUE_BLADE.get()) {
                event.setDamageModifier(1.75F);
                event.setResult(Event.Result.ALLOW);
            }
        }
    }

    @SubscribeEvent
    public static void lootTableLoad(LootTableLoadEvent event) {
        switch (event.getName().toString()) {
            case "minecraft:chests/simple_dungeon":
            case "minecraft:chests/village/village_temple":
            case "minecraft:chests/jungle_temple":
            case "minecraft:chests/stronghold_corridor":
            case "minecraft:chests/end_city_treasure":
                event.getTable().addPool(LootPool.lootPool()
                        .add(TableLootEntry.lootTableReference(new ResourceLocation(SketchBookAttributes.ID, "inject/shared")))
                        .bonusRolls(0, 1).name(SketchBookAttributes.ID + "_inject").build());
                System.out.println("Adding loot to "+event.getName());
        }
    }
}