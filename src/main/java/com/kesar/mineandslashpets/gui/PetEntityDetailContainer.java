package com.kesar.mineandslashpets.gui;

import com.robertx22.mine_and_slash.database.rarities.MobRarity;
import com.robertx22.mine_and_slash.database.stats.types.offense.PhysicalDamage;
import com.robertx22.mine_and_slash.db_lists.Rarities;
import com.robertx22.mine_and_slash.registry.SlashRegistry;
import com.robertx22.mine_and_slash.uncommon.capability.entity.EntityCap;
import com.robertx22.mine_and_slash.uncommon.datasaving.Load;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntArray;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PetEntityDetailContainer extends Container implements IInventoryChangedListener {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final EquipmentSlotType[] VALID_HANDS_EQUIPMENT_SLOTS = new EquipmentSlotType[]{EquipmentSlotType.MAINHAND, EquipmentSlotType.OFFHAND};
    private static final EquipmentSlotType[] VALID_ARMOR_EQUIPMENT_SLOTS = new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};

    public final MobEntity entity;

    private final Inventory handsInventory = new Inventory(2);
    private final Inventory armorInventory = new Inventory(4);

    private final IntArray valueIntArray = new IntArray(1);

    public PetEntityDetailContainer(int id, PlayerInventory playerInventory, MobEntity entity) {
        super(null, id);
        this.entity = entity;
        trackIntArray(this.valueIntArray);

        if (!entity.world.isRemote) {
            for (EquipmentSlotType type : VALID_HANDS_EQUIPMENT_SLOTS) {
                ItemStack itemStack = entity.getItemStackFromSlot(type);
                handsInventory.setInventorySlotContents(type.getIndex(), itemStack);
            }

            for (EquipmentSlotType type : VALID_ARMOR_EQUIPMENT_SLOTS) {
                ItemStack itemStack = entity.getItemStackFromSlot(type);
                armorInventory.setInventorySlotContents(type.getIndex(), itemStack);
            }
            updateValue();

            MinecraftForge.EVENT_BUS.register(this);
        }

        handsInventory.addListener(this);
        armorInventory.addListener(this);

        handsInventory.openInventory(playerInventory.player);
        armorInventory.openInventory(playerInventory.player);

        // entity hands
        for (int i = 0; i < VALID_HANDS_EQUIPMENT_SLOTS.length; i++) {
            this.addSlot(new Slot(handsInventory, i, 77, 44 + i * 18));
        }

        // entity armor
        for (int k = 0; k < VALID_ARMOR_EQUIPMENT_SLOTS.length; ++k) {
            final EquipmentSlotType equipmentslottype = VALID_ARMOR_EQUIPMENT_SLOTS[k];
            this.addSlot(new Slot(armorInventory, 3 - k, 8, 8 + k * 18) {
                /**
                 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in
                 * the case of armor slots)
                 */
                public int getSlotStackLimit() {
                    return 1;
                }

                /**
                 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
                 */
                public boolean isItemValid(ItemStack stack) {
                    return stack.canEquip(equipmentslottype, entity);
                }

                /**
                 * Return whether this slot's stack can be taken from this slot.
                 */
                public boolean canTakeStack(PlayerEntity playerIn) {
                    ItemStack itemstack = this.getStack();
                    return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.canTakeStack(playerIn);
                }
            });
        }

        // playerInventory
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.entity.isAlive();
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            int handsSize = this.handsInventory.getSizeInventory();
            int armorSize = this.armorInventory.getSizeInventory();
            int totalEntityInventorySize = handsSize + armorSize;
            if (index < totalEntityInventorySize) {
                if (!this.mergeItemStack(itemstack1, totalEntityInventorySize, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                EquipmentSlotType equipmentslottype = MobEntity.getSlotForItemStack(itemstack);
                if (equipmentslottype.getSlotType() == EquipmentSlotType.Group.ARMOR && !this.inventorySlots.get(handsSize + 3 - equipmentslottype.getIndex()).getHasStack()) {
                    int i = handsSize + 3 - equipmentslottype.getIndex();
                    if (!this.mergeItemStack(itemstack1, i, i + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (equipmentslottype == EquipmentSlotType.OFFHAND && !this.inventorySlots.get(1).getHasStack()) {
                    if (!this.mergeItemStack(itemstack1, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (index < 27 + totalEntityInventorySize) {
                        if (!this.mergeItemStack(itemstack1, 27 + totalEntityInventorySize, this.inventorySlots.size(), false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.mergeItemStack(itemstack1, totalEntityInventorySize, 27 + totalEntityInventorySize, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }

            }
        }

        return itemstack;
    }

    @SubscribeEvent
    public void onLivingEquipmentChangeEvent(LivingEvent event) {
        if(event.getEntity() == entity) {
            updateValue();
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.armorInventory.closeInventory(playerIn);
        this.handsInventory.closeInventory(playerIn);
        this.armorInventory.removeListener(this);
        this.handsInventory.removeListener(this);
        if (!entity.world.isRemote) {
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    @Override
    public void onInventoryChanged(IInventory iInventory) {
        if (!this.entity.world.isRemote) {
            if (iInventory == armorInventory) {
                for (int i = 0; i < armorInventory.getSizeInventory(); i++) {
                    ItemStack itemStack = armorInventory.getStackInSlot(i);
                    EquipmentSlotType equipmentslottype = MobEntity.getSlotForItemStack(itemStack);
                    entity.setItemStackToSlot(equipmentslottype, itemStack);
                }
            } else if (iInventory == handsInventory) {
                ItemStack mainHand = handsInventory.getStackInSlot(0);
                ItemStack offHand = handsInventory.getStackInSlot(1);
                entity.setItemStackToSlot(EquipmentSlotType.MAINHAND, mainHand);
                entity.setItemStackToSlot(EquipmentSlotType.OFFHAND, offHand);
            }
        }
    }

    private void updateValue() {
        EntityCap.UnitData data = Load.Unit(entity);
        int rarity = data.getRarity();
        MobRarity rar = Rarities.Mobs.get(rarity);

        float attackDamage = (float) entity.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
        attackDamage += EnchantmentHelper.getModifierForCreature(entity.getHeldItemMainhand(), CreatureAttribute.UNDEFINED);
        float vanilla = PhysicalDamage.getInstance().getScaling().scale(attackDamage, data.getLevel());
        float num = vanilla * rar.DamageMultiplier() * data.getMapTier().mob_damage_multi;
        num = (float) ((double) num * SlashRegistry.getEntityConfig(entity, data).DMG_MULTI);
        valueIntArray.set(0, (int) num);
    }

    public IntArray getValueIntArray() {
        return valueIntArray;
    }
}