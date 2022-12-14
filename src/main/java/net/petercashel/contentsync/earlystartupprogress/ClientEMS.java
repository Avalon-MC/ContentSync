package net.petercashel.contentsync.earlystartupprogress;

import net.minecraftforge.api.distmarker.Dist;
import net.petercashel.contentsync.earlystartupprogress.GUI.ClientProgressWindow;
import net.petercashel.contentsync.earlystartupprogress.GUI.Controls.ProgressBar;
import net.petercashel.contentsync.earlystartupprogress.GUI.Controls.TextLabel;
import net.petercashel.contentsync.earlystartupprogress.GUI.Core.Color;
import net.petercashel.contentsync.earlystartupprogress.GUI.Core.Font;
import org.apache.logging.log4j.Logger;

public class ClientEMS implements IEarlyMessageSystem{
    private Logger logger;
    private Thread UIUpdateThread;
    private ClientProgressWindow ProgressWindow;
    private ProgressBar PrimaryProgressBar;
    private TextLabel StageText;
    private TextLabel MessageText;
    private TextLabel MessageText2;

    @Override
    public void SetupMessageSystem(Logger logger, Dist side) {
        this.logger = logger;

        this.ProgressWindow = new ClientProgressWindow(logger);

        this.StageText = new TextLabel("", 24, 100, Color.WHITE).SetCemter(500);
        ProgressWindow.StageText = (StageText);

        this.MessageText = new TextLabel("", 24, 45, Color.WHITE).SetCemter(500);
        ProgressWindow.MessageText = (MessageText);
        this.MessageText2 = new TextLabel("", 24, 45 - (24 + 4), Color.WHITE).SetCemter(500);
        ProgressWindow.MessageText2 = (MessageText2);

        this.PrimaryProgressBar = new ProgressBar(25, 75, 450, 20, 4, 0, 100, Color.BLACK, Color.WHITE);
        ProgressWindow.AddRenderable(PrimaryProgressBar);

        Runnable r = new Runnable() {
            public void run() {
                ProgressWindow.init(500, 200, "ContentSync!");
                ProgressWindow.initGL(new Color(145, 29, 32));
                ProgressWindow.loop();
            }
        };
        UIUpdateThread = new Thread(r);
        UIUpdateThread.setName("ContentSync UI Thread");
        UIUpdateThread.setDaemon(true);
        UIUpdateThread.start();
    }

    @Override
    public void ShutdownMessageSystem() {
        ProgressWindow.ShouldStop = true;
        ProgressWindow.WaitForStop();
    }

    @Override
    public void AddMessageToQueue(String stage, String message) {
        logger.info("ContentSync Worker:" + stage + " - " + message);
        ProgressWindow.AddMessage(stage, message);
    }

    @Override
    public void SetPrimaryProgressBar(float value) {
        PrimaryProgressBar.SetValue(value);
    }

    @Override
    public void SetPrimaryProgressBar(float value, float max) {
        PrimaryProgressBar.SetValue(value);
        PrimaryProgressBar.SetMax(max);
    }
}
