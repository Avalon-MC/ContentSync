package net.petercashel.contentsync;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.gist.gistManager;
import net.petercashel.contentsync.network.ContentSyncServer;
import net.petercashel.contentsync.network.PacketHandler;
import org.slf4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("contentsync")
public class ContentSync
{
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MODID = "contentsync";

    public ContentSync(IEventBus bus, Dist dist)
    {
        // Register the setup method for modloading
        bus.addListener(this::setup);
        // Register the enqueueIMC method for modloading
        bus.addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        bus.addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(ForgeRegistryEvents.class);


        NeoForge.EVENT_BUS.register(ContentSyncEvents.class);


        bus.register(RegistryEvents.class);
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1")
                .executesOn(HandlerThread.NETWORK);
        PacketHandler.RegisterNetwork(registrar);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // Some example code to dispatch IMC to another mod
        //InterModComms.sendTo("contentsync", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // Some example code to receive and process InterModComms from other mods
//        LOGGER.info("Got IMC {}", event.getIMCStream().
//                map(m->m.messageSupplier().get()).
//                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        //LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server started");
        LOGGER.info("ContentSync ShareCode Status: " + gistManager.OnServerStarted(event).toString());
        LOGGER.info("ContentSync ShareCode: " + ContentSyncConfig.ConfigInstance.HostingServerSettings.gistAPISettings.CurrentGistCode);

    }


    public static class RegistryEvents
    {
//        @SubscribeEvent
//        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent)
//        {
//            // Register a new block here
//            //LOGGER.info("HELLO from Register Block");
//        }
    }

    public static class ForgeRegistryEvents
    {
        @SubscribeEvent
        public static void onPlayerJoined (final PlayerEvent.PlayerLoggedInEvent loggedInEvent) {

            if (loggedInEvent.getEntity().isLocalPlayer()) {
                return; //DONT FIRE FOR SP PLAYER

            }

            //HERE WE GO!
            ContentSyncServer.OnPlayerJoined(loggedInEvent);
        }
    }
}
