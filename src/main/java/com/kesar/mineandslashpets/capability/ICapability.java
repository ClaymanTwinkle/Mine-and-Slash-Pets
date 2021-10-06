package com.kesar.mineandslashpets.capability;

import net.minecraft.nbt.CompoundNBT;

public interface ICapability {
    CompoundNBT saveToNBT();

     void loadFromNBT(CompoundNBT nbt);
}
