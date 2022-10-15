package net.petercashel.contentsync.earlystartupprogress;

import net.minecraftforge.api.distmarker.Dist;
import org.apache.logging.log4j.Logger;

public class ServerEMS implements IEarlyMessageSystem{
    private Logger logger;

    @Override
    public void SetupMessageSystem(Logger logger, Dist side) {
        this.logger = logger;
    }

    @Override
    public void ShutdownMessageSystem() {

    }

    @Override
    public void AddMessageToQueue(String stage, String message) {
        logger.info("ContentSync Worker:" + stage + " - " + message);
    }

    @Override
    public void SetPrimaryProgressBar(float value, float max) {

    }
}
