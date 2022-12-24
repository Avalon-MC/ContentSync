package net.petercashel.contentsync.configuration.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import net.minecraft.nbt.CompoundTag;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.configuration.base.ContentEntry;
import net.petercashel.contentsync.data_formats.packrepo.PackTypeEnum;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ServerContentEntry extends ContentEntry {

    @Expose
    @SerializedName("servername")
    public String ServerName = "";

    @Expose
    @SerializedName("enabled")
    public boolean Enabled = false;

    @Expose
    @SerializedName("restriction")
    public ServerPackRestrictionEnum Restriction = ServerPackRestrictionEnum.None;

    @Expose
    @SerializedName("serverOnly")
    public boolean ServerOnly = false;

    @Expose
    @SerializedName("clientOnly")
    public boolean ClientOnly = false;

    @Override
    public void UpdateEnabledStatus(String LastServerName) {
        Enabled = ServerName.equals(LastServerName) && isNotRestricted();
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


    public boolean isNotRestricted() {
        switch (this.Restriction) {
            case None, NoneEnd -> {
                return true;
            }
            case Easter -> {
                return CheckDate(getEasterSundayDate(), 2, 1);
            }
            case Halloween -> {
                return CheckDate(getHalloweenDate(), 3, 0);
            }
            case Christmas -> {
                return CheckDate(getChristmasDate(), 5, 5);
            }
        }

        return true;
    }
    private boolean CheckDate(Calendar eventDate, int daysBefore, int daysAfter) {

        Calendar rangeStart = eventDate;
        Calendar rangeEnd = Calendar.getInstance();
        rangeEnd.setTime(rangeStart.getTime());

        rangeStart.add(Calendar.DATE, 0 - daysBefore);

        rangeEnd.add(Calendar.DATE, daysAfter);

        Calendar today = Calendar.getInstance();

        return !today.getTime().before(rangeStart.getTime()) && !today.getTime().after(rangeEnd.getTime());
    }

    public static Calendar getHalloweenDate()
    {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Calendar res = Calendar.getInstance();
        res.set(Calendar.YEAR, year);
        res.set(Calendar.MONTH, Calendar.OCTOBER);
        res.set(Calendar.DAY_OF_MONTH, 31);
        res.set(Calendar.YEAR, year);
        return res;
    }

    public static Calendar getChristmasDate()
    {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Calendar res = Calendar.getInstance();
        res.set(Calendar.YEAR, year);
        res.set(Calendar.MONTH, Calendar.DECEMBER);
        res.set(Calendar.DAY_OF_MONTH, 25);
        res.set(Calendar.YEAR, year);
        return res;
    }

    public static Calendar getEasterSundayDate()
    {
        int year = Calendar.getInstance().get(Calendar.YEAR);

        int a = year % 19,
                b = year / 100,
                c = year % 100,
                d = b / 4,
                e = b % 4,
                g = (8 * b + 13) / 25,
                h = (19 * a + b - d - g + 15) % 30,
                j = c / 4,
                k = c % 4,
                m = (a + 11 * h) / 319,
                r = (2 * e + 2 * j - k - h + m + 32) % 7,
                n = (h - m + r + 90) / 25,
                p = (h - m + r + n + 19) % 32;

        Calendar res = Calendar.getInstance();
        res.set(Calendar.YEAR, year);
        res.set(Calendar.MONTH, n - 1); //fucking bs
        res.set(Calendar.DAY_OF_MONTH, p);
        res.set(Calendar.YEAR, year);
        return res;
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

    public static String GetShareCodeContent() {
        List<ServerContentEntry> list = ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.stream().filter(x -> x.ServerOnly == false).toList();
        list = ContentSyncConfig.ConfigInstance.ServerPackSettings.serverContentEntriesList.stream().filter(x -> x.ServerOnly == false && x.ServerName.equals(ContentSyncConfig.ConfigInstance.HostingServerSettings.ThisServerAddress)).toList();
        String json = ContentSyncConfig.gson.toJson(list);
        return json;
    }

    private static  <T> List<T> getList(String jsonArray, Class<T> clazz) {
        Type typeOfT = TypeToken.getParameterized(List.class, clazz).getType();
        return ContentSyncConfig.gson.fromJson(jsonArray, typeOfT);
    }

    public static List<ServerContentEntry> GetListFromShareCodeContent(String content) {
        return getList(content, ServerContentEntry.class);
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
