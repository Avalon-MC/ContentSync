package net.petercashel.contentsync.events;

import net.minecraftforge.api.distmarker.Dist;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.configuration.modpack.ModpackContentEntry;
import net.petercashel.contentsync.configuration.server.ServerContentEntry;
import net.petercashel.contentsync.earlystartupprogress.ClientEMS;
import net.petercashel.contentsync.earlystartupprogress.IEarlyMessageSystem;
import net.petercashel.contentsync.earlystartupprogress.ServerEMS;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public abstract class BaseContentSyncWorker implements Runnable {
    private final Logger logger;
    private final Dist side;
    private final IEarlyMessageSystem ems;
    protected int StartupSleepTime = 0;
    public boolean DoGlobalPacks = true;
    public boolean DoServerPacks = true;

    public BaseContentSyncWorker(Logger logger, Dist dist, IEarlyMessageSystem ems) {
        this.logger = logger;
        this.side = dist;
        this.ems = ems;
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
        //Allow for different variants to wait for things like screen startup or game login
        if (StartupSleepTime != 0) Thread.sleep(StartupSleepTime);

        CheckForUpdates();
        Thread.sleep(200);

        if (needToRunUpdate) {
            if (DoGlobalPacks) {
                for (int i = 0; i < ContentSyncConfig.ConfigInstance.CommonPackSettings.contentEntriesList.size(); i++) {
                    ModpackContentEntry entry = ContentSyncConfig.ConfigInstance.CommonPackSettings.contentEntriesList.get(i);
                    if (entry.UpdateAvailable) {
                        try {
                            ems.AddMessageToQueue("Downloading ContentPack Update", entry.GetDisplayName());
                            currentTaskIndex++;
                            ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);
                            entry.DownloadUpdate();
                            Thread.sleep(200);
                            ems.AddMessageToQueue("Installing ContentPack Update", entry.GetDisplayName());
                            currentTaskIndex++;
                            ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);
                            entry.InstallUpdate();
                            Thread.sleep(200);
                            entry.CleanUpCacheFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                            ems.AddMessageToQueue("Error Installing ContentPack Update", entry.GetDisplayName());
                            Thread.sleep(1000);
                        }
                    }
                }
            }

            if (DoServerPacks) {
                for (int i = 0; i < ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.size(); i++) {
                    ServerContentEntry entry = ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.get(i);
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
            }

            Thread.sleep(200);
            currentTaskIndex = totalTasks;
            ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);

            //Done
            ems.AddMessageToQueue("Update Complete", "Have a great day!");
            Thread.sleep(200);
        }

    }

    private void CheckForUpdates() throws InterruptedException {
        currentTaskIndex = 0;
        //totalTasks = ContentSyncConfig.ConfigInstance.contentEntriesList.size() + ContentSyncConfig.ConfigInstance.serverContentEntriesList.size();
        totalTasks = GetTotalTasks();
        ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);

        if (DoGlobalPacks) {
            for (int i = 0; i < ContentSyncConfig.ConfigInstance.CommonPackSettings.contentEntriesList.size(); i++) {
                ems.AddMessageToQueue("Checking for ContentPack Updates", ContentSyncConfig.ConfigInstance.CommonPackSettings.contentEntriesList.get(i).GetDisplayName());
                boolean hasUpdates = ContentSyncConfig.ConfigInstance.CommonPackSettings.contentEntriesList.get(i).CheckForUpdates();
                if (hasUpdates) {
                    totalTasks += 2;
                    needToRunUpdate = true;
                }

                currentTaskIndex++;
                ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);
                Thread.sleep(200);
            }
        }

        if (DoServerPacks) {
            for (int i = 0; i < ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.size(); i++) {
                ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.get(i).UpdateEnabledStatus(ContentSyncConfig.ConfigInstance.ClientSettings.lastServerAddress);
                if (ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.get(i).Enabled) {
                    ems.AddMessageToQueue("Checking for ServerPack Updates", ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.get(i).GetDisplayName());
                    boolean hasUpdates = ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.get(i).CheckForUpdates();
                    if (hasUpdates && isCorrectSide(side, ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.get(i))) {
                        totalTasks += 2;
                        needToRunUpdate = true;
                    }

                    currentTaskIndex++;
                    ems.SetPrimaryProgressBar(currentTaskIndex, totalTasks);
                    Thread.sleep(200);
                }
            }
        }

        ContentSyncConfig.SaveConfig();
    }

    public int GetTotalTasks() {
        if (DoGlobalPacks && DoServerPacks) return ContentSyncConfig.ConfigInstance.CommonPackSettings.contentEntriesList.size() + ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.size();
        if (DoGlobalPacks) return ContentSyncConfig.ConfigInstance.CommonPackSettings.contentEntriesList.size();
        if (DoServerPacks) return ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.size();
        return 0;
    }

    private boolean isCorrectSide(Dist side, ServerContentEntry serverContentEntry) {
        if (side == Dist.DEDICATED_SERVER   && serverContentEntry.ClientOnly == false) return true;
        if (side == Dist.CLIENT             && serverContentEntry.ServerOnly == false) return true;

        return false;
    }

}
