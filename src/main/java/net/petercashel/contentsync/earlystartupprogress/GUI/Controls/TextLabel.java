package net.petercashel.contentsync.earlystartupprogress.GUI.Controls;

import net.petercashel.contentsync.earlystartupprogress.GUI.Core.Color;
import net.petercashel.contentsync.earlystartupprogress.GUI.Core.Font;

public class TextLabel implements IUIRenderable{
    private Font font = null;
    private int x;
    private final int y;
    private final Color textColor;
    private String text;
    public int FontSize = 24;
    private boolean center = false;

    public TextLabel(String text, int x, int y, Color textColor) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.textColor = textColor;
    }

    @Override
    public void render() {
        if (font == null) {
            this.font = new Font(FontSize, true);
        }

        int posx = x;

        if (center) {
            posx = posx - (font.getWidth(text) / 2);
        }

        font.drawText(text, posx,y, textColor);
    }

    public void SetText(String Text) {
        this.text = Text;
    }

    public TextLabel SetCemter(int windowWidth) {
        x = windowWidth / 2;
        center = true;
        return this;
    }
}
