package net.petercashel.contentsync.data_formats.packrepo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PackRepoEntry {

    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("displayName")
    @Expose
    public String displayName;
    @SerializedName("type")
    @Expose
    public PackTypeEnum type;
    @SerializedName("versions")
    @Expose
    public List<Version> versions = new ArrayList<Version>();

    public Version GetLatestVersion(String overrideVersion) {
        Collections.sort(versions);
        if (overrideVersion != null && !overrideVersion.isBlank()) {
            Optional<Version> targetVersion = versions.stream().filter(x -> x.version.equals(overrideVersion)).findFirst();
            if (targetVersion.isPresent()) {
                return targetVersion.get();
            }
        }
        return versions.get(0);
    }

    public String GetDisplayName() {
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }
        return name;
    }

    public boolean IsLatestVersion (String version) {
        if (version == null || version.isBlank()) {
            return false;
        }
        return GetLatestVersion("").GetSemver().isEquivalentTo(version);
    }

    public boolean IsValidVersion(String targetVersion) {
        return versions.stream().anyMatch(x -> x.version.equals(targetVersion));
    }
}

