package net.petercashel.contentsync.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.configuration.server.ServerContentEntry;

import java.util.List;

public class ContentSyncServer {

    public static void OnPlayerJoined(PlayerEvent.PlayerLoggedInEvent loggedInEvent) {

        //Kick off!
        if (loggedInEvent.getPlayer().level.isClientSide) {
            return; //Go away client side
        }

        //Empty? Stop
        if (ContentSyncConfig.ConfigInstance.serverContentEntriesList.size() == 0) {
            return;
        }

        //Invalid Name
        if (ContentSyncConfig.ConfigInstance.ThisServerAddress.length() <= 1) {
            return;
        }

        //Send list to packet, but none of the server only ones.
        List<ServerContentEntry> list = ContentSyncConfig.ConfigInstance.serverContentEntriesList.stream().filter(x -> x.ServerOnly == false).toList();
        ContentSyncServerPackPacket_SC packet = new ContentSyncServerPackPacket_SC(list);

        //Send Packet
        PacketHandler.sendToPlayer(packet, (ServerPlayer) loggedInEvent.getPlayer()); //YEET

    }
}
