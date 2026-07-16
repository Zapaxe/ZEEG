package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.ZeegRenderHooks;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelFeatureRenderer.class)
public class ModelFeatureRendererMixin {
    @Inject(
        method = "prepareModel",
        at = @At("HEAD")
    )
    private void zeeg$setGlintColorOnRender(
        ModelFeatureRenderer.Submit submit,
        CallbackInfo ci
    ) {
        int[] color = ZeegRenderHooks.ITEM_SUBMIT_COLORS.get(submit);
        if (color != null) {
            ZeegRenderHooks.GLINT_COLOR.set(color);
        } else {
            ZeegRenderHooks.GLINT_COLOR.remove();
        }
    }

    @Inject(
        method = "prepareModel",
        at = @At("RETURN")
    )
    private void zeeg$clearGlintColorOnRender(
        ModelFeatureRenderer.Submit submit,
        CallbackInfo ci
    ) {
        ZeegRenderHooks.GLINT_COLOR.remove();
    }
}
