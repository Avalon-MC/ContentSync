package net.petercashel.contentsync.data_formats.packrepo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.vdurmont.semver4j.Semver;
import org.jetbrains.annotations.NotNull;

import java.lang.Comparable;

public class Version implements Comparable<Version> {

    @SerializedName("Version")
    @Expose
    public String version;
    @SerializedName("URL")
    @Expose
    public String url;

    public Semver GetSemver() {
        return new Semver(version, Semver.SemverType.LOOSE);
    }

    @Override
    public int compareTo(@NotNull Version o) {
        return this.GetSemver().isGreaterThan(o.GetSemver()) ? 1 : -1;
    }
}
