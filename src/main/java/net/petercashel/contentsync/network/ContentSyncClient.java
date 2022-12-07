package net.petercashel.contentsync.network;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
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
        TriggerScreen(restartNeeded);
    }

    private static void TriggerScreen(boolean restartNeeded) {
        boolean flag = true;
        String string1 = "This server requires the use of custom ContentSync server resource packs.";
        String string2 = "Rejecting the custom server resource packs will disconnect you from this server.";

        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().setScreen(new ConfirmScreen((accepted) -> {
                Minecraft.getInstance().setScreen((Screen)null);
                if (accepted) {
                    //Do the update
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

                    }
                    Minecraft.getInstance().reloadResourcePacks();

                } else {
                    //Go away
                    Minecraft.getInstance().getConnection().getConnection().disconnect(new TranslatableComponent("multiplayer.requiredTexturePrompt.disconnect"));
                }

                //flag ? new TranslatableComponent("multiplayer.requiredTexturePrompt.line1") : new TranslatableComponent("multiplayer.texturePrompt.line1")
                //((Component)(flag ? (new TranslatableComponent("multiplayer.requiredTexturePrompt.line2")).withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}) : new TranslatableComponent("multiplayer.texturePrompt.line2")))

            }, new TextComponent(string1) ,
                    ((Component)((new TextComponent(string2)).withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}) )),
                    flag ? CommonComponents.GUI_PROCEED : CommonComponents.GUI_YES,
                    (Component)(flag ? new TranslatableComponent("menu.disconnect") : CommonComponents.GUI_NO)));
        });
    }
}
