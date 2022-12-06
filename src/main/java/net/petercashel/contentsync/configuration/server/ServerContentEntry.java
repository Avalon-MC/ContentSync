package net.petercashel.contentsync.configuration.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.nbt.CompoundTag;
import net.petercashel.contentsync.configuration.base.ContentEntry;
import net.petercashel.contentsync.data_formats.packrepo.PackTypeEnum;

import java.io.File;

public class ServerContentEntry extends ContentEntry {

    @Expose
    @SerializedName("servername")
    public String ServerName = "";

    @Expose
    @SerializedName("enabled")
    public boolean Enabled = false;

    @Expose
    @SerializedName("serverOnly")
    public boolean ServerOnly = false;

    @Expose
    @SerializedName("clientOnly")
    public boolean ClientOnly = false;

    @Override
    public void UpdateEnabledStatus(String LastServerName) {
        Enabled = ServerName.equals(LastServerName);
    }

    @Override
    public void UpdatePackMetaExtension() {
        if (IsKubeJSPack()) return; //TODO figure out how to do KubeJS packs.


        File enabledPackMeta = new File(GetFilePathBase() + "pack.mcmeta");
        File disabledPackMeta = new File(GetFilePathBase() + "pack.mcmeta.disabled");

        if (Enabled) {
            //Check if disabled meta exists, if enabled doesnt, SWITCH.
            // if exists, uhhh... delete?
            if (disabledPackMeta.exists()) {
                if (enabledPackMeta.exists() == false) {
                    disabledPackMeta.renameTo(enabledPackMeta);
                } else {
                    disabledPackMeta.delete();
                }
            }
        } else {
            //As above but reverse
            if (enabledPackMeta.exists()) {
                if (disabledPackMeta.exists() == false) {
                    enabledPackMeta.renameTo(disabledPackMeta);
                } else {
                    enabledPackMeta.delete();
                }
            }
        }
    }


    public CompoundTag serialise(CompoundTag itemTag) {
        itemTag.putInt("packtype", this.type.ordinal());
        itemTag.putString("name", this.Name);
        itemTag.putString("url", this.URL);

        return itemTag;
    }

    public static ServerContentEntry deserialise(CompoundTag itemTag) {
        ServerContentEntry entry = new ServerContentEntry();

        entry.type = PackTypeEnum.values()[itemTag.getInt("packtype")];
        entry.Name = itemTag.getString("name");
        entry.URL = itemTag.getString("url");

        return entry;
    }

    @Override
    public boolean IsServerPack() {
        return true;
    }

    @Override
    public boolean IsEnabled() {
        return Enabled;
    }
}
