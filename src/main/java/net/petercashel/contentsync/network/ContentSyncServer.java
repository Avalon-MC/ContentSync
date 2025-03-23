package net.petercashel.contentsync.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.configuration.server.ServerContentEntry;

import java.util.List;

public class ContentSyncServer {

    public static void OnPlayerJoined(PlayerEvent.PlayerLoggedInEvent loggedInEvent) {

        //Kick off!
        if (loggedInEvent.getEntity().level().isClientSide) {
            return; //Go away client side
        }

        //Empty? Stop
        if (ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.size() == 0) {
            return;
        }

        //Invalid Name
        if (ContentSyncConfig.ConfigInstance.HostingServerSettings.ThisServerAddress.length() <= 1) {
            return;
        }

        //Send list to packet, but none of the server only ones.
        List<ServerContentEntry> list = ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.stream().filter(x -> x.ServerOnly == false && x.ServerName.equals(ContentSyncConfig.ConfigInstance.HostingServerSettings.ThisServerAddress)).toList();

        if (list.size() == 0) return;

        //Create Packet
        ContentSyncServerPackPacket_SC packet = new ContentSyncServerPackPacket_SC(list, ContentSyncConfig.ConfigInstance.HostingServerSettings.ThisServerAddress, ContentSyncConfig.ConfigInstance.HostingServerSettings.EnforceServerPacks);

        //Send Packet
        PacketDistributor.sendToPlayer((ServerPlayer) loggedInEvent.getEntity(), packet);


    }
}
