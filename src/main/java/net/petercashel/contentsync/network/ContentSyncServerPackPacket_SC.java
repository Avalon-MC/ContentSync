package net.petercashel.contentsync.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.configuration.server.ServerContentEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.petercashel.contentsync.ContentSync.MODID;

public record ContentSyncServerPackPacket_SC(
        List<ServerContentEntry> serverContentEntriesList,
        String serverName,
        boolean enforceServerPacks
        ) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ContentSyncServerPackPacket_SC> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "ContentSyncServerPackPacket_SC"));

    // Each pair of elements defines the stream codec of the element to encode/decode and the getter for the element to encode
    // 'name' will be encoded and decoded as a string
    // 'age' will be encoded and decoded as an integer
    // The final parameter takes in the previous parameters in the order they are provided to construct the payload object
    public static final StreamCodec<ByteBuf, ContentSyncServerPackPacket_SC> STREAM_CODEC = StreamCodec.composite(
            ContentSyncServerPackPacket_SC.STREAM_CODEC_LIST_SERVERCONTENTENTRY,
            ContentSyncServerPackPacket_SC::serverContentEntriesList,
            ByteBufCodecs.STRING_UTF8,
            ContentSyncServerPackPacket_SC::serverName,
            ByteBufCodecs.BOOL,
            ContentSyncServerPackPacket_SC::enforceServerPacks,
            ContentSyncServerPackPacket_SC::new
    );

    public static final StreamCodec<ByteBuf, ServerContentEntry> STREAM_CODEC_SERVERCONTENTENTRY =
            ByteBufCodecs.COMPOUND_TAG.map(
                    // String -> ResourceLocation
                    ServerContentEntry::deserialise_new,
                    // ResourceLocation -> String
                    ServerContentEntry::serialise_new
            );

    public static final StreamCodec<ByteBuf, List<ServerContentEntry>> STREAM_CODEC_LIST_SERVERCONTENTENTRY =
            ContentSyncServerPackPacket_SC.STREAM_CODEC_SERVERCONTENTENTRY.apply(ByteBufCodecs.list());



    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handleDataOnMain_client(ContentSyncServerPackPacket_SC data, IPayloadContext context) {

        // Do something with the data, on the main thread
        context.enqueueWork(() -> {
                    ContentSyncClient.Process(data.serverContentEntriesList(), data.serverName(), data.enforceServerPacks(), false);
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.translatable("my_mod.networking.failed", e.getMessage()));
                    return null;
                });
    }

    public static void handleDataOnMain_server(ContentSyncServerPackPacket_SC contentSyncServerPackPacketSc, IPayloadContext iPayloadContext) {
        //NOP
    }
}
