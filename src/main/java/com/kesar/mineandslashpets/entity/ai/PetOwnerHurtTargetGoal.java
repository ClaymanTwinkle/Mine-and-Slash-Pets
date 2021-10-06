package com.kesar.mineandslashpets.entity.ai;

import com.kesar.mineandslashpets.capability.EntityTameableCapability;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;

import java.util.EnumSet;

public class PetOwnerHurtTargetGoal extends TargetGoal {
    private final MobEntity tameable;
    private LivingEntity attacker;
    private int timestamp;

    public PetOwnerHurtTargetGoal(MobEntity theEntityTameableIn) {
        super(theEntityTameableIn, false);
        this.tameable = theEntityTameableIn;
        this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean shouldExecute() {
        EntityTameableCapability.ITameable capability = EntityTameableCapability.getCapability(this.tameable);
        if (capability != null && capability.isTamed()) {
            LivingEntity livingentity = capability.getOwner(tameable.world);
            if (livingentity == null) {
                return false;
            } else {
                this.attacker = livingentity.getLastAttackedEntity();
                if (attacker == null) {
                    return false;
                }
                int i = livingentity.getLastAttackedEntityTime();
                return i != this.timestamp && this.isSuitableTarget(this.attacker, EntityPredicate.DEFAULT) && EntityTameableCapability.shouldAttackEntity(this.attacker, livingentity);
            }
        } else {
            return false;
        }
    }

    public void startExecuting() {
        this.goalOwner.setAttackTarget(this.attacker);
        EntityTameableCapability.ITameable capability = EntityTameableCapability.getCapability(this.tameable);
        if (capability != null) {
            LivingEntity livingentity = capability.getOwner(tameable.world);
            if (livingentity != null) {
                this.timestamp = livingentity.getLastAttackedEntityTime();
            }
        }
        super.startExecuting();
    }
}
