package net.petercashel.contentsync.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.petercashel.contentsync.network.ContentSyncClient;


public class ScreenContentSyncShareCode extends Screen {

    private static final int PADDING = 6;
    private static MutableComponent contentSync = Component.literal("ContentSync ShareCode");
    private static MutableComponent contentSyncLabel = Component.literal("Enter ShareCode");
    private Screen parentScreen;
    private boolean shouldReload;
    private Button doneButton, addButton;
    private EditBox shareCodeBox;

    public ScreenContentSyncShareCode(Screen parentScreen) {
        super(Component.literal("ContentSync"));
        this.parentScreen = parentScreen;
    }

    @Override
    public void init() {
        int screenWidth = width;
        int panelWidth = screenWidth;
        int centerX = screenWidth / 2;
        int centerY = height / 2;

        int doneButtonWidth = 150;
        int buttonY = this.height - 20 - PADDING;
        int fullButtonHeight = PADDING + 20 + PADDING;

        int editBoxWidth = 300;
        int editBoxHeight = 20;

        shareCodeBox = new EditBox(this.font, centerX - (editBoxWidth / 2), centerY - (editBoxHeight / 2), editBoxWidth, editBoxHeight, Component.literal("Some Text"));

        //doneButton = new Button(centerX + ((PADDING)) + (doneButtonWidth / 4), buttonY, doneButtonWidth, 20, Component.translatable("gui.cancel"), b -> ScreenContentSyncShareCode.this.onClose());
        doneButton = Button.builder(Component.translatable("gui.cancel"), b -> ScreenContentSyncShareCode.this.onClose()).pos(centerX + ((PADDING)) + (doneButtonWidth / 4), buttonY).size(doneButtonWidth, 20).build();


        //addButton = new Button(centerX - (doneButtonWidth + (PADDING) + (doneButtonWidth / 4)), buttonY, doneButtonWidth, 20, Component.literal("Add Packs"), b -> ScreenContentSyncShareCode.this.AddPacks());
        addButton = Button.builder(Component.literal("Add Packs"), b -> ScreenContentSyncShareCode.this.AddPacks()).pos(centerX - (doneButtonWidth + (PADDING) + (doneButtonWidth / 4)), buttonY).size(doneButtonWidth, 20).build();

        this.addRenderableWidget(shareCodeBox);
        this.addRenderableWidget(doneButton);
        this.addRenderableWidget(addButton);
    }

    private void AddPacks() {
        boolean result = ContentSyncClient.ProcessShareCode(shareCodeBox.getValue());
        if (result) {
            setReloadOnClose();
        }
        ScreenContentSyncShareCode.this.onClose();
    }

    @Override
    public void render(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        //renderDirtBackground(0);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        pPoseStack.drawString(getFontRenderer(), contentSync.getVisualOrderText(), width / 2 - ((PADDING / 4) + (300 / 2)), (int) (((height / 2) - 15) - ((int)PADDING * 1.2f)), 0xFFFFFF);



        pPoseStack.pose().pushPose();
        pPoseStack.pose().scale(1.5f,1.5f,1.5f);
        pPoseStack.drawString(getFontRenderer(), contentSync.getVisualOrderText(), width / 3 - (Minecraft.getInstance().font.width(contentSync.getVisualOrderText()) / 2), (int) ((int)PADDING * 1.2f), 0xFFFFFF);

        pPoseStack.pose().popPose();
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
    public void onClose()
    {
        this.minecraft.setScreen(this.parentScreen);

        if (shouldReload) {
            this.minecraft.reloadResourcePacks();
        }
    }
    public void setReloadOnClose() {
        shouldReload = true;
    }





}
