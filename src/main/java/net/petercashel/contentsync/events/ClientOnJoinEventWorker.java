package net.petercashel.contentsync.events;

import net.minecraftforge.api.distmarker.Dist;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.configuration.modpack.ModpackContentEntry;
import net.petercashel.contentsync.configuration.server.ServerContentEntry;
import net.petercashel.contentsync.earlystartupprogress.ClientEMS;
import net.petercashel.contentsync.earlystartupprogress.IEarlyMessageSystem;
import net.petercashel.contentsync.earlystartupprogress.IngameEMS;
import net.petercashel.contentsync.earlystartupprogress.ServerEMS;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ClientOnJoinEventWorker implements Runnable {
    private final Logger logger;
    private final Dist side;
    private final IEarlyMessageSystem ems;

    public ClientOnJoinEventWorker(Logger logger, Dist dist) {
        this.logger = logger;
        this.side = dist;

        ems = new IngameEMS();
    }

    @Override
    public void run() {
        ems.SetupMessageSystem(logger, side);
        ems.SetPrimaryProgressBar(1,5);
        try {
            DoWorkerTask();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ems.ShutdownMessageSystem();
        }
    }

    public boolean needToRunUpdate = false;
    private int currentTaskIndex = 0;
    private int totalTasks = 0;

    private void DoWorkerTask() throws InterruptedException {
        CheckForUpdates();
        Thread.sleep(200);

        if (needToRunUpdate) {
            for (int i = 0; i < ContentSyncConfig.ConfigInstance.serverContentEntriesList.size(); i++) {
                ServerContentEntry entry = ContentSyncConfig.ConfigInstance.serverContentEntriesList.get(i);
                if (entry.UpdateAvailable) {
                    try {
                        ems.AddMessageToQueue("Downloading ServerPack Update", entry.GetDisplayName());
                        currentTaskIndex++;
                        ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);
                        entry.DownloadUpdate();
                        Thread.sleep(200);
                        ems.AddMessageToQueue("Installing ServerPack Update", entry.GetDisplayName());
                        currentTaskIndex++;
                        ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);
                        entry.InstallUpdate();
                        Thread.sleep(200);
                        entry.CleanUpCacheFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        ems.AddMessageToQueue("Error Installing ServerPack Update", entry.GetDisplayName());
                        Thread.sleep(1000);
                    }
                }

            }

            Thread.sleep(200);
            currentTaskIndex = totalTasks;
            ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);

            ems.AddMessageToQueue("Update Complete", "Have a great day! If you wish to apply any resoucepack changes now, Hold F3 and press T, to reload textures.");
            Thread.sleep(200);
        }

    }

    private void CheckForUpdates() throws InterruptedException {
        currentTaskIndex = 0;
        totalTasks = ContentSyncConfig.ConfigInstance.serverContentEntriesList.size();
        ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);

        for (int i = 0; i < ContentSyncConfig.ConfigInstance.serverContentEntriesList.size(); i++) {
            ContentSyncConfig.ConfigInstance.serverContentEntriesList.get(i).UpdateEnabledStatus(ContentSyncConfig.ConfigInstance.lastServerAddress);
            if (ContentSyncConfig.ConfigInstance.serverContentEntriesList.get(i).Enabled) {
                ems.AddMessageToQueue("Checking for ServerPack Updates", ContentSyncConfig.ConfigInstance.serverContentEntriesList.get(i).GetDisplayName());
                boolean hasUpdates = ContentSyncConfig.ConfigInstance.serverContentEntriesList.get(i).CheckForUpdates();
                if (hasUpdates && isCorrectSide(side, ContentSyncConfig.ConfigInstance.serverContentEntriesList.get(i))) {
                    totalTasks += 2;
                    needToRunUpdate = true;
                }

                currentTaskIndex++;
                ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);
                Thread.sleep(200);
            }
        }

        ContentSyncConfig.SaveConfig();
    }

    private boolean isCorrectSide(Dist side, ServerContentEntry serverContentEntry) {
        if (side == Dist.DEDICATED_SERVER   && serverContentEntry.ClientOnly == false) return true;
        if (side == Dist.CLIENT             && serverContentEntry.ServerOnly == false) return true;

        return false;
    }

}
