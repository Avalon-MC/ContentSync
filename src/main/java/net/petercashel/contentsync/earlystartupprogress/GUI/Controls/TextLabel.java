package net.petercashel.contentsync.earlystartupprogress.GUI.Controls;

import net.petercashel.contentsync.earlystartupprogress.GUI.Core.Color;
import net.petercashel.contentsync.earlystartupprogress.GUI.Core.Font;

public class TextLabel implements IUIRenderable{
    private Font font = null;
    private final int x;
    private final int y;
    private final Color textColor;
    private String text;
    public int FontSize = 24;

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
        font.drawText(text, x,y, textColor);
    }

    public void SetText(String Text) {
        this.text = Text;
    }
}
