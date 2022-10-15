package net.petercashel.contentsync.events;

import net.minecraftforge.api.distmarker.Dist;
import net.petercashel.contentsync.configuration.ContentEntry;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.earlystartupprogress.ClientEMS;
import net.petercashel.contentsync.earlystartupprogress.IEarlyMessageSystem;
import net.petercashel.contentsync.earlystartupprogress.ServerEMS;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ConstructEventWorker implements Runnable {
    private final Logger logger;
    private final Dist side;
    private final IEarlyMessageSystem ems;

    public ConstructEventWorker(Logger logger, Dist dist) {
        this.logger = logger;
        this.side = dist;

        if (dist == Dist.CLIENT) {
            ems = new ClientEMS();
        } else {
            ems = new ServerEMS();
        }
    }

    @Override
    public void run() {
        logger.info("WE RAN! ContentSync Ran!");

        ems.SetupMessageSystem(logger, side);
        ems.AddMessageToQueue("Testing", "Startup");
        ems.SetPrimaryProgressBar(1,5);
        try {
            Thread.sleep(200); //Allow window to wake up
            Thread.sleep(200);
            Thread.sleep(200);
            Thread.sleep(200);
            Thread.sleep(200);

            DoWorkerTask();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ems.ShutdownMessageSystem();
        }


        logger.info("WE RAN! ContentSync SLEPT!");
    }

    private boolean needToRunUpdate = false;
    private int currentTaskIndex = 0;
    private int totalTasks = 0;

    private void DoWorkerTask() throws InterruptedException {
        CheckForUpdates();

        if (needToRunUpdate) {
            for (int i = 0; i < ContentSyncConfig.ConfigInstance.contentEntriesList.size(); i++) {
                ContentEntry entry = ContentSyncConfig.ConfigInstance.contentEntriesList.get(i);
                if (entry.UpdateAvailable) {
                    try {
                        ems.AddMessageToQueue("Downloading Update", entry.GetDisplayName());
                        currentTaskIndex++;
                        ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);
                        entry.DownloadUpdate();
                        ems.AddMessageToQueue("Installing Update", entry.GetDisplayName());
                        currentTaskIndex++;
                        ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);
                        entry.InstallUpdate();
                        entry.CleanUpCacheFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        ems.AddMessageToQueue("Error Installing Update", entry.GetDisplayName());
                        Thread.sleep(1000);
                    }
                }

            }
            currentTaskIndex = totalTasks;
            ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);
            ems.AddMessageToQueue("Done!", "Have a great day!");
        }
    }

    private void CheckForUpdates() throws InterruptedException {
        currentTaskIndex = 0;
        totalTasks = ContentSyncConfig.ConfigInstance.contentEntriesList.size();
        ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);

        for (int i = 0; i < ContentSyncConfig.ConfigInstance.contentEntriesList.size(); i++) {
            ems.AddMessageToQueue("Checking for Updates", ContentSyncConfig.ConfigInstance.contentEntriesList.get(i).GetDisplayName());
            boolean hasUpdates = ContentSyncConfig.ConfigInstance.contentEntriesList.get(i).CheckForUpdates();
            if (hasUpdates) {
                totalTasks += 2;
                needToRunUpdate = true;
            }

            currentTaskIndex++;
            ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);
            Thread.sleep(200);
        }

        ContentSyncConfig.SaveConfig();
    }

}
