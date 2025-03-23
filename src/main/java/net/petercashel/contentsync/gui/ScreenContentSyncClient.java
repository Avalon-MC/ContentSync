package net.petercashel.contentsync.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.MutableComponent;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.configuration.base.IPackEntry;
import net.petercashel.contentsync.network.ContentSyncClient;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ScreenContentSyncClient extends Screen {

    private static final int PADDING = 6;
    private static MutableComponent contentSync = Component.literal("ContentSync Packs");
    private Screen parentScreen;
    private PackListWidget packList;
    private PackListWidget.PackListEntry selected;
    private Button doneButton, updateButton, shareButton;
    private List<IPackEntry> packEntries;
    private boolean shouldReload;


    public ScreenContentSyncClient(Screen parentScreen) {
        super(Component.literal("ContentSync"));
        this.parentScreen = parentScreen;
        packEntries = ContentSyncConfig.ConfigInstance.GetAllPackEntries();
    }



    @Override
    public void init() {


        int screenWidth = width;
        int panelWidth = screenWidth;
        int centerX = screenWidth / 2;

        int doneButtonWidth = 150;
        int buttonY = this.height - 20 - PADDING;
        int fullButtonHeight = PADDING + 20 + PADDING;


        //doneButton = new Button(centerX + ((PADDING)) + (doneButtonWidth / 4), buttonY, doneButtonWidth, 20, Component.translatable("gui.done"), b -> ScreenContentSyncClient.this.onClose());
        doneButton = Button.builder(Component.translatable("gui.done"), b -> ScreenContentSyncClient.this.onClose()).pos(centerX + ((PADDING)) + (doneButtonWidth / 4), buttonY).size(doneButtonWidth, 20).build();
        //updateButton = new Button(centerX - (doneButtonWidth + (PADDING) + (doneButtonWidth / 4)), buttonY, doneButtonWidth, 20, Component.literal("Check for Pack Updates"), b -> this.minecraft.setScreen(new ScreenContentSyncUpdate(this)));
        updateButton = Button.builder(Component.literal("Check for Pack Updates"), b -> this.minecraft.setScreen(new ScreenContentSyncUpdate(this))).pos(centerX - (doneButtonWidth + (PADDING) + (doneButtonWidth / 4)), buttonY).size(doneButtonWidth, 20).build();
        //shareButton = new Button(centerX - (doneButtonWidth / 4), buttonY, doneButtonWidth / 2, 20, Component.literal("ShareCode"), b -> this.minecraft.setScreen(new ScreenContentSyncShareCode(this)));
        shareButton = Button.builder(Component.literal("ShareCode"), b -> this.minecraft.setScreen(new ScreenContentSyncShareCode(this))).pos(centerX - (doneButtonWidth / 4), buttonY).size(doneButtonWidth / 2, 20).build();
        packList = new PackListWidget(this, panelWidth, fullButtonHeight,buttonY - (PADDING * 1));


        this.addRenderableWidget(doneButton);
        this.addRenderableWidget(updateButton);
        this.addRenderableWidget(shareButton);
        this.addRenderableWidget(packList);

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
    public void render(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack, pMouseX, pMouseY, pPartialTick);

        packList.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        doneButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        updateButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        shareButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        pPoseStack.pose().pushPose();
        pPoseStack.pose().scale(1.5f,1.5f,1.5f);
        pPoseStack.drawString(getFontRenderer(), contentSync.getVisualOrderText(), width / 3 - (Minecraft.getInstance().font.width(contentSync.getVisualOrderText()) / 2), (int) ((int)PADDING * 1.2f), 0xFFFFFF);

        pPoseStack.pose().popPose();
    }


    @Override
    public void onClose()
    {
        this.minecraft.setScreen(this.parentScreen);
        
        if (shouldReload) {
            this.minecraft.reloadResourcePacks();
        }
    }

    public <T extends ObjectSelectionList.Entry<T>> void buildPackList(Consumer<T> packListViewConsumer, Function<IPackEntry, T> newEntry)
    {
        packEntries.forEach(entry->packListViewConsumer.accept(newEntry.apply(entry)));
    }

    public boolean setSelected(PackListWidget.PackListEntry entry) {

        boolean didAction = false;
        
        if (entry.CanToggle()) {
            if (this.selected == entry) {
                //Toggle
                ContentSyncConfig.ConfigInstance.ToggleEntry(entry.getInfo());
                didAction = true;
            }
        }

        this.selected = entry == this.selected ? null : entry;
        //updateCache();
        
        return didAction;
    }

    public boolean IsSelected(PackListWidget.PackListEntry packEntry) {
        return this.selected == (packEntry);
    }

    public void setReloadOnClose() {
        shouldReload = true;
    }
}
