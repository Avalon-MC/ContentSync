package net.petercashel.contentsync.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.petercashel.contentsync.ContentSync;

public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ContentSync.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }


    public static void RegisterNetwork(FMLCommonSetupEvent event) {
        INSTANCE.messageBuilder(ContentSyncServerPackPacket_SC.class, 0, NetworkDirection.PLAY_TO_CLIENT).decoder(ContentSyncServerPackPacket_SC::decoder).encoder(ContentSyncServerPackPacket_SC::encoder).consumer(ContentSyncServerPackPacket_SC::messageConsumer).add();

    }
}
