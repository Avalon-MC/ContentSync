package net.petercashel.contentsync.configuration.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.petercashel.contentsync.data_formats.packrepo.PackTypeEnum;
import org.apache.maven.artifact.versioning.ArtifactVersion;

public interface IPackEntry {

    String GetDisplayName();

    boolean IsResourcePack();

    boolean IsDataPack();

    boolean IsKubeJSPack();

    String GetName();
    String GetURL();


    boolean IsServerPack();
    boolean IsEnabled();

    String GetInstalledVersion();

    String GetTargetVersion();
}
