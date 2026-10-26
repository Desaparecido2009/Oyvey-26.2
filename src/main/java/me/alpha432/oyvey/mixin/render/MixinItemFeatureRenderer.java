package me.alpha432.oyvey.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.render.ChamsModule;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemFeatureRenderer.class)
public class MixinItemFeatureRenderer {
    @ModifyArg(
            method = "prepareMainSubmit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer;getVertexBuilder(Lnet/minecraft/client/renderer/rendertype/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            ),
            index = 0
    )
    private RenderType oyvey$chamsItem(RenderType renderType, @Local(argsOnly = true) ItemFeatureRenderer.Submit submit) {
        ChamsModule module = OyVey.moduleManager.getModuleByClass(ChamsModule.class);
        if (module != null && module.isEnabled() && module.shouldChamsItem(submit.displayContext())) {
            return module.getRenderType();
        }
        return renderType;
    }
}
