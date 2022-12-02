package net.petercashel.contentsync.network;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.configuration.server.ServerContentEntry;
import net.petercashel.contentsync.events.ClientOnJoinEventWorker;
import net.petercashel.contentsync.events.ConstructEventWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class ContentSyncClient {

    public static void Process(List<ServerContentEntry> serverContentEntriesList, String ServerName) {
        //Handle Ingest to configs
        boolean restartNeeded = false;

        if (ContentSyncConfig.ConfigInstance.lastServerAddress.equals(ServerName) == false) {
            ContentSyncConfig.ConfigInstance.lastServerAddress = ServerName;
            restartNeeded = true;
            ContentSyncConfig.SaveConfig();
        }

        for (ServerContentEntry entry : serverContentEntriesList) {
            entry.ServerName = ServerName;

            //LONG WAY TO SAY, if we dont have ANY entries matching that name
            if (ContentSyncConfig.ConfigInstance.serverContentEntriesList.stream().anyMatch(x -> x.Name.toLowerCase().equals(entry.Name.toLowerCase())) == false) {
                //Add it
                ContentSyncConfig.ConfigInstance.serverContentEntriesList.add(entry);
                restartNeeded = true;

            } else if (ContentSyncConfig.ConfigInstance.serverContentEntriesList.stream().anyMatch(x -> x.Name.toLowerCase().equals(entry.Name.toLowerCase())) == true) {
                //Find it
                Optional<ServerContentEntry> existing = ContentSyncConfig.ConfigInstance.serverContentEntriesList.stream().filter(x -> x.Name.toLowerCase().equals(entry.Name.toLowerCase())).findFirst();
                if (existing.isPresent()) {
                    //Ok. Get the index, access via list for safety and just update the URL
                    ContentSyncConfig.ConfigInstance.serverContentEntriesList.get(ContentSyncConfig.ConfigInstance.serverContentEntriesList.indexOf(existing.get())).URL = entry.URL;
                }
            }
        }

        ContentSyncConfig.SaveConfig();

        //Install

        Dist dist = FMLEnvironment.dist;
        ModLoadingContext context = ModLoadingContext.get();
        Logger logger = LogManager.getLogger(context.getActiveContainer().getModId());

        var runner = new ClientOnJoinEventWorker(logger, dist);

        Thread thread = new Thread(runner);
        thread.setDaemon(true);
        thread.setName("ContentSync MP Updater");

        //Should this have a thread? it is, its fine.
        //runner.run();
        thread.start();

        if (restartNeeded || runner.needToRunUpdate) {
            //Ok! Time to tell the client
            LocalPlayer player = Minecraft.getInstance().player;
            
            player.sendMessage(new TextComponent("ContentSync has added or updated content packs."), Util.NIL_UUID);
            player.sendMessage(new TextComponent("The server staff will instruct you if a restart is required."), Util.NIL_UUID);
            player.sendMessage(new TextComponent("Alternately if you wish to apply any resourcepack changes now, Hold F3 and press T, to reload them."), Util.NIL_UUID);

        }

    }
}
