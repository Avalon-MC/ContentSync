package net.petercashel.contentsync.earlystartupprogress;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import org.apache.logging.log4j.Logger;

public class IngameEMS implements IEarlyMessageSystem{
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
        //Minecraft.getInstance().player.sendMessage(new TextComponent("CS:" + stage + " - " + message), Util.NIL_UUID);
    }

    @Override
    public void SetPrimaryProgressBar(float value, float max) {

    }

    @Override
    public void SetPrimaryProgressBar(float value) {

    }
}
