package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.ZeegRenderHooks;
import com.zapaxe.zeeg.config.GlintConfig;
import java.util.List;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.ItemCommandRenderer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCommandRenderer.class)
public class ItemCommandRendererMixin {
    @Unique
    private static final ThreadLocal<List<int[]>> zeeg$colors = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<Integer> zeeg$index = ThreadLocal.withInitial(() -> 0);

    @Inject(method = "render", at = @At("HEAD"))
    private void zeeg$init(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vcp, OutlineVertexConsumerProvider outline, CallbackInfo ci) {
        zeeg$colors.set(ZeegRenderHooks.getCommandColors(queue));
        zeeg$index.set(0);
    }


    @Redirect(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V")
    )
    private static void zeeg$redirectRenderItem(ItemDisplayContext ctx, MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay, int[] tints, List<BakedQuad> quads, RenderLayer layer, ItemRenderState.Glint glint) {
        if (vcp instanceof VertexConsumerProvider.Immediate imm) {
            imm.draw();
            int idx = zeeg$index.get();
            zeeg$index.set(idx + 1);
            if (glint != ItemRenderState.Glint.NONE) {
                List<int[]> colors = zeeg$colors.get();
                if (colors != null && idx < colors.size()) {
                    ZeegRenderHooks.GLINT_COLOR.set(colors.get(idx));
                }
            }
        }
        net.minecraft.client.render.item.ItemRenderer.renderItem(ctx, matrices, vcp, light, overlay, tints, quads, layer, glint);
        if (vcp instanceof VertexConsumerProvider.Immediate imm) {
            if (glint != ItemRenderState.Glint.NONE) {
                imm.draw();
            }
        }
    }
}
