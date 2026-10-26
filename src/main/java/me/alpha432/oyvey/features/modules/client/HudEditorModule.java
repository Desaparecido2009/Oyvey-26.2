package me.alpha432.oyvey.features.modules.client;

import me.alpha432.oyvey.features.gui.HudEditorScreen;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.Register;

@Register
public class HudEditorModule extends Module {
    public HudEditorModule() {
        super("Edit HUD element positions", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        mc.gui.setScreen(HudEditorScreen.getInstance());
        disable();
    }
}

