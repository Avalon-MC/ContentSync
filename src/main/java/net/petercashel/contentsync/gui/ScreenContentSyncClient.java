package net.petercashel.contentsync.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.forgespi.language.IModInfo;
import net.petercashel.contentsync.configuration.ContentSyncConfig;
import net.petercashel.contentsync.configuration.base.IPackEntry;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ScreenContentSyncClient extends Screen {

    private static final int PADDING = 6;
    private static TextComponent contentSync = new TextComponent("ContentSync Packs");
    private Screen parentScreen;
    private PackListWidget packList;
    private PackListWidget.PackListEntry selected;
    private Button doneButton, updateButton;
    private List<IPackEntry> packEntries;
    private boolean shouldReload;


    public ScreenContentSyncClient(Screen parentScreen) {
        super(new TextComponent("ContentSync"));
        this.parentScreen = parentScreen;
        packEntries = ContentSyncConfig.ConfigInstance.GetAllPackEntries();
    }



    @Override
    public void init() {


        int screenWidth = width;
        int panelWidth = screenWidth;
        int centerX = screenWidth / 2;

        int doneButtonWidth = 200;
        int buttonY = this.height - 20 - PADDING;
        int fullButtonHeight = PADDING + 20 + PADDING;


        doneButton = new Button(centerX + ((PADDING / 2)), buttonY, doneButtonWidth, 20, new TranslatableComponent("gui.done"), b -> ScreenContentSyncClient.this.onClose());
        updateButton = new Button(centerX - (doneButtonWidth + (PADDING / 2)), buttonY, doneButtonWidth, 20, new TextComponent("Check for Pack Updates"), b -> this.minecraft.setScreen(new ScreenContentSyncUpdate(this)));
        packList = new PackListWidget(this, panelWidth, fullButtonHeight,buttonY - (PADDING * 1));


        this.addRenderableWidget(doneButton);
        this.addRenderableWidget(updateButton);
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
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderDirtBackground(0);

        packList.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        doneButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        updateButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        pPoseStack.pushPose();
        pPoseStack.scale(1.5f,1.5f,1.5f);
        getFontRenderer().draw(pPoseStack, contentSync.getVisualOrderText(), width / 3 - (Minecraft.getInstance().font.width(contentSync.getVisualOrderText()) / 2), ((int)PADDING * 1.2f), 0xFFFFFF);

        pPoseStack.popPose();
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
        
        if (this.selected == entry) {
            //Toggle
            ContentSyncConfig.ConfigInstance.ToggleEntry(entry.getInfo());
            didAction = true;
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
