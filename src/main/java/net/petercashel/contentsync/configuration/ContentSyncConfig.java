package net.petercashel.contentsync.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
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
    public boolean IsConfigured = false;
    @Expose
    public boolean DisableUI = false;
    @Expose
    public List<ContentEntry> contentEntriesList = new ArrayList<ContentEntry>();




    // File Paths

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
            CSconfig.contentEntriesList.add(new ContentEntry()); //Default
            cfgFile.getParentFile().mkdirs();
        }
        return CSconfig;
    }

    public static ContentSyncConfig LoadConfig() {
        ConfigInstance = LoadConfig(cfgFile, ConfigInstance);
        return ConfigInstance;
    }

    public static void SaveConfig() {
        SaveConfig(cfgFile, ConfigInstance);
    }
}
