package me.alpha432.oyvey.features.gui.items.buttons;

import me.alpha432.oyvey.features.gui.items.Item;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.client.ClickGuiModule;
import me.alpha432.oyvey.features.settings.Bind;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.util.models.Animation;
import me.alpha432.oyvey.util.render.ScissorUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton
        extends Button {
    private final Module module;
    private final Animation settingsAnimation = new Animation();
    private List<Item> items = new ArrayList<>();
    private boolean subOpen;

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;
        this.initSettings();
    }

    @SuppressWarnings("unchecked")
    public void initSettings() {
        ArrayList<Item> newItems = new ArrayList<>();
        if (!this.module.getSettings().isEmpty()) {
            for (Setting<?> setting : this.module.getSettings()) {
                if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled")) {
                    newItems.add(new BooleanButton((Setting<Boolean>) setting));
                }
                if (setting.getValue() instanceof Bind && !setting.getName().equalsIgnoreCase("Keybind") && !this.module.getName().equalsIgnoreCase("Hud")) {
                    newItems.add(new BindButton((Setting<Bind>) setting));
                }
                if ((setting.getValue() instanceof String || setting.getValue() instanceof Character) && !setting.getName().equalsIgnoreCase("displayName")) {
                    newItems.add(new StringButton((Setting<String>) setting));
                }
                if (setting.isColorSetting()) {
                    newItems.add(new ColorButton((Setting<java.awt.Color>) setting));
                    continue;
                }
                if (setting.isNumberSetting() && setting.hasRestriction()) {
                    newItems.add(new Slider((Setting<Number>) setting));
                    continue;
                }
                if (!setting.isEnumSetting()) continue;
                newItems.add(new EnumButton((Setting<Enum<?>>) setting));
            }
        }
        newItems.add(new BindButton((Setting<Bind>) this.module.getSettingByName("Keybind")));
        this.items = newItems;
    }

    @Override
    public void drawScreen(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(context, mouseX, mouseY, partialTicks);
        if (this.items.isEmpty() || this.settingsProgress() <= 0.001f) {
            return;
        }
        float height = 16.0f;
        ScissorUtil.push(context, Math.round(this.x), Math.round(this.y), Math.round(this.x + this.width) + 4, Math.round(this.y + this.getAnimatedHeight()));
        for (Item item : this.items) {
            if (!item.isHidden()) {
                item.setLocation(this.x + 1.0f, this.y + height);
                item.setWidth(this.width - 9);
                item.drawScreen(context, mouseX, mouseY, partialTicks);
                height += item.getAnimatedHeight() + 1f;
            }
            item.update();
        }
        ScissorUtil.pop(context);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!this.items.isEmpty()) {
            if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
                this.subOpen = !this.subOpen;
                ClickGuiModule config = ClickGuiModule.getInstance();
                if (config != null && config.animations.getValue()) {
                    this.settingsAnimation.animateTo(this.subOpen ? 1.0f : 0.0f, Math.max(1L, config.animationDuration.getValue().longValue()),
                            this.subOpen ? Animation.Easing.OUT_QUAD : Animation.Easing.IN_QUAD);
                } else {
                    this.settingsAnimation.set(this.subOpen ? 1.0f : 0.0f);
                }
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            }
            if (this.subOpen) {
                for (Item item : this.items) {
                    if (item.isHidden()) continue;
                    item.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        super.mouseReleased(mouseX, mouseY, releaseButton);
        if (!this.items.isEmpty() && this.subOpen) {
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                item.mouseReleased(mouseX, mouseY, releaseButton);
            }
        }
    }

    @Override
    public void onKeyTyped(String typedChar, int keyCode) {
        super.onKeyTyped(typedChar, keyCode);
        if (!this.items.isEmpty() && this.subOpen) {
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                item.onKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void onKeyPressed(int key) {
        super.onKeyPressed(key);
        if (!this.items.isEmpty() && this.subOpen) {
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                item.onKeyPressed(key);
            }
        }
    }

    @Override
    public int getHeight() {
        if (this.subOpen) {
            int height = 16;
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                height += item.getHeight() + 1;
            }
            return height;
        }
        return 14;
    }

    @Override
    public float getAnimatedHeight() {
        return 14.0f + this.getSettingsHeight() * this.settingsProgress();
    }

    private float getSettingsHeight() {
        float height = 2.0f;
        for (Item item : this.items) {
            if (item.isHidden()) continue;
            height += item.getAnimatedHeight() + 1.0f;
        }
        return height;
    }

    private float settingsProgress() {
        ClickGuiModule config = ClickGuiModule.getInstance();
        if (config == null || !config.animations.getValue()) {
            this.settingsAnimation.set(this.subOpen ? 1.0f : 0.0f);
            return this.subOpen ? 1.0f : 0.0f;
        }
        return this.settingsAnimation.get();
    }

    public Module getModule() {
        return this.module;
    }

    @Override
    public void toggle() {
        this.module.toggle();
    }

    @Override
    public boolean getState() {
        return this.module.isEnabled();
    }
}