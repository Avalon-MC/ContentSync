package net.petercashel.contentsync.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.events.ConstructEventWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class contentsyncKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        super.init();

        ContentSyncConfig CSconfig = ContentSyncConfig.LoadConfig();
        ContentSyncConfig.SaveConfig();

        Dist dist = FMLEnvironment.dist;
        Logger logger = LogManager.getLogger("contentsync");

        if (CSconfig.CommonSettings.IsConfigured) {
            var runner = new ConstructEventWorker(logger, dist);
            runner.run(); //Start now
        }
    }
}
