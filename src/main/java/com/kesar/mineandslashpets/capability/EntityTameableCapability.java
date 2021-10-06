package com.kesar.mineandslashpets.capability;

import com.kesar.mineandslashpets.ModConfig;
import com.kesar.mineandslashpets.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.GhastEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.UUID;

@Mod.EventBusSubscriber
public class EntityTameableCapability {

    @CapabilityInject(ITameable.class)
    public static Capability<ITameable> DATA = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(ITameable.class, new Storage(), DefaultImpl::new);
    }

    @Nullable
    public static ITameable getCapability(Entity entity) {
        return entity.getCapability(EntityTameableCapability.DATA).orElse(null);
    }

    public static boolean shouldAttackEntity(LivingEntity target, LivingEntity owner) {
        if (!(target instanceof CreeperEntity) && !(target instanceof GhastEntity)) {
            if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity) owner).canAttackPlayer((PlayerEntity) target)) {
                return false;
            } else if (target instanceof AbstractHorseEntity && ((AbstractHorseEntity) target).isTame()) {
                return false;
            } else {
                ITameable targetCapability = getCapability(target);
                if (targetCapability != null && targetCapability.isTamed()) {
                    return false;
                }
                return !(target instanceof TameableEntity) || !((TameableEntity) target).isTamed();
            }
        } else {
            return false;
        }
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {
        private static final ResourceLocation RESOURCE = new ResourceLocation(ModInfo.MOD_ID, "entity_tameable_capability");

        @SubscribeEvent
        public static void onEntityConstruct(AttachCapabilitiesEvent<Entity> event) {
            if (ModConfig.ENTITY_WHITE_LIST.contains(event.getObject().getClass())) {
                event.addCapability(RESOURCE, new Provider());
            }
        }
    }

    public static class Provider extends BaseProvider<ITameable> {
        @Override
        public ITameable defaultImpl() {
            return new DefaultImpl();
        }

        @Override
        public Capability<ITameable> dataInstance() {
            return DATA;
        }
    }

    public static class Storage extends BaseStorage<ITameable> {
    }

    public interface ITameable extends ICapability {
        @Nullable
        UUID getOwnerId();

        void setOwnerId(@Nullable UUID ownerId);

        @Nullable
        PlayerEntity getOwner(World world);

        boolean isTamed();

        void setTamed(boolean tamed);
    }

    public static class DefaultImpl implements ITameable {
        private static final String NBT_KEY_OWNER_UUID = "OwnerUUID";

        private UUID ownerId = null;
        private boolean tamed = false;

        @Nullable
        @Override
        public UUID getOwnerId() {
            return ownerId;
        }

        @Override
        public void setOwnerId(@Nullable UUID ownerId) {
            this.ownerId = ownerId;
        }

        @Override
        public boolean isTamed() {
            return tamed;
        }

        @Override
        public void setTamed(boolean tamed) {
            this.tamed = tamed;
        }

        @Override
        public CompoundNBT saveToNBT() {
            CompoundNBT compound = new CompoundNBT();
            if (getOwnerId() == null) {
                compound.putString(NBT_KEY_OWNER_UUID, "");
            } else {
                compound.putString(NBT_KEY_OWNER_UUID, getOwnerId().toString());
            }
            return compound;
        }

        @Override
        public void loadFromNBT(CompoundNBT nbt) {
            String s = "";
            if (nbt.contains(NBT_KEY_OWNER_UUID, 8)) {
                s = nbt.getString(NBT_KEY_OWNER_UUID);
            }

            if (!s.isEmpty()) {
                try {
                    setOwnerId(UUID.fromString(s));
                    setTamed(true);
                } catch (Throwable var4) {
                    setTamed(false);
                }
            } else {
                setTamed(false);
            }
        }

        @Nullable
        @Override
        public PlayerEntity getOwner(World world) {
            try {
                UUID uuid = this.getOwnerId();
                return uuid == null ? null : world.getPlayerByUuid(uuid);
            } catch (IllegalArgumentException var2) {
                return null;
            }
        }
    }
}
