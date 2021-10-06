package com.kesar.mineandslashpets;

import net.minecraft.entity.monster.CaveSpiderEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.monster.ZombieEntity;

import java.util.Arrays;
import java.util.List;

public interface ModConfig {
    List<Class<?>> ENTITY_WHITE_LIST = Arrays.asList(ZombieEntity.class, SpiderEntity.class, CaveSpiderEntity.class);
}
