package me.alpha432.oyvey.features.gui;

import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.gui.items.Item;
import me.alpha432.oyvey.features.gui.items.buttons.Button;
import me.alpha432.oyvey.features.modules.client.ClickGuiModule;
import me.alpha432.oyvey.util.ColorUtil;
import me.alpha432.oyvey.util.models.Animation;
import me.alpha432.oyvey.util.render.RenderUtil;
import me.alpha432.oyvey.util.render.ScissorUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Widget
        extends Feature {
    protected GuiGraphicsExtractor context;
    private final List<Item> items = new ArrayList<>();
    private final Animation openAnimation;
    public boolean drag;
    private int x;
    private int y;
    private int x2;
    private int y2;
    private int width;
    private int height;
    private boolean open;
    private boolean hidden = false;

    public Widget(String name, int x, int y, boolean open) {
        super(name);
        this.x = x;
        this.y = y;
        this.width = 88;
        this.height = 18;
        this.open = open;
        this.openAnimation = new Animation(open ? 1.0f : 0.0f);
    }

    private void drag(int mouseX, int mouseY) {
        if (!this.drag) {
            return;
        }
        this.x = this.x2 + mouseX;
        this.y = this.y2 + mouseY;
    }

    public void drawScreen(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTicks) {
        this.context = context;
        this.drag(mouseX, mouseY);
        float progress = this.openProgress();
        float contentHeight = Math.max(0.0f, this.getTotalItemHeight() - 2.0f) * progress;
        int color = ClickGuiModule.getInstance().topColor.getValue().getRGB();
        if (ClickGuiModule.getInstance().rainbow.getValue()) {
            color = ColorUtil.rainbow(ClickGuiModule.getInstance().rainbowHue.getValue()).getRGB();
        }
        context.fill(this.x, this.y - 1, this.x + this.width, this.y + this.height - 6, RenderUtil.applyAlpha(color));
        if (progress > 0.001f) {
            RenderUtil.rect(context, this.x, (float) this.y + 12.5f, this.x + this.width, (float) (this.y + this.height) + contentHeight, 0x77000000);
        }
        drawString(this.getName(), (float) this.x + 3.0f, (float) this.y - 4.0f - (float) OyVeyGui.getClickGui().getTextOffset(), -1);

        if (progress > 0.001f) {
            ScissorUtil.push(context, x, 0, x + width, Math.round((float) (this.y + this.height) - 3.0f + contentHeight));
            float y = (float) (this.getY() + this.getHeight()) - 3.0f;
            for (Item item : this.getItems()) {
                if (item.isHidden()) continue;
                item.setLocation((float) this.x + 2.0f, y);
                item.setWidth(this.getWidth() - 4);
                boolean hovering = item.isHovering(mouseX, mouseY);
                if (hovering) {
                    ScissorUtil.disable(context);
                }
                item.drawScreen(context, mouseX, mouseY, partialTicks);

                if (hovering) {
                    ScissorUtil.enable(context);
                }
                y += item.getAnimatedHeight() + 2f;
            }
            ScissorUtil.pop(context);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.x2 = this.x - mouseX;
            this.y2 = this.y - mouseY;
            OyVeyGui.getClickGui().getComponents().forEach(component -> {
                if (component.drag) {
                    component.drag = false;
                }
            });
            this.drag = true;
            return;
        }
        if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
            this.open = !this.open;
            ClickGuiModule config = ClickGuiModule.getInstance();
            if (config != null && config.animations.getValue()) {
                this.openAnimation.animateTo(this.open ? 1.0f : 0.0f, Math.max(1L, config.animationDuration.getValue().longValue()),
                        this.open ? Animation.Easing.OUT_QUAD : Animation.Easing.IN_QUAD);
            } else {
                this.openAnimation.set(this.open ? 1.0f : 0.0f);
            }
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            return;
        }
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0) {
            this.drag = false;
        }
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public void onKeyTyped(String typedChar, int keyCode) {
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.onKeyTyped(typedChar, keyCode));
    }

    public void onKeyPressed(int key) {
        if (!open) return;
        this.getItems().forEach(item -> item.onKeyPressed(key));
    }

    public void addButton(Button button) {
        this.items.add(button);
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isOpen() {
        return this.open;
    }

    public final List<Item> getItems() {
        return this.items;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight() - (this.open ? 2 : 0);
    }

    private float getTotalItemHeight() {
        float height = 0.0f;
        for (Item item : this.getItems()) {
            height += item.getAnimatedHeight() + 2.0f;
        }
        return height;
    }

    private float openProgress() {
        ClickGuiModule config = ClickGuiModule.getInstance();
        if (config == null || !config.animations.getValue()) {
            this.openAnimation.set(this.open ? 1.0f : 0.0f);
            return this.open ? 1.0f : 0.0f;
        }
        return this.openAnimation.get();
    }

    protected void drawString(String text, double x, double y, Color color) {
        drawString(text, x, y, color.hashCode());
    }

    protected void drawString(String text, double x, double y, int color) {
        context.text(mc.font, text, (int) x, (int) y, RenderUtil.applyAlpha(color));
    }
}