package com.kesar.mineandslashpets.gui;

import com.kesar.mineandslashpets.ModInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robertx22.mine_and_slash.database.rarities.MobRarity;
import com.robertx22.mine_and_slash.database.stats.types.offense.PhysicalDamage;
import com.robertx22.mine_and_slash.db_lists.Rarities;
import com.robertx22.mine_and_slash.registry.SlashRegistry;
import com.robertx22.mine_and_slash.uncommon.capability.entity.EntityCap;
import com.robertx22.mine_and_slash.uncommon.datasaving.Load;
import com.robertx22.mine_and_slash.uncommon.localization.CLOC;
import com.robertx22.mine_and_slash.uncommon.localization.Styles;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

import static net.minecraft.client.gui.screen.inventory.InventoryScreen.drawEntityOnScreen;

@OnlyIn(Dist.CLIENT)
public class PetEntityDetailScreen extends ContainerScreen<PetEntityDetailContainer> {
    private static final ResourceLocation OBSIDIAN_CONTAINER_RESOURCE = new ResourceLocation(ModInfo.MOD_ID, "textures/gui/container/inventory.png");
    private final int textureWidth = 176;
    private final int textureHeight = 166;

    private final MobEntity entity;

    private float oldMouseX;
    private float oldMouseY;

    public PetEntityDetailScreen(PetEntityDetailContainer screenContainer, PlayerInventory inv, MobEntity entity) {
        super(screenContainer, inv, entity.getDisplayName());
        this.entity = entity;
        this.xSize = textureWidth;
        this.ySize = textureHeight;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);

        this.oldMouseX = (float) mouseX;
        this.oldMouseY = (float) mouseY;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        EntityCap.UnitData data = Load.Unit(entity);

        // draw level
        this.font.drawString(I18n.format("mine-and-slash-pets.word.level") + ": " + data.getLevel(), 97.0F, 8.0F, TextFormatting.BLACK.getColor());

        // draw rarity
        int rarity = data.getRarity();
        MobRarity rar = Rarities.Mobs.get(rarity);
        ITextComponent rarityPrefix = rar.locName();
        ITextComponent rarityText = new StringTextComponent(I18n.format("mine-and-slash-pets.word.rarity") + ": ").appendSibling(rarityPrefix);
        this.font.drawString(rarityText.getFormattedText(), 97.0F, 18.0F, TextFormatting.BLACK.getColor());

        // draw health
        int currentHp = (int) data.getUnit()
                .getCurrentEffectiveHealth(entity, data);
        int maxHP = (int) data.getUnit()
                .getMaxEffectiveHealth();
        this.font.drawString(I18n.format("mine-and-slash-pets.word.health") + ": " + currentHp + "/" + maxHP, 97.0F, 28.0F, TextFormatting.BLACK.getColor());

        // draw attack value
        // this.getAttributes().applyAttributeModifiers(itemstack1.getAttributeModifiers(equipmentslottype))
        this.font.drawString(I18n.format("mine-and-slash-pets.word.attack") + String.format(": %d", container.getValueIntArray().get(0)), 97.0F, 38.0F, TextFormatting.BLACK.getColor());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.renderBackground();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(OBSIDIAN_CONTAINER_RESOURCE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        blit(i, j, 0, 0, xSize, ySize, this.textureWidth, textureHeight);

        drawEntityOnScreen(i + 51, j + 75, 30, (float) (i + 51) - this.oldMouseX, (float) (j + 75 - 50) - this.oldMouseY, this.entity);
    }
}
