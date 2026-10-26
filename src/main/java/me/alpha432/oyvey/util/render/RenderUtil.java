package me.alpha432.oyvey.util.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.alpha432.oyvey.util.traits.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.*;

public class RenderUtil implements Util {
    private static float guiAlpha = 1.0f;

    public static void setGuiAlpha(float alpha) {
        guiAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
    }

    public static int applyAlpha(int color) {
        if (guiAlpha >= 0.999f) {
            return color;
        }
        int a = Math.round(((color >>> 24) & 0xFF) * guiAlpha);
        return (a << 24) | (color & 0xFFFFFF);
    }

    public static void rect(GuiGraphicsExtractor context, float x1, float y1, float x2, float y2, int color) {
        int ix1 = Math.round(x1);
        int iy1 = Math.round(y1);
        int ix2 = Math.round(x2);
        int iy2 = Math.round(y2);
        context.fill(ix1, iy1, ix2, iy2, applyAlpha(color));
    }

    public static void rect(GuiGraphicsExtractor context, float x1, float y1, float x2, float y2, int color, float width) {
        int w = Math.max(1, Math.round(width));
        context.fill(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y1) + w, applyAlpha(color));
        context.fill(Math.round(x2) - w, Math.round(y1), Math.round(x2), Math.round(y2), applyAlpha(color));
        context.fill(Math.round(x1), Math.round(y2) - w, Math.round(x2), Math.round(y2), applyAlpha(color));
        context.fill(Math.round(x1), Math.round(y1), Math.round(x1) + w, Math.round(y2), applyAlpha(color));
    }

    public static void horizontalGradient(GuiGraphicsExtractor context, float x1, float y1, float x2, float y2, Color left, Color right) {
        int ix1 = Math.round(x1);
        int iy1 = Math.round(y1);
        int ix2 = Math.round(x2);
        int iy2 = Math.round(y2);
        if (ix1 > ix2) {
            int i = ix1;
            ix1 = ix2;
            ix2 = i;
        }
        int width = ix2 - ix1;
        if (width <= 0) return;
        for (int x = ix1; x < ix2; x++) {
            float t = (x - ix1) / (float) width;
            context.fill(x, iy1, x + 1, iy2, applyAlpha(lerpColor(left.getRGB(), right.getRGB(), t)));
        }
    }

    public static void verticalGradient(GuiGraphicsExtractor context, float x1, float y1, float x2, float y2, Color top, Color bottom) {
        context.fillGradient(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y2), applyAlpha(top.getRGB()), applyAlpha(bottom.getRGB()));
    }

    public static void gradient(GuiGraphicsExtractor context,
                                int x1, int y1, int x2, int y2,
                                int topLeft, int bottomLeft, int bottomRight, int topRight) {
        int width = x2 - x1;
        if (width <= 0) return;
        for (int x = x1; x < x2; x++) {
            float t = (x - x1) / (float) width;
            context.fillGradient(x, y1, x + 1, y2, applyAlpha(lerpColor(topLeft, topRight, t)), applyAlpha(lerpColor(bottomLeft, bottomRight, t)));
        }
    }

    private static int lerpColor(int from, int to, float t) {
        int a = (int) (((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * t);
        int r = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // 3d
    public static void drawBox(PoseStack stack, SubmitNodeCollector collector, AABB box, Color c, float lineWidth) {
        drawBox(stack, collector, Shapes.create(box), c, lineWidth);
    }

    public static void drawBox(PoseStack stack, SubmitNodeCollector collector, VoxelShape shape, Color c, float lineWidth) {
        Vec3 camera = mc.gameRenderer.mainCamera().position();
        int color = c.getRGB();

        collector.submitCustomGeometry(stack, RenderTypes.lines(), (pose, consumer) -> shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float nx = (float) (x2 - x1);
            float ny = (float) (y2 - y1);
            float nz = (float) (z2 - z1);

            consumer.addVertex(pose, (float) (x1 - camera.x), (float) (y1 - camera.y), (float) (z1 - camera.z))
                    .setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(lineWidth);
            consumer.addVertex(pose, (float) (x2 - camera.x), (float) (y2 - camera.y), (float) (z2 - camera.z))
                    .setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(lineWidth);
        }));
    }

    public static void drawBox(PoseStack stack, SubmitNodeCollector collector, Vec3 vec, Color c, float lineWidth) {
        drawBox(stack, collector, AABB.unitCubeFromLowerCorner(vec), c, lineWidth);
    }

    public static void drawBox(PoseStack stack, SubmitNodeCollector collector, BlockPos bp, Color c, float lineWidth) {
        drawBox(stack, collector, new AABB(bp), c, lineWidth);
    }

    public static PoseStack matrixFrom(Vec3 pos) {
        PoseStack matrices = new PoseStack();
        Camera camera = mc.gameRenderer.mainCamera();
        matrices.mulPose(Axis.XP.rotationDegrees(camera.xRot()));
        matrices.mulPose(Axis.YP.rotationDegrees(camera.yRot() + 180.0F));
        matrices.translate(pos.x() - camera.position().x, pos.y() - camera.position().y, pos.z() - camera.position().z);
        return matrices;
    }
}
