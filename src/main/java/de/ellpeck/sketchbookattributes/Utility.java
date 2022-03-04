package de.ellpeck.sketchbookattributes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Predicate;

public class Utility {

    public static EntityRayTraceResult pickEntity(PlayerEntity player, double range) {
        // see GameRenderer.pick for reference
        Vector3d eyePos = player.getEyePosition(1);
        Vector3d view = player.getViewVector(1).scale(range);
        AxisAlignedBB area = player.getBoundingBox().expandTowards(view).inflate(1, 1, 1);
        return Utility.getEntityHitResult(player, eyePos, eyePos.add(view), area, Entity::isAlive, range * range);
    }

    // non-client-only copy of ProjectileHelper
    private static EntityRayTraceResult getEntityHitResult(Entity p_221273_0_, Vector3d p_221273_1_, Vector3d p_221273_2_, AxisAlignedBB p_221273_3_, Predicate<Entity> p_221273_4_, double p_221273_5_) {
        World world = p_221273_0_.level;
        double d0 = p_221273_5_;
        Entity entity = null;
        Vector3d vector3d = null;

        for (Entity entity1 : world.getEntities(p_221273_0_, p_221273_3_, p_221273_4_)) {
            AxisAlignedBB axisalignedbb = entity1.getBoundingBox().inflate((double) entity1.getPickRadius());
            Optional<Vector3d> optional = axisalignedbb.clip(p_221273_1_, p_221273_2_);
            if (axisalignedbb.contains(p_221273_1_)) {
                if (d0 >= 0.0D) {
                    entity = entity1;
                    vector3d = optional.orElse(p_221273_1_);
                    d0 = 0.0D;
                }
            } else if (optional.isPresent()) {
                Vector3d vector3d1 = optional.get();
                double d1 = p_221273_1_.distanceToSqr(vector3d1);
                if (d1 < d0 || d0 == 0.0D) {
                    if (entity1.getRootVehicle() == p_221273_0_.getRootVehicle() && !entity1.canRiderInteract()) {
                        if (d0 == 0.0D) {
                            entity = entity1;
                            vector3d = vector3d1;
                        }
                    } else {
                        entity = entity1;
                        vector3d = vector3d1;
                        d0 = d1;
                    }
                }
            }
        }

        return entity == null ? null : new EntityRayTraceResult(entity, vector3d);
    }
}
