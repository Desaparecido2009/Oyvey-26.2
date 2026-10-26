package me.alpha432.oyvey.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.alpha432.oyvey.event.impl.render.Render3DEvent;
import me.alpha432.oyvey.event.impl.render.RenderBlockOutlineEvent;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.alpha432.oyvey.util.traits.Util.EVENT_BUS;
import static me.alpha432.oyvey.util.traits.Util.mc;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "submitFeatures", at = @At("HEAD"))
    private void oyvey$blockOutline(LevelRenderState levelRenderState, SubmitNodeCollector collector, boolean bl, CallbackInfo ci) {
        if (EVENT_BUS.post(new RenderBlockOutlineEvent())) {
            levelRenderState.blockOutlineRenderState = null;
        }
    }

    @Inject(method = "submitFeatures", at = @At("TAIL"))
    private void oyvey$render3d(LevelRenderState levelRenderState, SubmitNodeCollector collector, boolean bl, CallbackInfo ci) {
        CameraRenderState camera = levelRenderState.cameraRenderState;
        if (camera == null) return;

        PoseStack stack = new PoseStack();
        stack.pushPose();
        stack.mulPose(Axis.XP.rotationDegrees(camera.xRot));
        stack.mulPose(Axis.YP.rotationDegrees(camera.yRot + 180f));

        Render3DEvent event = new Render3DEvent(stack, collector, camera.pos, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        EVENT_BUS.post(event);
        stack.popPose();
    }
}
