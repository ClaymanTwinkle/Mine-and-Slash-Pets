package com.kesar.mineandslashpets.entity;

import com.kesar.mineandslashpets.MineAndSlashPets;
import com.kesar.mineandslashpets.ModConfig;
import com.kesar.mineandslashpets.capability.EntityTameableCapability;
import com.kesar.mineandslashpets.gui.PetEntityDetailContainer;
import com.kesar.mineandslashpets.network.packet.OpenGUIPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.HorseInventoryContainer;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
public class EntityHook {
    private static final String ITEM_TAG_NBT_KEY_IS_PET = "is_pet";
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }
        if (event.getEntity() instanceof MobEntity && ModConfig.ENTITY_WHITE_LIST.contains(event.getEntity().getClass())) {
            MobEntity entity = (MobEntity) event.getEntity();
            EntityTameableCapability.ITameable capability = EntityTameableCapability.getCapability(entity);
            if (capability != null && capability.isTamed()) {
                EntityAIHandler.handle(entity);
            }
        }
    }

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent event) {
        if (event instanceof PlayerInteractEvent.RightClickItem || event instanceof PlayerInteractEvent.RightClickBlock) {
            World worldIn = event.getWorld();
            ItemStack stack = event.getItemStack();
            if (worldIn.isRemote) {
                return;
            }
            if (!stack.isEmpty() && stack.getItem() instanceof SpawnEggItem) {
                CompoundNBT tag = stack.getTag();
                if (tag != null && tag.getBoolean(ITEM_TAG_NBT_KEY_IS_PET)) {
                    event.setCanceled(true);
                    event.setCancellationResult(ActionResultType.SUCCESS);

                    if (event instanceof PlayerInteractEvent.RightClickItem) return;
                    PlayerEntity playerIn = event.getPlayer();
                    if (!playerIn.abilities.isCreativeMode) {
                        stack.shrink(1);
                    }
                    BlockPos blockpos = event.getPos();
                    BlockState blockstate = worldIn.getBlockState(blockpos);
                    Direction direction = event.getFace();
                    BlockPos blockpos1;
                    if (blockstate.getCollisionShape(worldIn, blockpos).isEmpty()) {
                        blockpos1 = blockpos;
                    } else {
                        blockpos1 = blockpos.offset(direction);
                    }
                    SpawnEggItem spawnEggItem = (SpawnEggItem) stack.getItem();
                    EntityType<?> type = spawnEggItem.getType(stack.getTag());
                    SpawnReason reason = SpawnReason.SPAWN_EGG;
                    Entity entity = type.create(worldIn, (CompoundNBT) null, (ITextComponent) null, (PlayerEntity) null, blockpos1, reason, false, false);

                    if (entity instanceof net.minecraft.entity.MobEntity && net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn((net.minecraft.entity.MobEntity) entity, worldIn, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ(), null, reason)) {
                        return;
                    }
                    if (entity != null) {
                        EntityTameableCapability.ITameable capability = EntityTameableCapability.getCapability(entity);
                        if (capability != null) {
                            capability.setTamed(true);
                            capability.setOwnerId(playerIn.getUniqueID());
                            if (entity instanceof MobEntity) {
                                ((MobEntity) entity).setDropChance(EquipmentSlotType.MAINHAND, 2.0f);
                                ((MobEntity) entity).setDropChance(EquipmentSlotType.HEAD, 2.0f);
                            }
                            worldIn.addEntity(entity);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void mobOnDeathDrop(LivingDeathEvent event) {
        LivingEntity mobKilled = event.getEntityLiving();

        if (mobKilled.world.isRemote) {
            return;
        }

        if (ModConfig.ENTITY_WHITE_LIST.contains(mobKilled.getClass())) {
            EntityTameableCapability.ITameable capability = EntityTameableCapability.getCapability(mobKilled);
            if (capability != null && capability.isTamed()) {
                PlayerEntity owner = capability.getOwner(mobKilled.world);
                if (mobKilled.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && owner instanceof ServerPlayerEntity) {
                    owner.sendMessage(mobKilled.getCombatTracker().getDeathMessage());
                }
            } else {
                ItemStack itemStack = new ItemStack(SpawnEggItem.getEgg(mobKilled.getType()));
                itemStack.setDisplayName(mobKilled.getDisplayName());
                itemStack.setCount(1);
                CompoundNBT tag = itemStack.getTag();
                if (tag == null) {
                    tag = new CompoundNBT();
                }
                tag.putBoolean(ITEM_TAG_NBT_KEY_IS_PET, true);
                itemStack.setTag(tag);
                mobKilled.entityDropItem(itemStack, 1f);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Hand hand = event.getHand();
        Entity target = event.getTarget();
        PlayerEntity player = event.getPlayer();
        if (!(target instanceof MobEntity)) return;
        if (!(player instanceof ServerPlayerEntity)) return;
        if (hand == Hand.OFF_HAND) return;
        if (target.world.isRemote) return;

        if (ModConfig.ENTITY_WHITE_LIST.contains(target.getClass())) {
            EntityTameableCapability.ITameable capability = EntityTameableCapability.getCapability(target);
            if (capability != null && capability.isTamed()) {
                if (player.getUniqueID().equals(capability.getOwnerId())) {
                    if (player.isShiftKeyDown()) {
                        if (player.openContainer != player.container) {
                            player.closeScreen();
                        }
                        ((ServerPlayerEntity) player).getNextWindowId();

                        MineAndSlashPets.sendToClient(new OpenGUIPacket(((ServerPlayerEntity) player).currentWindowId, target), (ServerPlayerEntity) player);
                        player.openContainer = new PetEntityDetailContainer(((ServerPlayerEntity) player).currentWindowId, player.inventory, (MobEntity) target);
                        player.openContainer.addListener((ServerPlayerEntity) player);

                        event.setCanceled(true);
                        event.setCancellationResult(ActionResultType.SUCCESS);
                    }
                }
            }
        }
    }
}
