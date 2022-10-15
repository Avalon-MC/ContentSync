package net.petercashel.contentsync.earlystartupprogress;

import net.minecraftforge.api.distmarker.Dist;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import org.apache.logging.log4j.Logger;

public interface IEarlyMessageSystem {

    public void SetupMessageSystem(Logger logger, Dist side);
    public void ShutdownMessageSystem();
    public void AddMessageToQueue(String stage, String message);

    public void SetPrimaryProgressBar(float value, float max);
}
