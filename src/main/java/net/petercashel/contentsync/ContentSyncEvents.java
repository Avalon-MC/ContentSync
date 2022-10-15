package net.petercashel.contentsync;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.progress.*;
import net.petercashel.contentsync.configuration.ContentEntry;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.events.ConstructEventWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Files;

@Mod.EventBusSubscriber(modid = "contentsync", bus = Mod.EventBusSubscriber.Bus.MOD)

public class ContentSyncEvents {
    static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    @SubscribeEvent
    static void onConstructMod(FMLConstructModEvent event) {
        ContentSyncConfig CSconfig = new ContentSyncConfig();
        File cfgFile = new File("contentsync/config.json").getAbsoluteFile();
        CSconfig = LoadConfig(cfgFile, CSconfig);
        SaveConfig(cfgFile, CSconfig);

        Dist dist = FMLEnvironment.dist;
        ModLoadingContext context = ModLoadingContext.get();

        ContentSync contentSync = (ContentSync) context.getActiveContainer().getMod();
        contentSync.contentSyncConfig = CSconfig;

        Logger logger = LogManager.getLogger(context.getActiveContainer().getModId());
        logger.info("WE RAN! ContentSync Queue!");
        event.enqueueWork(new ConstructEventWorker(logger, dist));

        StartupMessageManager.modLoaderConsumer().ifPresent(c->c.accept("This is a test"));


    }

    private static void SaveConfig(File cfgFile, ContentSyncConfig CSconfig) {
        try {
            try(FileWriter writer = new FileWriter(cfgFile.getAbsoluteFile().getPath())) {
                gson.toJson(CSconfig, writer);
                writer.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static ContentSyncConfig LoadConfig(File cfgFile, ContentSyncConfig CSconfig) {
        if (cfgFile.exists()) {
            try {
                // create a reader
                Reader reader = Files.newBufferedReader(cfgFile.toPath());

                CSconfig = gson.fromJson(reader, ContentSyncConfig.class);

                // close reader
                reader.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (!cfgFile.exists() || CSconfig == null) {
            CSconfig = new ContentSyncConfig();
            CSconfig.contentEntriesList.add(new ContentEntry()); //Default
            cfgFile.getParentFile().mkdirs();
        }
        return CSconfig;
    }


}
