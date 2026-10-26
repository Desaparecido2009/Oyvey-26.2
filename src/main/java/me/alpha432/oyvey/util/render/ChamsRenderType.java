package me.alpha432.oyvey.util.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import me.alpha432.oyvey.util.traits.Util;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class ChamsRenderType implements Util {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("oyvey", "chams");
    private static RenderType visible;
    private static RenderType throughWalls;
    private static DynamicTexture texture;
    private static int lastColor = Integer.MIN_VALUE;
    private static int debugVariant;

    private ChamsRenderType() {
    }

    public static void setDebugVariant(int variant) {
        debugVariant = variant;
        visible = null;
        throughWalls = null;
    }

    public static RenderType get(boolean throughWalls, int color) {
        ensureTexture(color);
        if (throughWalls) {
            if (ChamsRenderType.throughWalls == null) {
                ChamsRenderType.throughWalls = create("oyvey_chams_through_walls", true);
            }
            return ChamsRenderType.throughWalls;
        }
        if (visible == null) {
            visible = create("oyvey_chams", false);
        }
        return visible;
    }

    private static RenderType create(String name, boolean throughWalls) {
        return switch (debugVariant) {
            case 1 -> createEntity(name, throughWalls, false);
            case 2 -> createItem(name, throughWalls);
            case 3 -> createVanillaItem(name);
            default -> createEntity(name, throughWalls, true);
        };
    }

    private static RenderType createEntity(String name, boolean throughWalls, boolean sortOnUpload) {
        RenderPipeline pipeline = entityPipeline(name, throughWalls);
        RenderSetup.RenderSetupBuilder builder = RenderSetup.builder(pipeline).withTexture("Sampler0", TEXTURE);
        if (sortOnUpload) {
            builder.sortOnUpload();
        }
        return RenderType.create(name, builder.createRenderSetup());
    }

    private static RenderPipeline entityPipeline(String name, boolean throughWalls) {
        return RenderPipeline.builder()
                .withLocation("pipeline/" + name)
                .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.FOG)
                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withShaderDefine("EMISSIVE")
                .withShaderDefine("NO_OVERLAY")
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withDepthStencilState(throughWalls
                        ? new DepthStencilState(CompareOp.ALWAYS_PASS, false)
                        : DepthStencilState.DEFAULT)
                .build();
    }

    private static RenderType createItem(String name, boolean throughWalls) {
        RenderPipeline pipeline = RenderPipeline.builder()
                .withLocation("pipeline/" + name + "_item")
                .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.FOG)
                .withBindGroupLayout(BindGroupLayouts.LIGHTING)
                .withVertexShader("core/item")
                .withFragmentShader("core/item")
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1_SAMPLER2)
                .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withDepthStencilState(throughWalls
                        ? new DepthStencilState(CompareOp.ALWAYS_PASS, false)
                        : DepthStencilState.DEFAULT)
                .build();
        RenderSetup setup = RenderSetup.builder(pipeline)
                .withTexture("Sampler0", TEXTURE)
                .useLightmap()
                .useOverlay()
                .createRenderSetup();
        return RenderType.create(name, setup);
    }

    private static RenderType createVanillaItem(String name) {
        RenderSetup setup = RenderSetup.builder(RenderPipelines.ITEM_TRANSLUCENT)
                .withTexture("Sampler0", TEXTURE)
                .useLightmap()
                .useOverlay()
                .createRenderSetup();
        return RenderType.create(name, setup);
    }

    private static void ensureTexture(int color) {
        if (texture == null) {
            texture = new DynamicTexture(() -> "oyvey_chams", new NativeImage(1, 1, false));
            mc.getTextureManager().register(TEXTURE, texture);
        }
        if (lastColor != color) {
            int a = (color >>> 24) & 0xFF;
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            texture.getPixels().setPixelABGR(0, 0, (a << 24) | (b << 16) | (g << 8) | r);
            texture.upload();
            lastColor = color;
        }
    }
}
