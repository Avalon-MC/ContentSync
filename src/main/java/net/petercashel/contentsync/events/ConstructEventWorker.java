package net.petercashel.contentsync.events;

import net.minecraftforge.api.distmarker.Dist;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.earlystartupprogress.ClientEMS;
import net.petercashel.contentsync.earlystartupprogress.IEarlyMessageSystem;
import net.petercashel.contentsync.earlystartupprogress.ServerEMS;
import org.apache.logging.log4j.Logger;


public class ConstructEventWorker extends BaseContentSyncWorker {

    public ConstructEventWorker(Logger logger, Dist dist) {
        super(logger, dist, getEMS(dist));
    }

    private static IEarlyMessageSystem getEMS(Dist dist) {
        if (dist == Dist.CLIENT) {
            if (ContentSyncConfig.ConfigInstance.CommonSettings.DisableUI) {
                return new ServerEMS();
            } else {
                return new ClientEMS();
            }
        } else {
            return new ServerEMS();
        }
    }
}
