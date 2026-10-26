package me.alpha432.oyvey.util.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScissorUtil {
    private static final Deque<int[]> STACK = new ArrayDeque<>();
    private static int depth;

    public static void push(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2) {
        int[] parent = depth > 0 ? STACK.peek() : null;
        int nx1 = x1;
        int ny1 = y1;
        int nx2 = x2;
        int ny2 = y2;
        if (parent != null) {
            nx1 = Math.max(nx1, parent[0]);
            ny1 = Math.max(ny1, parent[1]);
            nx2 = Math.min(nx2, parent[2]);
            ny2 = Math.min(ny2, parent[3]);
        }
        STACK.push(new int[]{nx1, ny1, nx2, ny2});
        apply(context, nx1, ny1, nx2, ny2);
    }

    public static void pop(GuiGraphicsExtractor context) {
        if (STACK.isEmpty()) {
            return;
        }
        STACK.pop();
        if (depth > 0) {
            context.disableScissor();
            depth--;
        }
    }

    public static void enable(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2) {
        apply(context, x1, y1, x2, y2);
    }

    public static void disable(GuiGraphicsExtractor context) {
        if (depth > 0) {
            context.disableScissor();
            depth--;
        }
    }

    public static void enable(GuiGraphicsExtractor context) {
        int[] top = STACK.peek();
        if (top != null) {
            apply(context, top[0], top[1], top[2], top[3]);
        }
    }

    private static void apply(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2) {
        context.enableScissor(x1, y1, Math.max(x1, x2), Math.max(y1, y2));
        depth++;
    }
}
