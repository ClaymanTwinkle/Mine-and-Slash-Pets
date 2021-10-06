package com.kesar.mineandslashpets.gui;

import com.kesar.mineandslashpets.ModInfo;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ContainerRegistry {
    // private static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, ModInfo.MOD_ID);
    // public static RegistryObject<ContainerType<PetEntityDetailContainer>> PET_ENTITY_DETAIL_CONTAINER = CONTAINERS.register("pet_entity_detail_container", () -> IForgeContainerType.create((int windowId, PlayerInventory inv, PacketBuffer data) -> new PetEntityDetailContainer(windowId, inv, null)));
}
