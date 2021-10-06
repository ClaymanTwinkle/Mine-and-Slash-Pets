package com.kesar.mineandslashpets.network.packet;

import com.kesar.mineandslashpets.gui.PetEntityDetailContainer;
import com.kesar.mineandslashpets.gui.PetEntityDetailScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenGUIPacket {
    public int windowId;
    public int entityId;

    public OpenGUIPacket() {
    }

    public OpenGUIPacket(int windowId, Entity entity) {
        this.windowId = windowId;
        this.entityId = entity.getEntityId();
    }

    public static OpenGUIPacket decode(PacketBuffer buf) {
        OpenGUIPacket newpkt = new OpenGUIPacket();
        newpkt.windowId = buf.readInt();
        newpkt.entityId = buf.readInt();
        return newpkt;
    }

    public static void encode(OpenGUIPacket packet, PacketBuffer tag) {
        tag.writeInt(packet.windowId);
        tag.writeInt(packet.entityId);
    }

    public static void handle(OpenGUIPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                World world = Minecraft.getInstance().world;
                if (world != null) {
                    Entity entity = world.getEntityByID(pkt.entityId);
                    if (entity instanceof MobEntity) {
                        ClientPlayerEntity clientplayerentity = Minecraft.getInstance().player;
                        PetEntityDetailContainer container = null;
                        if (clientplayerentity != null) {
                            container = new PetEntityDetailContainer(pkt.windowId, clientplayerentity.inventory, (MobEntity) entity);
                            clientplayerentity.openContainer = container;
                            Minecraft.getInstance().displayGuiScreen(new PetEntityDetailScreen(container, clientplayerentity.inventory, (MobEntity) entity));
                        }
                    }
                }

            } catch (Exception var4) {
                var4.printStackTrace();
            }

        });
        ctx.get().setPacketHandled(true);
    }
}
