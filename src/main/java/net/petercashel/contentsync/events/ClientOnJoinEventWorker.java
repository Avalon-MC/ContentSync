package net.petercashel.contentsync.events;

import net.minecraftforge.api.distmarker.Dist;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.configuration.modpack.ModpackContentEntry;
import net.petercashel.contentsync.configuration.server.ServerContentEntry;
import net.petercashel.contentsync.earlystartupprogress.ClientEMS;
import net.petercashel.contentsync.earlystartupprogress.IEarlyMessageSystem;
import net.petercashel.contentsync.earlystartupprogress.IngameEMS;
import net.petercashel.contentsync.earlystartupprogress.ServerEMS;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ClientOnJoinEventWorker extends BaseContentSyncWorker {


    public ClientOnJoinEventWorker(Logger logger, Dist dist) {
        super(logger, dist, new IngameEMS());

        DoGlobalPacks = false;
    }


}
