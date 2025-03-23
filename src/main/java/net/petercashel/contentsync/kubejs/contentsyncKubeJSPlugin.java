package net.petercashel.contentsync.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.ScriptManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.events.ConstructEventWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class contentsyncKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void init() {

        ContentSyncConfig CSconfig = ContentSyncConfig.LoadConfig();
        ContentSyncConfig.SaveConfig();

        Dist dist = FMLEnvironment.dist;
        Logger logger = LogManager.getLogger("contentsync");

        if (CSconfig.CommonSettings.IsConfigured) {
            var runner = new ConstructEventWorker(logger, dist);
            runner.run(); //Start now
        }
    }

    @Override
    public void beforeScriptsLoaded(ScriptManager manager) {
        //STARTUP("startup", "KubeJS Startup", KubeJSPaths.STARTUP_SCRIPTS),
        //SERVER("server", "KubeJS Server", KubeJSPaths.SERVER_SCRIPTS),
        //CLIENT("client", "KubeJS Client", KubeJSPaths.CLIENT_SCRIPTS);

//        if (manager.scriptType.isStartup()) {
//            manager.loadPackFromDirectory(PATH, "startup", true); //Use a java.nio.file.Path and resolve it that way.
//        }
    }
}
