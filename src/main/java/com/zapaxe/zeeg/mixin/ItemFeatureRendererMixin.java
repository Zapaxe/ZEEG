package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.ZeegRenderHooks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFeatureRenderer.class)
public class ItemFeatureRendererMixin {
    @Inject(
        method = "renderItem",
        at = @At("HEAD")
    )
    private void zeeg$setGlintColorOnRender(
        MultiBufferSource.BufferSource bufferSource,
        OutlineBufferSource outlineBufferSource,
        SubmitNodeStorage.ItemSubmit itemSubmit,
        CallbackInfo ci
    ) {
        bufferSource.endBatch();
        int[] color = ZeegRenderHooks.ITEM_SUBMIT_COLORS.get(itemSubmit);
        if (color != null) {
            ZeegRenderHooks.GLINT_COLOR.set(color);
        } else {
            ZeegRenderHooks.GLINT_COLOR.remove();
        }
    }

    @Inject(
        method = "renderItem",
        at = @At("RETURN")
    )
    private void zeeg$clearGlintColorOnRender(
        MultiBufferSource.BufferSource bufferSource,
        OutlineBufferSource outlineBufferSource,
        SubmitNodeStorage.ItemSubmit itemSubmit,
        CallbackInfo ci
    ) {
        bufferSource.endBatch();
        ZeegRenderHooks.GLINT_COLOR.remove();
    }
}
