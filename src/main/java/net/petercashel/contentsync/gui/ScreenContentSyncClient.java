package net.petercashel.contentsync.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.gui.ModListScreen;

public class ScreenContentSyncClient extends Screen {


    private Screen parentScreen;

    public ScreenContentSyncClient(Screen parentScreen, Component pTitle) {
        super(pTitle);
        this.parentScreen = parentScreen;
    }


    private static final int PADDING = 6;
    private int listWidth;
    private Button doneButton;
    private static TextComponent contentSyncToolTip = new TextComponent("Future Plans: ");

    @Override
    public void init() {


        listWidth = Math.max(Math.min(listWidth, width/3), 100);

        int modInfoWidth = this.width - this.listWidth - (PADDING * 3);
        int doneButtonWidth = Math.min(modInfoWidth, 200);
        int y = this.height - 20 - PADDING;
        int fullButtonHeight = PADDING + 20 + PADDING;

        doneButton = new Button(((listWidth + PADDING + this.width - doneButtonWidth) / 2), y, doneButtonWidth, 20, new TranslatableComponent("gui.done"), b -> ScreenContentSyncClient.this.onClose());



        this.addRenderableWidget(doneButton);

    }

    public Minecraft getMinecraftInstance()
    {
        return minecraft;
    }

    public Font getFontRenderer()
    {
        return font;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderDirtBackground(0);


        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }


    @Override
    public void onClose()
    {
        this.minecraft.setScreen(this.parentScreen);
    }
}
