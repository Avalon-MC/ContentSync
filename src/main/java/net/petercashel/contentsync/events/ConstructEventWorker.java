package net.petercashel.contentsync.events;

import net.minecraftforge.api.distmarker.Dist;
import net.petercashel.contentsync.earlystartupprogress.ClientEMS;
import net.petercashel.contentsync.earlystartupprogress.IEarlyMessageSystem;
import net.petercashel.contentsync.earlystartupprogress.ServerEMS;
import org.apache.logging.log4j.Logger;

public class ConstructEventWorker implements Runnable {
    private final Logger logger;
    private final Dist side;
    private final IEarlyMessageSystem ems;

    public ConstructEventWorker(Logger logger, Dist dist) {
        this.logger = logger;
        this.side = dist;

        if (dist == Dist.CLIENT) {
            ems = new ClientEMS();
        } else {
            ems = new ServerEMS();
        }
    }

    @Override
    public void run() {
        logger.info("WE RAN! ContentSync Ran!");

        ems.SetupMessageSystem(logger, side);
        ems.AddMessageToQueue("Testing", "Startup");
        ems.SetPrimaryProgressBar(1,5);
        try {
            Thread.sleep(2000);
            ems.AddMessageToQueue("Testing", "One");
            ems.SetPrimaryProgressBar(2,5);
            Thread.sleep(2000);
            ems.AddMessageToQueue("Testing", "Two");
            ems.SetPrimaryProgressBar(3,5);
            Thread.sleep(2000);
            ems.AddMessageToQueue("Testing", "Tree");
            ems.SetPrimaryProgressBar(4,5);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ems.ShutdownMessageSystem();

        logger.info("WE RAN! ContentSync SLEPT!");
    }
}
