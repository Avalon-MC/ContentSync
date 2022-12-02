package net.petercashel.contentsync.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.configuration.server.ServerContentEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ContentSyncServerPackPacket_SC {

    private final CompoundTag rootTag;

    //Serialisation
    public ContentSyncServerPackPacket_SC(List<ServerContentEntry> serverContentEntriesList) {
        rootTag = new CompoundTag();
        rootTag.putInt("count", serverContentEntriesList.size());
        rootTag.putString("servername", ContentSyncConfig.ConfigInstance.ThisServerAddress);

        for (int i = 0; i < serverContentEntriesList.size(); i++) {
            CompoundTag itemTag = serverContentEntriesList.get(i).serialise(new CompoundTag());
            rootTag.put(Integer.toString(i), itemTag);
        }
    }

    public void encoder(FriendlyByteBuf pBuffer) {
        pBuffer.writeNbt(rootTag);
    }



    //Deserialisation
    public ContentSyncServerPackPacket_SC(FriendlyByteBuf pBuffer) {
        rootTag = pBuffer.readNbt();
    }


    public static ContentSyncServerPackPacket_SC decoder(FriendlyByteBuf friendlyByteBuf) {
        return new ContentSyncServerPackPacket_SC(friendlyByteBuf);
    }



    //Run it client side
    public boolean messageConsumer(Supplier< NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            //Client Side
            try {
                List<ServerContentEntry> serverContentEntriesList = new ArrayList<>();
                int count = rootTag.getInt("count");
                String ServerName = rootTag.getString("servername");

                for (int i = 0; i < count; i++) {
                    CompoundTag itemTag = rootTag.getCompound(Integer.toString(i));
                    serverContentEntriesList.add(ServerContentEntry.deserialise(itemTag));
                }

                ContentSyncClient.Process(serverContentEntriesList, ServerName);

            } catch (Exception ex) {
                //Fail
            }

        });
        return true;
    }


}
