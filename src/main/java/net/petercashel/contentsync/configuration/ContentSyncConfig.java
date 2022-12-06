package net.petercashel.contentsync.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
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

    @Expose
    public int ConfigVersion;

    @Expose
    public boolean IsConfigured = false;
    @Expose
    public boolean HideMenuButton = false;
    @Expose
    public boolean DisableUI = false;
    @Expose
    public List<ModpackContentEntry> contentEntriesList = new ArrayList<ModpackContentEntry>();


    @Expose
    public String ThisServerAddress = "";
    @Expose
    public String lastServerAddress = "";
    @Expose
    public List<ServerContentEntry> serverContentEntriesList = new ArrayList<ServerContentEntry>();




    private void Migrate() {
        if (ConfigVersion == 0) {
            this.serverContentEntriesList.add(new ServerContentEntry()); //Default
            this.lastServerAddress = "";

            ConfigVersion = 1;
        }
        if (ConfigVersion == 1) {
            //Technically, 0 is v1, but 0  is the default value. so jump to 2.
            ConfigVersion = 2;
        }



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
        }
        if (!cfgFile.exists() || CSconfig == null) {
            CSconfig = new ContentSyncConfig();
            CSconfig.contentEntriesList.add(new ModpackContentEntry()); //Default
            cfgFile.getParentFile().mkdirs();
            CSconfig.ConfigVersion = 0; //Force Upgrade Migration on new file.
        }
        CSconfig.Migrate();
        return CSconfig;
    }

    public static ContentSyncConfig LoadConfig() {
        ConfigInstance = LoadConfig(cfgFile, ConfigInstance);
        return ConfigInstance;
    }

    public static void SaveConfig() {
        SaveConfig(cfgFile, ConfigInstance);
    }

    public List<IPackEntry> GetAllPackEntries() {
        ArrayList<IPackEntry> entries = new ArrayList<>();

        entries.addAll(contentEntriesList);
        entries.addAll(serverContentEntriesList);

        return entries;
    }

    public void ToggleEntry(IPackEntry entry) {
        for (ServerContentEntry serverContentEntry : serverContentEntriesList) {
            if (serverContentEntry.GetName().equals(entry.GetName())) {
                serverContentEntry.Enabled = !serverContentEntry.Enabled;
            }
        }
        SaveConfig();
    }
}
