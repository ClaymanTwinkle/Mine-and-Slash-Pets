package com.kesar.mineandslashpets.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.monster.SpiderEntity;

public class SpiderAttackGoal extends MeleeAttackGoal {
    public SpiderAttackGoal(SpiderEntity spider) {
        super(spider, 1.0D, true);
    }

    public boolean shouldExecute() {
        return super.shouldExecute() && !this.attacker.isBeingRidden();
    }

    protected double getAttackReachSqr(LivingEntity attackTarget) {
        return (double)(4.0F + attackTarget.getWidth());
    }
}
