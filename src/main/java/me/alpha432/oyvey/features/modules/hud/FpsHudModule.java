package me.alpha432.oyvey.features.modules.hud;

import me.alpha432.oyvey.event.impl.render.Render2DEvent;
import me.alpha432.oyvey.features.modules.Register;
import me.alpha432.oyvey.features.modules.client.HudModule;
import me.alpha432.oyvey.features.settings.Setting;

@Register("FPS")
public class FpsHudModule extends HudModule {
    public final Setting<Boolean> label = bool("Label", true);

    public FpsHudModule() {
        super("Display frames per second", 40, 10);
    }

    @Override
    protected void render(Render2DEvent e) {
        super.render(e);

        String text = (label.getValue() ? "FPS: " : "") + mc.getFps();
        e.getContext().text(mc.font, text,
                (int) getX(), (int) getY(), -1);

        setWidth(mc.font.width(text));
        setHeight(mc.font.lineHeight);
    }
}
