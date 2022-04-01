package de.ellpeck.sketchbookattributes.data;

import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import de.ellpeck.sketchbookattributes.network.SyncAttributesPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AttributeData extends WorldSavedData {

    private static final String NAME = SketchBookAttributes.ID + "_attribute_data";
    private static AttributeData clientData;

    private final Map<UUID, PlayerAttributes> attributes = new HashMap<>();
    private final World level;

    public AttributeData(World level) {
        super(NAME);
        this.level = level;
    }

    @Override
    public void load(CompoundNBT nbt) {
        ListNBT list = nbt.getList("attributes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entry = list.getCompound(i);
            UUID id = entry.getUUID("id");
            PlayerAttributes data = this.getAttributes(id);
            data.deserializeNBT(entry.getCompound("data"));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        ListNBT list = new ListNBT();
        for (Map.Entry<UUID, PlayerAttributes> data : this.attributes.entrySet()) {
            CompoundNBT entry = new CompoundNBT();
            entry.putUUID("id", data.getKey());
            entry.put("data", data.getValue().serializeNBT());
            list.add(entry);
        }
        nbt.put("attributes", list);
        return nbt;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public PlayerAttributes getAttributes(PlayerEntity player) {
        return this.getAttributes(player.getUUID());
    }

    public PlayerAttributes getAttributes(UUID id) {
        return this.attributes.computeIfAbsent(id, u -> new PlayerAttributes());
    }

    public SyncAttributesPacket getPacket() {
        return new SyncAttributesPacket(this.serializeNBT());
    }

    public void resetAttributes(UUID id) {
        // attributes will be re-computed when getAttributes is called again
        this.attributes.remove(id);
    }

    public static AttributeData get(World level) {
        if (level.isClientSide) {
            if (clientData == null || clientData.level != level)
                clientData = new AttributeData(level);
            return clientData;
        } else {
            return ((ServerWorld) level).getServer().overworld().getDataStorage().computeIfAbsent(() -> new AttributeData(level), NAME);
        }
    }

}
