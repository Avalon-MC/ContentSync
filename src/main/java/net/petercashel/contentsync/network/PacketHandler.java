package net.petercashel.contentsync.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.petercashel.contentsync.ContentSync;

public class PacketHandler {

    public static void RegisterNetwork(PayloadRegistrar registrar) {

        registrar.commonToClient(
                ContentSyncServerPackPacket_SC.TYPE,
                ContentSyncServerPackPacket_SC.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ContentSyncServerPackPacket_SC::handleDataOnMain_client,
                        ContentSyncServerPackPacket_SC::handleDataOnMain_server
                )
        );

    }
}
