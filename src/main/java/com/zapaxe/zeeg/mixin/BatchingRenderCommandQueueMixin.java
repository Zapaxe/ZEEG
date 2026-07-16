package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.ZeegRenderHooks;
import com.zapaxe.zeeg.config.GlintConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BatchingRenderCommandQueue.class)
public class BatchingRenderCommandQueueMixin {
    @Inject(method = "submitItem", at = @At("RETURN"))
    private void zeeg$captureColor(MatrixStack matrices, ItemDisplayContext displayContext, int light, int overlay, int seed, int[] tints, List<BakedQuad> quads, RenderLayer renderLayer, ItemRenderState.Glint glintType, CallbackInfo ci) {
        BatchingRenderCommandQueue self = (BatchingRenderCommandQueue) (Object) this;
        int[] color = ZeegRenderHooks.GLINT_COLOR.get();
        if (color == null) {
            color = new int[]{GlintConfig.getRed(), GlintConfig.getGreen(), GlintConfig.getBlue()};
        }
        List<int[]> colors = ZeegRenderHooks.getCommandColors(self);
        if (colors == null) {
            colors = new ArrayList<>();
            ZeegRenderHooks.putCommandColors(self, colors);
        }
        colors.add(color);
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void zeeg$clearColors(CallbackInfo ci) {
        ZeegRenderHooks.removeCommandColors((BatchingRenderCommandQueue) (Object) this);
    }
}
