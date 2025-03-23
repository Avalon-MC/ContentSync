package net.petercashel.contentsync;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.gui.ScreenContentSyncClient;


@EventBusSubscriber(modid = "contentsync", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ContentSyncClientEvents {

    @SubscribeEvent
    public static void onScreenOpenEvent (ScreenEvent.Opening event) {
        if (event.getScreen() instanceof TitleScreen && mainButton != null) {
            if (!ContentSyncConfig.ConfigInstance.CommonSettings.IsConfigured|| ContentSyncConfig.ConfigInstance.CommonSettings.HideMenuButton) return;
            int width = event.getScreen().width;
            int height = event.getScreen().height;
            int l = height / 4 + 48;
            mainButton.setPosition(width / 2 + 105, l + 24);
        }

    }


    private static Button mainButton = null;
    private static MutableComponent contentSync = Component.literal("ContentSync");
    private static MutableComponent contentSyncShort = Component.literal("CS");
    private static MutableComponent contentSyncToolTip = Component.literal("Opens the ContentSync screen");

    @SubscribeEvent
    public static void onInitScreenEvent (ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen) {

            if (!ContentSyncConfig.ConfigInstance.CommonSettings.IsConfigured || ContentSyncConfig.ConfigInstance.CommonSettings.HideMenuButton) return;

            int width = event.getScreen().width;
            int height = event.getScreen().height;
            int l = height / 4 + 48;

            if (mainButton == null) {

                mainButton = Button.builder(contentSyncShort, pButton -> {
                    //Do on press
                    ScreenContentSyncClient screenContentSyncClient = new ScreenContentSyncClient(Minecraft.getInstance().screen);
                    Minecraft.getInstance().setScreen(screenContentSyncClient);
                })
                .pos(width / 2 + 105,l + 24)
                .size(20,20)
                .createNarration((pButton) -> {
                    //Minecraft.getInstance().screen.setTooltipForNextRenderPass(contentSyncToolTip);
                    //Minecraft.getInstance().screen.renderTooltip(pPoseStack, contentSyncToolTip, pMouseX, pMouseY);
                    return contentSyncToolTip;
                })
                .build();

            } else {
                mainButton.setPosition(width / 2 + 105, l + 24);
            }

            event.addListener(mainButton);
            event.getScreen().renderables.add(mainButton);


        }

    }
}
