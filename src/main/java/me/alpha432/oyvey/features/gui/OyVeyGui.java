package me.alpha432.oyvey.features.gui;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.gui.items.buttons.ModuleButton;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.client.ClickGuiModule;
import me.alpha432.oyvey.util.models.Animation;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class OyVeyGui extends Screen {
    private static OyVeyGui INSTANCE;
    private static Color colorClipboard = null;

    static {
        INSTANCE = new OyVeyGui();
    }

    private Screen lastScreen;
    private final ArrayList<Widget> widgets = new ArrayList<>();
    private final Animation openAnimation = new Animation();
    private boolean opened;
    private boolean closing;

    public OyVeyGui() {
        super(Component.literal("OyVey"));
        setInstance();
        load();
    }

    public static OyVeyGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OyVeyGui();
        }
        return INSTANCE;
    }

    public static OyVeyGui getClickGui() {
        return OyVeyGui.getInstance();
    }

    public static void open(Screen previous) {
        OyVeyGui gui = getInstance();
        gui.lastScreen = previous;
        Minecraft.getInstance().gui.setScreen(gui);
    }

    @Override
    protected void init() {
        super.init();
        boolean wasClosing = this.closing;
        this.closing = false;
        if (!this.animationsEnabled()) {
            this.openAnimation.set(1.0f);
            this.opened = true;
            return;
        }
        if (wasClosing) {
            this.openAnimation.animateTo(1.0f, this.animationDuration(), Animation.Easing.OUT_CUBIC);
        } else if (!this.opened) {
            this.openAnimation.set(0.0f);
            this.openAnimation.animateTo(1.0f, this.animationDuration(), Animation.Easing.OUT_CUBIC);
        }
        this.opened = true;
    }

    @Override
    public void onClose() {
        if (this.closing) {
            return;
        }
        if (!this.animationsEnabled()) {
            this.finishClose();
            return;
        }
        this.closing = true;
        this.widgets.forEach(widget -> widget.mouseReleased(0, 0, 0));
        this.openAnimation.animateTo(0.0f, this.closingDuration(), Animation.Easing.IN_CUBIC);
    }

    @Override
    public void removed() {
        super.removed();
        this.closing = false;
        this.opened = false;
        this.openAnimation.set(0.0f);
        this.lastScreen = null;
        RenderUtil.setGuiAlpha(1.0f);
    }

    private boolean animationsEnabled() {
        ClickGuiModule config = ClickGuiModule.getInstance();
        return config != null && config.animations.getValue();
    }

    private long animationDuration() {
        ClickGuiModule config = ClickGuiModule.getInstance();
        return config == null ? 150L : config.animationDuration.getValue().longValue();
    }

    private long closingDuration() {
        return Math.max(60L, this.animationDuration() * 3L / 4L);
    }

    private void finishClose() {
        Screen previous = this.lastScreen;
        this.lastScreen = null;
        this.closing = false;
        this.opened = false;
        this.openAnimation.set(0.0f);
        this.minecraft.gui.setScreen(previous);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.closing) {
            return;
        }
        if (!this.animationsEnabled() || this.openAnimation.get() <= 0.001f) {
            this.finishClose();
        }
    }

    private void setInstance() {
        INSTANCE = this;
    }

    private void load() {
        int x = -84;
        for (Module.Category category : OyVey.moduleManager.getCategories()) {
            if (category == Module.Category.HUD) continue;
            Widget panel = new Widget(category.getName(), x += 90, 4, true);
            OyVey.moduleManager.stream()
                    .filter(m -> m.getCategory() == category && !m.hidden)
                    .map(ModuleButton::new)
                    .forEach(panel::addButton);
            this.widgets.add(panel);
        }
        this.widgets.forEach(components -> components.getItems().sort(Comparator.comparing(Feature::getName)));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        float progress;
        if (!this.animationsEnabled()) {
            this.openAnimation.set(1.0f);
            progress = 1.0f;
        } else if (!this.closing && this.openAnimation.isFinished() && this.openAnimation.getValue() <= 0.0f) {
            this.openAnimation.set(1.0f);
            progress = 1.0f;
        } else {
            progress = this.openAnimation.get();
        }

        if (lastScreen != null) {
            lastScreen.extractBackground(context, Integer.MIN_VALUE, Integer.MIN_VALUE, delta);
            lastScreen.extractRenderState(context, Integer.MIN_VALUE, Integer.MIN_VALUE, delta);
            context.nextStratum();
        }

        context.fill(0, 0, context.guiWidth(), context.guiHeight(), ((int) (120.0f * progress)) << 24);
        RenderUtil.setGuiAlpha(progress);
        try {
            this.widgets.forEach(components -> components.drawScreen(context, mouseX, mouseY, delta));
        } finally {
            RenderUtil.setGuiAlpha(1.0f);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (this.closing) {
            return true;
        }
        this.widgets.forEach(components -> components.mouseClicked((int) click.x(), (int) click.y(), click.button()));
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (this.closing) {
            return true;
        }
        this.widgets.forEach(components -> components.mouseReleased((int) click.x(), (int) click.y(), click.button()));
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.closing) {
            return true;
        }
        if (verticalAmount < 0) {
            this.widgets.forEach(component -> component.setY(component.getY() - 10));
        } else if (verticalAmount > 0) {
            this.widgets.forEach(component -> component.setY(component.getY() + 10));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (this.closing) {
            return true;
        }
        this.widgets.forEach(component -> component.onKeyPressed(input.input()));
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (this.closing) {
            return true;
        }
        this.widgets.forEach(component -> component.onKeyTyped(input.codepointAsString(), 0));
        return super.charTyped(input);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }//ignore 1.21.8 blur thing

    public final ArrayList<Widget> getComponents() {
        return this.widgets;
    }

    public int getTextOffset() {
        return -6;
    }

    public static Color getColorClipboard() {
        return colorClipboard;
    }

    public static void setColorClipboard(Color color) {
        colorClipboard = color;
    }
}
