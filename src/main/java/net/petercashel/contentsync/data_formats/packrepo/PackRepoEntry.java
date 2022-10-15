package net.petercashel.contentsync.data_formats.packrepo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PackRepoEntry {

    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("type")
    @Expose
    public PackTypeEnum type;
    @SerializedName("versions")
    @Expose
    public List<Version> versions = new ArrayList<Version>();

    public Version GetLatestVersion() {
        Collections.sort(versions);
        return versions.get(0);
    }

    public boolean IsLatestVersion (String version) {
        return GetLatestVersion().GetSemver().isEquivalentTo(version);
    }
}

