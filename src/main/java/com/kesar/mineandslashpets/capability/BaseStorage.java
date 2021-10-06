package com.kesar.mineandslashpets.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class BaseStorage<TYPE extends ICapability> implements Capability.IStorage<TYPE> {

    @Override
    public INBT writeNBT(Capability<TYPE> capability, TYPE instance, Direction side) {

        return instance.saveToNBT();
    }

    @Override
    public void readNBT(Capability<TYPE> capability, TYPE instance, Direction side, INBT nbt) {

        instance.loadFromNBT((CompoundNBT) nbt);

    }

}