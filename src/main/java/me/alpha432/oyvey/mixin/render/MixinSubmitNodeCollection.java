package me.alpha432.oyvey.mixin.render;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.render.ChamsModule;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SubmitNodeCollection.class)
public class MixinSubmitNodeCollection {
    @WrapMethod(method = "submitModel")
    private <S> void oyvey$chams(Model<? super S> model, S state, PoseStack poseStack, RenderType renderType,
                                 int lightCoords, int overlayCoords, int color, TextureAtlasSprite sprite,
                                 int outlineColor, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
                                 Operation<Void> original) {
        ChamsModule module = OyVey.moduleManager.getModuleByClass(ChamsModule.class);
        if (module != null && module.isEnabled() && module.shouldChams(model, state)) {
            renderType = module.getRenderType();
        }
        original.call(model, state, poseStack, renderType, lightCoords, overlayCoords, color, sprite, outlineColor, crumblingOverlay);
    }
}
