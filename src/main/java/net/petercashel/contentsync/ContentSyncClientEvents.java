package net.petercashel.contentsync;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.petercashel.contentsync.gui.ScreenContentSyncClient;

@Mod.EventBusSubscriber(modid = "contentsync", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ContentSyncClientEvents {

    //@SubscribeEvent
    public static void onScreenOpenEvent (ScreenOpenEvent event) {
        if (event.getScreen() instanceof TitleScreen && mainButton != null) {
            int width = event.getScreen().width;
            int height = event.getScreen().height;
            int l = height / 4 + 48;
            mainButton.x = width / 2 + 105;
            mainButton.y = l + 24;
        }

    }


    private static Button mainButton = null;
    private static TextComponent contentSync = new TextComponent("ContentSync");
    private static TextComponent contentSyncShort = new TextComponent("CS");
    private static TextComponent contentSyncToolTip = new TextComponent("Opens the ContentSync screen");

    //@SubscribeEvent
    public static void onInitScreenEvent (ScreenEvent.InitScreenEvent.Post event) {
        if (event.getScreen() instanceof TitleScreen) {

            int width = event.getScreen().width;
            int height = event.getScreen().height;
            int l = height / 4 + 48;

            if (mainButton == null) {
                mainButton = new Button(width / 2 + 105, l + 24, 20, 20, contentSyncShort, pButton -> {
                    //Do on press
                    ScreenContentSyncClient screenContentSyncClient = new ScreenContentSyncClient(Minecraft.getInstance().screen, contentSync);
                    Minecraft.getInstance().setScreen(screenContentSyncClient);
                }, (pButton, pPoseStack, pMouseX, pMouseY) -> {
                    Minecraft.getInstance().screen.renderTooltip(pPoseStack, contentSyncToolTip, pMouseX, pMouseY);
                });
            } else {
                mainButton.x = width / 2 + 105;
                mainButton.y = l + 24;
            }

            event.addListener(mainButton);
            event.getScreen().renderables.add(mainButton);


        }

    }
}
