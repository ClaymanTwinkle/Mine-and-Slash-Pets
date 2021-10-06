package com.kesar.mineandslashpets.network;

import com.kesar.mineandslashpets.MineAndSlashPets;
import com.kesar.mineandslashpets.network.packet.OpenGUIPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketRegistry {
    static int index = 0;

    // bit shorter
    private static <MSG> void reg(Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder,
                                  Function<PacketBuffer, MSG> decoder,
                                  BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {

        MineAndSlashPets.NETWORK.registerMessage(index++, messageType, encoder, decoder, messageConsumer);
    }

    public static void register() {
        reg(OpenGUIPacket.class, OpenGUIPacket::encode, OpenGUIPacket::decode, OpenGUIPacket::handle);
    }
}
