package net.petercashel.contentsync.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.petercashel.contentsync.network.ContentSyncClient;

public class ScreenContentSyncShareCode extends Screen {

    private static final int PADDING = 6;
    private static TextComponent contentSync = new TextComponent("ContentSync ShareCode");
    private static TextComponent contentSyncLabel = new TextComponent("Enter ShareCode");
    private Screen parentScreen;
    private boolean shouldReload;
    private Button doneButton, addButton;
    private EditBox shareCodeBox;

    public ScreenContentSyncShareCode(Screen parentScreen) {
        super(new TextComponent("ContentSync"));
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

        shareCodeBox = new EditBox(this.font, centerX - (editBoxWidth / 2), centerY - (editBoxHeight / 2), editBoxWidth, editBoxHeight, new TextComponent("Some Text"));

        doneButton = new Button(centerX + ((PADDING)) + (doneButtonWidth / 4), buttonY, doneButtonWidth, 20, new TranslatableComponent("gui.cancel"), b -> ScreenContentSyncShareCode.this.onClose());
        addButton = new Button(centerX - (doneButtonWidth + (PADDING) + (doneButtonWidth / 4)), buttonY, doneButtonWidth, 20, new TextComponent("Add Packs"), b -> ScreenContentSyncShareCode.this.AddPacks());

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
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        renderDirtBackground(0);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        getFontRenderer().draw(pPoseStack, contentSync.getVisualOrderText(), width / 2 - ((PADDING / 4) + (300 / 2)), ((height / 2) - 15) - ((int)PADDING * 1.2f), 0xFFFFFF);



        pPoseStack.pushPose();
        pPoseStack.scale(1.5f,1.5f,1.5f);
        getFontRenderer().draw(pPoseStack, contentSync.getVisualOrderText(), width / 3 - (Minecraft.getInstance().font.width(contentSync.getVisualOrderText()) / 2), ((int)PADDING * 1.2f), 0xFFFFFF);

        pPoseStack.popPose();
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
