package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.Register;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.util.render.ChamsRenderType;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemDisplayContext;

import java.awt.*;

@Register
public class ChamsModule extends Module {
    public final Setting<Boolean> players = bool("Players", true);
    public final Setting<Boolean> crystals = bool("Crystals", true);
    public final Setting<Boolean> mainHand = bool("MainHand", true);
    public final Setting<Boolean> throughWalls = bool("ThroughWalls", true);
    public final Setting<Color> color = color("Color", 0, 255, 255, 255);

    public ChamsModule() {
        super("Renders players and crystals through walls", Category.RENDER);
    }

    public boolean shouldChams(Object state) {
        return (players.getValue() && state instanceof AvatarRenderState)
                || (crystals.getValue() && state instanceof EndCrystalRenderState);
    }

    public boolean shouldChams(Model<?> model, Object state) {
        if (state instanceof AvatarRenderState) {
            return players.getValue();
        }
        if (state instanceof EndCrystalRenderState) {
            return crystals.getValue();
        }
        return state == Unit.INSTANCE && model instanceof TridentModel;
    }

    public boolean shouldChamsItem(ItemDisplayContext context) {
        return mainHand.getValue() && context.firstPerson();
    }

    public RenderType getRenderType() {
        return ChamsRenderType.get(throughWalls.getValue(), color.getValue().getRGB());
    }
}
