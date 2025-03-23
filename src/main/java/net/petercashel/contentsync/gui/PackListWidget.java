package net.petercashel.contentsync.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.petercashel.contentsync.configuration.base.IPackEntry;
import net.petercashel.contentsync.configuration.server.ServerContentEntry;
import net.petercashel.contentsync.configuration.server.ServerPackRestrictionEnum;
import org.joml.Vector2ic;

import java.util.List;

public class PackListWidget extends ObjectSelectionList<PackListWidget.PackListEntry> {

    private final int listWidth;
    private ScreenContentSyncClient parent;
    public PackListWidget(ScreenContentSyncClient parent, int listWidth, int top, int bottom)
    {
        super(parent.getMinecraftInstance(), listWidth, (bottom - top), parent.height, parent.getFontRenderer().lineHeight * 2 + 10);
        this.parent = parent;
        this.listWidth = listWidth;
        this.refreshList();
    }

    @Override
    protected int getScrollbarPosition()
    {
        return this.listWidth;
    }

    @Override
    public int getRowWidth()
    {
        return this.listWidth;
    }


    private void refreshList() {
        this.clearEntries();
        parent.buildPackList(this::addEntry, pack->new PackListEntry(pack, this.parent, this));
    }

    public class PackListEntry extends ObjectSelectionList.Entry<PackListWidget.PackListEntry> {
        private final IPackEntry packEntry;
        private final ScreenContentSyncClient parent;
        private final PackListWidget listWidget;

        public PackListEntry(IPackEntry packEntry, ScreenContentSyncClient parent, PackListWidget listWidget) {
            this.packEntry = packEntry;
            this.parent = parent;
            this.listWidget = listWidget;
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", packEntry.GetDisplayName());
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
        {
            if (parent.setSelected(this)) {
                parent.setReloadOnClose();
            }
            PackListWidget.this.setSelected(this);
            return false;
        }

        public IPackEntry getInfo()
        {
            return packEntry;
        }

        private static String stripControlCodes(String value) { return net.minecraft.util.StringUtil.stripColor(value); }

        private static int RightOffset = 112;

        @Override
        public void render(GuiGraphics graphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
            Component name = Component.literal(stripControlCodes(packEntry.GetDisplayName()));
            MutableComponent version = Component.literal("Installed Version:");
            if (packEntry.GetInstalledVersion().length() > 1 && !packEntry.GetInstalledVersion().isBlank()) {
                version.append(" " + stripControlCodes(packEntry.GetInstalledVersion()));
            } else {
                version.append(" §80.0.0");
            }

            if (packEntry.GetTargetVersion().length() > 1 && !packEntry.GetTargetVersion().isBlank()) {
                version.append(" §7(T:" + stripControlCodes(packEntry.GetTargetVersion()) + ")");
            }

            if (packEntry.IsServerPack() && !CanToggle()) {
                version.append(" §7(" + (GetRestrictionType(packEntry)) + ")");
            }

            String PackType = packEntry.IsServerPack()? "Server Pack" : "Content Pack";
            if (packEntry.IsServerPack() && !CanToggle()) {
                //PackType = "Seasonal Pack";
            }

            Component packTypeComp = Component.literal("Type: ");
            Component packTypeCompValue = Component.literal(PackType);

            String Enabled = packEntry.IsServerPack() == false ? "§6Always Enabled" : (packEntry.IsEnabled() ? "§6Enabled" : "§4Disabled");

            if (packEntry.IsServerPack() && !CanToggle()) {
                Enabled = (packEntry.IsEnabled() ? "§6Enabled" : "§4Disabled");
            }

            Component enabledComp = Component.literal("State: ");
            Component enabledCompValue = Component.literal(Enabled);

            Font font = this.parent.getFontRenderer();
            int valueOffset = font.width("State: ");
            graphics.drawString(font, Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(name,    listWidth))), pLeft + 3, pTop + 2, 0xFFFFFF);
            graphics.drawString(font, Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(packTypeComp, listWidth))), width - RightOffset, pTop + 2, 0xCCCCCC);
            graphics.drawString(font, Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(packTypeCompValue, listWidth))), width - RightOffset+ valueOffset, pTop + 2, 0xCCCCCC);


            graphics.drawString(font, Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(version, listWidth))), pLeft + 3, pTop + 4 + (font.lineHeight * 1), 0xCCCCCC);
            graphics.drawString(font, Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(enabledComp, listWidth))), width - RightOffset, pTop + 4 + (font.lineHeight * 1), 0xCCCCCC);
            graphics.drawString(font, Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(enabledCompValue, listWidth))), (width - RightOffset) + valueOffset, pTop + 4 + (font.lineHeight * 1), 0xCCCCCC);


            if (isMouseOver(pMouseX, pMouseY) && parent.IsSelected(this)) {
                MutableComponent MutableComponent = Component.literal("");

                if (!packEntry.IsServerPack()) {
                    MutableComponent.append("Content Packs cannot be disabled");
                } else {
                    if (CanToggle()) {
                        if (packEntry.IsEnabled()) {
                            MutableComponent.append("Server Packs can be disabled by double clicking.");
                        } else {
                            MutableComponent.append("Server Packs can be enabled by double clicking.");
                        }
                    } else {
                        MutableComponent.append("Seasonal Packs cannot be manually enabled or disabled.");
                    }
                }


                //ToolTip
                //Minecraft.getInstance().screen.renderTooltip(pPoseStack, MutableComponent, pMouseX, pMouseY);
                this.parent.setTooltipForNextRenderPass(font.split(MutableComponent, 170));

            }

        }

        private String GetRestrictionType(IPackEntry packEntry) {
            ServerContentEntry entry = (ServerContentEntry) packEntry;
            return entry.Restriction.name();
        }

        public boolean CanToggle() {
            if (packEntry.IsServerPack() == false) {
                return false;
            }
            ServerContentEntry entry = (ServerContentEntry) packEntry;
            if (entry.Restriction == ServerPackRestrictionEnum.None) {
                return true;
            }

            return false;
        }
    }

}
