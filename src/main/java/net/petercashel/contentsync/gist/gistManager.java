package net.petercashel.contentsync.gist;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.configuration.server.ServerContentEntry;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistBuilder;
import org.kohsuke.github.GHGistUpdater;
import org.kohsuke.github.GitHub;

import java.io.IOException;

public class gistManager {

    private static gistManager Instance = null;

    public static gistManager GetGistManager() throws IOException {
        if (Instance == null) {
            Instance = new gistManager();
        }
        return Instance;
    }

    public GitHub github;

    public gistManager() throws IOException {
        github = GitHub.connectUsingOAuth(ContentSyncConfig.ConfigInstance.HostingServerSettings.gistAPISettings.PersonalAccessToken);
    }

    public enum GistManagerStatus {
        None,
        NoShareCodeRequired,
        InvalidCredentials,
        CreatedGist,
        UpdatedGist,
        Error
    }

    public static GistManagerStatus CreateOrUpdateGist() {
        if (!ValidateCredentials()) {
            return GistManagerStatus.InvalidCredentials;
        }

        if (HasGistCode()) {
            return UpdateGist();
        } else {
            return CreateGist();
        }

    }

    private static GistManagerStatus CreateGist() {

        String content = ServerContentEntry.GetShareCodeContent();
        GHGistBuilder builder = Instance.github.createGist();

        builder = builder.description(ContentSyncConfig.ConfigInstance.HostingServerSettings.ThisServerAddress  + " Share Code")
                .public_(true).file("ServerName.txt", ContentSyncConfig.ConfigInstance.HostingServerSettings.ThisServerAddress);
        builder = builder.file("sharecode.txt", content).file("EnforceServerPacks.txt", Boolean.toString(ContentSyncConfig.ConfigInstance.HostingServerSettings.EnforceServerPacks));

        try {
            GHGist gist = builder.create();
            ContentSyncConfig.ConfigInstance.HostingServerSettings.gistAPISettings.CurrentGistCode = gist.getGistId();
            ContentSyncConfig.SaveConfig();
        } catch (IOException e) {
            e.printStackTrace();
            return GistManagerStatus.Error;
        }

        return GistManagerStatus.CreatedGist;
    }

    private static GistManagerStatus UpdateGist() {

        String content = ServerContentEntry.GetShareCodeContent();
        try {
            GHGist gist = Instance.github.getGist(ContentSyncConfig.ConfigInstance.HostingServerSettings.gistAPISettings.CurrentGistCode);

            if (gist.getFile("sharecode.txt").getContent().equals(content) && gist.getFiles().keySet().size() == 3) {
                //No Update needed
                return GistManagerStatus.UpdatedGist;
            }

            GHGistUpdater updater = gist.update().updateFile("sharecode.txt", content);
            if (gist.getFiles().keySet().contains("EnforceServerPacks.txt")) {
                updater = updater.updateFile("EnforceServerPacks.txt", Boolean.toString(ContentSyncConfig.ConfigInstance.HostingServerSettings.EnforceServerPacks));
            } else {
                updater = updater.addFile("EnforceServerPacks.txt", Boolean.toString(ContentSyncConfig.ConfigInstance.HostingServerSettings.EnforceServerPacks));
            }
            if (gist.getFiles().keySet().contains("ServerName.txt")) {
                updater = updater.updateFile("ServerName.txt", ContentSyncConfig.ConfigInstance.HostingServerSettings.ThisServerAddress);
            } else {
                updater = updater.addFile("ServerName.txt", ContentSyncConfig.ConfigInstance.HostingServerSettings.ThisServerAddress);
            }

            gist = updater.update();
            ContentSyncConfig.ConfigInstance.HostingServerSettings.gistAPISettings.CurrentGistCode = gist.getGistId();
            ContentSyncConfig.SaveConfig();

        } catch (IOException e) {
            e.printStackTrace();
            return GistManagerStatus.Error;
        }

        return GistManagerStatus.UpdatedGist;
    }

    private static boolean HasGistCode() {
        if (ContentSyncConfig.ConfigInstance.HostingServerSettings.gistAPISettings.CurrentGistCode == null || ContentSyncConfig.ConfigInstance.HostingServerSettings.gistAPISettings.CurrentGistCode.isEmpty() || ContentSyncConfig.ConfigInstance.HostingServerSettings.gistAPISettings.CurrentGistCode.isBlank()) {
            return false;
        }

        return true;
    }

    private static boolean ValidateCredentials(){
        if (ContentSyncConfig.ConfigInstance.HostingServerSettings.gistAPISettings.PersonalAccessToken == null || ContentSyncConfig.ConfigInstance.HostingServerSettings.gistAPISettings.PersonalAccessToken.isBlank() || ContentSyncConfig.ConfigInstance.HostingServerSettings.gistAPISettings.PersonalAccessToken.isEmpty()) {
            return false;
        }

        try {
            GetGistManager();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        //Default true
        return true;
    }


    public static GistManagerStatus OnServerStarted(ServerStartedEvent event) {

        return CreateOrUpdateGist();

    }

}
