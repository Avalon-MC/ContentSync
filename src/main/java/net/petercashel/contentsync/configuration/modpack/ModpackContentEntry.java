package net.petercashel.contentsync.configuration.modpack;

import net.petercashel.contentsync.configuration.base.ContentEntry;

public class ModpackContentEntry extends ContentEntry {
    @Override
    public void UpdateEnabledStatus(String LastServerName) {

    }

    @Override
    public void UpdatePackMetaExtension() {

    }

    @Override
    public boolean IsServerPack() {
        return false;
    }

    @Override
    public boolean IsEnabled() {
        return true;
    }
}
