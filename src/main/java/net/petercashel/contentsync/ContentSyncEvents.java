package net.petercashel.contentsync;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.events.ConstructEventWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = "contentsync", bus = Mod.EventBusSubscriber.Bus.MOD)

public class ContentSyncEvents {


    @SubscribeEvent
    static void onConstructMod(FMLConstructModEvent event) {
//        ContentSyncConfig CSconfig = ContentSyncConfig.LoadConfig();
//        ContentSyncConfig.SaveConfig();
//
//        Dist dist = FMLEnvironment.dist;
//        ModLoadingContext context = ModLoadingContext.get();
//        Logger logger = LogManager.getLogger(context.getActiveContainer().getModId());
//
//        ContentSync contentSync = (ContentSync) context.getActiveContainer().getMod();
//        contentSync.contentSyncConfig = CSconfig;
//
//        if (CSconfig.IsConfigured) {
//            var runner = new ConstructEventWorker(logger, dist);
//            event.enqueueWork(runner);
//        }
    }
}
