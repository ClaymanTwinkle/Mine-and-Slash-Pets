package com.kesar.mineandslashpets.item;

import com.kesar.mineandslashpets.ModInfo;
import net.minecraft.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, ModInfo.MOD_ID);

    // public static RegistryObject<Item> ZOMBIE_SPAWN_EGG = ITEMS.register("zombie_spawn_egg", () -> new SpawnEggItem(EntityType.ZOMBIE, 44975, 7969893, (new Item.Properties()).group(ItemGroup.FOOD)));
}
