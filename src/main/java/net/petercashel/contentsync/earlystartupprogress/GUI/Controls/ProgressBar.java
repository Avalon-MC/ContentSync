package net.petercashel.contentsync.earlystartupprogress.GUI.Controls;

import net.petercashel.contentsync.earlystartupprogress.GUI.Core.Color;
import org.lwjgl.opengl.GL11;

public class ProgressBar implements IUIRenderable{
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float border;
    private float value;
    private float max;
    private final Color backColor;
    private final Color barColor;

    public ProgressBar(float x, float y, float width, float height, float border, float value, float max, Color backColor, Color barColor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.border = border;
        this.value = value;
        this.max = max;
        this.backColor = backColor;
        this.barColor = barColor;
    }

    @Override
    public void render() {
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glColor4f(backColor.getRed(), backColor.getGreen(), backColor.getBlue(), backColor.getAlpha());

        GL11.glVertex2f(x,y);
        GL11.glVertex2f(x + width,y);
        GL11.glVertex2f(x + width,y + height);
        GL11.glVertex2f(x,y + height);

        float x1 = x + border;
        float y1 = y + border;
        float width1 = width - (border * 2);
        float height1 = height - (border * 2);

        float percent = (value / max) * 100f;

        width1 = (width1 / 100) * percent;


        GL11.glColor4f(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), barColor.getAlpha());

        GL11.glVertex2f(x1,y1);
        GL11.glVertex2f(x1 + width1,y1);
        GL11.glVertex2f(x1 + width1,y1 + height1);
        GL11.glVertex2f(x1,y1 + height1);

        GL11.glEnd();
    }

    public float getValue() {
        return value;
    }

    public void SetValue(float i) {
        if (i > max) {
            value = max;
            return;
        }
        value = i;
    }
    public void SetMax(float i) {
        max = i;
        if (value > max) {
            value = max;
        }
    }
}
