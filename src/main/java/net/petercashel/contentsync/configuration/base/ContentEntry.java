package net.petercashel.contentsync.configuration.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.data_formats.packrepo.PackRepoEntry;
import net.petercashel.contentsync.data_formats.packrepo.PackTypeEnum;
import net.petercashel.contentsync.utils.WebHelper;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static net.petercashel.contentsync.configuration.ContentSyncConfig.SaveConfig;
import static net.petercashel.contentsync.configuration.ContentSyncConfig.gson;

public abstract class ContentEntry implements IPackEntry {

    //region Instance Fields

    @Expose
    @SerializedName("name")
    public String Name = "";
    @SerializedName("displayName")
    @Expose
    public String displayName;
    @Expose
    public PackTypeEnum type = null;
    @Expose
    public String URL = "";
    @Expose
    public String targetVersion = ""; //Force a specific version, Testing only
    @Expose
    public String installedVersion = "";

    //endregion
    //region Helpers

    @Override
    public String GetDisplayName() {
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }
        return Name;
    }

    @Override
    public String GetName() {
        return Name;
    }

    @Override
    public String GetURL() {
        return URL;
    }

    @Override
    public String GetInstalledVersion() {
        return installedVersion;
    }
    @Override
    public String GetTargetVersion() {
        return targetVersion;
    }

    @Override
    public boolean IsResourcePack() {
        return type == PackTypeEnum.resourcepack;
    }

    @Override
    public boolean IsDataPack() {
        return type == PackTypeEnum.datapack;
    }

    @Override
    public boolean IsKubeJSPack() {
        return type == PackTypeEnum.kubejs;
    }

    //endregion

    //region Updater Temp Variables
    public boolean UpdateAvailable = false;
    public String cachedUpdatePath = null;
    public Optional<PackRepoEntry> targetEntry;
    //endregion

    public boolean CheckForUpdates() {
        boolean needsUpdate = type == null || installedVersion.isBlank();
        if (this.Name.isBlank() || this.Name.length() < 2) {
            return false;
        }
        try {
            String indexJson = WebHelper.DownloadURLAsString(URL);

            if (indexJson == null || indexJson.isBlank()) {
                return false;
            }

            Type listType = new TypeToken<List<PackRepoEntry>>() {
            }.getType();
            List<PackRepoEntry> entries = gson.fromJson(indexJson, listType);

            targetEntry = entries.stream().filter(x -> x.name.equals(Name)).findFirst();
            if (targetEntry.isEmpty()) {
                return false;
            }

            //Update local data.
            this.displayName = targetEntry.get().GetDisplayName();
            this.type = targetEntry.get().type;

            if (targetVersion.isBlank()) {
                UpdateAvailable = !targetEntry.get().IsLatestVersion(installedVersion);
            } else {
                //Specific
                if (!targetVersion.isBlank() && !installedVersion.equals(targetVersion) && targetEntry.get().IsValidVersion(targetVersion)) {
                    UpdateAvailable = true;
                }
            }

            if (!UpdateAvailable) {
                needsUpdate = false;
            } else {
                needsUpdate = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        UpdateAvailable = needsUpdate; //Make sure these are in sync
        return needsUpdate;
    }

    public void DownloadUpdate() throws IOException {
        String filepath = ContentSyncConfig.cacheFolder.getAbsolutePath() + File.separator + targetEntry.get().name + ".zip";
        cachedUpdatePath = WebHelper.DownloadURLtoFile(targetEntry.get().GetLatestVersion(targetVersion).url, filepath);
    }

    public void InstallUpdate() throws IOException {

        UpdateEnabledStatus(ContentSyncConfig.ConfigInstance.ClientSettings.lastServerAddress);
        UpdatePackMetaExtension();

        if (IsKubeJSPack()) {
            //More work
            File Target_Client = new File(ContentSyncConfig.kubejs_client_scripts + File.separator + targetEntry.get().name + File.separator);
            File Target_Server = new File(ContentSyncConfig.kubejs_server_scripts + File.separator + targetEntry.get().name + File.separator);
            File Target_Startup = new File(ContentSyncConfig.kubejs_startup_scripts + File.separator + targetEntry.get().name + File.separator);

            MakeClean(Target_Client);
            MakeClean(Target_Server);
            MakeClean(Target_Startup);

            ExtractUpdateKubeJSToFolder(Target_Client.getAbsolutePath(), Target_Server.getAbsolutePath(),Target_Startup.getAbsolutePath());

            installedVersion = targetEntry.get().GetLatestVersion(targetVersion).version;
            SaveConfig();

        } else {
            File TargetBase = null; //Unneeded, is always set but compiler

            if (IsDataPack()) TargetBase = ContentSyncConfig.openloader_datapacks;
            if (IsResourcePack()) TargetBase = ContentSyncConfig.openloader_resourcepacks;


            String filepath = TargetBase + File.separator + targetEntry.get().name + File.separator;
            File TargetFolder = new File(filepath);

            MakeClean(TargetFolder);

            ExtractUpdateToFolder(filepath);

            if (type == PackTypeEnum.resourcepack) ExtractUpdateToFolderResourcesExtra();

            installedVersion = targetEntry.get().GetLatestVersion(targetVersion).version;
            SaveConfig();
        }
    }


    public String GetFilePathBase () {
        File TargetBase = null; //Unneeded, is always set but compiler

        if (IsDataPack()) TargetBase = ContentSyncConfig.openloader_datapacks;
        if (IsResourcePack()) TargetBase = ContentSyncConfig.openloader_resourcepacks;


        String filepath = TargetBase + File.separator + targetEntry.get().name + File.separator;
        return filepath;
    }


    public abstract void UpdateEnabledStatus(String LastServerName);
    public abstract void UpdatePackMetaExtension();


    private void ExtractUpdateKubeJSToFolder(String clientPath, String serverPath, String startupPath) throws IOException {
        //buffer for read and write data to file
        byte[] buffer = new byte[2048];
        FileInputStream fileInputStream = new FileInputStream(cachedUpdatePath);
        ZipInputStream zipFileStream = new ZipInputStream(fileInputStream);
        ZipEntry zipEntry = zipFileStream.getNextEntry();

        while(zipEntry != null){
            String fileName = zipEntry.getName();

            String filepath;
            if (fileName.startsWith("client_scripts")) {
                filepath = clientPath;
                fileName = fileName.substring("client_scripts/".length());
                if (fileName.length() == 0) {
                    zipEntry = zipFileStream.getNextEntry();
                    continue;
                }
            }
            else if (fileName.startsWith("server_scripts")) {
                filepath = serverPath;
                fileName = fileName.substring("server_scripts/".length());
                if (fileName.length() == 0) {
                    zipEntry = zipFileStream.getNextEntry();
                    continue;
                }
            }
            else if (fileName.startsWith("startup_scripts")) {
                filepath = startupPath;
                fileName = fileName.substring("startup_scripts/".length());
                if (fileName.length() == 0) {
                    zipEntry = zipFileStream.getNextEntry();
                    continue;
                }
                ContentSyncConfig.HadKubeJSStartupScriptsUpdate = true;
            }
            else {
                zipEntry = zipFileStream.getNextEntry();
                continue;
            }

            File newFile = new File(filepath + File.separator + fileName);

            //create directories for sub directories in zip
            new File(newFile.getParent()).mkdirs();

            if (zipEntry.isDirectory()) {
                newFile.mkdirs();
            }
            else {
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zipFileStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();

                //close this ZipEntry
                zipFileStream.closeEntry();
            }

            zipEntry = zipFileStream.getNextEntry();
        }
        //close last ZipEntry
        zipFileStream.closeEntry();
        zipFileStream.close();
        fileInputStream.close();
    }

    private void ExtractUpdateToFolder(String filepath) throws IOException {
        //buffer for read and write data to file
        byte[] buffer = new byte[2048];
        FileInputStream fileInputStream = new FileInputStream(cachedUpdatePath);
        ZipInputStream zipFileStream = new ZipInputStream(fileInputStream);
        ZipEntry zipEntry = zipFileStream.getNextEntry();

        while(zipEntry != null){

            String fileName = zipEntry.getName();
            {
                File newFile = new File(filepath + File.separator + fileName);

                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                }
                else {
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zipFileStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();

                    //close this ZipEntry
                    zipFileStream.closeEntry();
                }
            }
            zipEntry = zipFileStream.getNextEntry();
        }
        //close last ZipEntry
        zipFileStream.closeEntry();
        zipFileStream.close();
        fileInputStream.close();
    }

    private void ExtractUpdateToFolderResourcesExtra() throws IOException {
        //buffer for read and write data to file
        byte[] buffer = new byte[2048];
        FileInputStream fileInputStream = new FileInputStream(cachedUpdatePath);
        ZipInputStream zipFileStream = new ZipInputStream(fileInputStream);
        ZipEntry zipEntry = zipFileStream.getNextEntry();

        while(zipEntry != null){

            String fileName = zipEntry.getName();
            {
                String filepath;
                if (fileName.startsWith("assets/kubejs")) {
                    filepath = ContentSyncConfig.kubejs_assets_kubejs.getAbsolutePath();
                    fileName = fileName.substring("assets/kubejs/".length());
                    if (fileName.length() == 0) {
                        zipEntry = zipFileStream.getNextEntry();
                        continue;
                    }
                }
                else {
                    zipEntry = zipFileStream.getNextEntry();
                    continue;
                }

                if (!zipEntry.isDirectory()) {
                    File newFile = new File(filepath + File.separator + fileName);

                    //create directories for sub directories in zip
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zipFileStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    //close this ZipEntry
                    zipFileStream.closeEntry();
                }
            }

            zipEntry = zipFileStream.getNextEntry();
        }
        //close last ZipEntry
        zipFileStream.closeEntry();
        zipFileStream.close();
        fileInputStream.close();
    }

    private void MakeClean(File TargetFolder) throws IOException {
        if (TargetFolder.exists())
        {
            FileUtils.deleteDirectory(TargetFolder);
        }
        if (!TargetFolder.exists()) TargetFolder.mkdirs();
    }

    public void CleanUpCacheFile() {
        File cache = new File(cachedUpdatePath);
        if (cache.exists()) {
            try {
                if (!cache.delete()) cache.deleteOnExit();
            } catch (Exception ex) {
                cache.deleteOnExit();
            }

        }
    }
}
