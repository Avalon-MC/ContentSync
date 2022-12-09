package net.petercashel.contentsync.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.petercashel.contentsync.configuration.base.IPackEntry;
import net.petercashel.contentsync.configuration.modpack.ModpackContentEntry;
import net.petercashel.contentsync.configuration.server.ServerContentEntry;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ContentSyncConfig {


    @Expose
    public int ConfigVersion = 2;

    @Expose
    public CommonSettings CommonSettings = new CommonSettings();
    public class CommonSettings {
        @Expose
        public boolean IsConfigured = false;
        @Expose
        public boolean HideMenuButton = false;
        @Expose
        public boolean DisableUI = false;
    }

    @Expose
    public CommonPackSettings CommonPackSettings = new CommonPackSettings();
    public class CommonPackSettings {
        @Expose
        public List<ModpackContentEntry> contentEntriesList = new ArrayList<ModpackContentEntry>();

    }

    @Expose
    public ServerPackSettings ServerPackSettings = new ServerPackSettings();
    public class ServerPackSettings {
        @Expose
        public List<ServerContentEntry> serverContentEntriesList = new ArrayList<ServerContentEntry>();

    }


    @Expose
    public HostingServerSettings HostingServerSettings = new HostingServerSettings();
    public class HostingServerSettings {
        @Expose
        public String ThisServerAddress = "";
        @Expose
        public boolean EnforceServerPacks = true;

    }

    @Expose
    public ClientSettings ClientSettings = new ClientSettings();
    public class ClientSettings {
        @Expose
        public String lastServerAddress = "";

    }









    public ContentSyncConfig() {
        try {
            MakeClean(cacheFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!kubejs_client_scripts.exists()) kubejs_client_scripts.mkdirs();
        if (!kubejs_server_scripts.exists()) kubejs_server_scripts.mkdirs();
        if (!kubejs_startup_scripts.exists()) kubejs_startup_scripts.mkdirs();
        if (!openloader_datapacks.exists()) openloader_datapacks.mkdirs();
        if (!openloader_resourcepacks.exists()) openloader_resourcepacks.mkdirs();

    }

    private void MakeClean(File TargetFolder) throws IOException {
        if (TargetFolder.exists())
        {
            FileUtils.deleteDirectory(TargetFolder);
        }
        if (!TargetFolder.exists()) TargetFolder.mkdirs();
    }


    private void Migrate() {


    }


    //Static Stuff

    // File Paths

    public static File kubejs_assets_kubejs = new File("kubejs/assets/kubejs/").getAbsoluteFile();


    public static File kubejs_client_scripts = new File("kubejs/client_scripts/").getAbsoluteFile();
    public static File kubejs_server_scripts = new File("kubejs/server_scripts/").getAbsoluteFile();
    public static File kubejs_startup_scripts = new File("kubejs/startup_scripts/").getAbsoluteFile();
    public static boolean HadKubeJSStartupScriptsUpdate = false;

    public static File openloader_datapacks = new File("config/openloader/data/").getAbsoluteFile();
    public static File openloader_resourcepacks = new File("config/openloader/resources/").getAbsoluteFile();


    public static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
    public static File cfgFile = new File("contentsync/config.json").getAbsoluteFile();
    public static File cacheFolder = new File("contentsync/downloadCache/").getAbsoluteFile();
    public static ContentSyncConfig ConfigInstance = new ContentSyncConfig();

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
            if (CSconfig.ConfigVersion < 3) {
                //Load Old format in new structure
                try {
                    // create a reader
                    Reader reader = Files.newBufferedReader(cfgFile.toPath());

                    var CommonSettings = gson.fromJson(reader, ContentSyncConfig.CommonSettings.class);
                    reader.close();
                    reader = Files.newBufferedReader(cfgFile.toPath());
                    var CommonPackSettings = gson.fromJson(reader, ContentSyncConfig.CommonPackSettings.class);
                    reader.close();
                    reader = Files.newBufferedReader(cfgFile.toPath());
                    var ServerPackSettings = gson.fromJson(reader, ContentSyncConfig.ServerPackSettings.class);
                    reader.close();
                    reader = Files.newBufferedReader(cfgFile.toPath());
                    var HostingServerSettings = gson.fromJson(reader, ContentSyncConfig.HostingServerSettings.class);
                    reader.close();
                    reader = Files.newBufferedReader(cfgFile.toPath());
                    var ClientSettings = gson.fromJson(reader, ContentSyncConfig.ClientSettings.class);

                    // close reader
                    reader.close();


                    CSconfig = new ContentSyncConfig();
                    CSconfig.ConfigVersion = 3;

                    if (CommonSettings != null) {
                        CSconfig.CommonSettings = CommonSettings;
                    }
                    if (CommonPackSettings != null) {
                        CSconfig.CommonPackSettings = CommonPackSettings;
                    }
                    if (ServerPackSettings != null) {
                        CSconfig.ServerPackSettings = ServerPackSettings;
                    }
                    if (HostingServerSettings != null) {
                        CSconfig.HostingServerSettings = HostingServerSettings;
                    }
                    if (ClientSettings != null) {
                        CSconfig.ClientSettings = ClientSettings;
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
        if (!cfgFile.exists() || CSconfig == null) {
            CSconfig = new ContentSyncConfig();
            CSconfig.CommonPackSettings.contentEntriesList.add(new ModpackContentEntry()); //Default
            CSconfig.ServerPackSettings.serverContentEntriesList.add(new ServerContentEntry()); //Default
            cfgFile.getParentFile().mkdirs();
            CSconfig.ConfigVersion = 3;
        }
        return CSconfig;
    }

    public static ContentSyncConfig LoadConfig() {
        ConfigInstance = LoadConfig(cfgFile, ConfigInstance);

        if (ConfigInstance.ConfigVersion < 3) {
            ConfigInstance.ConfigVersion = 3;
            ConfigInstance.Migrate(); //Migrate for 3+;
        }

        if (ConfigInstance.ServerPackSettings.serverContentEntriesList == null) {
            ConfigInstance.ServerPackSettings.serverContentEntriesList = new ArrayList<>();
        }
        if (ConfigInstance.CommonPackSettings.contentEntriesList == null) {
            ConfigInstance.CommonPackSettings.contentEntriesList = new ArrayList<>();
        }

        if (ConfigInstance.HostingServerSettings.ThisServerAddress == null) {
            ConfigInstance.HostingServerSettings.ThisServerAddress = "";
        }

        if (ConfigInstance.ClientSettings.lastServerAddress == null) {
            ConfigInstance.ClientSettings.lastServerAddress = "";
        }

        if (!ConfigInstance.ServerPackSettings.serverContentEntriesList.isEmpty() || !ConfigInstance.CommonPackSettings.contentEntriesList.isEmpty()) {
            ConfigInstance.CommonSettings.IsConfigured = true;
        }

        //Finally
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            //Enforce proper DS settings
            ConfigInstance.CommonSettings.DisableUI = true;
            ConfigInstance.CommonSettings.HideMenuButton = true;
        }

        return ConfigInstance;
    }

    public static void SaveConfig() {
        SaveConfig(cfgFile, ConfigInstance);
    }

    public List<IPackEntry> GetAllPackEntries() {
        ArrayList<IPackEntry> entries = new ArrayList<>();

        entries.addAll(CommonPackSettings.contentEntriesList);
        entries.addAll(ServerPackSettings.serverContentEntriesList);

        return entries;
    }

    public void ToggleEntry(IPackEntry entry) {
        for (ServerContentEntry serverContentEntry : ServerPackSettings.serverContentEntriesList) {
            if (serverContentEntry.GetName().equals(entry.GetName())) {
                serverContentEntry.Enabled = !serverContentEntry.Enabled;
            }
        }
        SaveConfig();
    }
}
