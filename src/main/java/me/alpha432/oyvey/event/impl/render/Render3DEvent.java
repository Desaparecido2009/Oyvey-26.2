package me.alpha432.oyvey.event.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.alpha432.oyvey.event.Event;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;

public class Render3DEvent extends Event {
    private final PoseStack matrix;
    private final SubmitNodeCollector collector;
    private final Vec3 cameraPos;
    private final float delta;

    public Render3DEvent(PoseStack matrix, SubmitNodeCollector collector, Vec3 cameraPos, float delta) {
        this.matrix = matrix;
        this.collector = collector;
        this.cameraPos = cameraPos;
        this.delta = delta;
    }

    public PoseStack getMatrix() {
        return matrix;
    }

    public SubmitNodeCollector getCollector() {
        return collector;
    }

    public Vec3 getCameraPos() {
        return cameraPos;
    }

    public float getDelta() {
        return delta;
    }
}
