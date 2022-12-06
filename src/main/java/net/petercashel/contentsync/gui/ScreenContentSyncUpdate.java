package net.petercashel.contentsync.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.petercashel.contentsync.earlystartupprogress.GUI.Controls.IUIRenderable;
import net.petercashel.contentsync.earlystartupprogress.GUI.Controls.ProgressBar;
import net.petercashel.contentsync.earlystartupprogress.GUI.Controls.TextLabel;
import net.petercashel.contentsync.earlystartupprogress.GUI.Core.Color;
import net.petercashel.contentsync.earlystartupprogress.GUI.Core.Font;
import net.petercashel.contentsync.earlystartupprogress.IEarlyMessageSystem;
import net.petercashel.contentsync.events.ClientOnJoinEventWorker;
import net.petercashel.contentsync.events.InMenuEventWorker;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;

public class ScreenContentSyncUpdate extends Screen implements IEarlyMessageSystem {

    private Screen parentScreen;
    private boolean CloseNextFrame;

    public ScreenContentSyncUpdate(Screen parentScreen) {
        super(new TextComponent(""));
        this.parentScreen = parentScreen;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parentScreen);
    }

    private static final int PADDING = 6;
    private static TextComponent contentSync = new TextComponent("ContentSync Pack Update!");
    private static Color ContentSyncRed = new Color(145, 29, 32);
    private static Color ContentSyncRedDark = new Color(107, 21, 24);
    private static Color ContentSyncRedDarker = new Color(66, 13, 15);

    private String LastStage = "";
    private String LastMessage = "";
    private String LastMessage2 = "";
    Queue<Pair<String, String>> MessageQueue = new LinkedList<>();
    private ArrayList<IUIRenderable> Controls = new ArrayList<>();

    private ProgressBar PrimaryProgressBar;

    private Thread UIWorkerThread;

    @Override
    protected void init() {
        super.init();

        this.PrimaryProgressBar = new ProgressBar(25, height / 2 - 10, width - 50, 20, 4, 0, 100, Color.BLACK, Color.GRAY);

        StartUpdaterThread();

    }

    private void StartUpdaterThread() {

        Runnable r = new Runnable() {
            public void run() {
                Dist dist = FMLEnvironment.dist;
                ModLoadingContext context = ModLoadingContext.get();
                Logger logger = LogManager.getLogger(context.getActiveContainer().getModId());

                var runner = new InMenuEventWorker(logger, dist, ScreenContentSyncUpdate.this);
                runner.run();

                if (runner.needToRunUpdate) {
                    ScreenContentSyncUpdate.this.AddMessageToQueue("Update Complete","You will need to restart minecraft to finish updating");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    ScreenContentSyncUpdate.this.AddMessageToQueue("Update Complete", "Have a great day!");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                CloseNextFrame = true;
            }
        };
        UIWorkerThread = new Thread(r);
        UIWorkerThread.setName("ContentSync UI Worker Thread");
        UIWorkerThread.setDaemon(true);
        UIWorkerThread.start();

    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void SetupMessageSystem(Logger logger, Dist side) {

    }

    @Override
    public void ShutdownMessageSystem() {

    }

    @Override
    public void AddMessageToQueue(String stage, String message) {
        MessageQueue.add(new ImmutablePair<String, String>(stage, message));
    }

    @Override
    public void SetPrimaryProgressBar(float value, float max) {
        PrimaryProgressBar.SetValue(value);
        PrimaryProgressBar.SetMax(max);
    }

    @Override
    public void SetPrimaryProgressBar(float value) {
        PrimaryProgressBar.SetValue(value);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (CloseNextFrame) {
            this.onClose();
            return;
        }
        renderDirtBackground(0);

        //Do Render

        //Inital work
        if (!MessageQueue.isEmpty() && MessageQueue.peek() != null) {
            var message = MessageQueue.poll();
            LastStage = message.getLeft();
            LastMessage2 = "";
            LastMessage = message.getRight();
            if (LastMessage.contains("@;@;")) {
                var split = LastMessage.split("@;@;");
                LastMessage = split[0];
                LastMessage2 = split[1];
            }
        }

        pPoseStack.pushPose();
        renderUI(pPoseStack);
        pPoseStack.popPose();
    }


    public Minecraft getMinecraftInstance()
    {
        return minecraft;
    }
    public net.minecraft.client.gui.Font getFontRenderer()
    {
        return font;
    }

    private void renderUI(PoseStack pPoseStack) {

        //MAKE IT CONTENT SYNC RED

        fill(pPoseStack, 0,0, width, height, ContentSyncRedDark.pack());

        int buttonY = this.height - 20 - PADDING;
        int fullButtonHeight = PADDING + 20 + PADDING;

        fill(pPoseStack, 0,fullButtonHeight, width, buttonY - (PADDING * 1), ContentSyncRedDarker.pack());



        {
            pPoseStack.pushPose();
            pPoseStack.scale(1.5f,1.5f,1.5f);
            getFontRenderer().draw(pPoseStack, contentSync.getVisualOrderText(), width / 3 - (Minecraft.getInstance().font.width(contentSync.getVisualOrderText()) / 2), ((int)PADDING * 1.2f), 0xFFFFFF);

            pPoseStack.popPose();

        }


//        this.StageText = new TextLabel("", 24, 100, Color.WHITE).SetCemter(500);
//        this.MessageText = new TextLabel("", 24, 45, Color.WHITE).SetCemter(500);
//        this.MessageText2 = new TextLabel("", 24, 45 - (24 + 4), Color.WHITE).SetCemter(500);

        {
            pPoseStack.pushPose();
            PrimaryProgressBar.render(pPoseStack, this);
            pPoseStack.popPose();

            TextComponent text = new TextComponent(LastStage);

            pPoseStack.pushPose();
            pPoseStack.scale(1.5f, 1.5f, 1.5f);

            getFontRenderer().draw(pPoseStack, text, width / 3 - (Minecraft.getInstance().font.width(text.getVisualOrderText()) / 2), ((height / 3) - font.lineHeight * 2) - (PrimaryProgressBar.GetHeight() / 3), 0xFFFFFF);
            text = new TextComponent(LastMessage);

            getFontRenderer().draw(pPoseStack, text, width / 3 - (Minecraft.getInstance().font.width(text.getVisualOrderText()) / 2), ((height / 3) + PrimaryProgressBar.GetHeight() / 3 + font.lineHeight * 1), 0xFFFFFF);

            text = new TextComponent(LastMessage2);
            getFontRenderer().draw(pPoseStack, text, width / 3 - (Minecraft.getInstance().font.width(text.getVisualOrderText()) / 2), ((height / 3) + PrimaryProgressBar.GetHeight() / 3 + font.lineHeight * 2), 0xFFFFFF);

            pPoseStack.popPose();
        }

    }


}
