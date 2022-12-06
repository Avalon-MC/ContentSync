package net.petercashel.contentsync.events;

import net.minecraftforge.api.distmarker.Dist;
import net.petercashel.contentsync.earlystartupprogress.IEarlyMessageSystem;
import net.petercashel.contentsync.earlystartupprogress.IngameEMS;
import org.apache.logging.log4j.Logger;

public class InMenuEventWorker extends BaseContentSyncWorker {

    public InMenuEventWorker(Logger logger, Dist dist, IEarlyMessageSystem ems) {
        super(logger, dist, ems);
        this.StartupSleepTime = 100;
    }


}
