package com.kesar.mineandslashpets.entity;

import com.kesar.mineandslashpets.entity.ai.PetFollowOwnerGoal;
import com.kesar.mineandslashpets.entity.ai.PetOwnerHurtByTargetGoal;
import com.kesar.mineandslashpets.entity.ai.PetOwnerHurtTargetGoal;
import com.kesar.mineandslashpets.entity.ai.SpiderAttackGoal;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.CaveSpiderEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.reflect.Field;
import java.util.Set;

public class EntityAIHandler {
    public static void handle(MobEntity entity) {
        // 1. clear goals
        entity.goalSelector.goals.clear();
        entity.targetSelector.goals.clear();

        Class<?> clazz = entity.getClass();

        // 2. add goals
        if (clazz == ZombieEntity.class) {
            entity.goalSelector.addGoal(1, new SwimGoal(entity));
            entity.goalSelector.addGoal(2, new MeleeAttackGoal((CreatureEntity) entity, 1.5D, true));
            entity.goalSelector.addGoal(4, new PetFollowOwnerGoal(entity, 1.5D, 8.0F, 2.0F, false));
            entity.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal((CreatureEntity) entity, 1.0D));
            entity.goalSelector.addGoal(8, new LookAtGoal(entity, PlayerEntity.class, 8.0F));
            entity.goalSelector.addGoal(8, new LookRandomlyGoal(entity));

            entity.targetSelector.addGoal(1, new PetOwnerHurtByTargetGoal(entity));
            entity.targetSelector.addGoal(2, new PetOwnerHurtTargetGoal(entity));
            entity.targetSelector.addGoal(3, new HurtByTargetGoal((CreatureEntity) entity));
        } else if (clazz == SpiderEntity.class || clazz == CaveSpiderEntity.class) {
            entity.goalSelector.addGoal(1, new SwimGoal(entity));
            entity.goalSelector.addGoal(2, new LeapAtTargetGoal(entity, 0.4F));
            entity.goalSelector.addGoal(3, new SpiderAttackGoal((SpiderEntity) entity));
            entity.goalSelector.addGoal(4, new PetFollowOwnerGoal(entity, 1.5D, 8.0F, 2.0F, false));
            entity.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal((CreatureEntity) entity, 0.8D));
            entity.goalSelector.addGoal(6, new LookAtGoal(entity, PlayerEntity.class, 8.0F));
            entity.goalSelector.addGoal(6, new LookRandomlyGoal(entity));

            entity.targetSelector.addGoal(1, new PetOwnerHurtByTargetGoal(entity));
            entity.targetSelector.addGoal(2, new PetOwnerHurtTargetGoal(entity));
            entity.targetSelector.addGoal(3, new HurtByTargetGoal((CreatureEntity) entity));
        }
    }
}
